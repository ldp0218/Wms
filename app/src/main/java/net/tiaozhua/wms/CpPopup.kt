package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.popup_scan_product.view.*
import net.tiaozhua.wms.bean.*
import net.tiaozhua.wms.utils.BaseCallback
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
* Created by ldp on 2017/12/27.
*/
class CpPopup(private val activity: Activity) : BasePopupWindow(activity), View.OnClickListener {

    internal lateinit var popupView: View

    init {
        bindEvent()
    }

    fun setScan() {
        clearData()
        popupView.editText_protm.isFocusableInTouchMode = true
        popupView.editText_protm.isFocusable = true
        popupView.button_commit.visibility = View.VISIBLE
        popupView.button_commit.text = "确认"
        when (activity) {
            is CprkActivity -> {
                popupView.textView_kclabel.visibility = View.GONE
                popupView.textView_prokcnum.visibility = View.GONE
                popupView.layout_hw.visibility = View.GONE
            }
        }
    }

    fun setUpdate() {
        popupView.editText_protm.isFocusable = false
        popupView.editText_protm.isFocusableInTouchMode = false
        popupView.button_commit.text = "修改"
        when (activity) {
            is CprkActivity -> {
                popupView.textView_kclabel.visibility = View.GONE
                popupView.textView_prokcnum.visibility = View.GONE
                popupView.layout_hw.visibility = View.GONE
                val p = activity.rkmx
                popupView.editText_protm.setText(p.xsd_bz_id.toString())
                popupView.textView_scdno.text = p.scd_no
                popupView.textView_proname.text = p.pro_name
                popupView.textView_bzcode.text = p.bz_code
                popupView.editText_pronum.setText(p.mx_num.toString())
                popupView.editText_pronum.requestFocus()
                popupView.editText_pronum.setSelection(popupView.editText_pronum.text.toString().trim().length)
                popupView.editText_remark.setText(p.mx_remark)
            }
            is KcpdActivity -> {
                val p = activity.pdmx
                popupView.editText_protm.setText(p.xsd_bz_id.toString())
                popupView.textView_scdno.text = p.scd_no
                popupView.textView_proname.text = p.name
                popupView.textView_bzcode.text = p.bz_code
                popupView.textView_prokcnum.text = p.kc_num.toString()
                popupView.editText_pronum.setText(p.pd_num.toString())
                popupView.editText_pronum.requestFocus()
                popupView.editText_pronum.setSelection(popupView.editText_pronum.text.toString().trim().length)
                popupView.editText_remark.setText(p.comment)
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
        popupView.button_commit.setOnClickListener(this)
        popupView.editText_protm.setOnEditorActionListener({ _, code, _ ->
            when (code) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {  // 点击完成按钮或虚拟键
                    val barcode = popupView.editText_protm.text.toString()
                    LoadingDialog.show(activity)
                    val id = try {
                        barcode.toInt()
                    } catch (e: NumberFormatException) {
                        0
                    }
                    if (id == 0) {
                        Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                    } else {
                        when (activity) {
                            is CprkActivity -> {
                                RetrofitManager.instance.getProductRkInfo(id)
                                        .enqueue(object : BaseCallback<Rkmx>(context) {
                                            override fun successData(data: Rkmx) {
                                                activity.rkmx = data
                                                popupView.textView_scdno.text = activity.rkmx.scd_no
                                                popupView.textView_proname.text = activity.rkmx.pro_name
                                                popupView.textView_bzcode.text = activity.rkmx.bz_code
                                                popupView.editText_pronum.requestFocus()
                                                popupView.editText_pronum.setSelection(popupView.editText_pronum.text.toString().trim().length)
                                                DialogUtil.showInputMethod(context, popupView.editText_pronum, true, 100)
                                            }

                                            override fun successInfo(info: String) {
                                                Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                            }
                            is KcpdActivity -> {
                                RetrofitManager.instance.productList(id, activity.ckId)
                                        .enqueue(object : BaseCallback<ResponseList<Product>>(context) {
                                            override fun successData(data: ResponseList<Product>) {
                                                when {
                                                    data.totalCount == 0 -> Toast.makeText(context, "未查询到相关信息", Toast.LENGTH_SHORT).show()
                                                    data.totalCount > 1 -> {
                                                    }
                                                    else -> {
                                                        val pdmx = activity.pdmx
                                                        val product = data.items[0]
                                                        pdmx.txm = barcode
                                                        pdmx.iid = product.pro_id
                                                        pdmx.kc_num = product.kc_num
                                                        pdmx.bz_code = product.bz_code
                                                        pdmx.scd_no = product.scd_no
                                                        pdmx.hao = product.pro_code
                                                        pdmx.ck_id = activity.ckId
                                                        pdmx.name = product.pro_name
                                                        pdmx.spec = product.pro_spec
                                                        pdmx.model = product.pro_model
                                                        pdmx.hw = product.kc_hw_name
                                                        popupView.textView_proname.text = pdmx.name
                                                        popupView.textView_bzcode.text = pdmx.bz_code
                                                        popupView.textView_scdno.text = pdmx.scd_no
                                                        popupView.textView_prokcnum.text = pdmx.kc_num.toString()
                                                        popupView.editText_prohw.setText(pdmx.hw)
                                                        popupView.editText_pronum.requestFocus()
                                                        popupView.editText_pronum.setSelection(popupView.editText_pronum.text.toString().trim().length)
                                                        DialogUtil.showInputMethod(context, popupView.editText_pronum, true, 100)
                                                        // 扫描后为pdmx设值
                                                    }
                                                }
                                            }
                                        })
                            }
                        }
                    }
                }
                else -> Toast.makeText(context, code.toString(), Toast.LENGTH_SHORT).show()
            }
            return@setOnEditorActionListener false      // 防止响应两次
        })
        popupView.editText_pronum.setOnEditorActionListener({ _, code, _ ->
            when (code) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {  // 点击完成按钮或虚拟键
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    // 隐藏软键盘
                    imm.hideSoftInputFromWindow(popupView.editText_pronum.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
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
                if (popupView.button_commit.text == "确认") {     // 点击确认按钮执行
                    when (activity) {
                        is CprkActivity -> {
                            if (popupView.editText_protm.text.isBlank()) {
                                Toast.makeText(context, "请扫描二维码", Toast.LENGTH_SHORT).show()
                                return
                            }
                            val scdNo = popupView.textView_scdno.text.toString()
                            if (scdNo.isNotBlank()) {
                                if (activity.status == ScanStatus.EMPTY) {  // 如果还未开始扫描修改状态为扫描
                                    activity.status = ScanStatus.SCAN
                                }
                                val id = popupView.editText_protm.text.toString().toInt()
                                val num = popupView.editText_pronum.text.toString().toInt()
                                val remark = popupView.editText_remark.text.toString()
                                val proName = popupView.textView_proname.text.toString()
                                val bzCode = popupView.textView_bzcode.text.toString()
                                Toast.makeText(context, "数量清点完毕", Toast.LENGTH_SHORT).show()
                                activity.rkd.rkmx.add(Rkmx(id, num, remark, scdNo, proName, bzCode))
                                // 重新加载数据
                                activity.cpAdapter.notifyDataSetChanged()
                                clearData()
                            } else {
                                Toast.makeText(context, "请扫描其它二维码", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is KcpdActivity -> {
                            if (popupView.editText_protm.text.toString().isBlank()) {
                                Toast.makeText(context, "请扫描二维码", Toast.LENGTH_SHORT).show()
                                return
                            }
                            val numStr = popupView.editText_pronum.text.toString()
                            if (numStr.isBlank()) {
                                Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            LoadingDialog.show(context)
                            activity.pdmx.id = null
                            activity.pdmx.pd_num = numStr.toInt()
                            val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(activity.pdmx))
                            RetrofitManager.instance.updatePdmx(requestBody).enqueue(object : Callback<PdResult> {
                                        override fun onResponse(call: Call<PdResult>?, response: Response<PdResult>?) {
                                            LoadingDialog.dismiss()
                                            if (response?.code() == 200) {
                                                val data = response.body()
                                                when (data?.code) {
                                                    0 -> {
//                                                        if (data.info.equals("此产品已盘点，如需要，请修改！")) {
//                                                            activity.pdmxList.add(activity.pdmx)
//                                                            activity.pdAdapter.notifyDataSetChanged()
//                                                            activity.pdmx = Pdmx(0, activity.pdmx.pd_id, null, 0, 0, null, 0, "", "",
//                                                                    "", "", "", "", "")
//                                                        } else {
                                                            Toast.makeText(context, data.info ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
//                                                        }
                                                    }
                                                    1 -> { // 正常
                                                        Toast.makeText(context, "已记录", Toast.LENGTH_SHORT).show()
                                                        activity.pdmx.pd_id = data.pd_id
                                                        activity.pdmx.id = data.id
                                                        activity.pdmxList.add(activity.pdmx)
                                                        activity.pdAdapter.notifyDataSetChanged()
                                                        activity.pdmx = Pdmx(0, activity.pdmx.pd_id, null, 0, 0, null, 0, "", "",
                                                                "", "", "", "", "")
                                                        clearData()
                                                    }
                                                    2 -> { // 登录超时
                                                        DialogUtil.showAlert(context, "请重新登录",
                                                                DialogInterface.OnClickListener { _, _ ->
                                                                    context.startActivity(Intent(context, LoginActivity::class.java))
                                                                }
                                                        )
                                                    }
                                                    3 -> { // 无权限
                                                        Toast.makeText(context, data.info, Toast.LENGTH_SHORT).show()
                                                    }
                                                    else -> Toast.makeText(context, data?.info, Toast.LENGTH_SHORT).show()
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
                } else if (popupView.button_commit.text == "修改") {      // 点击修改按钮执行
                    val numStr = popupView.editText_pronum.text.toString()
                    if (numStr.trim().isBlank() || numStr.toInt() == 0) {
                        Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val num = numStr.toInt()
                    when (activity) {
                        is CprkActivity -> {
                            Toast.makeText(context, "数量清点完毕", Toast.LENGTH_SHORT).show()
                            activity.rkmx.mx_num = num
                            activity.cpAdapter.notifyDataSetChanged()
                        }
                        is KcpdActivity -> {
                            LoadingDialog.show(context)
                            activity.pdmx.pd_num = num
                            Log.i("json", Gson().toJson(activity.pdmx))
                            val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson().toJson(activity.pdmx))
                            RetrofitManager.instance.updatePdmx(requestBody).enqueue(object : Callback<PdResult> {
                                override fun onResponse(call: Call<PdResult>?, response: Response<PdResult>?) {
                                    Log.i("data", response?.body().toString())
                                    LoadingDialog.dismiss()
                                    if (response?.code() == 200) {
                                        val data = response.body()
                                        when (data?.code) {
                                            0 -> {
                                                Toast.makeText(context, data.info ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                                            }
                                            1 -> { // 正常
                                                var flag = false
                                                for (item in activity.pdmxList) {
                                                    if (item.hao == activity.pdmx.hao) {
                                                        item.pd_num = activity.pdmx.pd_num
                                                        flag = true
                                                        break
                                                    }
                                                }
                                                if (!flag) {
                                                    activity.pdmxList.add(activity.pdmx)
                                                }
                                                activity.pdAdapter.notifyDataSetChanged()
                                                activity.pdmx = Pdmx(0, activity.pdmx.pd_id, null, 0, 0, null, 0, "", "",
                                                        "", "", "", "", "")
                                                clearData()
                                            }
                                            2 -> { // 登录超时
                                                DialogUtil.showAlert(context, "请重新登录",
                                                        DialogInterface.OnClickListener { _, _ ->
                                                            context.startActivity(Intent(context, LoginActivity::class.java))
                                                        }
                                                )
                                            }
                                            3 -> { // 无权限
                                                Toast.makeText(context, data.info, Toast.LENGTH_SHORT).show()
                                            }
                                            else -> Toast.makeText(context, data?.info, Toast.LENGTH_SHORT).show()
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
                    clearData()
                    this.dismiss()
                }
            }
        }
    }

    /***
     * 清空弹框数据
     */
    private fun clearData() {
        popupView.editText_protm.text.clear()
        popupView.textView_scdno.text = ""
        popupView.textView_proname.text = ""
        popupView.textView_bzcode.text = ""
        popupView.editText_pronum.text.clear()
        popupView.textView_prokcnum.text = ""
        popupView.editText_prohw.text.clear()
        popupView.editText_remark.text.clear()
    }
}