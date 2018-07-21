package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.device.ScanManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.android.synthetic.main.activity_wlck.*
import kotlinx.android.synthetic.main.popup_scan_material.view.*
import net.tiaozhua.wms.adapter.WlckAdapter
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*

class WlckActivity : BaseActivity(R.layout.activity_wlck), View.OnClickListener {

    private lateinit var mScanManager: ScanManager
    private lateinit var barcodeStr: String
    private var receiverTag: Boolean = false
    private var status = ScanStatus.EMPTY
    internal var ckd = Ckd(0, "", 0, "", "", "", 0,
    null, 0, "", mutableListOf())
    internal val scdmxList = mutableListOf<Scdmx>()
    internal var wlckList = mutableListOf<Wlck>()
    internal var material: Material? = null
    internal var wl: Wlck? = null
    internal var wlPopup: WlPopup? = null
    internal lateinit var adapter: WlckAdapter
    private var gzId: Int? = null
    private var overUsePopup: OverUsePopup? = null
    internal var overList = mutableListOf<Scdpl>()
    private var overKcPopup: OverKcPopup? = null
    internal var overKcList = mutableListOf<Ckdmx>()
    private var ckListPopup: CkListPopup? = null

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            (application as App).playAndVibrate(this@WlckActivity)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            if (barcodeStr.startsWith(DjFlag.LLD.value)) {  //  以此标志判断扫描的是单据还是物料
                val id = try {      // 截取后面的id
                    barcodeStr.substring(2).toInt()
                } catch (e: NumberFormatException) { 0 }
                if (id == 0) {
                    Toast.makeText(this@WlckActivity, "未识别的二维码", Toast.LENGTH_SHORT).show()
                    return
                }
                if (gzId == null) {
                    Toast.makeText(this@WlckActivity, "请选择领料人", Toast.LENGTH_SHORT).show()
                    return
                }
                if (ckd.ck_id == 0) {
                    Toast.makeText(this@WlckActivity, "请选择仓库", Toast.LENGTH_SHORT).show()
                    return
                }
                LoadingDialog.show(this@WlckActivity)
                RetrofitManager.instance.scdmxInfo(id, ckd.ck_id, gzId!!)
                        .enqueue(object : BaseCallback<Scdmx>(context = this@WlckActivity) {
                            override fun successData(data: Scdmx) {
                                if (data.pdacode == 0) {    // 判断是否可领料
                                    Toast.makeText(this@WlckActivity, "该流程卡不可领料", Toast.LENGTH_SHORT).show()
                                    return
                                } else if (data.pdacode == 2) {     // 是否存在该单据
                                    Toast.makeText(this@WlckActivity, "该流程卡不存在", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                // 查找是否已扫描该流程卡
                                var scdmx = scdmxList.find { it.scdmx_id == data.scdmx_id }
                                if (scdmx == null) {
                                    data.plList.forEach {
                                        it.checked = true
                                        val klnum = it.mx_num - it.mx_wcnum
                                        if (klnum < 0.0) {
                                            it.num = 0.0
                                        } else {
                                            it.num = klnum
                                        }
                                    }
                                    scdmx = data
                                }
                                val activityIntent = Intent(this@WlckActivity, LldInfoActivity::class.java)
                                activityIntent.putExtra("scdmx", scdmx)
                                startActivityForResult(activityIntent, 0)
                            }

                            override fun successInfo(info: String) {
                                Toast.makeText(this@WlckActivity, info, Toast.LENGTH_SHORT).show()
                            }
                        })
//                RetrofitManager.instance.lldInfo(id)
//                        .enqueue(object : BaseCallback<Ckd>(context = this@WlckActivity) {
//                            override fun successData(data: Ckd) {
//                                ckd = data
//                                if (ckd.pdacode == 0) {    // 判断是否已领料
//                                    Toast.makeText(this@WlckActivity, "已领完料", Toast.LENGTH_SHORT).show()
//                                    return
//                                } else if (ckd.pdacode == 2) {     // 是否存在该单据
//                                    Toast.makeText(this@WlckActivity, "该领料单不存在", Toast.LENGTH_SHORT).show()
//                                    return
//                                }
//                                val ckdmxList = ckd.ckdmx
//                                if (ckdmxList.size > 0) {
//                                    status = ScanStatus.SCAN
//                                    ckdmxList.forEach { it.mx_num = 0.0 }
//                                    wlAdapter = object : CommonAdapter<Ckdmx>(ckdmxList, R.layout.listview_wlck_item) {
//                                        override fun convert(holder: ViewHolder, t: Ckdmx, position: Int) {
//                                            holder.setText(R.id.textView_name, t.ma_name)
//                                            holder.setText(R.id.textView_cknum, t.ck_num.toString())
//                                            holder.setText(R.id.textView_hw, t.hj_name ?: "")
//                                            holder.setText(R.id.textView_unit, t.ma_unit)
//                                            holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
//                                                //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
//                                                (holder.getConvertView() as SwipeMenuLayout).quickClose()
//                                                val obj = ckd.ckdmx[position]
//                                                if (obj.ck_num == 0.0) {
//                                                    ckd.ckdmx.removeAt(position)
//                                                    notifyDataSetChanged()
//                                                } else {
//                                                    Toast.makeText(this@WlckActivity, "物料已扫描不能删除", Toast.LENGTH_SHORT).show()
//                                                }
//                                            })
//                                            holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
//                                                if (ckd.ckdmx[position].ck_num > 0) {
//                                                    if (wlPopup == null) {
//                                                        wlPopup = WlPopup(this@WlckActivity)
//                                                    }
//                                                    ckdmx = ckd.ckdmx[position]
//                                                    wlPopup!!.setUpdate()     // 设置为修改界面
//                                                    wlPopup!!.showPopupWindow()
//                                                }
//                                            })
//                                        }
//                                    }
//                                    listView_wlck.adapter = wlAdapter
//                                }
//                            }
//                        })
            } else {
//                when (status) {
//                    ScanStatus.SCAN, ScanStatus.FINISH -> {
//                        if (!wlckList.any { it.ma_code == barcodeStr }) {
//                            Toast.makeText(this@WlckActivity, "领料单中不存在该物料", Toast.LENGTH_SHORT).show()
//                            return
//                        }

                        wlPopup = wlPopup ?: WlPopup(this@WlckActivity)
                        wl = wlckList.find { it.ma_code == barcodeStr }
                        if (wl != null) {
                            val ma = wl!!
                            material = Material("", "", "", false, ma.kc_hw_name,
                                    0, ma.kc_num, ma.num, "", ma.ma_code, ma.ma_id, 0.0,
                                    0, ma.ma_kind, ma.ma_name, ma.ma_spec, "", ma.ma_unit)
                        }
                        wlPopup!!.setUpdate()
                        wlPopup!!.showPopupWindow()
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
                                                popupView.editText_tm.setText(material!!.ma_code)
                                                popupView.textView_name.text = material!!.ma_name
                                                popupView.textView_kcnum.text = material!!.kc_num.toString()
                                                popupView.textView_kind.text = material!!.ma_kind_name
                                                popupView.textView_hw.text = material!!.kc_hw_name
                                                popupView.textView_remark.text = material!!.comment
                                                popupView.editText_num.requestFocus()
                                                popupView.editText_num.setSelection(popupView.editText_num.text.toString().trim().length)
                                                DialogUtil.showInputMethod(context, popupView.editText_num, true, 100)
                                            }
                                        }
                                    }
                                })
