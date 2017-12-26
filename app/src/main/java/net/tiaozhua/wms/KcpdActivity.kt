package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.content.*
import android.device.ScanManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.BaseAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_kcpd.*
import kotlinx.android.synthetic.main.activity_pd.*
import kotlinx.android.synthetic.main.popup_scan_material.view.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import java.text.SimpleDateFormat
import java.util.*

class KcpdActivity : BaseActivity(R.layout.activity_kcpd), View.OnClickListener {

    private val type by lazy { intent.getIntExtra("type", 0) }  // 0代表物料；1代表成品
    private val pdId by lazy { intent.getIntExtra("pdId", -1) }
    internal var ckId: Int = 0
    private var dialogPopup: DialogPopup? = null
    private lateinit var mVibrator: Vibrator
    private lateinit var mScanManager: ScanManager
    private val soundpool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100)
    private val soundid by lazy { soundpool.load("/etc/Scan_new.ogg", 1) }
    private lateinit var barcodeStr: String
    private var receiverTag: Boolean = false
    internal lateinit var pdmxList: MutableList<Pdmx>
    internal lateinit var pdmx: Pdmx
    internal lateinit var wlAdapter: BaseAdapter

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            soundpool.play(soundid, 1f, 1f, 0, 0, 1f)
            mVibrator.vibrate(100)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))


            if (dialogPopup == null) {
                dialogPopup = DialogPopup(this@KcpdActivity)
                dialogPopup!!.setScan()
                dialogPopup!!.showPopupWindow()
            }
            if (!dialogPopup!!.isShowing) {
                var flag = true
                for (item in pdmxList) {
                    if (item.hao == barcodeStr) {
                        if (item.pd_num > 0) {
                            pdmx = item
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
                for (item in pdmxList) {
                    if (item.hao == barcodeStr) {
                        if (item.pd_num > 0) {
                            pdmx = item
                            dialogPopup!!.setUpdate()
                        }
                        break
                    }
                }
            }
            if (dialogPopup != null && dialogPopup!!.isShowing) {
                val view = dialogPopup!!.popupView
                view.editText_tm.setText(barcodeStr)
                LoadingDialog.show(this@KcpdActivity)
                RetrofitManager.instance.materialList(barcodeStr, ckId)
                        .enqueue(object : BaseCallback<ResponseList<Material>>(this@KcpdActivity) {
                            override fun successData(data: ResponseList<Material>) {
                                when {
                                    data.totalCount == 0 -> Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                                    data.totalCount > 1 -> {
                                        val activityIntent = Intent(this@KcpdActivity, MaterialSelectActivity::class.java)
                                        activityIntent.putExtra("code", barcodeStr)
                                        activityIntent.putExtra("ckId", ckId)
                                        intent.putExtra("data", data)
                                        startActivityForResult(activityIntent, 0)
                                    }
                                    else -> {
                                        val material = data.items[0]
                                        pdmx.iid = material.ma_id
                                        pdmx.kc_num = material.kc_num
                                        pdmx.hao = material.ma_code
                                        pdmx.ck_id = ckId
                                        pdmx.txm = material.ma_txm
                                        pdmx.name = material.ma_name
                                        pdmx.spec = material.ma_spec
                                        pdmx.model = material.ma_model
                                        pdmx.hw = material.kc_hw_name
                                        pdmx.comment = material.comment
                                        val popupView = dialogPopup!!.popupView
                                        popupView.editText_tm.setText(pdmx.txm)
                                        popupView.textView_no.text = pdmx.hao
                                        popupView.textView_name.text = pdmx.name
                                        popupView.textView_kcnum.text = pdmx.kc_num.toString()
                                        popupView.textView_spec.text = pdmx.spec
                                        popupView.textView_model.text = pdmx.model
                                        popupView.textView_hw.text = pdmx.hw
                                        popupView.textView_remark.text = pdmx.comment
                                        popupView.editText_num.requestFocus()
                                        popupView.editText_num.setSelection(popupView.editText_num.text.toString().trim().length)
                                        DialogUtil.showInputMethod(context, popupView.editText_num, true, 100)
                                        // 扫描后为pdmx设值
                                    }
                                }
                            }
                        })
            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = if (type == 0) "物料盘点" else "成品盘点"
        mVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        scan.setOnClickListener(this@KcpdActivity)
        finish.setOnClickListener(this@KcpdActivity)

        scrollView.smoothScrollTo(0, 0)
        if (!receiverTag) {     //在注册广播接受者的时候 判断是否已被注册,避免重复多次注册广播
            receiverTag = true
            registerReceiver(mScanReceiver, IntentFilter(SCAN_ACTION))
        }

        pdmx = Pdmx(0, null, null, 0, 0, null, 0, "", "", "", "", "", "", "")     // 初始化pdmx
        ckId = when {
            intent.hasExtra("ckId") -> intent.getIntExtra("ckId", -1)
            type == 0 -> 79
            else -> 83
        }
        val ckName = when {
            intent.hasExtra("ckName") -> intent.getStringExtra("ckName")
            type == 0 -> "物料盘点"
            else -> "成品盘点"
        }
        textView_ck.text = ckName
//        LoadingDialog.show(this@KcpdActivity)
//        RetrofitManager.instance.ckList()
//                .enqueue(object : BaseCallback<ResponseList<Ck>>(context = this) {
//                    override fun successData(data: ResponseList<Ck>) {
//                        ckId = data.items[0].ck_id
//                        textView_ck.text = data.items[0].ck_name
//                    }
//                })

        val pdDate = if (intent.hasExtra("pdDate")) {
            intent.getStringExtra("pdDate")
        } else SimpleDateFormat("yyyy-MM-dd").format(Date())
        textView_date.text = pdDate
        if (pdId > -1) {    // 盘点中(点击盘点详情)
            LoadingDialog.show(this)
            RetrofitManager.instance.pdmxList(type, ckId, pdId)
                    .enqueue(object : BaseCallback<ResponseList<Pdmx>>(this@KcpdActivity) {
                        override fun successData(data: ResponseList<Pdmx>) {
                            LoadingDialog.dismiss()
                            pdmxList = data.items.toMutableList()
                            wlAdapter = object : CommonAdapter<Pdmx>(pdmxList, R.layout.listview_kcpd_item) {
                                override fun convert(holder: ViewHolder, t: Pdmx, position: Int) {
                                    holder.setText(R.id.textView_no, t.hao)
                                    holder.setText(R.id.textView_kcnum, t.kc_num.toString())
                                    holder.setText(R.id.textView_pdnum, t.pd_num.toString())
                                    holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                                        if (dialogPopup == null) {
                                            dialogPopup = DialogPopup(this@KcpdActivity)
                                        }
                                        pdmx = pdmxList[position]
                                        dialogPopup!!.setUpdate()     // 设置为修改界面
                                        dialogPopup!!.showPopupWindow()
                                    })
                                }
                            }
                            listView_kcpd.adapter = wlAdapter
                        }
                    })
        } else {    // 新盘点
            pdmxList = mutableListOf()
            wlAdapter = object : CommonAdapter<Pdmx>(pdmxList, R.layout.listview_kcpd_item) {
                override fun convert(holder: ViewHolder, t: Pdmx, position: Int) {
                    holder.setText(R.id.textView_no, t.hao)
                    holder.setText(R.id.textView_kcnum, t.kc_num.toString())
                    holder.setText(R.id.textView_pdnum, t.pd_num.toString())
                    holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                        if (dialogPopup == null) {
                            dialogPopup = DialogPopup(this@KcpdActivity)
                        }
                        pdmx = pdmxList[position]
                        dialogPopup!!.setUpdate()     // 设置为修改界面
                        dialogPopup!!.showPopupWindow()
                    })
                }
            }
            listView_kcpd.adapter = wlAdapter
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.scan -> {  // 扫描
                if (type == 0) {    // 物料
                    dialogPopup = DialogPopup(this@KcpdActivity)
                    dialogPopup!!.setScan()     // 设置为扫描界面
                    dialogPopup!!.showPopupWindow()
                } else {    // 成品

                }
            }
            R.id.finish -> {    // 完成盘点
                if (pdmxList.size == 0) {
                    Toast.makeText(this, "请先盘点", Toast.LENGTH_SHORT).show()
                } else {
                    DialogUtil.showDialog(this, null, "系统将库存数量和盘点数量做比较, 若两者存在差异，则自动作盘盈盘亏处理。",
                            null,
                            DialogInterface.OnClickListener { _, _ ->
                                LoadingDialog.show(this)
                                RetrofitManager.instance.finishPd(pdId).enqueue(object : BaseCallback<String>(this) {
                                    override fun successInfo(info: String) {
                                        RetrofitManager.instance.pdList(type, 1, WlpdActivity.self.page * 10)
                                                .enqueue(object : BaseCallback<ResponseList<Pd>>(this@KcpdActivity) {
                                                    override fun successData(data: ResponseList<Pd>) {
                                                        LoadingDialog.dismiss()
                                                        WlpdActivity.self.pdList!!.clear()
                                                        WlpdActivity.self.pdList!!.addAll(data.items)
                                                        WlpdActivity.self.pdAdapter.notifyDataSetChanged()
                                                        Toast.makeText(this@KcpdActivity, "已完成", Toast.LENGTH_SHORT).show()
                                                        finish()
                                                    }
                                                })
                                    }
                                })
                            }
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        when (resultCode) {
            1 -> {      // 选择了物料
                val material = intent.getSerializableExtra("material") as Material
                pdmx.iid = material.ma_id
                pdmx.kc_num = material.kc_num
                pdmx.hao = material.ma_code
                pdmx.txm = material.ma_txm
                pdmx.name = material.ma_name
                pdmx.spec = material.ma_spec
                pdmx.model = material.ma_model
                pdmx.hw = material.kc_hw_name
                pdmx.comment = material.comment
                val view = dialogPopup!!.popupView
                view.editText_tm.setText(material.ma_txm)
                view.textView_no.text = material.ma_code
                view.textView_name.text = material.ma_name
                view.textView_kcnum.text = material.kc_num.toString()
                view.textView_spec.text = material.ma_spec
                view.textView_model.text = material.ma_model
                view.textView_hw.text = material.kc_hw_name
                view.textView_remark.text = material.comment
            }
        }
    }
}
