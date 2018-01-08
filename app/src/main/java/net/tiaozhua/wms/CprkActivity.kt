package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.content.*
import android.device.ScanManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.gson.Gson
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import kotlinx.android.synthetic.main.activity_cprk.*
import kotlinx.android.synthetic.main.popup_scan_product.view.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.Rkd
import net.tiaozhua.wms.bean.Rkmx
import net.tiaozhua.wms.bean.SCAN_ACTION
import net.tiaozhua.wms.bean.ScanStatus
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import okhttp3.MediaType
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*

class CprkActivity : BaseActivity(R.layout.activity_cprk), View.OnClickListener {

    private var cpPopup: CpPopup? = null
    internal var status = ScanStatus.EMPTY
    private lateinit var mVibrator: Vibrator
    private lateinit var mScanManager: ScanManager
    private val soundpool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100)
    private val soundid by lazy { soundpool.load("/etc/Scan_new.ogg", 1) }
    private lateinit var barcodeStr: String
    private var receiverTag: Boolean = false
    internal lateinit var rkd: Rkd
    internal lateinit var rkmx: Rkmx
    internal lateinit var cpAdapter: BaseAdapter

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            soundpool.play(soundid, 1f, 1f, 0, 0, 1f)
            mVibrator.vibrate(100)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            if (cpPopup == null) {      // 如果弹窗不存在，先创建
                cpPopup = CpPopup(this@CprkActivity)
                cpPopup!!.setScan()     // 设置为扫描界面
                cpPopup!!.showPopupWindow()
            }
            var flag = true
            if (!cpPopup!!.isShowing) {     // 如果弹窗未显示，先打开
                for (item in rkd.rkmx) {
                    if (item.xsd_bz_id.toString() == barcodeStr) {
                        if (item.mx_num > 0) {
                            rkmx = item
                            cpPopup!!.setUpdate()
                        } else {
                            cpPopup!!.setScan()
                        }
                        flag = false
                        break
                    }
                }
                if (flag) {
                    cpPopup!!.setScan()
                }
                cpPopup!!.showPopupWindow()
            } else {        // 如果弹窗存在，检查是否需要修改
                for (item in rkd.rkmx) {
                    if (item.xsd_bz_id.toString() == barcodeStr) {
                        if (item.mx_num > 0) {
                            rkmx = item
                            cpPopup!!.setUpdate()
                            flag = false
                        }
                        break
                    }
                }
            }
            if (flag) {     // 扫描新产品
                cpPopup!!.popupView.editText_protm.setText(barcodeStr)
                val id = try {
                    barcodeStr.toInt()
                } catch (e: NumberFormatException) {
                    0
                }
                if (id == 0) {
                    Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                    return
                }
                LoadingDialog.show(this@CprkActivity)
                RetrofitManager.instance.getProductRkInfo(id)
                        .enqueue(object : BaseCallback<Rkmx>(this@CprkActivity) {
                            override fun successData(data: Rkmx) {
                                LoadingDialog.dismiss()
                                rkmx = data
                                val popup = cpPopup!!.popupView
                                popup.textView_scdno.text = rkmx.scd_no
                                popup.textView_proname.text = rkmx.pro_name
                                popup.textView_bzcode.text = rkmx.bz_code
                                popup.editText_pronum.requestFocus()
                                popup.editText_pronum.setSelection(popup.editText_pronum.text.toString().trim().length)
                                DialogUtil.showInputMethod(context, popup.editText_pronum, true, 100)
                            }

                            override fun successInfo(info: String) {
                                Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                            }
                        })
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        toolbarTitle.text = "成品入库"
        rkd = Rkd("", 0, "", "", "", mutableListOf(), 1)

        rkd.ck_id = 83
        rkd.ck_name = "成品仓库"
        rkd.rk_ldrq = SimpleDateFormat("yyyy-MM-dd").format(Date())
        editText_ck.setText(rkd.ck_name)
        editText_date.setText(rkd.rk_ldrq)
        refreshNo()

//        RetrofitManager.instance.ckList()
//                .enqueue(object : BaseCallback<ResponseList<Ck>>(context = this) {
//                    override fun successData(data: ResponseList<Ck>) {
//                        ckId = data.items[0].ck_id
//                        textView_ck.text = data.items[0].ck_name
//                    }
//                })

        // 为按钮设置点击监听事件
        scan.setOnClickListener(this@CprkActivity)
        rk.setOnClickListener(this@CprkActivity)

        scrollView_wlrk.smoothScrollTo(0, 0)
        if (!receiverTag) {     //在注册广播接受者的时候 判断是否已被注册,避免重复多次注册广播
            receiverTag = true
            registerReceiver(mScanReceiver, IntentFilter(SCAN_ACTION))
        }

        cpAdapter = object : CommonAdapter<Rkmx>(rkd.rkmx, R.layout.listview_cp_item) {
            override fun convert(holder: ViewHolder, t: Rkmx, position: Int) {
                holder.setText(R.id.textView_no, t.scd_no)
                holder.setText(R.id.textView_name, t.pro_name)
                holder.setText(R.id.textView_bzcode, t.bz_code)
                holder.setText(R.id.textView_num, t.mx_num.toString())
                holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
                    //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                    (holder.getConvertView() as SwipeMenuLayout).quickClose()
                    DialogUtil.showDialog(this@CprkActivity, null, "是否删除?",
                            null,
                            DialogInterface.OnClickListener { _, _ ->
                                rkd.rkmx.removeAt(position)
                                notifyDataSetChanged()
                            }
                    )
                })
                holder.setOnClickListener(R.id.layout_cp, View.OnClickListener { _ ->
                    if (t.mx_num > 0) {
                        if (cpPopup == null) {
                            cpPopup = CpPopup(this@CprkActivity)
                        }
                        rkmx = t
                        cpPopup!!.setUpdate()     // 设置为修改界面
                        cpPopup!!.showPopupWindow()
                    }
                })
            }
        }
        listView_cprk.adapter = cpAdapter
    }

    override fun onResume() {
        super.onResume()
        mScanManager = ScanManager()
        mScanManager.openScanner()
        mScanManager.switchOutputMode(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (receiverTag) {   //判断广播是否注册
            receiverTag = false
            unregisterReceiver(mScanReceiver)
        }
        if (cpPopup != null) {
            cpPopup!!.dismiss()
            cpPopup = null
        }
    }

    override fun isExit(): Boolean {
        return status == ScanStatus.SCAN
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (status == ScanStatus.SCAN) {
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
            R.id.scan -> {      // 扫描
                cpPopup = CpPopup(this@CprkActivity)
                cpPopup!!.setScan()     // 设置为扫描界面
                cpPopup!!.showPopupWindow()
            }
            R.id.rk -> {    // 入库
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@CprkActivity, "请扫描成品", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
                        LoadingDialog.show(this)
                        val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(rkd))
                        RetrofitManager.instance.insertRk(requestBody)
                                .enqueue(object : BaseCallback<List<Rkmx>>(context = this) {
                                    override fun successInfo(info: String) {
                                        Toast.makeText(this@CprkActivity, "完成入库", Toast.LENGTH_SHORT).show()
                                        clearData()
                                    }
                                    override fun successData(data: List<Rkmx>) {
                                        Toast.makeText(this@CprkActivity, "有成品超出可入库数量", Toast.LENGTH_SHORT).show()
                                    }
                                })
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun refreshNo() {
        RetrofitManager.instance.getRkNo().enqueue(object : BaseCallback<String>(this) {
            @SuppressLint("SimpleDateFormat")
            override fun successData(data: String) {
                editText_no.setText(data)     // 查询入库单编号
                rkd.rk_no = data
            }
        })
    }

    /***
     * 清除数据,重置表单
     */
    private fun clearData() {
        status = ScanStatus.EMPTY
        refreshNo()
        editText_beizhu.text.clear()
        rkd.rkmx.clear()
        cpAdapter.notifyDataSetChanged()
    }
}
