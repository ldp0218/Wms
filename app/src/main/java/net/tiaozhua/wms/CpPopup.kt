package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.popup_scan_product.view.*
import net.tiaozhua.wms.bean.PdResult
import net.tiaozhua.wms.bean.Pdmx
import net.tiaozhua.wms.bean.Rkmx
import net.tiaozhua.wms.bean.ScanStatus
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import okhttp3.MediaType
import okhttp3.RequestBody
import razerdp.basepopup.BasePopupWindow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
* Created by ldp on 2017/10/27.
*/
class CpPopup(private val activity: Activity) : BasePopupWindow(activity), View.OnClickListener {

    lateinit var popupView: View

    init {
        bindEvent()
    }

    fun setScan() {
//        clearData()
        popupView.button_ok.text = "确认"
        when (activity) {
            is CprkActivity -> {
                val bz = activity.bz
                popupView.textView_proname.text = bz.pro_name
                popupView.textView_prokcnum.text = bz.kc_num.toString()
                popupView.layout_wwcnum.visibility = View.VISIBLE
                popupView.textView_wrknum.text = bz.wrk_num.toString()
                popupView.editText_pronum.setText(bz.check_num.toString())
                popupView.textView_scdno.text = bz.scd_no
                popupView.textView_bzcode.text = bz.bz_hao
                popupView.editText_pronum.requestFocus()
                popupView.editText_pronum.setSelection(popupView.editText_pronum.text.toString().trim().length)
            }
            is CppdmxActivity -> {
                val pdmx = activity.pdmx!!
                popupView.textView_proname.text = pdmx.name
                popupView.textView_prokcnum.text = pdmx.kc_num.toInt().toString()
                popupView.layout_wwcnum.visibility = View.GONE
                popupView.editText_pronum.setText(pdmx.pd_num.toInt().toString())
                popupView.textView_scdno.text = pdmx.scd_no
                popupView.textView_bzcode.text = pdmx.bz_hao
                popupView.editText_pronum.requestFocus()
                popupView.editText_pronum.setSelection(popupView.editText_pronum.text.toString().trim().length)
            }
        }
    }

