package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_wlrk.*
import kotlinx.android.synthetic.main.popup_scan_material.view.*
import net.tiaozhua.wms.bean.Material
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.RetrofitManager
import razerdp.basepopup.BasePopupWindow

/**
* Created by ldp on 2017/10/27.
*/
class DialogPopup(private val activity: Activity) : BasePopupWindow(activity), View.OnClickListener {

    lateinit var popupView: View

    init {
        bindEvent()
    }

    fun setScan() {
        clearData()
        popupView.editText_tm.isFocusableInTouchMode = true
        popupView.editText_tm.isFocusable = true
        popupView.button_commit.visibility = View.VISIBLE
        popupView.button_commit.text = "确认"
    }

    fun setUpdate() {
        popupView.editText_tm.isFocusable = false
        popupView.editText_tm.isFocusableInTouchMode = false
        popupView.button_commit.text = "修改"
        when (activity) {
            is WlrkActivity -> {
                val m = activity.jhmx
                popupView.editText_tm.setText(m.ma_txm)
                popupView.textView_no.text = m.ma_code
                popupView.textView_name.text = m.ma_name
                popupView.editText_num.setText(m.mx_num.toString())
                popupView.editText_num.requestFocus()
                popupView.textView_kcnum.text = m.kcnum.toString()
                popupView.textView_spec.text = m.ma_spec
                popupView.textView_model.text = m.ma_model
                popupView.textView_hw.text = m.hj_name
                popupView.textView_remark.text = m.mx_remark
            }
            is WlckActivity -> {
                val m = activity.ckdmx
                popupView.editText_tm.setText(m.ma_txm)
                popupView.textView_no.text = m.ma_code
                popupView.textView_name.text = m.ma_name
                popupView.editText_num.setText(m.ck_num.toString())
                popupView.editText_num.requestFocus()
                popupView.textView_kcnum.text = m.kc_num.toString()
                popupView.textView_spec.text = m.ma_spec
                popupView.textView_model.text = m.ma_model
                popupView.textView_hw.text = m.hj_name
                popupView.textView_remark.text = m.mx_remark
            }
        }
    }

    override fun initShowAnimation(): Animation {
        return getTranslateAnimation(500, 0, 200)
    }

