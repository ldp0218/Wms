package net.tiaozhua.wms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_lld_info.*
import net.tiaozhua.wms.adapter.LldInfoAdapter
import net.tiaozhua.wms.bean.Material
import net.tiaozhua.wms.bean.SCAN_ACTION
import net.tiaozhua.wms.bean.Scdmx
import net.tiaozhua.wms.bean.Scdpl
import net.tiaozhua.wms.utils.App
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager


class LldInfoActivity : BaseActivity(R.layout.activity_lld_info), View.OnClickListener {
    internal lateinit var scdmx: Scdmx
    private lateinit var mScanManager: ScanManager
    private lateinit var barcodeStr: String
    private var receiverTag: Boolean = false
    private lateinit var adapter: LldInfoAdapter
    private var ckId = 0
    private var gzId = 0
    private var overUsePopup: OverUsePopup? = null
    internal lateinit var overList: List<Scdpl>

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            (application as App).playAndVibrate(this@LldInfoActivity)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            LoadingDialog.show(this@LldInfoActivity)
            RetrofitManager.instance.materialInfo(barcodeStr, ckId)
                    .enqueue(object : BaseCallback<Material>(this@LldInfoActivity) {
                        override fun successData(data: Material) {
                            if (scdmx.plList.find { it.ma_id == data.ma_id } == null) {
                                scdmx.plList.add(Scdpl(null, data.ma_id, scdmx.scdmx_id, data.kc_num, 0.0, data.kc_hw_name,
                                        0.0, 0.0, null, data.ma_name, data.ma_code,
                                        data.ma_spec, data.ma_kind_name, data.ma_unit, gzId, 1, scdmx.scd_no, "", ""))
                                adapter.notifyDataSetChanged()
                            } else {
                                Toast.makeText(this@LldInfoActivity, "已存在该物料", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun successInfo(info: String) {
                            Toast.makeText(this@LldInfoActivity, info, Toast.LENGTH_SHORT).show()
                        }
                    })
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "流程卡物料明细"

        button_ok.setOnClickListener(this)
        button_cancel.setOnClickListener(this)

        scdmx = intent.getParcelableExtra("scdmx")
        val app = (application as App)
        ckId = app.user?.ck_id!!
        gzId = app.gzId
        editText_scdno.setText(scdmx.scd_no)
        scdmx.plList.forEach { it.scd_no = scdmx.scd_no }
        adapter = LldInfoAdapter(scdmx.plList, this@LldInfoActivity)
        listView_lld.adapter = adapter
//        LoadingDialog.show(this@LldInfoActivity)
//        RetrofitManager.instance.wlckd().enqueue(object : BaseCallback<ResponseList<Ckd>>(context = this@LldInfoActivity) {
//            override fun successData(data: ResponseList<Ckd>) {
//                val items = data.items
//                listView_dj.emptyView = empty
//                listView_dj.adapter = WlckdAdapter(items, this@LldInfoActivity)
//                listView_dj.setOnItemClickListener { _, _, i, _ ->
//                    DialogUtil.showDialog(this@LldInfoActivity, null, "是否选择该领料单?",
//                            null,
//                            DialogInterface.OnClickListener { _, _ ->
//                                val intent = Intent()
//                                intent.putExtra("ckd", items[i])
//                                setResult(Activity.RESULT_OK, intent) // 放入回传的值,并添加一个Code,方便区分返回的数据
//                                finish()
//                            }
//                    )
//                }
//            }
//        })
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

    override fun onPause() {
        super.onPause()
        if (receiverTag) {   //判断广播是否注册
            receiverTag = false
            unregisterReceiver(mScanReceiver)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (receiverTag) {   //判断广播是否注册
            receiverTag = false
            unregisterReceiver(mScanReceiver)
        }
        if (overUsePopup != null && overUsePopup!!.isShowing) {
            overUsePopup!!.dismiss()
            overUsePopup = null
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button_ok -> {
//                lateinit var layout: RelativeLayout
//                lateinit var editText: EditText
//                lateinit var cb: CheckBox
//                for (i in 0 until listView_lld.adapter.count) {
//                    layout = listView_lld.getChildAt(i) as RelativeLayout
//                    editText = layout.findViewById(R.id.editText_cknum)
//                    cb = layout.findViewById(R.id.checkBox)
//                    scdmx.plList[i].num = editText.text.toString().toDouble()
//                    scdmx.plList[i].checked = cb.isChecked
//                }
                if ((scdmx.plList.count {it.checked}) == 0) {
                    Toast.makeText(this, "请选择物料", Toast.LENGTH_SHORT).show()
                    return
                }
                //已勾选且超领没填说明的物料
                overList = scdmx.plList.filter { it.checked
                        && it.num > 0.0 && it.num > it.mx_num - it.mx_wcnum
                        && (it.mx_remark == null || it.mx_remark!!.trim() == "")}
                if (overList.isNotEmpty()) {
                    if (overUsePopup == null) {
                        overUsePopup = OverUsePopup(this@LldInfoActivity)
                    } else {
                        overUsePopup!!.update(overList)
                    }
                    overUsePopup!!.showPopupWindow()
                } else {
                    val intent = Intent()
                    intent.putExtra("scdmx", scdmx)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            R.id.button_cancel -> {
                finish()
            }
        }
    }
}