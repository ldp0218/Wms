package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
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
import kotlinx.android.synthetic.main.activity_wlrk.*
import kotlinx.android.synthetic.main.popup_scan_material.view.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*

class WlrkActivity : BaseActivity(R.layout.activity_wlrk), View.OnClickListener {

    private var wlPopup: WlPopup? = null
    private lateinit var mScanManager: ScanManager
    private lateinit var barcodeStr: String
    internal var receiverTag: Boolean = false
    private var status = ScanStatus.EMPTY
    internal lateinit var jhd: Jhd
    internal var jhmx: Jhdmx? = null
    internal var material: Material? = null
    internal lateinit var wlAdapter: BaseAdapter
    private var ckListPopup: CkListPopup? = null

    internal val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            (application as App).playAndVibrate(this@WlrkActivity)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            if (barcodeStr.startsWith(DjFlag.JHD.value)) {  //  以此标志判断扫描的是单据还是物料
                if (status == ScanStatus.SCAN) {
                    Toast.makeText(this@WlrkActivity, "有进货单未完成", Toast.LENGTH_SHORT).show()
                    return
                }
                val id = try {      // 截取后面的id
                    barcodeStr.substring(2).toInt()
                } catch (e: NumberFormatException) { 0 }
                if (id == 0) {
                    Toast.makeText(this@WlrkActivity, "未识别的二维码", Toast.LENGTH_SHORT).show()
                    return
                }
                if (receiverTag) {
                    receiverTag = false
                    unregisterReceiver(this)
                }
                LoadingDialog.show(this@WlrkActivity)
                RetrofitManager.instance.jhdInfo(id)
                        .enqueue(object : BaseCallback<Jhd>(context = this@WlrkActivity) {
                            override fun successData(data: Jhd) {
                                jhd.pdacode = data.pdacode
                                if (jhd.pdacode == 0) {    // 判断是否已入库
                                    Toast.makeText(this@WlrkActivity, "该进货单已入库", Toast.LENGTH_SHORT).show()
                                    return
                                } else if (jhd.pdacode == 2) {     // 是否存在该单据
                                    Toast.makeText(this@WlrkActivity, "该进货单未审核", Toast.LENGTH_SHORT).show()
                                    return
                                }

                                jhd.client_id = data.client_id
                                jhd.client_name = data.client_name
                                jhd.handler_id = data.handler_id
                                jhd.handler_name = data.handler_name
                                jhd.jhd_id = data.jhd_id
                                jhd.jhd_ldrq = data.jhd_ldrq
                                jhd.jhd_no = data.jhd_no
                                jhd.jhd_wwcnum = data.jhd_wwcnum
                                jhd.jhmx = data.jhmx
                                editText_no.setText(jhd.jhd_no)
                                val jhmxList = jhd.jhmx
                                if (jhmxList !== null && jhmxList.size > 0) {
                                    status = ScanStatus.SCAN
                                    val iterator = jhmxList.iterator()
                                    while (iterator.hasNext()) {
                                        if (iterator.next().mx_wwcnum == 0.0) {
                                            iterator.remove()
                                        }
                                    }
                                    wlAdapter = object : CommonAdapter<Jhdmx>(jhmxList, R.layout.listview_wlrk_item) {
                                        override fun convert(holder: ViewHolder, t: Jhdmx, position: Int) {
                                            holder.setText(R.id.textView_no, t.ma_name)
                                            holder.setText(R.id.textView_wrknum, t.mx_wwcnum.toString())
                                            holder.setText(R.id.textView_cknum, t.mx_num.toString())
                                            holder.setText(R.id.textView_price, t.mx_price.toString())
                                            holder.setText(R.id.textView_hw, t.hj_name ?: "")
                                            holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
                                                //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                                                (holder.getConvertView() as SwipeMenuLayout).quickClose()
                                                jhmxList.removeAt(position)
                                                notifyDataSetChanged()
                                            })
                                            holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                                                if (t.mx_num > 0) {
                                                    if (wlPopup == null) {
                                                        wlPopup = WlPopup(this@WlrkActivity)
                                                    }
                                                    jhmx = t
                                                    wlPopup!!.setUpdate()     // 设置为修改界面
                                                    wlPopup!!.showPopupWindow()
                                                }
                                            })
                                        }
                                    }
                                    listView_wl.adapter = wlAdapter
                                }
                            }
                        })
            } else {
                when (status) {
                    ScanStatus.SCAN, ScanStatus.FINISH -> {
                        var flag = true
                        for (item in jhd.jhmx!!) {
                            if (item.ma_code == barcodeStr) {
                                if (wlPopup == null) {
                                    wlPopup = WlPopup(this@WlrkActivity)
                                    wlPopup!!.showPopupWindow()
                                }
                                if (!wlPopup!!.isShowing) {
                                    wlPopup!!.showPopupWindow()
                                }
                                jhmx = item
                                if (item.mx_num > 0) {
                                    wlPopup!!.setUpdate()
                                } else {
                                    wlPopup!!.setScan()
                                }
                                flag = false
                                break
                            }
                        }
                        if (flag) {
                            Toast.makeText(this@WlrkActivity, "入库单不包含该物料，请扫描其它物料", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    else -> {
                        Toast.makeText(this@WlrkActivity, "请选择进货单", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
//                if (flag) {     // 扫描新的
//                    wlPopup!!.popupView.editText_tm.setText(barcodeStr)
//                    if (receiverTag) {
//                        receiverTag = false
//                        unregisterReceiver(this)
//                    }
//                    LoadingDialog.show(this@WlrkActivity)
//                    RetrofitManager.instance.materialList(barcodeStr, jhd.ck_id)
//                            .enqueue(object : BaseCallback<ResponseList<Material>>(this@WlrkActivity) {
//                                override fun successData(data: ResponseList<Material>) {
//                                    when {
//                                        data.totalCount == 0 -> Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
//                                        data.totalCount > 1 -> {
//                                            val activityIntent = Intent(this@WlrkActivity, MaterialSelectActivity::class.java)
//                                            activityIntent.putExtra("code", barcodeStr)
//                                            activityIntent.putExtra("ckId", jhd.ck_id)
//                                            intent.putExtra("data", data)
//                                            startActivityForResult(activityIntent, 0)
//                                        }
//                                        else -> {
//                                            material = data.items[0]
//                                            val popupView = wlPopup!!.popupView
//                                            popupView.editText_tm.setText(material!!.ma_code)
//                                            popupView.textView_name.text = material!!.ma_name
//                                            popupView.textView_kcnum.text = material!!.kc_num.toString()
//                                            popupView.textView_kind.text = material!!.ma_kind_name
//                                            popupView.textView_hw.text = material!!.kc_hw_name
//                                            popupView.textView_remark.text = material!!.comment
//                                            popupView.editText_num.requestFocus()
//                                            popupView.editText_num.setSelection(popupView.editText_num.text.toString().trim().length)
//                                            DialogUtil.showInputMethod(context, popupView.editText_num, true, 100)
//                                        }
//                                    }
//                                }
//                            })
//                }
            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "物料入库"
        val user = (application as App).user
        editText_ck.setText(user?.ck_name)

        // 为按钮设置点击监听事件
        scan.setOnClickListener(this)
        btn_jhd.setOnClickListener(this)
        rk.setOnClickListener(this)
        editText_ck.setOnClickListener(this)

        jhd = Jhd(0,"",0,"","",0,0,"", "",
                "",0,"",0.0,0.0,"","", mutableListOf())
        jhd.ck_id = user?.ck_id ?: 0
        jhd.ck_name = user?.ck_name ?: ""

        jhd.jh_ldrq = SimpleDateFormat("yyyy-MM-dd").format(Date())
        refreshNo()

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
        if (wlPopup != null) {
            wlPopup!!.dismiss()
            wlPopup = null
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {     // 选择了入库单
                LoadingDialog.show(this@WlrkActivity)
                RetrofitManager.instance.jhdInfo(intent!!.getIntExtra("jhd_id", 0))
                        .enqueue(object : BaseCallback<Jhd>(context = this) {
                            override fun successData(data: Jhd) {
                                jhd.client_id = data.client_id
                                jhd.client_name = data.client_name
                                jhd.handler_id = data.handler_id
                                jhd.handler_name = data.handler_name
                                jhd.jhd_id = data.jhd_id
                                jhd.jhd_ldrq = data.jhd_ldrq
                                jhd.jhd_no = data.jhd_no
                                jhd.jhd_wwcnum = data.jhd_wwcnum
                                jhd.jhmx = data.jhmx
                                jhd.pdacode = data.pdacode
                                if (jhd.pdacode == 0) {    // 判断是否已领料
                                    Toast.makeText(this@WlrkActivity, "该进货单已入库", Toast.LENGTH_SHORT).show()
                                    return
                                } else if (jhd.pdacode == 2) {     // 是否存在该单据
                                    Toast.makeText(this@WlrkActivity, "该进货单不存在", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                editText_no.setText(jhd.jhd_no)
                                val jhmxList = jhd.jhmx
                                if (jhmxList != null && jhmxList.size > 0) {
                                    status = ScanStatus.SCAN
                                    val iterator = jhmxList.iterator()
                                    while (iterator.hasNext()) {
                                        if (iterator.next().mx_wwcnum == 0.0) {
                                            iterator.remove()
                                        }
                                    }
                                    wlAdapter = object : CommonAdapter<Jhdmx>(jhmxList, R.layout.listview_wlrk_item) {
                                        override fun convert(holder: ViewHolder, t: Jhdmx, position: Int) {
                                            holder.setText(R.id.textView_no, t.ma_name)
                                            holder.setText(R.id.textView_wrknum, t.mx_wwcnum.toString())
                                            holder.setText(R.id.textView_cknum, t.mx_num.toString())
                                            holder.setText(R.id.textView_price, t.mx_price.toString())
                                            holder.setText(R.id.textView_hw, t.hj_name ?: "")
                                            holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
                                                //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                                                (holder.getConvertView() as SwipeMenuLayout).quickClose()
                                                if (t.mx_num == 0.0) {
                                                    jhmxList.removeAt(position)
                                                    notifyDataSetChanged()
                                                } else {
                                                    Toast.makeText(this@WlrkActivity, "物料已扫描不能删除", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                            holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                                                if (t.mx_num > 0) {
                                                    if (wlPopup == null) {
                                                        wlPopup = WlPopup(this@WlrkActivity)
                                                    }
                                                    jhmx = t
                                                    wlPopup!!.setUpdate()     // 设置为修改界面
                                                    wlPopup!!.showPopupWindow()
                                                }
                                            })
                                        }
                                    }
                                    listView_wl.adapter = wlAdapter
                                }
                            }
                        })
            }
            1 -> {      // 选择了物料
                material = intent!!.getParcelableExtra("material") as Material
                val view = wlPopup!!.popupView
                view.editText_tm.setText(material!!.ma_txm)
                view.textView_name.text = material!!.ma_name
                view.textView_kcnum.text = material!!.kc_num.toString()
                view.textView_kind.text = material!!.ma_kind_name
                view.textView_hw.text = material!!.kc_hw_name
                view.editText_remark.setText(material!!.comment)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.scan -> {      // 扫描
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@WlrkActivity, "请选择进货单", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN, ScanStatus.FINISH -> {
                        wlPopup = WlPopup(this@WlrkActivity)
                        wlPopup!!.setScan()     // 设置为扫描界面
                        wlPopup!!.showPopupWindow()
                    }
                }
            }
            R.id.btn_jhd -> {   // 入库单
                if (status == ScanStatus.SCAN) {
                    Toast.makeText(this@WlrkActivity, "有进货单未完成", Toast.LENGTH_SHORT).show()
                    return
                }
                val intent = Intent(this@WlrkActivity, WljhdActivity::class.java)
                startActivityForResult(intent, 0)
            }
            R.id.rk -> {    // 入库
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@WlrkActivity, "请选择进货单", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
                        if (jhd.ck_id == 0) {
                            Toast.makeText(this@WlrkActivity, "请选择仓库", Toast.LENGTH_SHORT).show()
                            return
                        }
                        val isNotFinished = jhd.jhmx!!.any { it.mx_num == 0.0 }
                        if (isNotFinished) {
                            Toast.makeText(this@WlrkActivity, "有物料未扫描", Toast.LENGTH_SHORT).show()
                        } else {
                            status = ScanStatus.FINISH
                            DialogUtil.showDialog(this, null, "物料已全部扫描,是否入库?",
                                    null,
                                    DialogInterface.OnClickListener { _, _ ->
                                        LoadingDialog.show(this@WlrkActivity)
                                        val json = Gson().toJson(jhd)
//                                        val json = ObjectMapper().writeValueAsBytes(jhd)
                                        val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                                        RetrofitManager.instance.insertJh(requestBody)
                                                .enqueue(object : BaseCallback<List<Jhdmx>>(context = this) {
                                                    override fun successInfo(info: String) {
                                                        Toast.makeText(this@WlrkActivity, "完成入库", Toast.LENGTH_SHORT).show()
                                                        clearData()
                                                    }
                                                    override fun failureData(data: List<Jhdmx>) {
                                                        Log.i("result", data.toString())
                                                        Toast.makeText(this@WlrkActivity, "有物料超出可入库数量", Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                    }
                            )
                        }
                    }
                    ScanStatus.FINISH -> {
                        if (jhd.ck_id == 0) {
                            Toast.makeText(this@WlrkActivity, "请选择仓库", Toast.LENGTH_SHORT).show()
                            return
                        }
                        DialogUtil.showDialog(this, null, "物料已全部扫描,是否入库?",
                                null,
                                DialogInterface.OnClickListener { _, _ ->
                                    LoadingDialog.show(this@WlrkActivity)
                                    val json = Gson().toJson(jhd)
                                    val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                                    RetrofitManager.instance.insertJh(requestBody)
                                            .enqueue(object : BaseCallback<List<Jhdmx>>(context = this) {
                                                override fun successInfo(info: String) {
                                                    Toast.makeText(this@WlrkActivity, "完成入库", Toast.LENGTH_SHORT).show()
                                                    clearData()
                                                }
//                                                override fun failureData(data: List<Jhdmx>) {
//                                                    Toast.makeText(this@WlrkActivity, "有物料超出可入库数量", Toast.LENGTH_SHORT).show()
//                                                }
                                            })
                                }
                        )
                    }
                }
            }
            R.id.editText_ck -> {   //更换仓库
                ckListPopup = ckListPopup ?: CkListPopup(this@WlrkActivity)
                ckListPopup!!.getCkList()
                ckListPopup!!.showPopupWindow()
            }
        }
    }

    private fun refreshNo() {
        RetrofitManager.instance.getJhNo().enqueue(object : BaseCallback<String>(this) {
            @SuppressLint("SimpleDateFormat")
            override fun successData(data: String) {
                jhd.jh_no = data
            }
        })
    }

    /***
     * 清除数据,重置表单
     */
    private fun clearData() {
        status = ScanStatus.EMPTY
        editText_no.text.clear()
        refreshNo()
        jhd.jhmx?.clear()
        wlAdapter.notifyDataSetChanged()
    }
}