    override fun initExitAnimation(): Animation {
        return getTranslateAnimation(0, 500, 200)
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
        popupView.button_close.setOnClickListener(this)
        popupView.button_commit.setOnClickListener(this)
        popupView.editText_tm.setOnEditorActionListener({ _, code, _ ->
            when (code) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {  // 点击完成按钮或虚拟键
                    val barcode = popupView.editText_tm.text.toString()
                    when (activity) {
                        is WlrkActivity -> {
                            val ckId = activity.jhrk.ck_id
                            RetrofitManager.instance.materialList(barcode, ckId)
                                    .enqueue(object : BaseCallback<ResponseList<Material>>(context) {
                                        override fun successData(data: ResponseList<Material>) {
                                            when {
                                                data.totalCount == 0 -> Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                                                data.totalCount > 1 -> {
                                                    val intent = Intent(context, MaterialSelectActivity::class.java)
                                                    intent.putExtra("code", barcode)
                                                    intent.putExtra("ckId", ckId)
                                                    intent.putExtra("data", data)
                                                    activity.startActivityForResult(intent, 0)
                                                }
                                                else -> {
                                                    val material = data.items[0]
                                                    activity.material = material
                                                    popupView.editText_tm.setText(material.ma_txm)
                                                    popupView.textView_no.text = material.ma_code
                                                    popupView.textView_no.text = material.ma_name
                                                    popupView.textView_kcnum.text = material.kc_num.toString()
                                                    popupView.textView_spec.text = material.ma_spec
                                                    popupView.textView_model.text = material.ma_model
                                                    popupView.textView_hw.text = material.kc_hw_name
                                                    popupView.textView_remark.text = material.comment
                                                    popupView.editText_num.requestFocus()
                                                    popupView.editText_num.setSelection(popupView.editText_num.text.toString().trim().length)
                                                }
                                            }
                                        }
                                    })
                        }
                        is WlckActivity -> {

                        }
                    }
                }
                else -> Toast.makeText(context, code.toString(), Toast.LENGTH_SHORT).show()
            }
            return@setOnEditorActionListener true
        })
        popupView.editText_num.setOnEditorActionListener({ _, code, _ ->
            when (code) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {  // 点击完成按钮或虚拟键
                    when (activity) {
                        is WlrkActivity -> {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            Toast.makeText(context, imm.isActive.toString(), Toast.LENGTH_SHORT).show()
                            // 隐藏软键盘
                            imm.hideSoftInputFromWindow(popupView.editText_num.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                        }
                        is WlckActivity -> {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(popupView.editText_num.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                        }
                    }

                }
                else -> Toast.makeText(context, code.toString(), Toast.LENGTH_SHORT).show()
            }
            return@setOnEditorActionListener true
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_close -> this.dismiss()
            R.id.button_commit -> {
                if (popupView.button_commit.text == "确认") {
                    when (activity) {
                        is WlrkActivity -> {
                            if (activity.material == null) {
                                Toast.makeText(context, "请扫描物料", Toast.LENGTH_SHORT).show()
                                return
                            }
                            var hasMaterial = false
                            for (item in activity.jhrk.jhmx) {
                                if (item.ma_id == activity.material?.ma_id) {
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
                                    item.mx_num = num
                                    // 重新加载数据
                                    activity.listView_wl.adapter = activity.wlAdapter
                                    activity.wlAdapter.notifyDataSetChanged()
                                    clearData()
                                    hasMaterial = true
                                    break
                                }
                            }
                            if (!hasMaterial) {
                                Toast.makeText(context, "入库单不包含该物料，请扫描其它物料", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is WlckActivity -> {
                            if (activity.material == null) {
                                Toast.makeText(context, "请扫描物料", Toast.LENGTH_SHORT).show()
                                return
                            }
                            var hasMaterial = false
                            for (item in activity.ckd.ckdmx) {
                                if (item.ma_id == activity.material?.ma_id) {
                                    val numStr = popupView.editText_num.text.toString()
                                    if (numStr.trim().isBlank()) {
                                        Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                    val num = numStr.toInt()
                                    if (num > item.mx_num - item.wc_num) {
                                        Toast.makeText(context, "不能超过未领数量", Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                    Toast.makeText(context, "数量清点完毕", Toast.LENGTH_SHORT).show()
                                    item.ck_num = num
                                    // 重新加载数据
                                    activity.listView_wl.adapter = activity.wlAdapter
                                    activity.wlAdapter.notifyDataSetChanged()
                                    clearData()
                                    hasMaterial = true
                                    break
                                }
                            }
                            if (!hasMaterial) {
                                Toast.makeText(context, "领料单不包含该物料，请扫描其它物料", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else if (popupView.button_commit.text == "修改") {
                    val numStr = popupView.editText_num.text.toString()
                    if (numStr.trim().isBlank()) {
                        Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val num = numStr.toInt()
                    when (activity) {
                        is WlrkActivity -> {
                            if (num > activity.jhmx.jhdmx_num) {
                                Toast.makeText(context, "不能超过未入库数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            activity.jhmx.mx_num = num
                            activity.listView_wl.adapter = activity.wlAdapter
                            activity.wlAdapter.notifyDataSetChanged()
                        }
                        is WlckActivity -> {
                            if (num > activity.ckdmx.mx_num - activity.ckdmx.wc_num) {
                                Toast.makeText(context, "不能超过未领数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            activity.ckdmx.ck_num = num
                            activity.listView_wl.adapter = activity.wlAdapter
                            activity.wlAdapter.notifyDataSetChanged()
                        }
                    }
                    clearData()
                    this.dismiss()
                }
            }
        }
    }

    /***
     * 清空弹框数据
     */
    fun clearData() {
        popupView.editText_tm.setText("")
        popupView.textView_no.text = ""
        popupView.textView_no.text = ""
        popupView.editText_num.setText("")
        popupView.textView_kcnum.text = ""
        popupView.textView_spec.text = ""
        popupView.textView_hw.text = ""
        popupView.textView_remark.text = ""
    }
}