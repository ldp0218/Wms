package net.tiaozhua.wms

import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.activity_cprk.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import okhttp3.MediaType
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*

class CprkActivity : BaseActivity(R.layout.activity_cprk), View.OnClickListener {

    private var cpPopup: CpPopup? = null
    internal var status = ScanStatus.EMPTY
    private lateinit var mVibrator: Vibrator
    private lateinit var mScanManager: ScanManager
    private val soundpool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100)
    private val soundid by lazy { soundpool.load("/etc/Scan_new.ogg", 1) }
    private lateinit var barcodeStr: String
    private var receiverTag: Boolean = false
    internal lateinit var rkd: Rkd
    internal lateinit var rkmxList: MutableList<Bzmx>
    internal lateinit var cpAdapter: BaseAdapter
    private var productList: HashSet<String> = hashSetOf()

    private val mScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            soundpool.play(soundid, 1f, 1f, 0, 0, 1f)
            mVibrator.vibrate(100)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))
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

            LoadingDialog.show(this@CprkActivity)
            RetrofitManager.instance.getProductInfo(id)
                    .enqueue(object : BaseCallback<Bzmx>(this@CprkActivity) {
                        override fun successData(data: Bzmx) {
                            if (data.pro_type == 1) {   // 记录异形，保证整套入库
                                var find = false
                                for (item in rkmxList) {
                                    if (item.scd_no == data.scd_no) {
                                        item.check_num++
                                        find = true
                                        break
                                    }
                                }
                                if (!find) {
                                    data.check_num = 1
                                    rkmxList.add(data)
                                }
                                rkd.rkmx.add(Rkmx(data.xsd_bz_id, 1, data.scd_no, data.pro_name, data.bz_id, data.bz_code))
                            } else {    // 常规记录重复件数
                                var find = false
                                for (item in rkd.rkmx) {
                                    if (item.bz_id == data.bz_id) {
                                        item.mx_num++
                                        find = true
                                        break
                                    }
                                }
                                if (!find) {
                                    rkd.rkmx.add(Rkmx(data.xsd_bz_id, 1, data.scd_no, data.pro_name, data.bz_id, data.bz_code))
                                }
                            }
                            cpAdapter.notifyDataSetChanged()
                            productList.add(barcodeStr)
                            if (status == ScanStatus.EMPTY) {  // 如果还未开始扫描修改状态为扫描
                                status = ScanStatus.SCAN
                            }
                            Toast.makeText(context, "已扫描", Toast.LENGTH_SHORT).show()
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
        mVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        toolbarTitle.text = "成品入库"
        rkd = Rkd("", 0, "", "", "", mutableListOf(), 1)
        rkmxList = mutableListOf()

        rkd.ck_id = 83
        rkd.ck_name = "成品仓库"
        rkd.rk_ldrq = SimpleDateFormat("yyyy-MM-dd").format(Date())
//        editText_ck.setText(rkd.ck_name)
//        editText_date.setText(rkd.rk_ldrq)
        refreshNo()

//        RetrofitManager.instance.ckList()
//                .enqueue(object : BaseCallback<ResponseList<Ck>>(context = this) {
//                    override fun successData(data: ResponseList<Ck>) {
//                        ckId = data.items[0].ck_id
//                        textView_ck.text = data.items[0].ck_name
//                    }
//                })

        // 为按钮设置点击监听事件
        rk.setOnClickListener(this)

        scrollView_cprk.smoothScrollTo(0, 0)
        if (!receiverTag) {     //在注册广播接受者的时候 判断是否已被注册,避免重复多次注册广播
            receiverTag = true
            registerReceiver(mScanReceiver, IntentFilter(SCAN_ACTION))
        }

        cpAdapter = object : CommonAdapter<Rkmx>(rkd.rkmx, R.layout.listview_cp_item) {
            override fun convert(holder: ViewHolder, t: Rkmx, position: Int) {
                holder.setText(R.id.textView_no, t.scd_no)
                holder.setText(R.id.textView_name, t.pro_name)
                holder.setText(R.id.textView_bzcode, t.bz_code)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (receiverTag) {   //判断广播是否注册
            receiverTag = false
            unregisterReceiver(mScanReceiver)
        }
        if (cpPopup != null) {
            cpPopup!!.dismiss()
            cpPopup = null
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
                    ScanStatus.EMPTY -> Toast.makeText(this@CprkActivity, "请扫描成品", Toast.LENGTH_SHORT).show()
                    ScanStatus.SCAN -> {
                        Log.i("data", rkmxList.toString())
                        for (item in rkmxList) {
                            if (item.check_num < item.pro_bz_num) {
                                Toast.makeText(this@CprkActivity, "有包装未扫描", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                        LoadingDialog.show(this)
                        val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(rkd))
                        RetrofitManager.instance.insertRk(requestBody)
                                .enqueue(object : BaseCallback<List<Bzmx>>(context = this) {
                                    override fun successInfo(info: String) {
                                        Toast.makeText(this@CprkActivity, "完成入库", Toast.LENGTH_SHORT).show()
                                        clearData()
                                    }
                                    override fun successData(data: List<Bzmx>) {
                                        Toast.makeText(this@CprkActivity, "有成品超出可入库数量", Toast.LENGTH_SHORT).show()
                                    }
                                })
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
