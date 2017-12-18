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
import net.tiaozhua.wms.utils.RetrofitManager
import net.tiaozhua.wms.utils.ScanStatus
import okhttp3.MediaType
import okhttp3.RequestBody

class WlckActivity : BaseActivity(R.layout.activity_wlck) {
    private var dialogPopup: DialogPopup? = null
    private lateinit var mVibrator: Vibrator
    private lateinit var mScanManager: ScanManager
    private val soundpool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100)
    private val soundid by lazy { soundpool.load("/etc/Scan_new.ogg", 1) }
    private lateinit var barcodeStr: String
    private var status = ScanStatus.EMPTY
    internal lateinit var ckd: Ckd
    internal lateinit var ckdmx: Ckdmx
    internal var material: Material? = null
    internal lateinit var wlAdapter: BaseAdapter
    private var receiverTag: Boolean = false

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            soundpool.play(soundid, 1f, 1f, 0, 0, 1f)
            mVibrator.vibrate(100)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            when (status) {
                ScanStatus.SCAN, ScanStatus.FINISH -> {
                    if (dialogPopup == null) {
                        dialogPopup = DialogPopup(this@WlckActivity)
                        dialogPopup!!.setScan()
                        dialogPopup!!.showPopupWindow()
                    }
                    if (!dialogPopup!!.isShowing) {
                        for (item in ckd.ckdmx) {
                            if (item.ma_id == material?.ma_id) {
                                if (item.ck_num > 0) {
                                    ckdmx = item
                                    dialogPopup!!.setUpdate()
                                } else {
                                    dialogPopup!!.setScan()
                                }
                                break
                            }
                        }
                        dialogPopup!!.showPopupWindow()
                    } else {
                        for (item in ckd.ckdmx) {
                            if (item.ma_id == material?.ma_id) {
                                if (item.ck_num > 0) {
                                    ckdmx = item
                                    dialogPopup!!.setUpdate()
                                }
                                break
                            }
                        }
                    }
                }
                else -> Toast.makeText(this@WlckActivity, "请选择领料单", Toast.LENGTH_SHORT).show()
            }
            if (dialogPopup != null && dialogPopup!!.isShowing) {
                val view = dialogPopup!!.popupView
                view.editText_tm.setText(barcodeStr)
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
        toolbarTitle.text = "物料出库"

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
            Activity.RESULT_OK -> {     // 选择了领料单
                ckd = intent!!.getSerializableExtra("ckd") as Ckd
                RetrofitManager.instance.ckdmxList(ckd.ckd_id, ckd.ck_id)
                        .enqueue(object : BaseCallback<List<Ckdmx>>(context = this) {
                            override fun successData(data: List<Ckdmx>) {
                                ckd.ckdmx = data.toMutableList()
                                editText_no.setText(ckd.ckd_no)
                                editText_date.setText(ckd.ckd_ldrq)
                                editText_llr.setText(ckd.llr_name)
                                editText_bm.setText(ckd.bm_name)
                                editText_ck.setText(ckd.ck_name)
                                editText_beizhu.setText(ckd.remark)
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
//                                                if (jhrk.delJhmx == null) {
//                                                    jhrk.delJhmx = mutableListOf()
//                                                }
//                                                val obj = jhmxList[position]
//                                                jhrk.delJhmx!!.add(obj)
                                                ckd.ckdmx.removeAt(position)
                                                notifyDataSetChanged()
                                            })
                                            holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                                                if (ckd.ckdmx[position].ck_num > 0) {
                                                    if (dialogPopup == null) {
                                                        dialogPopup = DialogPopup(this@WlckActivity)
                                                    }
                                                    ckdmx = ckd.ckdmx[position]
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

    fun scanClick(view: View) {
        when (status) {
            ScanStatus.EMPTY -> Toast.makeText(this@WlckActivity, "请选择领料单", Toast.LENGTH_SHORT).show()
            ScanStatus.SCAN, ScanStatus.FINISH -> {
                dialogPopup = DialogPopup(this@WlckActivity)
                dialogPopup!!.setScan()     // 设置为扫描界面
                dialogPopup!!.showPopupWindow()
            }
        }
    }

    fun lldClick(view: View) {
        val intent = Intent(this@WlckActivity, WlckdActivity::class.java)
        startActivityForResult(intent, 0)
    }

    fun ckClick(view: View) = when (status) {
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
                            val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(ckd))
                            RetrofitManager.instance.updateJh(requestBody)
                                    .enqueue(object : BaseCallback<List<Jhmx>>(context = this) {
                                        override fun successInfo(info: String) {
                                            if (info == "1") {
                                                Toast.makeText(this@WlckActivity, "完成出库", Toast.LENGTH_SHORT).show()
                                                clearData()
                                            }
                                        }
                                        override fun failureData(data: List<Jhmx>) {
                                            Log.i("result", data.toString())
                                            Toast.makeText(this@WlckActivity, "有物料超出可领数量", Toast.LENGTH_SHORT).show()
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
                        val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(ckd))
                        RetrofitManager.instance.updateJh(requestBody)
                                .enqueue(object : BaseCallback<List<Jhmx>>(context = this) {
                                    override fun successInfo(info: String) {
                                        if (info == "1") {
                                            Toast.makeText(this@WlckActivity, "完成出库", Toast.LENGTH_SHORT).show()
                                            clearData()
                                        }
                                    }
                                    override fun failureData(data: List<Jhmx>) {
                                        Log.i("result", data.toString())
                                        Toast.makeText(this@WlckActivity, "有物料超出可出库数量", Toast.LENGTH_SHORT).show()
                                    }
                                })
                    }
            )
        }
    }

    /***
     * 清除数据,重置表单
     */
    fun clearData() {
        status = ScanStatus.EMPTY
        editText_no.setText("")
        editText_date.setText("")
        editText_llr.setText("")
        editText_bm.setText("")
        editText_ck.setText("")
        editText_beizhu.setText("")
        ckd.ckdmx.clear()
        listView_wl.adapter = wlAdapter
        wlAdapter.notifyDataSetChanged()
    }
}
