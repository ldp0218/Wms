package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_wlrk.*
import kotlinx.android.synthetic.main.popup_scan_material.view.*
import net.tiaozhua.wms.bean.Jhmx
import net.tiaozhua.wms.bean.Material
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.RetrofitManager

import razerdp.basepopup.BasePopupWindow

/**
* Created by ldp on 2017/10/27.
*/
const val SCAN_ACTION = "urovo.rcv.message"//扫描结束action
class DialogPopup(context: Activity) : BasePopupWindow(context), View.OnClickListener {

    lateinit var popupView: View
    private var receiverTag: Boolean = false
    private lateinit var jhmx: Jhmx

    init {
        bindEvent()
    }

    fun setScan() {
        popupView.editText_tm.isFocusableInTouchMode = true
        popupView.editText_tm.isFocusable = true
    }

    fun setUpdate(jh: Jhmx) {
        this.jhmx = jh
        popupView.editText_tm.isFocusable = false
        popupView.editText_tm.isFocusableInTouchMode = false
        popupView.editText_tm.setText(jhmx.ma_txm)
        popupView.textView_no.text = jhmx.ma_code
        popupView.textView_name.text = jhmx.ma_name
        popupView.editText_num.setText(jhmx.scan_num.toString())
        popupView.textView_kcnum.text = jhmx.kcnum.toString()
        popupView.textView_spec.text = jhmx.ma_spec
        popupView.textView_model.text = jhmx.ma_model
        popupView.textView_hw.text = jhmx.hj_name
        popupView.textView_remark.text = jhmx.mx_remark
    }

    override fun initShowAnimation(): Animation {
        return getTranslateAnimation(500, 0, 200)
    }

    override fun initExitAnimation(): Animation {
        return getTranslateAnimation(0, 500, 200)
    }

    override fun dismiss() {
        super.dismiss()
        if (receiverTag) {   //判断广播是否注册
            receiverTag = false
            context.unregisterReceiver(WlrkActivity.self.mScanReceiver)
        }
    }

    override fun getClickToDismissView(): View {
        return popupView.findViewById(R.id.click_to_dismiss)
    }

    @SuppressLint("InflateParams")
    override fun onCreatePopupView(): View? {
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_scan_material, null)
        return popupView
    }

    override fun initAnimaView(): View {
        return popupView.findViewById(R.id.popup_anima)
    }

    private fun bindEvent() {
        if (!receiverTag) {     //在注册广播接受者的时候 判断是否已被注册,避免重复多次注册广播
            receiverTag = true
            context.registerReceiver(WlrkActivity.self.mScanReceiver, IntentFilter(SCAN_ACTION))
        }
        popupView.button_close.setOnClickListener(this)
        popupView.button_commit.setOnClickListener(this)
        popupView.editText_tm.setOnEditorActionListener({ _, i, _ ->
            when (i) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {  // 点击完成按钮或虚拟键
                    val barcode = popupView.editText_tm.text.toString()
                    val ckId = WlrkActivity.self.jhrk.ck_id
                    RetrofitManager.instance.materialList(barcode, ckId)
                            .enqueue(object : BaseCallback<ResponseList<Material>>(context) {
                                override fun success(data: ResponseList<Material>) {
                                    when {
                                        data.totalCount == 0 -> Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                                        data.totalCount > 1 -> {
                                            val intent = Intent(context, MaterialSelectActivity::class.java)
                                            intent.putExtra("code", barcode)
                                            intent.putExtra("ckId", ckId)
                                            intent.putExtra("data", data)
                                            WlrkActivity.self.startActivityForResult(intent, 0)
                                        }
                                        else -> {
                                            val material = data.items[0]
                                            WlrkActivity.self.material = material
                                            popupView.editText_tm.setText(material.ma_txm)
                                            popupView.textView_no.text = material.ma_code
                                            popupView.textView_name.text = material.ma_name
                                            popupView.textView_kcnum.text = material.kc_num.toString()
                                            popupView.textView_spec.text = material.ma_spec
                                            popupView.textView_model.text = material.ma_model
                                            popupView.textView_hw.text = material.kc_hw_name
                                            popupView.textView_remark.text = material.comment
                                        }
                                    }
                                }
                            })
                }
                else -> Toast.makeText(context, i.toString(), Toast.LENGTH_SHORT).show()
            }
            return@setOnEditorActionListener true
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_close -> this.dismiss()
            R.id.button_commit -> {
                if (WlrkActivity.self.material == null) {
                    Toast.makeText(context, "请扫描物料", Toast.LENGTH_SHORT).show()
                    return
                }
                var hasMaterial = false
                for (item in WlrkActivity.self.jhmxList) {
                    if (item.ma_id == WlrkActivity.self.material?.ma_id) {
                        val numStr = popupView.editText_num.text.toString()
                        if (numStr.trim().isBlank()) {
                            Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                            return
                        }
                        val num = numStr.toInt()
                        if (num > item.jhdmx_num) {
                            Toast.makeText(context, "不能超过未入库数量", Toast.LENGTH_SHORT).show()
                            return
                        }
                        Toast.makeText(context, "数量清点完毕", Toast.LENGTH_SHORT).show()
                        item.scan_num = num
                        // 重新加载数据
                        WlrkActivity.self.listView_wl.adapter = WlrkActivity.self.wlAdapter
                        WlrkActivity.self.wlAdapter.notifyDataSetChanged()
                        popupView.editText_tm.setText("")
                        popupView.textView_no.text = ""
                        popupView.textView_name.text = ""
                        popupView.editText_num.setText("")
                        popupView.textView_kcnum.text = ""
                        popupView.textView_spec.text = ""
                        popupView.textView_hw.text = ""
                        popupView.textView_remark.text = ""
                        hasMaterial = true
                        break
                    }
                }
                if (!hasMaterial) {
                    Toast.makeText(context, "入库单不包含该物料，请扫描其它物料", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.button_update -> {
                val numStr = popupView.editText_num.text.toString()
                if (numStr.trim().isBlank()) {
                    Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                    return
                }
                val num = numStr.toInt()
                if (num > jhmx.jhdmx_num) {
                    Toast.makeText(context, "不能超过未入库数量", Toast.LENGTH_SHORT).show()
                    return
                }
                jhmx.scan_num = num
                // 重新加载数据
                WlrkActivity.self.listView_wl.adapter = WlrkActivity.self.wlAdapter
                WlrkActivity.self.wlAdapter.notifyDataSetChanged()
                this.dismiss()
            }
        }
    }
}