package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.content.*
import android.device.ScanManager
import android.os.Bundle
import android.view.View
import android.widget.BaseAdapter
import android.widget.Toast
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.android.synthetic.main.activity_cppd.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CppdmxActivity : BaseActivity(R.layout.activity_cppd), View.OnClickListener {

    private var ckId: Int = 1
    private lateinit var mScanManager: ScanManager
    private lateinit var barcodeStr: String
    private var receiverTag: Boolean = false
    internal lateinit var pdmxList: MutableList<Pdmx>
    internal lateinit var pdmx: Pdmx
    internal lateinit var pdAdapter: BaseAdapter
    private var productList: HashSet<String> = hashSetOf()

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            (application as App).playAndVibrate(this@CppdmxActivity)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))
            val code = parseProductQRCode(context, barcodeStr, productList)
            if (code.xsd_bz_id == 0 && code.bz_id == 0 && code.check_num == 0) return
            if (receiverTag) {   //判断广播是否注册
                receiverTag = false
                unregisterReceiver(this)
            }
            val that = this
            LoadingDialog.show(this@CppdmxActivity)
            RetrofitManager.instance.getProductInfo(code.xsd_bz_id)
                    .enqueue(object : BaseCallback<Baozhuang>(this@CppdmxActivity) {
                        override fun successData(data: Baozhuang) {
//                            if (data.pro_type == 0) {    // 常规记录重复件数
                                var find = false
                                for (item in pdmxList) {
                                    if (item.iid == data.bz_id) {
                                        item.pd_num += code.check_num
                                        pdmx = item
                                        find = true
                                        break
                                    }
                                }
                                if (find) {     // list中存在
                                    if (receiverTag) {   //判断广播是否注册
                                        receiverTag = false
                                        unregisterReceiver(that)
                                    }
                                    LoadingDialog.show(context)
                                    val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ObjectMapper().writeValueAsBytes(pdmx))
                                    RetrofitManager.instance.updatePdmx(requestBody).enqueue(object : Callback<PdResult> {
                                        override fun onResponse(call: Call<PdResult>?, response: Response<PdResult>?) {
                                            LoadingDialog.dismiss()
                                            if (response?.code() == 200) {
                                                val result = response.body()
                                                when (result?.code) {
                                                    0 -> {
                                                        Toast.makeText(context, result.info ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                                                    }
                                                    1 -> { // 正常
                                                        Toast.makeText(context, "已记录", Toast.LENGTH_SHORT).show()
                                                        for (item in pdmxList) {
                                                            if (item.iid == pdmx.iid) {
                                                                item.pd_id = pdmx.pd_id
                                                                item.id = pdmx.id
                                                                break
                                                            }
                                                        }
                                                        pdAdapter.notifyDataSetChanged()
                                                        pdmx = Pdmx(0, result.pd_id, null, 0.0, 0.0, null, 0, "", "",
                                                                "", "", "", "", "","","",0)
                                                    }
                                                    2 -> { // 登录超时
                                                        DialogUtil.showAlert(context, "请重新登录",
                                                                DialogInterface.OnClickListener { _, _ ->
                                                                    context.startActivity(Intent(context, LoginActivity::class.java))
                                                                }
                                                        )
                                                    }
                                                    3 -> { // 无权限
                                                        Toast.makeText(context, result.info, Toast.LENGTH_SHORT).show()
                                                    }
                                                    else -> Toast.makeText(context, result?.info, Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<PdResult>?, t: Throwable?) {
                                            LoadingDialog.dismiss()
                                            Toast.makeText(context, "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                } else {        // list中不存在
                                    pdmx.iid = data.bz_id
                                    pdmx.xsd_bz_id = data.xsd_bz_id
                                    pdmx.pd_num = code.check_num.toDouble()
//                                    pdmx.kc_num = data.kc_num
//                                    pdmx.bz_code = data.bz_code
//                                    pdmx.scd_no = data.scd_no
//                                    pdmx.hao = data.pro_model ?: ""
                                    pdmx.ck_id = ckId
                                    LoadingDialog.show(context)
                                    val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ObjectMapper().writeValueAsBytes(pdmx))
                                    RetrofitManager.instance.updatePdmx(requestBody).enqueue(object : Callback<PdResult> {
                                        override fun onResponse(call: Call<PdResult>?, response: Response<PdResult>?) {
                                            LoadingDialog.dismiss()
                                            if (response?.code() == 200) {
                                                val result = response.body()
                                                when (result?.code) {
                                                    0 -> {
                                                        Toast.makeText(context, result.info ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                                                    }
                                                    1 -> { // 正常
                                                        Toast.makeText(context, "已记录", Toast.LENGTH_SHORT).show()
                                                        pdmx.pd_id = result.pd_id
                                                        pdmx.id = result.id
                                                        pdmxList.add(pdmx)
                                                        pdAdapter.notifyDataSetChanged()
                                                        pdmx = Pdmx(0, result.pd_id, null, 0.0, 0.0, null, 0, "", "",
                                                                "", "", "", "", "","","",0)
                                                    }
                                                    2 -> { // 登录超时
                                                        DialogUtil.showAlert(context, "请重新登录",
                                                                DialogInterface.OnClickListener { _, _ ->
                                                                    context.startActivity(Intent(context, LoginActivity::class.java))
                                                                }
                                                        )
                                                    }
                                                    3 -> { // 无权限
                                                        Toast.makeText(context, result.info, Toast.LENGTH_SHORT).show()
                                                    }
                                                    else -> Toast.makeText(context, result?.info, Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<PdResult>?, t: Throwable?) {
                                            LoadingDialog.dismiss()
                                            Toast.makeText(context, "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
                            }
//                        }
                    })
        }

    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "成品盘点"
        finish.setOnClickListener(this)

        scrollView.smoothScrollTo(0, 0)

        val finish = intent.getIntExtra("finish", -1)
        if (finish == 1) {
            imageView_line.visibility = View.GONE
            layout_menu.visibility = View.GONE
        }
        pdmx = Pdmx(1, null, null, 0.0, 0.0, null, 0,
                "", "", "", "", "", "", "","","",0)     // 初始化pdmx
//        ckId = when {
//            intent.hasExtra("ckId") -> intent.getIntExtra("ckId", -1)
//        }
//        val ckName = when {
//            intent.hasExtra("ckName") -> intent.getStringExtra("ckName")
//        }

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
        val pdId = intent.getIntExtra("pdId", -1)
        pdmx.pd_id = if (pdId == -1) null else pdId
        if (pdmx.pd_id != null) {    // 盘点中(点击盘点详情)
            LoadingDialog.show(this)
            RetrofitManager.instance.pdmxList(1, ckId, pdmx.pd_id!!)
                    .enqueue(object : BaseCallback<ResponseList<Pdmx>>(this@CppdmxActivity) {
                        override fun successData(data: ResponseList<Pdmx>) {
                            pdmxList = data.items.toMutableList()
                            pdAdapter = object : CommonAdapter<Pdmx>(pdmxList, R.layout.listview_cppd_item) {
                                override fun convert(holder: ViewHolder, t: Pdmx, position: Int) {
                                    holder.setText(R.id.textView_no, t.hao)
                                    holder.setText(R.id.textView_bzcode, t.bz_code)
                                    holder.setText(R.id.textView_kcnum, t.kc_num.toString())
                                    holder.setText(R.id.textView_pdnum, t.pd_num.toString())
                                }
                            }
                            listView_kcpd.adapter = pdAdapter
                        }
                    })
        } else {    // 新盘点
            pdmxList = mutableListOf()
            pdAdapter = object : CommonAdapter<Pdmx>(pdmxList, R.layout.listview_cppd_item) {
                override fun convert(holder: ViewHolder, t: Pdmx, position: Int) {
                    holder.setText(R.id.textView_no, t.hao)
                    holder.setText(R.id.textView_bzcode, t.bz_code)
                    holder.setText(R.id.textView_kcnum, t.kc_num.toString())
                    holder.setText(R.id.textView_pdnum, t.pd_num.toString())
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
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.finish -> {    // 完成盘点
                if (pdmxList.size == 0) {
                    Toast.makeText(this, "请先盘点", Toast.LENGTH_SHORT).show()
                } else {
                    DialogUtil.showDialog(this, null, "系统将库存数量和盘点数量做比较, 若两者存在差异，则自动作盘盈盘亏处理。",
                            null,
                            DialogInterface.OnClickListener { _, _ ->
                                LoadingDialog.show(this)
                                RetrofitManager.instance.finishPd(pdmx.pd_id!!).enqueue(object : BaseCallback<String>(this) {
                                    override fun successInfo(info: String) {
                                        val page = CppdActivity.self.page
                                        RetrofitManager.instance.pdList(1, 1, page * 10)
                                                .enqueue(object : BaseCallback<ResponseList<Pd>>(this@CppdmxActivity) {
                                                    override fun successData(data: ResponseList<Pd>) {
                                                        CppdActivity.self.pdList!!.clear()
                                                        CppdActivity.self.pdList!!.addAll(data.items)
                                                        CppdActivity.self.pdAdapter.notifyDataSetChanged()
                                                        Toast.makeText(this@CppdmxActivity, "已完成", Toast.LENGTH_SHORT).show()
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
}
