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
import kotlinx.android.synthetic.main.activity_wlrk.*
import kotlinx.android.synthetic.main.popup_scan_material.view.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.*
import okhttp3.MediaType
import okhttp3.RequestBody

class WlrkActivity : BaseActivity(R.layout.activity_wlrk), View.OnClickListener {

    private var dialogPopup: DialogPopup? = null
    private lateinit var mVibrator: Vibrator
    private lateinit var mScanManager: ScanManager
    private val soundpool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100)
    private val soundid by lazy { soundpool.load("/etc/Scan_new.ogg", 1) }
    private lateinit var barcodeStr: String
    private var status = ScanStatus.EMPTY
    internal lateinit var jhrk: Jhrk
    internal lateinit var jhmx: Jhmx
    internal var material: Material? = null
    internal lateinit var wlAdapter: BaseAdapter
    private var receiverTag: Boolean = false

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            soundpool.play(soundid, 1f, 1f, 0, 0, 1f)
            mVibrator.vibrate(100)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            if (barcodeStr.startsWith(DjFlag.RKD.value)) {  //  以此标志判断扫描的是单据还是物料
                val id = try {      // 截取后面的id
                    barcodeStr.substring(2).toInt()
                } catch (e: NumberFormatException) { 0 }
                if (id == 0) {
                    Toast.makeText(this@WlrkActivity, "未识别二维码", Toast.LENGTH_SHORT).show()
                    return
                }
                LoadingDialog.show(this@WlrkActivity)
                RetrofitManager.instance.jhrkInfo(id)
                        .enqueue(object : BaseCallback<Jhrk>(context = this@WlrkActivity) {
                            override fun successData(data: Jhrk) {
                                jhrk = data
                                if (jhrk.pdacode == 0) {    // 判断是否已入库
                                    Toast.makeText(this@WlrkActivity, "该入库单已入库", Toast.LENGTH_SHORT).show()
                                    return
                                } else if (jhrk.pdacode == 2) {     // 是否存在该单据
                                    Toast.makeText(this@WlrkActivity, "该入库单不存在", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                editText_no.setText(jhrk.jh_no)
                                editText_date.setText(jhrk.jh_ldrq)
                                editText_ghdw.setText(jhrk.client_name)
                                editText_jsr.setText(jhrk.handler_name)
                                editText_ck.setText(jhrk.ck_name)
                                editText_beizhu.setText(jhrk.remark)
                                val jhmxList = jhrk.jhmx
                                if (jhmxList.size > 0) {
                                    status = ScanStatus.SCAN
                                    jhmxList.forEach { it.mx_num = 0 }
                                    wlAdapter = object : CommonAdapter<Jhmx>(jhmxList, R.layout.listview_wl_item) {
                                        override fun convert(holder: ViewHolder, t: Jhmx, position: Int) {
                                            holder.setText(R.id.textView_no, t.ma_name)
                                            holder.setText(R.id.textView_wrknum, t.jhdmx_num.toString())
                                            holder.setText(R.id.textView_cknum, t.mx_num.toString())
                                            holder.setText(R.id.textView_hw, t.hj_name ?: "")
                                            holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
                                                //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                                                (holder.getConvertView() as SwipeMenuLayout).quickClose()
                                                if (jhrk.delJhmx == null) {
                                                    jhrk.delJhmx = mutableListOf()
                                                }
                                                val obj = jhmxList[position]
                                                jhrk.delJhmx!!.add(obj)
                                                jhmxList.removeAt(position)
                                                notifyDataSetChanged()
                                            })
                                            holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                                                if (jhmxList[position].mx_num > 0) {
                                                    if (dialogPopup == null) {
                                                        dialogPopup = DialogPopup(this@WlrkActivity)
                                                    }
                                                    jhmx = jhmxList[position]
                                                    dialogPopup!!.setUpdate()     // 设置为修改界面
                                                    dialogPopup!!.showPopupWindow()
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
                        if (dialogPopup == null) {
                            dialogPopup = DialogPopup(this@WlrkActivity)
                            dialogPopup!!.setScan()     // 设置为扫描界面
                            dialogPopup!!.showPopupWindow()
                        }
                        if (!dialogPopup!!.isShowing) {
                            var flag = true
                            for (item in jhrk.jhmx) {
                                if (item.ma_txm == barcodeStr) {
                                    if (item.mx_num > 0) {
                                        jhmx = item
                                        dialogPopup!!.setUpdate()
                                    } else {
                                        dialogPopup!!.setScan()
                                    }
                                    flag = false
                                    break
                                }
                            }
                            if (flag) {
                                dialogPopup!!.setScan()
                            }
                            dialogPopup!!.showPopupWindow()
                        } else {
                            for (item in jhrk.jhmx) {
                                if (item.ma_txm == barcodeStr) {
                                    if (item.mx_num > 0) {
                                        jhmx = item
                                        dialogPopup!!.setUpdate()
                                    }
                                    break
                                }
                            }
                        }
                    }
                    else -> {
                        Toast.makeText(this@WlrkActivity, "请选择入库单", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                dialogPopup!!.popupView.editText_tm.setText(barcodeStr)
                LoadingDialog.show(this@WlrkActivity)
                RetrofitManager.instance.materialList(barcodeStr, jhrk.ck_id)
                        .enqueue(object : BaseCallback<ResponseList<Material>>(this@WlrkActivity) {
                            override fun successData(data: ResponseList<Material>) {
                                when {
                                    data.totalCount == 0 -> Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                                    data.totalCount > 1 -> {
                                        val activityIntent = Intent(this@WlrkActivity, MaterialSelectActivity::class.java)
                                        activityIntent.putExtra("code", barcodeStr)
                                        activityIntent.putExtra("ckId", jhrk.ck_id)
                                        intent.putExtra("data", data)
                                        startActivityForResult(activityIntent, 0)
                                    }
                                    else -> {
                                        material = data.items[0]
                                        val popupView = dialogPopup!!.popupView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        toolbarTitle.text = "物料入库"

        // 为按钮设置点击监听事件
        scan.setOnClickListener(this@WlrkActivity)
        rkd.setOnClickListener(this@WlrkActivity)
        rk.setOnClickListener(this@WlrkActivity)

        scrollView_wlrk.smoothScrollTo(0, 0)
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
        if (dialogPopup != null) {
            dialogPopup!!.dismiss()
            dialogPopup = null
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
        //super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {     // 选择了入库单
                LoadingDialog.show(this@WlrkActivity)
                RetrofitManager.instance.jhrkInfo(intent!!.getIntExtra("id", 0))
                        .enqueue(object : BaseCallback<Jhrk>(context = this) {
                            override fun successData(data: Jhrk) {
                                jhrk = data
                                jhrk.dj_id = intent.getIntExtra("dj_id", 0)
                                editText_no.setText(jhrk.jh_no)
                                editText_date.setText(jhrk.jh_ldrq)
                                editText_ghdw.setText(jhrk.client_name)
                                editText_jsr.setText(jhrk.handler_name)
                                editText_ck.setText(jhrk.ck_name)
                                editText_beizhu.setText(jhrk.remark)
                                val jhmxList = jhrk.jhmx
                                if (jhmxList.size > 0) {
                                    status = ScanStatus.SCAN
                                    jhmxList.forEach { it.mx_num = 0 }
                                    wlAdapter = object : CommonAdapter<Jhmx>(jhmxList, R.layout.listview_wl_item) {
                                        override fun convert(holder: ViewHolder, t: Jhmx, position: Int) {
                                            holder.setText(R.id.textView_no, t.ma_name)
                                            holder.setText(R.id.textView_wrknum, t.jhdmx_num.toString())
                                            holder.setText(R.id.textView_cknum, t.mx_num.toString())
                                            holder.setText(R.id.textView_hw, t.hj_name ?: "")
                                            holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
                                                //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                                                (holder.getConvertView() as SwipeMenuLayout).quickClose()
                                                val obj = jhmxList[position]
                                                if (obj.mx_num == 0) {
                                                    if (jhrk.delJhmx == null) {
                                                        jhrk.delJhmx = mutableListOf()
                                                    }
                                                    jhrk.delJhmx!!.add(obj)
                                                    jhmxList.removeAt(position)
                                                    notifyDataSetChanged()
                                                } else {
                                                    Toast.makeText(this@WlrkActivity, "物料已扫描不能删除", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                            holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                                                if (jhmxList[position].mx_num > 0) {
                                                    if (dialogPopup == null) {
                                                        dialogPopup = DialogPopup(this@WlrkActivity)
                                                    }
                                                    jhmx = jhmxList[position]
                                                    dialogPopup!!.setUpdate()     // 设置为修改界面
                                                    dialogPopup!!.showPopupWindow()
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
                material = intent!!.getSerializableExtra("material") as Material
                val view = dialogPopup!!.popupView
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

    override fun onClick(v: View) {
        Log.i("view", v.toString())
        when (v.id) {
            R.id.scan -> {      // 扫描
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@WlrkActivity, "请选择入库单", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN, ScanStatus.FINISH -> {
                        dialogPopup = DialogPopup(this@WlrkActivity)
                        dialogPopup!!.setScan()     // 设置为扫描界面
                        dialogPopup!!.showPopupWindow()
                    }
                }
            }
            R.id.rkd -> {   // 入库单
                val intent = Intent(this@WlrkActivity, WlrkdActivity::class.java)
                startActivityForResult(intent, 0)
            }
            R.id.rk -> {    // 入库
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@WlrkActivity, "请选择入库单", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
                        val isNotFinished = jhrk.jhmx.any { it.mx_num == 0 }
                        if (isNotFinished) {
                            Toast.makeText(this@WlrkActivity, "有物料未扫描", Toast.LENGTH_SHORT).show()
                        } else {
                            status = ScanStatus.FINISH
                            DialogUtil.showDialog(this, null, "物料已全部扫描,是否入库?",
                                    null,
                                    DialogInterface.OnClickListener { _, _ ->
                                        jhrk.oldJhmx = jhrk.jhmx
                                        LoadingDialog.show(this@WlrkActivity)
                                        val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(jhrk))
                                        RetrofitManager.instance.updateJh(requestBody)
                                                .enqueue(object : BaseCallback<List<Jhmx>>(context = this) {
                                                    override fun successInfo(info: String) {
                                                        Toast.makeText(this@WlrkActivity, "完成入库", Toast.LENGTH_SHORT).show()
                                                        clearData()
                                                    }
                                                    override fun failureData(data: List<Jhmx>) {
                                                        Log.i("result", data.toString())
                                                        Toast.makeText(this@WlrkActivity, "有物料超出可入库数量", Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                    }
                            )
                        }
                    }
                    ScanStatus.FINISH -> {
                        DialogUtil.showDialog(this, null, "物料已全部扫描,是否入库?",
                                null,
                                DialogInterface.OnClickListener { _, _ ->
                                    jhrk.oldJhmx = jhrk.jhmx
                                    LoadingDialog.show(this@WlrkActivity)
                                    val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(jhrk))
                                    RetrofitManager.instance.updateJh(requestBody)
                                            .enqueue(object : BaseCallback<List<Jhmx>>(context = this) {
                                                override fun successInfo(info: String) {
                                                    Toast.makeText(this@WlrkActivity, "完成入库", Toast.LENGTH_SHORT).show()
                                                    clearData()
                                                }
                                                override fun failureData(data: List<Jhmx>) {
                                                    Toast.makeText(this@WlrkActivity, "有物料超出可入库数量", Toast.LENGTH_SHORT).show()
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
        editText_no.setText("")
        editText_date.setText("")
        editText_ghdw.setText("")
        editText_jsr.setText("")
        editText_ck.setText("")
        editText_beizhu.setText("")
        jhrk.jhmx.clear()
        wlAdapter.notifyDataSetChanged()
    }
}
