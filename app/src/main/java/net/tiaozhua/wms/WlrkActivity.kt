package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.device.ScanManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Vibrator
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import kotlinx.android.synthetic.main.activity_wlrk.*
import kotlinx.android.synthetic.main.popup_scan_material.view.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.Jhmx
import net.tiaozhua.wms.bean.Jhrk
import net.tiaozhua.wms.bean.Material
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.RetrofitManager

class WlrkActivity : BaseActivity(R.layout.activity_wlrk) {

    private var dialogPopup: DialogPopup? = null
    private lateinit var mVibrator: Vibrator
    private lateinit var mScanManager: ScanManager
    private val soundpool: SoundPool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100)
    private val soundid: Int by lazy { soundpool.load("/etc/Scan_new.ogg", 1) }
    private lateinit var barcodeStr: String
    private var status = RkStatus.EMPTY
    internal lateinit var jhrk: Jhrk
    internal lateinit var jhmxList: MutableList<Jhmx>
    internal lateinit var material: Material

    internal val mScanReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            soundpool.play(soundid, 1f, 1f, 0, 0, 1f)
            mVibrator.vibrate(100)
            barcodeStr = String(intent.getByteArrayExtra("barocode"), 0, intent.getIntExtra("length", 0))

            if (dialogPopup != null && dialogPopup!!.isShowing) {
                val view = dialogPopup!!.popupView
                view.editText_tm.setText(barcodeStr)
                RetrofitManager.instance.materialList(barcodeStr, jhrk.ck_id)
                        .enqueue(object : BaseCallback<ResponseList<Material>>(this@WlrkActivity) {
                            override fun success(data: ResponseList<Material>) {
                                if (data.totalPages > 1) {
                                    val activityIntent = Intent(this@WlrkActivity, MaterialSelectActivity::class.java)
                                    activityIntent.putExtra("code", barcodeStr)
                                    activityIntent.putExtra("ckId", jhrk.ck_id)
                                    intent.putExtra("data", data)
                                    startActivityForResult(activityIntent, 0)
                                }
                            }
                        })
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        toolbarTitle.text = "物料入库"

        self = this
        scrollView_wlrk.smoothScrollTo(0, 0)
    }

    override fun onResume() {
        super.onResume()
        initScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialogPopup != null) {
            dialogPopup!!.dismiss()
            dialogPopup = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                RetrofitManager.instance.jhrkInfo(data!!.getIntExtra("id", 0))
                        .enqueue(object : BaseCallback<Jhrk>(context = this) {
                            override fun success(data: Jhrk) {
                                jhrk = data
                                editText_no.setText(jhrk.jh_no)
                                editText_date.setText(jhrk.jh_ldrq)
                                editText_ghdw.setText(jhrk.client_name)
                                editText_jsr.setText(jhrk.handler_name)
                                editText_rkck.setText(jhrk.ck_name)
                                editText_beizhu.setText(jhrk.remark)
                                jhmxList = jhrk.jhmx as MutableList<Jhmx>
                                if (jhmxList.size > 0) {
                                    status = RkStatus.NOTSCAN
                                    listView_wl.adapter = object : CommonAdapter<Jhmx>(jhmxList, R.layout.listview_wl_item) {
                                        override fun convert(holder: ViewHolder, t: Jhmx, position: Int) {
                                            holder.setText(R.id.textView_name, t.ma_name)
                                            holder.setText(R.id.textView_wrknum, t.jhdmx_num.toString())
                                            holder.setText(R.id.textView_scannum, "0")
                                            holder.setText(R.id.textView_hw, t.hj_name ?: "")
                                            holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
                                                Toast.makeText(this@WlrkActivity, "删除:" + position, Toast.LENGTH_SHORT).show()
                                                //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                                                (holder.getConvertView() as SwipeMenuLayout).quickClose()
                                                jhmxList.removeAt(position)
                                                notifyDataSetChanged()
                                            })
                                        }
                                    }
                                }
                            }
                        })
            }
            1 -> {
                material = data!!.getSerializableExtra("material") as Material
                val view = dialogPopup!!.popupView
                view.editText_tm.setText(material.ma_txm)
                view.editText_no.setText(material.ma_code)
                view.textView_name.text = material.ma_name
                view.textView_kcnum.text = material.kc_num.toString()
                view.textView_spec.text = material.ma_spec
                view.textView_model.text = material.ma_model
                view.textView_hw.text = material.kc_hw_name
                view.textView_remark.text = material.comment
            }
        }
    }

    override fun onBackPressed() {

        super.onBackPressed()
    }

    private fun initScan() {
        mScanManager = ScanManager()
        mScanManager.openScanner()
        mScanManager.switchOutputMode(0)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var self: WlrkActivity
    }

    fun scanClick(view: View) {
        if (status != RkStatus.EMPTY) {
            dialogPopup = DialogPopup(this@WlrkActivity)
            dialogPopup!!.showPopupWindow()
        } else {
            Toast.makeText(this@WlrkActivity, "请选择入库单", Toast.LENGTH_SHORT).show()
        }
    }

    fun lldClick(view: View) {
        val intent = Intent(this@WlrkActivity, RkdActivity::class.java)
        startActivityForResult(intent, 0)
    }

    fun ckClick(view: View) {
        when (status) {
            RkStatus.EMPTY -> Toast.makeText(this@WlrkActivity, "请选择入库单", Toast.LENGTH_SHORT).show()
            RkStatus.NOTSCAN, RkStatus.NOTFINISH -> Toast.makeText(this@WlrkActivity, "有物料未扫描", Toast.LENGTH_SHORT).show()
            RkStatus.FINISHED -> {

            }
        }
    }
}

enum class RkStatus {
    EMPTY, NOTSCAN, NOTFINISH, FINISHED
}
