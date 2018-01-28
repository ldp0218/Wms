package net.tiaozhua.wms

import android.app.Activity
import android.content.*
import android.device.ScanManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.gson.Gson
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import kotlinx.android.synthetic.main.activity_wlck.*
import kotlinx.android.synthetic.main.popup_scan_material.view.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import okhttp3.MediaType
import okhttp3.RequestBody

class WlckActivity : BaseActivity(R.layout.activity_wlck), View.OnClickListener {

    private var wlPopup: WlPopup? = null
    private lateinit var mVibrator: Vibrator
    private lateinit var mScanManager: ScanManager
    private val soundpool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100)
    private val soundid by lazy { soundpool.load("/etc/Scan_new.ogg", 1) }
    private lateinit var barcodeStr: String
    private var receiverTag: Boolean = false
    private var status = ScanStatus.EMPTY
    internal lateinit var ckd: Ckd
    internal lateinit var ckdmx: Ckdmx
    internal var material: Material? = null
    internal lateinit var wlAdapter: BaseAdapter

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            soundpool.play(soundid, 1f, 1f, 0, 0, 1f)
            mVibrator.vibrate(100)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            if (barcodeStr.startsWith(DjFlag.LLD.value)) {  //  以此标志判断扫描的是单据还是物料
                val id = try {      // 截取后面的id
                    barcodeStr.substring(2).toInt()
                } catch (e: NumberFormatException) { 0 }
                if (id == 0) {
                    Toast.makeText(this@WlckActivity, "未识别的二维码", Toast.LENGTH_SHORT).show()
                    return
                }
                LoadingDialog.show(this@WlckActivity)
                RetrofitManager.instance.lldInfo(id)
                        .enqueue(object : BaseCallback<Ckd>(context = this@WlckActivity) {
                            override fun successData(data: Ckd) {
                                ckd = data
                                if (ckd.pdacode == 0) {    // 判断是否已领料
                                    Toast.makeText(this@WlckActivity, "已领完料", Toast.LENGTH_SHORT).show()
                                    return
                                } else if (ckd.pdacode == 2) {     // 是否存在该单据
                                    Toast.makeText(this@WlckActivity, "该领料单不存在", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                editText_no.setText(ckd.ckd_no)
//                                editText_date.setText(ckd.ckd_ldrq)
//                                editText_llr.setText(ckd.llr_name)
//                                editText_bm.setText(ckd.bm_name)
//                                editText_ck.setText(ckd.ck_name)
//                                editText_beizhu.setText(ckd.remark)
                                val ckdmxList = ckd.ckdmx
                                if (ckdmxList.size > 0) {
                                    status = ScanStatus.SCAN
                                    ckdmxList.forEach { it.mx_num = 0 }
                                    wlAdapter = object : CommonAdapter<Ckdmx>(ckdmxList, R.layout.listview_wlck_item) {
                                        override fun convert(holder: ViewHolder, t: Ckdmx, position: Int) {
                                            holder.setText(R.id.textView_no, t.ma_name)
                                            holder.setText(R.id.textView_wcknum, (t.mx_num - t.wc_num).toString())
                                            holder.setText(R.id.textView_cknum, t.ck_num.toString())
                                            holder.setText(R.id.textView_hw, t.hj_name ?: "")
                                            holder.setText(R.id.textView_dw, t.ma_unit)
                                            holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
                                                //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                                                (holder.getConvertView() as SwipeMenuLayout).quickClose()
                                                val obj = ckd.ckdmx[position]
                                                if (obj.ck_num == 0) {
                                                    ckd.ckdmx.removeAt(position)
                                                    notifyDataSetChanged()
                                                } else {
                                                    Toast.makeText(this@WlckActivity, "物料已扫描不能删除", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                            holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                                                if (ckd.ckdmx[position].ck_num > 0) {
                                                    if (wlPopup == null) {
                                                        wlPopup = WlPopup(this@WlckActivity)
                                                    }
                                                    ckdmx = ckd.ckdmx[position]
                                                    wlPopup!!.setUpdate()     // 设置为修改界面
                                                    wlPopup!!.showPopupWindow()
                                                }
                                            })
                                        }
                                    }
                                    listView_wlck.adapter = wlAdapter
                                }
                            }
                        })
            } else {
                var flag = true
                when (status) {
                    ScanStatus.SCAN, ScanStatus.FINISH -> {
                        if (wlPopup == null) {
                            wlPopup = WlPopup(this@WlckActivity)
                            wlPopup!!.setScan()
                            wlPopup!!.showPopupWindow()
                        }
                        if (!wlPopup!!.isShowing) {
                            for (item in ckd.ckdmx) {
                                if (item.ma_txm == barcodeStr) {
                                    if (item.ck_num > 0) {
                                        ckdmx = item
                                        wlPopup!!.setUpdate()
                                    } else {
                                        wlPopup!!.setScan()
                                    }
                                    flag = false
                                    break
                                }
                            }
                            if (flag) {
                                wlPopup!!.setScan()
                            }
                            wlPopup!!.showPopupWindow()
                        } else {
                            for (item in ckd.ckdmx) {
                                if (item.ma_txm == barcodeStr) {
                                    if (item.ck_num > 0) {
                                        ckdmx = item
                                        wlPopup!!.setUpdate()
                                        flag = false
                                    }
                                    break
                                }
                            }
                        }
                    }
                    else -> Toast.makeText(this@WlckActivity, "请选择领料单", Toast.LENGTH_SHORT).show()
                }
                if (flag) {     // 扫描新的
                    val view = wlPopup!!.popupView
                    view.editText_tm.setText(barcodeStr)
                    LoadingDialog.show(this@WlckActivity)
                    RetrofitManager.instance.materialList(barcodeStr, ckd.ck_id)
                            .enqueue(object : BaseCallback<ResponseList<Material>>(this@WlckActivity) {
                                override fun successData(data: ResponseList<Material>) {
                                    when {
                                        data.totalCount == 0 -> Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                                        data.totalCount > 1 -> {
                                            val activityIntent = Intent(this@WlckActivity, MaterialSelectActivity::class.java)
                                            activityIntent.putExtra("code", barcodeStr)
                                            activityIntent.putExtra("ckId", ckd.ck_id)
                                            intent.putExtra("data", data)
                                            startActivityForResult(activityIntent, 0)
                                        }
                                        else -> {
                                            material = data.items[0]
                                            val popupView = wlPopup!!.popupView
                                            popupView.editText_tm.setText(material!!.ma_txm)
                                            popupView.textView_no.text = material!!.ma_code
                                            popupView.textView_no.text = material!!.ma_name
                                            popupView.textView_kcnum.text = material!!.kc_num.toString()
                                            popupView.textView_spec.text = material!!.ma_spec
                                            popupView.textView_model.text = material!!.ma_model
                                            popupView.textView_hw.text = material!!.kc_hw_name
                                            popupView.textView_remark.text = material!!.comment
                                            popupView.editText_num.requestFocus()
                                            popupView.editText_num.setSelection(popupView.editText_num.text.toString().trim().length)
                                            DialogUtil.showInputMethod(context, popupView.editText_num, true, 100)
                                        }
                                    }
                                }
                            })
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        toolbarTitle.text = "物料出库"

        // 为按钮设置点击监听事件
        scan.setOnClickListener(this)
        lld.setOnClickListener(this)
        ck.setOnClickListener(this)

        scrollView_wlck.smoothScrollTo(0, 0)
        if (!receiverTag) {     //在注册广播接受者的时候 判断是否已被注册,避免重复多次注册广播
            receiverTag = true
            registerReceiver(mScanReceiver, IntentFilter(SCAN_ACTION))
        }
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
            Activity.RESULT_OK -> {     // 选择了领料单
                ckd = intent?.getSerializableExtra("ckd") as Ckd
                LoadingDialog.show(this@WlckActivity)
                RetrofitManager.instance.ckdmxList(ckd.ckd_id, ckd.ck_id)
                        .enqueue(object : BaseCallback<List<Ckdmx>>(context = this) {
                            override fun successData(data: List<Ckdmx>) {
                                ckd.ckdmx = data.toMutableList()
                                editText_no.setText(ckd.ckd_no)
//                                editText_date.setText(ckd.ckd_ldrq)
//                                editText_llr.setText(ckd.llr_name)
//                                editText_bm.setText(ckd.bm_name)
//                                editText_ck.setText(ckd.ck_name)
//                                editText_beizhu.setText(ckd.remark)
                                if (ckd.ckdmx.size > 0) {
                                    status = ScanStatus.SCAN
                                    wlAdapter = object : CommonAdapter<Ckdmx>(ckd.ckdmx, R.layout.listview_wlck_item) {
                                        override fun convert(holder: ViewHolder, t: Ckdmx, position: Int) {
                                            holder.setText(R.id.textView_no, t.ma_name)
                                            holder.setText(R.id.textView_wcknum, (t.mx_num - t.wc_num).toString())
                                            holder.setText(R.id.textView_cknum, t.ck_num.toString())
                                            holder.setText(R.id.textView_hw, t.hj_name ?: "")
                                            holder.setText(R.id.textView_dw, t.ma_unit)
                                            holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
                                                //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                                                (holder.getConvertView() as SwipeMenuLayout).quickClose()
                                                val obj = ckd.ckdmx[position]
                                                if (obj.ck_num == 0) {
                                                    ckd.ckdmx.removeAt(position)
                                                    notifyDataSetChanged()
                                                } else {
                                                    Toast.makeText(this@WlckActivity, "物料已扫描不能删除", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                            holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                                                if (ckd.ckdmx[position].ck_num > 0) {
                                                    if (wlPopup == null) {
                                                        wlPopup = WlPopup(this@WlckActivity)
                                                    }
                                                    ckdmx = ckd.ckdmx[position]
                                                    wlPopup!!.setUpdate()     // 设置为修改界面
                                                    wlPopup!!.showPopupWindow()
                                                }
                                            })
                                        }
                                    }
                                    listView_wlck.adapter = wlAdapter
                                }
                            }
                        })
            }
            1 -> {      // 选择了物料
                material = intent?.getSerializableExtra("material") as Material
                val view = wlPopup!!.popupView
                view.editText_tm.setText(material!!.ma_txm)
                view.textView_no.text = material!!.ma_code
                view.textView_name.text = material!!.ma_name
                view.textView_kcnum.text = material!!.kc_num.toString()
                view.textView_spec.text = material!!.ma_spec
                view.textView_model.text = material!!.ma_model
                view.textView_hw.text = material!!.kc_hw_name
                view.textView_remark.text = material!!.comment
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.scan -> {      // 扫描
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@WlckActivity, "请选择领料单", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN, ScanStatus.FINISH -> {
                        wlPopup = WlPopup(this@WlckActivity)
                        wlPopup!!.setScan()     // 设置为扫描界面
                        wlPopup!!.showPopupWindow()
                    }
                }
            }
            R.id.lld -> {   // 领料单
                val intent = Intent(this@WlckActivity, WlckdActivity::class.java)
                startActivityForResult(intent, 0)
            }
            R.id.ck -> {    // 出库
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@WlckActivity, "请选择领料单", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
                        val isNotFinished = ckd.ckdmx.any { it.ck_num == 0 }
                        if (isNotFinished) {
                            Toast.makeText(this@WlckActivity, "有物料未扫描", Toast.LENGTH_SHORT).show()
                        } else {
                            status = ScanStatus.FINISH
                            DialogUtil.showDialog(this, null, "物料已全部扫描,是否出库?",
                                    null,
                                    DialogInterface.OnClickListener { _, _ ->
                                        LoadingDialog.show(this@WlckActivity)
                                        ckd.ckdmx.forEach { it.mx_num = it.ck_num }
                                        val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(ckd))
                                        RetrofitManager.instance.lldCk(requestBody)
                                                .enqueue(object : BaseCallback<CkdFailureData>(context = this) {
                                                    override fun successInfo(info: String) {
                                                        Toast.makeText(this@WlckActivity, "完成出库", Toast.LENGTH_SHORT).show()
                                                        clearData()
                                                    }
                                                    override fun failureData(data: CkdFailureData) {
                                                        Log.i("result", data.toString())
                                                        if (data.clldList!= null && data.clldList.isNotEmpty()) {
                                                            Toast.makeText(this@WlckActivity, "有物料超出可领数量", Toast.LENGTH_SHORT).show()
                                                        }
                                                        if (data.ckcList!= null && data.ckcList.isNotEmpty()) {
                                                            Toast.makeText(this@WlckActivity, "有物料超出库存数量", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                })
                                    }
                            )
                        }
                    }
                    ScanStatus.FINISH -> {
                        DialogUtil.showDialog(this, null, "物料已全部扫描,是否出库?",
                                null,
                                DialogInterface.OnClickListener { _, _ ->
                                    LoadingDialog.show(this@WlckActivity)
                                    ckd.ckdmx.forEach { it.mx_num = it.ck_num }
                                    val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(ckd))
                                    RetrofitManager.instance.lldCk(requestBody)
                                            .enqueue(object : BaseCallback<CkdFailureData>(context = this) {
                                                override fun successInfo(info: String) {
                                                    Toast.makeText(this@WlckActivity, "完成出库", Toast.LENGTH_SHORT).show()
                                                    clearData()
                                                }
                                                override fun failureData(data: CkdFailureData) {
                                                    Log.i("result", data.toString())
                                                    if (data.clldList!= null && data.clldList.isNotEmpty()) {
                                                        Toast.makeText(this@WlckActivity, "有物料超出可领数量", Toast.LENGTH_SHORT).show()
                                                    }
                                                    if (data.ckcList!= null && data.ckcList.isNotEmpty()) {
                                                        Toast.makeText(this@WlckActivity, "有物料超出库存数量", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            })
                                }
                        )
                    }
                }
            }
        }
    }

    /***
     * 清除数据,重置表单
     */
    fun clearData() {
        status = ScanStatus.EMPTY
        editText_no.text.clear()
//        editText_date.text.clear()
//        editText_llr.text.clear()
//        editText_bm.text.clear()
//        editText_ck.text.clear()
//        editText_beizhu.text.clear()
        ckd.ckdmx.clear()
        wlAdapter.notifyDataSetChanged()
    }
}
