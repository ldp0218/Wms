package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.content.*
import android.device.ScanManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_cprk.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*

class CprkActivity : BaseActivity(R.layout.activity_cprk), View.OnClickListener {

    internal var status = ScanStatus.EMPTY
    private lateinit var mScanManager: ScanManager
    private lateinit var barcodeStr: String
    internal var receiverTag: Boolean = false
    internal lateinit var rkd: Rkd
    private lateinit var rkmxList: MutableList<Baozhuang>
    internal lateinit var cpAdapter: BaseAdapter
    private var productList: HashSet<String> = hashSetOf()
    internal lateinit var bz: Baozhuang
    internal var rkmx: Rkmx? = null
    internal var cpPopup: CpPopup? = null

    internal val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            (application as App).playAndVibrate(this@CprkActivity)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))
            val code = parseProductQRCode(context, barcodeStr, productList)
            if (code.xsd_bz_id == 0 && code.bz_id == 0 && code.check_num == 0) return

            if (receiverTag) {   //判断广播是否注册
                receiverTag = false
                unregisterReceiver(this)
            }
            LoadingDialog.show(this@CprkActivity)
            RetrofitManager.instance.getProductInfo(code.xsd_bz_id, code.bz_id)
                    .enqueue(object : BaseCallback<Baozhuang>(this@CprkActivity) {
                        override fun successData(data: Baozhuang) {
                            if (data.wrk_num == 0) {
                                Toast.makeText(context, "已完成入库", Toast.LENGTH_SHORT).show()
                                return
                            }
                            productList.add(barcodeStr)
                            bz = data
                            if (code.check_num == bz.wrk_num) {
                                var find = false
                                for (item in rkd.rkmx) {
                                    if (item.bz_id == bz.bz_id) {
                                        item.mx_num += code.check_num
                                        find = true
                                        break
                                    }
                                }
                                if (!find) {
                                    rkd.rkmx.add(Rkmx(bz.xsd_bz_id, code.check_num, bz.scd_no,
                                            bz.pro_name, bz.bz_id, bz.bz_code, bz.bz_hao,
                                            bz.pro_id, bz.kc_num, bz.wrk_num))
                                }
                                if (status == ScanStatus.EMPTY) {  // 如果还未开始扫描修改状态为扫描
                                    status = ScanStatus.SCAN
                                }
                                cpAdapter.notifyDataSetChanged()
                            } else {
                                cpPopup = cpPopup ?: CpPopup(this@CprkActivity)
                                rkmx = rkd.rkmx.find { it.bz_id == bz.bz_id }
                                if (rkmx != null) {
                                    cpPopup!!.setUpdate()
                                } else {
                                    bz.check_num = code.check_num
                                    cpPopup!!.setScan()
                                }
                                cpPopup!!.showPopupWindow()
                            }

//                            if (data.pro_type == 1) {   // 记录异形，保证整套入库
//                                var find = false
//                                for (item in rkmxList) {
//                                    if (item.scd_no == data.scd_no) {
//                                        item.check_num += code.check_num
//                                        find = true
//                                        break
//                                    }
//                                }
//                                if (!find) {
//                                    data.check_num = 1
//                                    rkmxList.add(data)
//                                }
//                                rkd.rkmx.add(Rkmx(data.xsd_bz_id, code.check_num, data.scd_no, data.pro_name, data.bz_id, data.bz_code, data.pro_id))
//                            } else {    // 常规记录重复件数
//                                var find = false
//                                for (item in rkd.rkmx) {
//                                    if (item.bz_id == data.bz_id) {
//                                        item.mx_num += code.check_num
//                                        find = true
//                                        break
//                                    }
//                                }
//                                if (!find) {
//                                    rkd.rkmx.add(Rkmx(data.xsd_bz_id, code.check_num, data.scd_no, data.pro_name, data.bz_id, data.bz_code, data.pro_id))
//                                }
//                            }
//                            cpAdapter.notifyDataSetChanged()
//                            productList.add(barcodeStr)
//                            if (status == ScanStatus.EMPTY) {  // 如果还未开始扫描修改状态为扫描
//                                status = ScanStatus.SCAN
//                            }
//                            Toast.makeText(context, "已扫描", Toast.LENGTH_SHORT).show()
                        }

                        override fun successInfo(info: String) {
                            Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                        }
                    })
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "成品入库"
        rkd = Rkd("", null, "", "", "", mutableListOf(), 1)
        rkmxList = mutableListOf()

        rkd.rk_ldrq = SimpleDateFormat("yyyy-MM-dd").format(Date())
        refreshNo()
//        editText_ck.setText(rkd.ck_name)
//        editText_date.setText(rkd.rk_ldrq)

//        RetrofitManager.instance.ckList()
//                .enqueue(object : BaseCallback<ResponseList<Ck>>(context = this) {
//                    override fun successData(data: ResponseList<Ck>) {
//                        rkd.ck_id = data.items[0].ck_id
//                        rkd.ck_name = data.items[0].ck_name
//                    }
//                })

        // 为按钮设置点击监听事件
        rk.setOnClickListener(this)

        scrollView_cprk.smoothScrollTo(0, 0)

        cpAdapter = object : CommonAdapter<Rkmx>(rkd.rkmx, R.layout.listview_cp_item) {
            override fun convert(holder: ViewHolder, t: Rkmx, position: Int) {
                holder.setText(R.id.textView_no, t.scd_no ?: "")
                holder.setText(R.id.textView_name, t.pro_name)
                holder.setText(R.id.textView_bzcode, t.bz_hao)
                holder.setText(R.id.textView_num, t.mx_num.toString())
            }
        }
        listView_cprk.adapter = cpAdapter
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
            R.id.rk -> {    // 入库
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@CprkActivity, "请扫描包装", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
//                        for (item in rkmxList) {
//                            if (item.check_num < item.pro_bz_num) {
//                                Toast.makeText(this@CprkActivity, "有包装未扫描", Toast.LENGTH_SHORT).show()
//                                return
//                            }
//                        }
                        DialogUtil.showDialog(this, null, "是否入库?",
                                null,
                                DialogInterface.OnClickListener { _, _ ->
                                    LoadingDialog.show(this)
                                    val json = Gson().toJson(rkd)
                                    val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                                    RetrofitManager.instance.insertRk(requestBody)
                                            .enqueue(object : BaseCallback<List<Bzmx>>(context = this) {
                                                override fun successInfo(info: String) {
                                                    Toast.makeText(this@CprkActivity, "完成入库", Toast.LENGTH_SHORT).show()
                                                    clearData()
                                                }
                                                override fun successData(data: List<Bzmx>) {
                                                    Toast.makeText(this@CprkActivity, "有包装超出可入库数量", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                }
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    private fun refreshNo() {
        RetrofitManager.instance.getRkNo().enqueue(object : BaseCallback<String>(this) {
            @SuppressLint("SimpleDateFormat")
            override fun successData(data: String) {
//                editText_no.setText(data)     // 查询入库单编号
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
        rkmxList.clear()
        rkd.rkmx.clear()
        cpAdapter.notifyDataSetChanged()
    }
}
