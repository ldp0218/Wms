package net.tiaozhua.wms

import android.app.AlertDialog
import android.content.*
import android.device.ScanManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.gson.Gson
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import kotlinx.android.synthetic.main.activity_bcprk.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.*
import okhttp3.MediaType
import okhttp3.RequestBody


class BcprkActivity : BaseActivity(R.layout.activity_bcprk), View.OnClickListener {

    private var bcpPopup: BcpPopup? = null
    private lateinit var mScanManager: ScanManager
    private lateinit var barcodeStr: String
    private var receiverTag: Boolean = false
    private var status = ScanStatus.EMPTY
    internal lateinit var bcpAdapter: BaseAdapter
    private var ckListPopup: CkListPopup? = null
    internal lateinit var bcpRkd: BcpRkd
    internal var bcpRkmx: BcpRkmx? = null

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            (application as App).playAndVibrate(this@BcprkActivity)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            LoadingDialog.show(this@BcprkActivity)
            RetrofitManager.instance.getBcpInfo(barcodeStr)
                    .enqueue(object : BaseCallback<SemiProduct>(this@BcprkActivity) {
                        override fun successData(data: SemiProduct) {
                            if (data.scdmxs.isEmpty()) {
                                Toast.makeText(this@BcprkActivity, "此半成品未生产", Toast.LENGTH_SHORT).show()
                                return
                            } else if (data.scdmxs.size == 1) {
                                val wrkNum = (data.scdmxs[0].mx_num - data.scdmxs[0].mx_wcnum).toInt()
                                bcpRkmx = BcpRkmx(data.ma_id, data.ma_name, data.ma_code, data.scdmxs[0].scdmx_id, 1, wrkNum, "", "")
                                showPopup()
                            } else {    //多个生产批次
                                val items = mutableListOf<String>()
                                for (item in data.scdmxs) {
                                    items.add(item.scd_no + "\n未入库数量\t\t" + (item.mx_num - item.mx_wcnum).toInt())
                                }
                                var i = 0
                                val builder = AlertDialog.Builder(this@BcprkActivity, 3)
                                builder.setTitle("选择批次")
                                builder.setCancelable(false)    //不能取消
                                builder.setSingleChoiceItems(items.toTypedArray(), 0) { _, which ->
                                    i = which
                                }
                                builder.setPositiveButton("确定") { dialog, _ ->
                                    val result = items[i].substring(0, items[i].indexOf("\n"))
                                    for (item in data.scdmxs) {
                                        if (item.scd_no == result) {
                                            bcpRkmx = BcpRkmx(data.ma_id, data.ma_name, data.ma_code, item.scdmx_id, 1,
                                                    (item.mx_num - item.mx_wcnum).toInt(), "", "")
                                            showPopup()
                                            break
                                        }
                                    }
                                    dialog.dismiss()
                                }
                                builder.create().show()
                            }
                        }
                    })
        }

    }

    /**
     * 显示弹框
     */
    private fun showPopup() {
        if (status == ScanStatus.EMPTY) {
            status = ScanStatus.SCAN
        }
        if (bcpPopup == null) {
            bcpPopup = BcpPopup(this@BcprkActivity)
        }
        var flag = true
        for (item in bcpRkd.bcpRkmxs) {
            if (item.ma_code == barcodeStr) {
                if (item.num > 0) {
                    bcpPopup!!.setUpdate()
                } else {
                    bcpPopup!!.setScan()
                }
                flag = false
                break
            }
        }
        if (flag) {
            bcpPopup!!.setScan()
        }
        bcpPopup!!.showPopupWindow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "半成品入库"
        val user = (application as App).user
        editText_bcp_ck.setText(user?.ck_name)
        bcpRkd = BcpRkd(user!!.ck_id ?: 0, user.ck_name ?: "", mutableListOf())

        // 为按钮设置点击监听事件
        rk.setOnClickListener(this)
        editText_bcp_ck.setOnClickListener(this)

        bcpAdapter = object : CommonAdapter<BcpRkmx>(bcpRkd.bcpRkmxs, R.layout.listview_bcprk_item) {
            override fun convert(holder: ViewHolder, t: BcpRkmx, position: Int) {
                holder.setText(R.id.textView_name, t.ma_name)
                holder.setText(R.id.textView_wrknum, t.wrk_num.toString())
                holder.setText(R.id.textView_num, t.num.toString())
//                holder.setText(R.id.textView_hw, t.hw ?: "")
                holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
                    DialogUtil.showDialog(this@BcprkActivity, null, "是否删除?", null,
                        DialogInterface.OnClickListener { _, _ ->
                            //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                            (holder.getConvertView() as SwipeMenuLayout).quickClose()
                            bcpRkd.bcpRkmxs.removeAt(position)
                            notifyDataSetChanged()
                        }
                    )
                })
                holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                    if (t.num > 0) {
                        if (bcpPopup == null) {
                            bcpPopup = BcpPopup(this@BcprkActivity)
                        }
                        bcpRkmx = t
                        bcpPopup!!.setUpdate()     // 设置为修改界面
                        bcpPopup!!.showPopupWindow()
                    }
                })
            }
        }
        listView_wl.adapter = bcpAdapter
        scrollView_wlrk.smoothScrollTo(0, 0)
    }

    override fun onResume() {
        super.onResume()
        mScanManager = ScanManager()
        mScanManager.openScanner()
        mScanManager.switchOutputMode(0)
        if (!receiverTag) {     //在注册广播接受者的时候 判断是否已被注册,避免重复多次注册广播
            receiverTag = true
            registerReceiver(mScanReceiver, IntentFilter(SCAN_ACTION))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (receiverTag) {   //判断广播是否注册
            receiverTag = false
            unregisterReceiver(mScanReceiver)
        }
        if (bcpPopup != null) {
            bcpPopup!!.dismiss()
            bcpPopup = null
        }
        if (ckListPopup != null) {
            ckListPopup!!.dismiss()
            ckListPopup = null
        }
    }

    override fun isExit(): Boolean {
        return status == ScanStatus.SCAN || status == ScanStatus.FINISH
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (status == ScanStatus.SCAN || status == ScanStatus.FINISH) {
                    DialogUtil.showDialog(this, null, "数据未保存,是否离开?",
                            null,
                            DialogInterface.OnClickListener { _, _ ->
                                finish()
                            }
                    )
                } else {
                    finish()
                }
            }
        }
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
//            R.id.scan -> {      // 扫描
//                when (status) {
//                    ScanStatus.EMPTY -> Toast.makeText(this@BcprkActivity, "请选择进货单", Toast.LENGTH_SHORT).show()
//                    ScanStatus.SCAN, ScanStatus.FINISH -> {
//                        bcpPopup = BcpPopup(this@BcprkActivity)
//                        bcpPopup!!.setScan()     // 设置为扫描界面
//                        bcpPopup!!.showPopupWindow()
//                    }
//                }
//            }
            R.id.rk -> {    // 入库
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@BcprkActivity, "请扫描半成品", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
                        DialogUtil.showDialog(this, null, "是否入库?",
                                null,
                                DialogInterface.OnClickListener { _, _ ->
                                    LoadingDialog.show(this@BcprkActivity)
//                                    val json = ObjectMapper().writeValueAsBytes(bcpRkd)
                                    val json = Gson().toJson(bcpRkd)
                                    val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                                    RetrofitManager.instance.bcpRk(requestBody)
                                            .enqueue(object : BaseCallback<List<BcpRkmx>>(context = this) {
                                                override fun successInfo(info: String) {
                                                    Toast.makeText(this@BcprkActivity, "完成入库", Toast.LENGTH_SHORT).show()
                                                    clearData()
                                                }
                                                override fun successData(data: List<BcpRkmx>) {
                                                    Log.i("result", data.toString())
                                                    Toast.makeText(this@BcprkActivity, "有半成品超出可入库数量", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                }
                        )
                    }
                    else -> {
                    }
                }
            }
            R.id.editText_bcp_ck -> {   //更换仓库
                ckListPopup = ckListPopup ?: CkListPopup(this@BcprkActivity)
                ckListPopup!!.getCkList()
                ckListPopup!!.showPopupWindow()
            }
        }
    }

    /***
     * 清除数据,重置表单
     */
    private fun clearData() {
        status = ScanStatus.EMPTY
        bcpRkmx = null
        bcpRkd.bcpRkmxs.clear()
        bcpAdapter.notifyDataSetChanged()
    }
}