    fun setUpdate() {
        popupView.button_ok.text = "修改"
        when (activity) {
            is CprkActivity -> {
                val bz = activity.rkmx!!
                popupView.textView_proname.text = bz.pro_name
                popupView.textView_prokcnum.text = bz.kc_num.toString()
                popupView.layout_wwcnum.visibility = View.VISIBLE
                popupView.textView_wrknum.text = bz.wrk_num.toString()
                popupView.editText_pronum.setText(bz.mx_num.toString())
                popupView.textView_scdno.text = bz.scd_no
                popupView.textView_bzcode.text = bz.bz_hao
                popupView.editText_pronum.requestFocus()
                popupView.editText_pronum.setSelection(popupView.editText_pronum.text.toString().trim().length)
            }
            is CppdmxActivity -> {
                val pdmx = activity.pdmx!!
                popupView.textView_proname.text = pdmx.name
                popupView.textView_prokcnum.text = pdmx.kc_num.toInt().toString()
                popupView.layout_wwcnum.visibility = View.GONE
                popupView.editText_pronum.setText(pdmx.pd_num.toInt().toString())
                popupView.textView_scdno.text = pdmx.scd_no
                popupView.textView_bzcode.text = pdmx.bz_hao
                popupView.editText_pronum.requestFocus()
                popupView.editText_pronum.setSelection(popupView.editText_pronum.text.toString().trim().length)
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
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_scan_product, null)
        return popupView
    }

    override fun initAnimaView(): View {
        return popupView.findViewById(R.id.popup_anima)
    }

    private fun bindEvent() {
        popupView.button_close.setOnClickListener(this)
        popupView.button_ok.setOnClickListener(this)
        popupView.editText_pronum.setOnEditorActionListener { _, code, _ ->
            when (code) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {  // 点击完成按钮或虚拟键
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    // 隐藏软键盘
                    imm.hideSoftInputFromWindow(popupView.editText_pronum.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                }
                else -> Toast.makeText(context, code.toString(), Toast.LENGTH_SHORT).show()
            }
            return@setOnEditorActionListener true
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_close -> dismiss()
            R.id.button_ok -> {
                val numStr = popupView.editText_pronum.text.toString()
                if (numStr.isBlank() || numStr.toInt() == 0) {
                    Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                    return
                }
                val num = numStr.toInt()
                if (popupView.button_ok.text == "确认") {     // 点击确认按钮执行
                    when (activity) {
                        is CprkActivity -> {
                            val bz = activity.bz
                            if (num > bz.wrk_num) {
                                Toast.makeText(context, "不能超过未入库数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            Toast.makeText(context, "数量清点完毕", Toast.LENGTH_SHORT).show()
                            bz.check_num = num
//                            if (bz.yx == 1) {   // 记录异形，保证整套入库
//                                for (item in activity.rkmxList) {
//                                    if (item.scd_no == bz.scd_no) {
//                                        item.check_num += bz.check_num
//                                        find = true
//                                        break
//                                    }
//                                }
//                                if (!find) {
//                                    activity.rkmxList.add(bz)
//                                }
//                                activity.rkd.rkmx.add(Rkmx(bz.xsd_bz_id, bz.check_num, bz.scd_no, bz.pro_name, bz.bz_id, bz.bz_code, bz.bz_hao, bz.pro_id))
//                            } else {    // 常规记录重复件数
                            var find = false
                            for (item in activity.rkd.rkmx) {
                                if (item.bz_id == bz.bz_id) {
                                    item.mx_num += bz.check_num
                                    find = true
                                    break
                                }
                            }
                            if (!find) {
                                activity.rkd.rkmx.add(Rkmx(bz.xsd_bz_id, bz.check_num, bz.scd_no, bz.pro_name, bz.bz_id, bz.bz_code, bz.bz_hao, bz.pro_id, bz.kc_num, bz.wrk_num))
                            }
                            if (activity.status == ScanStatus.EMPTY) {  // 如果还未开始扫描修改状态为扫描
                                activity.status = ScanStatus.SCAN
                            }
                            activity.cpAdapter.notifyDataSetChanged()
                            clearData()
                            dismiss()
                        }
                        is CppdmxActivity -> {
                            activity.pdmx!!.pd_num = num.toDouble()
                            LoadingDialog.show(context)
                            val json = Gson().toJson(activity.pdmx)
                            val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                            RetrofitManager.instance.updatePdmx(requestBody).enqueue(object : Callback<PdResult> {
                                override fun onResponse(call: Call<PdResult>?, response: Response<PdResult>?) {
                                    LoadingDialog.dismiss()
                                    if (response?.code() == 200) {
                                        val result = response.body()
                                        when (result?.code) {
                                            0 -> {
                                                Toast.makeText(context, result.info
                                                        ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                                            }
                                            1 -> { // 正常
                                                activity.productList.add(activity.barcodeStr)
                                                Toast.makeText(context, "已记录", Toast.LENGTH_SHORT).show()
                                                activity.pdmx!!.pd_id = result.pd_id
                                                activity.pdmx!!.id = result.id
                                                activity.pdmxList.add(activity.pdmx!!)
                                                activity.pdAdapter.notifyDataSetChanged()
                                                activity.pdmx = Pdmx(1, result.pd_id, null, 0.0, 0.0, null, 0, "", "",
                                                        "", "", "", "", "", "", "", "", 0)
                                                dismiss()
                                            }
                                            2 -> { // 登录超时
                                                DialogUtil.showAlert(context, "请重新登录",
                                                        DialogInterface.OnClickListener { _, _ ->
                                                            (context as Activity).finish()
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
                } else if (popupView.button_ok.text == "修改") {      // 点击修改按钮执行
                    when (activity) {
                        is CprkActivity -> {
                            val bz = activity.rkmx!!
                            if (num > bz.wrk_num) {
                                Toast.makeText(context, "不能超过未入库数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            for (item in activity.rkd.rkmx) {
                                if (item.bz_id == bz.bz_id) {
                                    item.mx_num = num
                                    break
                                }
                            }
                            Toast.makeText(context, "数量已修改", Toast.LENGTH_SHORT).show()
                            activity.cpAdapter.notifyDataSetChanged()
                            clearData()
                            dismiss()
                        }
                        is CppdmxActivity -> {
                            activity.pdmx!!.pd_num = num.toDouble()
                            LoadingDialog.show(context)
                            val json = Gson().toJson(activity.pdmx)
                            val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                            RetrofitManager.instance.updatePdmx(requestBody).enqueue(object : Callback<PdResult> {
                                override fun onResponse(call: Call<PdResult>?, response: Response<PdResult>?) {
                                    LoadingDialog.dismiss()
                                    if (response?.code() == 200) {
                                        val result = response.body()
                                        when (result?.code) {
                                            0 -> {
                                                Toast.makeText(context, result.info
                                                        ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                                            }
                                            1 -> { // 正常
                                                Toast.makeText(context, "已修改", Toast.LENGTH_SHORT).show()
                                                for (item in activity.pdmxList) {
                                                    if (item.id == activity.pdmx!!.id) {
                                                        item.pd_num = activity.pdmx!!.pd_num
                                                    }
                                                }
                                                activity.pdAdapter.notifyDataSetChanged()
                                                activity.pdmx = Pdmx(1, result.pd_id, null, 0.0, 0.0, null, 0, "", "",
                                                        "", "", "", "", "", "", "", "", 0)
                                                dismiss()
                                            }
                                            2 -> { // 登录超时
                                                DialogUtil.showAlert(context, "请重新登录",
                                                        DialogInterface.OnClickListener { _, _ ->
                                                            (context as Activity).finish()
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
                }
            }
        }
    }

    /***
     * 清空弹框数据
     */
    private fun clearData() {
        popupView.textView_proname.text = ""
        popupView.editText_pronum.text.clear()
        popupView.textView_wrknum.text = ""
        popupView.textView_prokcnum.text = ""
    }
}