//                    }
//                    else -> Toast.makeText(this@WlckActivity, "请扫描流程卡", Toast.LENGTH_SHORT).show()
//                }
            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "物料出库"
        WlckActivity.self = this
        val user = (application as App).user
        editText_wlck_ck.setText(user?.ck_name)

        RetrofitManager.instance.getJhNo().enqueue(object : BaseCallback<String>(this) {
            @SuppressLint("SimpleDateFormat")
            override fun successData(data: String) {
                ckd.ckd_no = data
            }
        })
        ckd.ckd_ldrq = SimpleDateFormat("yyyy-MM-dd").format(Date())

        ckd.ck_id = user!!.ck_id ?: 0
        ckd.ck_name = user.ck_name ?: ""

        // 为按钮设置点击监听事件
        button_selectLlr.setOnClickListener(this)
        scan.setOnClickListener(this)
        lld.setOnClickListener(this)
        ck.setOnClickListener(this)
        editText_wlck_ck.setOnClickListener(this)

        wlckList = mutableListOf()
        adapter = WlckAdapter(wlckList, this)
        listView_wlck.adapter = adapter

        scrollView_wlck.smoothScrollTo(0, 0)
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
        when (requestCode) {
            0 -> {  // LldInfoActivity
                when (resultCode) {
                    Activity.RESULT_OK -> {     // 选择了领料单
                        if (intent != null) {
                            status = ScanStatus.SCAN
                            val scdmx = intent.getParcelableExtra<Scdmx>("scdmx")
                            updateWlckAdapter(scdmx)
                        }
//                ckd = intent?.getSerializableExtra("ckd") as Ckd
//                LoadingDialog.show(this@WlckActivity)
//                RetrofitManager.instance.ckdmxList(ckd.ckd_id, ckd.ck_id)
//                        .enqueue(object : BaseCallback<List<Ckdmx>>(context = this) {
//                            override fun successData(data: List<Ckdmx>) {
//                                ckd.ckdmx = data.toMutableList()
////                                editText_date.setText(ckd.ckd_ldrq)
////                                editText_llr.setText(ckd.llr_name)
////                                editText_bm.setText(ckd.bm_name)
////                                editText_ck.setText(ckd.ck_name)
////                                editText_beizhu.setText(ckd.remark)
//                                if (ckd.ckdmx.size > 0) {
//                                    status = ScanStatus.SCAN
//                                    wlAdapter = object : CommonAdapter<Ckdmx>(ckd.ckdmx, R.layout.listview_wlck_item) {
//                                        override fun convert(holder: ViewHolder, t: Ckdmx, position: Int) {
//                                            holder.setText(R.id.textView_no, t.ma_name)
////                                            holder.setText(R.id.textView_wcknum, (t.mx_num - t.wc_num).toString())
//                                            holder.setText(R.id.textView_cknum, t.ck_num.toString())
//                                            holder.setText(R.id.textView_hw, t.hj_name ?: "")
//                                            holder.setText(R.id.textView_unit, t.ma_unit)
//                                            holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
//                                                //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
//                                                (holder.getConvertView() as SwipeMenuLayout).quickClose()
//                                                val obj = ckd.ckdmx[position]
//                                                if (obj.ck_num == 0.0) {
//                                                    ckd.ckdmx.removeAt(position)
//                                                    notifyDataSetChanged()
//                                                } else {
//                                                    Toast.makeText(this@WlckActivity, "物料已扫描不能删除", Toast.LENGTH_SHORT).show()
//                                                }
//                                            })
//                                            holder.setOnClickListener(R.id.layout_wl, View.OnClickListener { _ ->
//                                                if (ckd.ckdmx[position].ck_num > 0) {
//                                                    if (wlPopup == null) {
//                                                        wlPopup = WlPopup(this@WlckActivity)
//                                                    }
//                                                    ckdmx = ckd.ckdmx[position]
//                                                    wlPopup!!.setUpdate()     // 设置为修改界面
//                                                    wlPopup!!.showPopupWindow()
//                                                }
//                                            })
//                                        }
//                                    }
//                                    listView_wlck.adapter = wlAdapter
//                                }
//                            }
//                        })
                    }
//                    1 -> {      // 选择了物料
//                        material = intent?.getParcelableExtra("material") as Material
//                        val view = wlPopup!!.popupView
//                        view.editText_tm.setText(material!!.ma_txm)
//                        view.textView_name.text = material!!.ma_name
//                        view.textView_kcnum.text = material!!.kc_num.toString()
//                        view.textView_spec.text = material!!.ma_spec
//                        view.textView_kind.text = material!!.ma_kind
//                        view.textView_hw.text = material!!.kc_hw_name
//                        view.textView_remark.text = material!!.comment
//                    }
                }
            }
            1 -> {  // UserSelectActivity
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val user = intent?.getParcelableExtra<User>("user")
                        editText_llr.setText(user?.a_name)
                        gzId = user?.gz_id
                        (application as App).gzId = gzId!!
                        ckd.llr_id = user?.a_id ?: 0
                        ckd.llr_name = user?.a_name ?: ""
                    }
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button_selectLlr -> {  //选择领料人
                val intent = Intent(this, UserSelectActivity::class.java)
                startActivityForResult(intent, 1)
            }
            R.id.scan -> {      // 扫描
                wlPopup = WlPopup(this)
                wlPopup!!.setScan()     // 设置为扫描界面
                wlPopup!!.showPopupWindow()
            }
            R.id.lld -> {   // 领料单
                val intent = Intent(this@WlckActivity, WlckdActivity::class.java)
                startActivity(intent)
            }
            R.id.ck -> {    // 出库
                if (gzId == null) {
                    Toast.makeText(this@WlckActivity, "请选择领料人", Toast.LENGTH_SHORT).show()
                    return
                }
                status = when {
                    wlckList.size == 0 -> ScanStatus.EMPTY
                    wlckList.any { it.scanNum == 0.0 } -> ScanStatus.SCAN
                    else -> ScanStatus.FINISH
                }
                when (status) {
                    ScanStatus.EMPTY -> Toast.makeText(this, "请扫描流程卡", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> Toast.makeText(this, "有物料未清点", Toast.LENGTH_SHORT).show()
                    ScanStatus.FINISH -> {
                        DialogUtil.showDialog(this, null, "物料已清点完毕,是否出库?",
                                null,
                                DialogInterface.OnClickListener { _, _ ->
                                    ckd.ckdmx.clear()       //先清空
                                    for (item in wlckList) {
                                        if (item.mxList == null) {      //单独领的物料
                                            ckd.ckdmx.add(Ckdmx(null, null, item.ma_code, item.ma_id, item.ma_kind, item.ma_name, item.ma_spec,
                                                    item.ma_unit, 0, item.mx_remark, 0.0, 0.0, item.kc_num, item.num,
                                                    0.0, item.gz_id, item.isAdd, null))
                                        }
                                    }
                                    for (item in scdmxList) {
                                        for (it in item.plList) {
                                            if (it.checked) {
                                                ckd.ckdmx.add(Ckdmx(it.scdpl_id, it.scdmx_id, it.ma_code, it.ma_id, it.ma_kind, it.ma_name, it.ma_spec,
                                                        it.ma_unit, 0, it.mx_remark, it.mx_num, it.mx_wcnum, 0.0, it.num,
                                                        it.mx_num - it.mx_wcnum, it.gz_id, it.isAdd, null))
                                            }
                                        }
                                    }
                                    LoadingDialog.show(this@WlckActivity)
                                    val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ObjectMapper().writeValueAsBytes(ckd))
                                    RetrofitManager.instance.lldCk(requestBody)
                                            .enqueue(object : BaseCallback<CkdFailureData>(context = this) {
                                                override fun successInfo(info: String) {
                                                    Toast.makeText(this@WlckActivity, "完成出库", Toast.LENGTH_SHORT).show()
                                                    clearData()
                                                }

                                                override fun successData(data: CkdFailureData) {
                                                    Log.i("result", data.toString())
                                                    if (data.clData!= null && data.clData!!.isNotEmpty()) {
                                                        //修改生产单明细配料中的mx_num和mx_wcnum
                                                        for (item in scdmxList) {
                                                            for (it in item.plList) {
                                                                for (i in data.clData!!) {
                                                                    if (it.scdpl_id == i.scdpl_id) {
                                                                        it.mx_num = i.mx_num
                                                                        it.mx_wcnum = i.wc_num
                                                                        i.scd_no = it.scd_no
                                                                        break
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        for (item in data.clData!!) {
                                                            overList.add(Scdpl(item.scdpl_id, item.ma_id, 0, 0.0, item.mx_num, "",
                                                                    item.wc_num, item.ck_num, null, item.ma_name, item.ma_code,
                                                                    "", "", "", 0, 0, item.scd_no, "", ""))
                                                        }
                                                        if (overUsePopup == null) {
                                                            overUsePopup = OverUsePopup(this@WlckActivity)
                                                        } else {
                                                            overUsePopup!!.update(overList)
                                                        }
                                                        overUsePopup!!.showPopupWindow()
                                                    }
                                                    if (data.kcData!= null && data.kcData!!.isNotEmpty()) {
                                                        for (item in scdmxList) {
                                                            for (it in item.plList) {
                                                                for (kc in data.kcData!!) {
                                                                    if (it.ma_id == kc.ma_id) {
                                                                        it.kc_num = kc.kc_num
                                                                        break
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        overKcList = data.kcData!!.toMutableList()
                                                        if (overKcPopup == null) {
                                                            overKcPopup = OverKcPopup(this@WlckActivity)
                                                        } else {
                                                            overKcPopup!!.update(overKcList)
                                                        }
                                                        overKcPopup!!.showPopupWindow()
                                                    }
                                                }
                                            })
                                }
                        )
                    }
                }
            }
            R.id.editText_wlck_ck -> {   //更换仓库
                ckListPopup = ckListPopup ?: CkListPopup(this@WlckActivity)
                ckListPopup!!.showPopupWindow()
            }
        }
    }

    /***
     * 清除数据,重置表单
     */
    fun clearData() {
        status = ScanStatus.EMPTY
        editText_llr.text.clear()
        ckd = Ckd(ckd.ck_id, ckd.ck_name, 0, "", "", "", 0,
                null, 0, "", mutableListOf())
        scdmxList.clear()
        wlckList.clear()
        adapter.notifyDataSetChanged()
        wl = null
        gzId = null
        overList.clear()
        overKcList.clear()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var self: WlckActivity

        internal fun updateWlckAdapter(scdmx: Scdmx) {
            val d = self.scdmxList.find { it.scdmx_id == scdmx.scdmx_id }
            if (d == null) {
                self.scdmxList.add(scdmx)    //添加新流程卡
            } else {
                d.plList.clear()
                d.plList.addAll(scdmx.plList)
            }
            val wlList = mutableListOf<Wlck>()
            // 找出已选物料，合并相同物料数量
            for (item in self.scdmxList) {
                for (it in item.plList) {
                    if (it.checked && it.num > 0.0) {
                        var flag = false
                        for (i in wlList) {
                            if (i.ma_id == it.ma_id) {
                                val mx = i.mxList?.find { it.scdmx_id == item.scdmx_id }
                                if (mx == null) {
                                    i.mxList!!.add(it)
                                } else {
                                    mx.num = it.num
                                }
                                flag = true
                                break
                            }
                        }
                        if (!flag) {
                            it.scd_no = item.scd_no
                            wlList.add(Wlck(it.ma_id, it.kc_num, it.kc_hw_name, it.num, 0.0, it.mx_remark, it.ma_name, it.ma_code,
                                    it.ma_spec, it.ma_kind, it.ma_unit, it.gz_id, it.isAdd, mutableListOf(it)))
                        }
                    }
                }
            }
            for (item in wlList) {
                for (it in self.wlckList) {
                    if (item.ma_id == it.ma_id) {
                        item.scanNum = it.scanNum   // 重新赋值扫描数量
                        break
                    }
                }
            }
            for (item in wlList) {
                item.num = 0.0      //重新统计总出库数量
                for (it in item.mxList!!) {
                    item.num += it.num
                }
            }
            self.wlckList = wlList
            self.adapter = WlckAdapter(self.wlckList, self)
            self.listView_wlck.adapter = self.adapter
        }
    }
}
