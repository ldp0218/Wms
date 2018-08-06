package net.tiaozhua.wms

import android.content.*
import android.device.ScanManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_cpck.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.*
import okhttp3.MediaType
import okhttp3.RequestBody

@Suppress("DEPRECATION")
class CpbhActivity : BaseActivity(R.layout.activity_cpck), View.OnClickListener {
    private lateinit var mScanManager: ScanManager
    private lateinit var barcodeStr: String
    internal var receiverTag: Boolean = false
    private var status = ScanStatus.EMPTY
    internal var xsList = mutableListOf<Xs>()   // 汇款通知单List
    internal var xsmxList = mutableListOf<Xsmx>()    //销售明细List，用于显示
    internal lateinit var cpAdapter: BaseAdapter
    internal var productList: HashMap<String, Xsmx> = hashMapOf()
    internal var nonScanningList: HashMap<Int, Bz> = hashMapOf()
    private var nonScanningPopup: NonScanningPopup? = null
    private var hktzdPopup: HktzdPopup? = null

    internal val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            (application as App).playAndVibrate(this@CpbhActivity)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            if (barcodeStr.startsWith(DjFlag.HKTZD.value)) {  //  以此标志判断扫描的是单据还是产品
                val id = try {      // 截取后面的id
                    barcodeStr.substring(2).toInt()
                } catch (e: NumberFormatException) { 0 }
                if (id == 0) {
                    Toast.makeText(this@CpbhActivity, "未识别的二维码", Toast.LENGTH_SHORT).show()
                    return
                }
//                if (productList.size > 0) {
//                    var flag = false
//                    DialogUtil.showDialog(this@CpckActivity, null, "数据未提交，是否重新扫描?",
//                            DialogInterface.OnClickListener {_, _ ->
//                                flag = true
//                            },
//                            DialogInterface.OnClickListener { _, _ ->
//                                LoadingDialog.show(this@CpckActivity)
//                                val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(data))
//                                RetrofitManager.instance.cpck(requestBody)
//                                        .enqueue(object : BaseCallback<String>(context = this@CpckActivity) {
//                                            override fun successInfo(info: String) {
//                                                flag = false
//                                            }
//                                        })
//                            }
//                    )
//                    if (flag) {
//                        return
//                    }
//                }
                //判断汇款通知单是否已扫描
                when {
                    xsList.find { it.xs_id == id } == null -> {
                        if (receiverTag) {   //判断广播是否注册
                            receiverTag = false
                            unregisterReceiver(this)
                        }
                        LoadingDialog.show(this@CpbhActivity)
                        RetrofitManager.instance.productStock(id)
                                .enqueue(object : BaseCallback<Xs>(context = this@CpbhActivity) {
                                    override fun successData(data: Xs) {
                                        when (data.pdacode) {
                                            -1 -> {
                                                Toast.makeText(this@CpbhActivity, "此单不存在", Toast.LENGTH_SHORT).show()
                                                return
                                            }
                                            0 -> {
                                                Toast.makeText(this@CpbhActivity, "草稿单无法使用", Toast.LENGTH_SHORT).show()
                                                return
                                            }
                                            2 -> {
                                                Toast.makeText(this@CpbhActivity, "此单已备货", Toast.LENGTH_SHORT).show()
                                                return
                                            }
                                            3 -> {
                                                Toast.makeText(this@CpbhActivity, "此单已出库", Toast.LENGTH_SHORT).show()
                                                return
                                            }
                                        }
                                        xsList.add(data)
                                        if (hktzdPopup != null) {
                                            hktzdPopup!!.update(xsList)
                                        }
                                        for (item in data.xsmx) {
                                            if (item.yx == 0) {
                                                val x = xsmxList.find { xsmx ->
                                                    xsmx.yx == 0 && xsmx.pro_id == item.pro_id    //找出同一套常规产品
                                                }
                                                if (x != null) {
                                                    for (bao in x.bzList) {
                                                        for (bz in item.bzList) {
                                                            if (bao.bz_id == bz.bz_id) {
                                                                bao.total += bz.total
                                                                break
                                                            }
                                                        }
                                                    }
                                                    x.package_num = x.package_num?.plus(item.package_num!!)
                                                    continue
                                                }
                                            }
                                            xsmxList.add(item)
                                        }

                                        //productList.clear()
                                        for (item in xsmxList) {
                                            item.xsdbzList = mutableListOf()
                                        }
                                        if (xsmxList.size > 0) {
                                            //记录未扫描的包装
                                            xsmxList.forEach {
                                                if (it.bzList.isNotEmpty()) {
                                                    it.bzList.forEach { bz ->
                                                        //扫描完的包装不再添加
                                                        if (bz.total - bz.num > 0) {
                                                            bz.scd_no = it.scd_no
                                                            nonScanningList[bz.bz_id] = bz
                                                        }
                                                    }
                                                }
                                            }
                                            status = ScanStatus.SCAN
                                            cpAdapter = object : CommonAdapter<Xsmx>(xsmxList, R.layout.listview_cpck_item) {
                                                override fun convert(holder: ViewHolder, t: Xsmx, position: Int) {
                                                    holder.setText(R.id.textView_model, t.pro_model ?: "")
                                                    holder.setText(R.id.textView_bz, t.scd_no ?: "")
                                                    holder.setText(R.id.textView_num, (t.package_num ?: "").toString())
                                                    holder.setText(R.id.textView_scan, t.check_num.toString())
                                                }
                                            }
                                            listView_cpck.adapter = cpAdapter
                                        }
                                    }
                                })
                    }
                    else -> Toast.makeText(this@CpbhActivity, "此单已扫描", Toast.LENGTH_SHORT).show()
                }
            } else {        // 扫描包装
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@CpbhActivity, "请扫描汇款通知单", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
                        val code = parseProductQRCode(context, barcodeStr, productList.keys.toHashSet())
                        if (code.xsd_bz_id == 0 && code.bz_id == 0 && code.check_num == 0) return

                        var findFlag: Xsmx? = null  //是否找到
                        var overFlag = false        //超量标志
                        for (item in xsmxList) {
                            for (it in item.bzList) {
                                if (it.bz_id == code.bz_id) {
                                    findFlag = item
                                    val xsdbz = item.xsdbzList.find { it.xsd_bz_id == code.xsd_bz_id }
                                    if (it.num < it.total) {
                                        it.num += code.check_num
                                        item.check_num += code.check_num
                                        if (xsdbz == null) {
                                            item.xsdbzList.add(code)
                                        } else {
                                            xsdbz.check_num += code.check_num
                                        }
                                    } else {
                                        overFlag = true
                                    }
                                    break
                                }
                            }
                        }
                        if (nonScanningList.containsKey(code.bz_id)) {
                            val bz = nonScanningList[code.bz_id]
                            if (bz!!.total - bz.num == 0) {
                                nonScanningList.remove(code.bz_id)
                            }
                            if (nonScanningPopup != null) {
                                nonScanningPopup!!.update(nonScanningList)
                            }
                        }

                        cpAdapter.notifyDataSetChanged()
                        if (findFlag != null) {     // 处理完毕
                            if (overFlag) {
                                Toast.makeText(context, "超过此类包装的可出库数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            productList[barcodeStr] = findFlag     // 记录扫描的二维码
                            Toast.makeText(context, "已扫描", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "此包装不在汇款通知单内，请核实", Toast.LENGTH_SHORT).show()
                        }

                    }
                    ScanStatus.FINISH -> {
                        stockUp()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "成品备货"
        btn_chuku.text = "完成"

        if (xsList.size > 0) {
            xsList.clear()
        }
        if (xsmxList.size > 0) {
            xsmxList.clear()
        }

        // 为按钮设置点击监听事件
        btn_hktzd.setOnClickListener(this)
        btn_chuku.setOnClickListener(this)

        scrollView_cpck.smoothScrollTo(0, 0)
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
        if (nonScanningPopup != null) {
            nonScanningPopup!!.dismiss()
            nonScanningPopup = null
        }
        if (hktzdPopup != null) {
            hktzdPopup!!.dismiss()
            hktzdPopup = null
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_hktzd -> {   // 汇款通知单
                if (xsList.size == 0) {
                    status = ScanStatus.EMPTY
                    Toast.makeText(this@CpbhActivity, "请扫描汇款通知单", Toast.LENGTH_SHORT).show()
                    return
                }
                if (hktzdPopup == null) {
                    hktzdPopup = HktzdPopup(this@CpbhActivity)
                } else {
                    hktzdPopup!!.update(xsList)
                }
                hktzdPopup!!.showPopupWindow()
            }
            R.id.btn_chuku -> {    // 备货完成
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@CpbhActivity, "请扫描汇款通知单", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
//                        val count = xsList
//                                .map { xs -> xs.xsmx.any { if (it.package_num != null) it.check_num < it.package_num else false } }
//                                .count { it }
                        if (xsList.size == 0) {
                            status = ScanStatus.EMPTY
                            Toast.makeText(this@CpbhActivity, "请扫描汇款通知单", Toast.LENGTH_SHORT).show()
                            return
                        }
                        val isNotFinished = xsmxList.any { if (it.package_num != null) it.check_num < it.package_num!! else false }
                        if (isNotFinished) {
                            if (nonScanningPopup == null) {
                                nonScanningPopup = NonScanningPopup(this@CpbhActivity)
                            } else {
                                nonScanningPopup!!.update(nonScanningList)
                            }
                            nonScanningPopup!!.showPopupWindow()
                        } else {
                            status = ScanStatus.FINISH
                            stockUp()
                        }
                    }
                    ScanStatus.FINISH -> {
                        stockUp()
                    }
                }
            }
        }
    }

    private fun stockUp() {
        LoadingDialog.show(this@CpbhActivity)
        val json = Gson().toJson(xsList)
        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
        RetrofitManager.instance.cpbh(requestBody)
                .enqueue(object : BaseCallback<List<Xs>>(context = this) {
                    override fun successInfo(info: String) {
                        Toast.makeText(this@CpbhActivity, "完成备货", Toast.LENGTH_SHORT).show()
                        clearData()
                    }

                    override fun failureData(data: List<Xs>) {
                        Toast.makeText(this@CpbhActivity, "有单据已完成备货，请重新审查", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    /***
     * 清除数据,重置表单
     */
    fun clearData() {
        status = ScanStatus.EMPTY
        xsList.clear()
        xsmxList.clear()
        cpAdapter.notifyDataSetChanged()
    }
}
