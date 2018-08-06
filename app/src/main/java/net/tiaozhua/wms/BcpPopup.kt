package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.popup_scan_bcp.view.*
import razerdp.basepopup.BasePopupWindow

/**
* Created by ldp on 2017/10/27.
*/
class BcpPopup(private val activity: Activity) : BasePopupWindow(activity), View.OnClickListener {

    lateinit var popupView: View

    init {
        bindEvent()
    }

    fun setScan() {
        clearData()
//        popupView.editText_tm.isFocusableInTouchMode = true
//        popupView.editText_tm.isFocusable = true
        popupView.button_commit.text = "确认"
        when (activity) {
            is BcprkActivity -> {
                val rkmx = activity.bcpRkmx!!
                popupView.editText_tm.setText(rkmx.ma_code)
                popupView.textView_name.text = rkmx.ma_name
                popupView.textView_wrknum.text = rkmx.wrk_num.toString()
                popupView.editText_num.setText(rkmx.num.toString())
//                popupView.textView_hw.text = rkmx.hw
//                popupView.textView_remark.text = rkmx.remark
            }
        }
    }

    fun setUpdate() {
        popupView.button_commit.text = "修改"
        when (activity) {
            is BcprkActivity -> {
                val rkmx = activity.bcpRkmx!!
                popupView.editText_tm.setText(rkmx.ma_code)
                popupView.textView_name.text = rkmx.ma_name
                popupView.textView_wrknum.text = rkmx.wrk_num.toString()
                popupView.editText_num.setText(rkmx.num.toString())
//                popupView.textView_hw.text = rkmx.hw
//                popupView.textView_remark.text = rkmx.remark
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
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_scan_bcp, null)
        return popupView
    }

    override fun initAnimaView(): View {
        return popupView.findViewById(R.id.popup_anima)
    }

    private fun bindEvent() {
        popupView.button_close.setOnClickListener(this)
        popupView.button_commit.setOnClickListener(this)
//        popupView.editText_tm.setOnEditorActionListener { _, code, _ ->
//            when (code) {
//                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {  // 点击完成按钮或虚拟键
//                    val barcode = popupView.editText_tm.text.toString()
//                    when (activity) {
//
//                    }
//                }
//                else -> Toast.makeText(context, code.toString(), Toast.LENGTH_SHORT).show()
//            }
//            return@setOnEditorActionListener false      // 防止响应两次
//        }
        popupView.editText_num.setOnEditorActionListener { _, code, _ ->
            when (code) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {  // 点击完成按钮或虚拟键
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    // 隐藏软键盘
                    imm.hideSoftInputFromWindow(popupView.editText_num.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                }
                else -> Toast.makeText(context, code.toString(), Toast.LENGTH_SHORT).show()
            }
            return@setOnEditorActionListener true
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_close -> this.dismiss()
            R.id.button_commit -> {
                when (activity) {
                    is BcprkActivity -> {
                        val rkmx = activity.bcpRkmx!!
                        if (popupView.button_commit.text == "确认") {     // 点击确认按钮执行
                            val numStr = popupView.editText_num.text.toString()
                            if (numStr.isBlank() || numStr.toInt() == 0) {
                                Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            val num = numStr.toInt()
                            if (num > rkmx.wrk_num) {
                                Toast.makeText(context, "不能超过未入库数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            rkmx.num = num
                            activity.bcpRkd.bcpRkmxs.add(rkmx)
                        } else if (popupView.button_commit.text == "修改") {      // 点击修改按钮执行
                            val numStr = popupView.editText_num.text.toString()
                            if (numStr.isBlank() || numStr.toInt() == 0) {
                                Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            val num = numStr.toInt()
                            if (num > rkmx.wrk_num) {
                                Toast.makeText(context, "不能超过未入库数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            for (item in activity.bcpRkd.bcpRkmxs) {
                                if (item.ma_id == rkmx.ma_id) {
                                    item.num = numStr.toInt()
                                    item.wrk_num = popupView.textView_wrknum.text.toString().toInt()
                                    break
                                }
                            }
                        }
                        activity.bcpAdapter.notifyDataSetChanged()
                        clearData()
                        dismiss()
                    }
                }
            }
        }
    }

    /***
     * 清空弹框数据
     */
    private fun clearData() {
        popupView.editText_tm.text.clear()
        popupView.textView_name.text = ""
        popupView.textView_wrknum.text = ""
        popupView.editText_num.text.clear()
//        popupView.textView_hw.text = ""
//        popupView.textView_remark.text = ""
    }
}