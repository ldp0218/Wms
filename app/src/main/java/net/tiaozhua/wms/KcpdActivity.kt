package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.content.*
import android.device.ScanManager
import android.os.Bundle
import android.view.View
import android.widget.BaseAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_kcpd.*
import kotlinx.android.synthetic.main.popup_scan_material.view.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.*
import java.text.SimpleDateFormat
import java.util.*

class KcpdActivity : BaseActivity(R.layout.activity_kcpd), View.OnClickListener {

    internal var ckId: Int = -1
    private var wlPopup: WlPopup? = null
    private lateinit var mScanManager: ScanManager
    private lateinit var barcodeStr: String
    internal var receiverTag: Boolean = false
    internal lateinit var pdmxList: MutableList<Pdmx>
    internal lateinit var pdmx: Pdmx
    internal lateinit var pdAdapter: BaseAdapter
    private var ckListPopup: CkListPopup? = null

    internal val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            (application as App).playAndVibrate(this@KcpdActivity)
            if (ckId == -1) {
                Toast.makeText(this@KcpdActivity, "请选择仓库", Toast.LENGTH_SHORT).show()
                return
            }
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            if (wlPopup == null) {
                wlPopup = WlPopup(this@KcpdActivity)
                wlPopup!!.setScan()
                wlPopup!!.showPopupWindow()
            }
            if (!wlPopup!!.isShowing) {
                var flag = true
                for (item in pdmxList) {
                    if (item.hao == barcodeStr) {
                        if (item.pd_num > 0) {
                            pdmx = item
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
                for (item in pdmxList) {
                    if (item.hao == barcodeStr) {
                        if (item.pd_num > 0) {
                            pdmx = item
                            wlPopup!!.setUpdate()
                        }
                        break
                    }
                }
            }
            if (wlPopup != null && wlPopup!!.isShowing) {
                val view = wlPopup!!.popupView
                view.editText_tm.setText(barcodeStr)
                if (receiverTag) {
                    receiverTag = false
                    unregisterReceiver(this)
                }
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
                                        pdmx.name = material.ma_name
                                        pdmx.kind = material.ma_kind_name
                                        pdmx.spec = material.ma_spec ?: ""
                                        pdmx.hw = material.kc_hw_name
                                        pdmx.comment = material.comment ?: ""
                                        val popupView = wlPopup!!.popupView
                                        popupView.editText_tm.setText(pdmx.hao)
                                        popupView.textView_name.text = pdmx.name
                                        popupView.textView_kcnum.text = pdmx.kc_num.toString()
                                        popupView.textView_kind.text = pdmx.kind
                                        popupView.textView_hw.text = pdmx.hw
                                        popupView.editText_remark.setText(pdmx.comment)
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
        toolbarTitle.text = "物料盘点"
        scan.setOnClickListener(this)
        finish.setOnClickListener(this)
        button_selectCk.setOnClickListener(this)

        scrollView.smoothScrollTo(0, 0)

        val finish = intent.getIntExtra("finish", -1)
        if (finish == 1) {
            imageView_line.visibility = View.GONE
            layout_menu.visibility = View.GONE
        }
        ckId = intent.getIntExtra("ckId", -1)
        pdmx = Pdmx(0, null, null, 0.0, 0.0, null, ckId,
                "", "", "", "", "", "", "","", "","",0)     // 初始化pdmx

        textView_pdck.text = intent.getStringExtra("ckName")
        val pdDate = if (intent.hasExtra("pdDate")) {
            intent.getStringExtra("pdDate")
        } else SimpleDateFormat("yyyy-MM-dd").format(Date())
        textView_date.text = pdDate
        val pdId = intent.getIntExtra("pdId", -1)
        pdmx.pd_id = if (pdId == -1) null else pdId
        if (pdmx.pd_id != null) {    // 盘点中(点击盘点详情)
            LoadingDialog.show(this)
            RetrofitManager.instance.pdmxList(0, ckId, pdmx.pd_id!!)
                    .enqueue(object : BaseCallback<ResponseList<Pdmx>>(this@KcpdActivity) {
                        override fun successData(data: ResponseList<Pdmx>) {
                            pdmxList = data.items.toMutableList()
                            pdAdapter = object : CommonAdapter<Pdmx>(pdmxList, R.layout.listview_kcpd_item) {
                                override fun convert(holder: ViewHolder, t: Pdmx, position: Int) {
                                    holder.setText(R.id.textView_no, t.hao)
                                    holder.setText(R.id.textView_kcnum, t.kc_num.toString())
                                    holder.setText(R.id.textView_pdnum, t.pd_num.toString())
                                    holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                                        if (wlPopup == null) {
                                            wlPopup = WlPopup(this@KcpdActivity)
                                        }
                                        pdmx = t
                                        pdmx.type = 0
                                        wlPopup!!.setUpdate()     // 设置为修改界面
                                        wlPopup!!.showPopupWindow()
                                    })
                                }
                            }
                            listView_kcpd.adapter = pdAdapter
                        }
                    })
        } else {    // 新盘点
            pdmxList = mutableListOf()
            pdAdapter = object : CommonAdapter<Pdmx>(pdmxList, R.layout.listview_kcpd_item) {
                override fun convert(holder: ViewHolder, t: Pdmx, position: Int) {
                    holder.setText(R.id.textView_no, t.hao)
                    holder.setText(R.id.textView_kcnum, t.kc_num.toString())
                    holder.setText(R.id.textView_pdnum, t.pd_num.toString())
                    holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
                        if (wlPopup == null) {
                            wlPopup = WlPopup(this@KcpdActivity)
                        }
                        pdmx = t
                        pdmx.type = 0
                        wlPopup!!.setUpdate()     // 设置为修改界面
                        wlPopup!!.showPopupWindow()
                    })
                }
            }
            listView_kcpd.adapter = pdAdapter
        }
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.scan -> {  // 扫描
                if (ckId == -1) {
                    Toast.makeText(this, "请选择仓库", Toast.LENGTH_SHORT).show()
                    return
                }
                wlPopup = WlPopup(this@KcpdActivity)
                wlPopup!!.setScan()     // 设置为扫描界面
                wlPopup!!.showPopupWindow()
            }
            R.id.finish -> {    // 完成盘点
                if (ckId == -1) {
                    Toast.makeText(this, "请选择仓库", Toast.LENGTH_SHORT).show()
                    return
                }
                if (pdmxList.size == 0) {
                    Toast.makeText(this, "请先盘点", Toast.LENGTH_SHORT).show()
                } else {
                    DialogUtil.showDialog(this, null, "系统将库存数量和盘点数量做比较, 若两者存在差异，则自动做盘盈盘亏处理。",
                            null,
                            DialogInterface.OnClickListener { _, _ ->
                                LoadingDialog.show(this)
                                RetrofitManager.instance.finishPd(pdmx.pd_id!!).enqueue(object : BaseCallback<String>(this) {
                                    override fun successInfo(info: String) {
                                        val page = WlpdActivity.self.page
                                        RetrofitManager.instance.pdList(0, 1, page * 10)
                                                .enqueue(object : BaseCallback<ResponseList<Pd>>(this@KcpdActivity) {
                                                    override fun successData(data: ResponseList<Pd>) {
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
            R.id.button_selectCk -> {
                ckListPopup = ckListPopup ?: CkListPopup(this)
                ckListPopup!!.getCkList()
                ckListPopup!!.showPopupWindow()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (resultCode) {
            1 -> {      // 选择了物料
                val material = intent?.getParcelableExtra("material") as Material
                pdmx.iid = material.ma_id
                pdmx.kc_num = material.kc_num
                pdmx.hao = material.ma_code
                pdmx.txm = material.ma_txm
                pdmx.name = material.ma_name
                pdmx.kind = material.ma_kind_name
                pdmx.spec = material.ma_spec ?: ""
                pdmx.hw = material.kc_hw_name
                pdmx.comment = material.comment ?: ""
                val view = wlPopup!!.popupView
                view.editText_tm.setText(material.ma_txm)
                view.textView_name.text = material.ma_name
                view.textView_kcnum.text = material.kc_num.toString()
                view.textView_kind.text = material.ma_kind_name
                view.textView_hw.text = material.kc_hw_name
                view.editText_remark.setText(material.comment)
            }
        }
    }
}
