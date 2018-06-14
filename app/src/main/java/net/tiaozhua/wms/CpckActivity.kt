package net.tiaozhua.wms

import android.content.*
import android.device.ScanManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_cpck.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import okhttp3.MediaType
import okhttp3.RequestBody

@Suppress("DEPRECATION")
class CpckActivity : BaseActivity(R.layout.activity_cpck), View.OnClickListener {
    private lateinit var mVibrator: Vibrator
    private lateinit var mScanManager: ScanManager
    private val soundpool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100)
    private val soundid by lazy { soundpool.load("/etc/Scan_new.ogg", 1) }
    private lateinit var barcodeStr: String
    private var receiverTag: Boolean = false
    private var status = ScanStatus.EMPTY
    internal var xsList = mutableListOf<Xs>()   // 汇款通知单List
    internal var xsmxList = mutableListOf<Xsmx>()    //销售明细List，用于显示
    internal lateinit var cpAdapter: BaseAdapter
    internal var productList: HashMap<String, Xsmx> = hashMapOf()
    internal var nonScanningList: HashMap<Int, String> = hashMapOf()
    private var nonScanningPopup: NonScanningPopup? = null
    private var hktzdPopup: HktzdPopup? = null

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            soundpool.play(soundid, 1f, 1f, 0, 0, 1f)
            mVibrator.vibrate(100)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            if (barcodeStr.startsWith(DjFlag.HKTZD.value)) {  //  以此标志判断扫描的是单据还是产品
                val id = try {      // 截取后面的id
                    barcodeStr.substring(2).toInt()
                } catch (e: NumberFormatException) { 0 }
                if (id == 0) {
                    Toast.makeText(this@CpckActivity, "未识别的二维码", Toast.LENGTH_SHORT).show()
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
                        LoadingDialog.show(this@CpckActivity)
                        RetrofitManager.instance.hktzd(id)
                                .enqueue(object : BaseCallback<Xs>(context = this@CpckActivity) {
                                    override fun successData(data: Xs) {
                                        if (data.pdacode == 0) {    // 判断是否已出库
                                            Toast.makeText(this@CpckActivity, "此单已完成", Toast.LENGTH_SHORT).show()
                                            return
                                        } else if (data.pdacode == 2) {     // 是否存在该单据
                                            Toast.makeText(this@CpckActivity, "此单不存在", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        xsList.add(data)
                                        if (hktzdPopup != null) {
                                            hktzdPopup!!.update(xsList)
                                        }
                                        data.xsmx.forEach { xsmxList.add(it) }
                                        //productList.clear()
                                        for (item in xsmxList) {
                                            item.xsdbzList = mutableListOf()
                                        }
                                        if (xsmxList.size > 0) {
                                            //记录未扫描的包装
                                            xsmxList.forEach {
                                                if (it.bzList.isNotEmpty()) {
                                                    it.bzList.forEach { bz ->
                                                        nonScanningList[bz.xsd_bz_id] = it.pro_model + bz.bz_code
                                                    }
                                                }
                                            }
                                            status = ScanStatus.SCAN
                                            cpAdapter = object : CommonAdapter<Xsmx>(xsmxList, R.layout.listview_cpck_item) {
                                                override fun convert(holder: ViewHolder, t: Xsmx, position: Int) {
                                                    holder.setText(R.id.textView_model, t.pro_model ?: "")
                                                    holder.setText(R.id.textView_bz, if (t.pro_type == 0) t.mx_remark ?: "" else t.scd_no ?: "")
                                                    holder.setText(R.id.textView_num, (t.package_num ?: "").toString())
                                                    holder.setText(R.id.textView_scan, t.check_num.toString())
                                                }
                                            }
                                            listView_cpck.adapter = cpAdapter
                                        }
                                    }
                                })
                    }
                    else -> Toast.makeText(this@CpckActivity, "此单已扫描", Toast.LENGTH_SHORT).show()
                }
            } else {        // 扫描包装
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@CpckActivity, "请扫描汇款通知单", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
                        val id = try {
                            if (productList.contains(barcodeStr)) {
                                Toast.makeText(context, "请勿重复扫描", Toast.LENGTH_SHORT).show()
                                return
                            }
                            barcodeStr.substring(0, barcodeStr.indexOf("#")).toInt()
                        } catch (e: NumberFormatException) {
                            0
                        }
                        if (id == 0) {
                            Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                            return
                        }
                        LoadingDialog.show(this@CpckActivity)
                        RetrofitManager.instance.getProductInfo(id)
                                .enqueue(object : BaseCallback<Bzmx>(this@CpckActivity) {
                                    override fun successData(data: Bzmx) {
                                        if (data.kc_num == 0) {
                                            Toast.makeText(context, "此包装还未入库", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        if (nonScanningList.containsKey(data.xsd_bz_id)) {
                                            nonScanningList.remove(data.xsd_bz_id)
                                            if (nonScanningPopup != null) {
                                                nonScanningPopup!!.update(nonScanningList)
                                            }
                                        }
                                        var doneFlag: Xsmx? = null
                                        for (item in xsmxList) {
                                            if (item.bz_id != null && item.bz_id == data.bz_id) {   // 单件
                                                if (item.package_num != null && item.check_num < item.package_num) {
                                                    item.check_num++
                                                    item.xsdbzList.add(data)
                                                    doneFlag = item
                                                    break
                                                }
                                            }
                                        }
                                        if (doneFlag == null) {    // 如果没找到单件找整套
                                            xsmxList
                                                    .asSequence()
                                                    .filter { it.bz_id == null } // 筛选出整套的
                                                    .forEach { item ->
                                                        if (data.pro_type == 0) {       // 常规处理
                                                            for (it in item.bzList) {
                                                                if (it.bz_id == data.bz_id) {
                                                                    if (it.num < it.total) {
                                                                        it.num++
                                                                        item.check_num++
                                                                        item.xsdbzList.add(data)
                                                                        doneFlag = item
                                                                        break
                                                                    }
                                                                }
                                                            }
                                                        } else if (data.pro_type == 1) {    // 异形处理
                                                            if (item.package_num != null) {     // 有件数
                                                                if (item.xsdmx_id == data.xsdmx_id) {
                                                                    item.check_num++
                                                                    item.xsdbzList.add(data)
                                                                    doneFlag = item
                                                                }
                                                            } else {    // 无件数(借调)
                                                                for (it in item.jdList) {
                                                                    if (it.hxsdmx_id == data.xsdmx_id) {    // 查找是否是借调的包装
                                                                        item.check_num++
                                                                        item.xsdbzList.add(data)
                                                                        doneFlag = item
                                                                        break
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                        }

                                        cpAdapter.notifyDataSetChanged()
                                        if (doneFlag != null) {     // 处理完毕
                                            productList[barcodeStr] = doneFlag!!     // 记录扫描的二维码
                                            Toast.makeText(context, "已扫描", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "此包装不在汇款通知单内，请核实", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun successInfo(info: String) {
                                        Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                                    }
                                })
                    }
                    ScanStatus.FINISH -> {
                        checkOut()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        toolbarTitle.text = "成品出库"

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
                    Toast.makeText(this@CpckActivity, "请扫描汇款通知单", Toast.LENGTH_SHORT).show()
                    return
                }
                if (hktzdPopup == null) {
                    hktzdPopup = HktzdPopup(this@CpckActivity)
                } else {
                    hktzdPopup!!.update(xsList)
                }
                hktzdPopup!!.showPopupWindow()
            }
            R.id.btn_chuku -> {    // 出库
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this@CpckActivity, "请扫描汇款通知单", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
//                        val count = xsList
//                                .map { xs -> xs.xsmx.any { if (it.package_num != null) it.check_num < it.package_num else false } }
//                                .count { it }
                        if (xsList.size == 0) {
                            status = ScanStatus.EMPTY
                            Toast.makeText(this@CpckActivity, "请扫描汇款通知单", Toast.LENGTH_SHORT).show()
                            return
                        }
                        val isNotFinished = xsmxList.any { if (it.package_num != null) it.check_num < it.package_num else false }
                        if (isNotFinished) {
                            if (nonScanningPopup == null) {
                                nonScanningPopup = NonScanningPopup(this@CpckActivity)
                            } else {
                                nonScanningPopup!!.update(nonScanningList)
                            }
                            nonScanningPopup!!.showPopupWindow()
//                            Toast.makeText(this@CpckActivity, "有包装未扫描", Toast.LENGTH_SHORT).show()
                        } else {
                            status = ScanStatus.FINISH
                            checkOut()
                        }
                    }
                    ScanStatus.FINISH -> {
                        checkOut()
                    }
                }
            }
        }
    }

    private fun checkOut() {
        DialogUtil.showDialog(this, null, "包装已全部扫描,是否出库?",
                null,
                DialogInterface.OnClickListener { _, _ ->
                    LoadingDialog.show(this@CpckActivity)
                    val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(xsList.toTypedArray()))
                    RetrofitManager.instance.cpck(requestBody)
                            .enqueue(object : BaseCallback<List<Xs>>(context = this) {
                                override fun successInfo(info: String) {
                                    Toast.makeText(this@CpckActivity, "完成出库", Toast.LENGTH_SHORT).show()
                                    clearData()
                                }

                                override fun failureData(data: List<Xs>) {
                                    Toast.makeText(this@CpckActivity, "有单据已完成，请重新审查", Toast.LENGTH_SHORT).show()
                                }
                            })
                }
        )
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
