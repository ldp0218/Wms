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
import kotlinx.android.synthetic.main.popup_scan_material.view.*
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
* Created by ldp on 2017/10/27.
*/
class WlPopup(private val activity: Activity) : BasePopupWindow(activity), View.OnClickListener {

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
                popupView.editText_tm.setText(m.ma_code)
                popupView.textView_name.text = m.ma_name
                popupView.editText_num.setText(m.mx_num.toString())
                popupView.editText_num.requestFocus()
                popupView.textView_kcnum.text = m.kcnum.toString()
                popupView.editText_price.setText(m.mx_price.toString())
                popupView.textView_kind.text = m.ma_kind
                popupView.textView_hw.text = m.hj_name
                popupView.textView_remark.text = m.mx_remark
            }
            is WlckActivity -> {
                popupView.button_commit.text = "确认"
                val wl = activity.material
                if (wl != null) {
                    popupView.editText_tm.setText(wl.ma_code)
                    popupView.textView_name.text = wl.ma_name
                    popupView.textView_kcnum.text = wl.kc_num.toString()
                    popupView.textView_cknum.text = wl.ck_num.toString()
                    popupView.textView_kind.text = wl.ma_kind_name
                    popupView.textView_hw.text = wl.kc_hw_name
                    popupView.textView_remark.text = wl.comment
                    popupView.editText_num.requestFocus()
                    popupView.editText_num.setSelection(popupView.editText_num.text.toString().trim().length)
                }
            }
            is KcpdActivity -> {
                val m = activity.pdmx
                popupView.editText_tm.setText(m.txm)
                popupView.textView_name.text = m.name
                popupView.editText_num.setText(m.pd_num.toString())
                popupView.editText_num.requestFocus()
                popupView.editText_num.setSelection(popupView.editText_num.text.toString().trim().length)
                popupView.textView_kcnum.text = m.kc_num.toString()
                popupView.textView_kind.text = m.kind
                popupView.textView_hw.text = m.hw
                popupView.textView_remark.text = m.comment
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
        when (activity) {
            is WlrkActivity -> {
                popupView.layout_cknum.visibility = View.GONE
                popupView.layout_price.visibility = View.VISIBLE
            }
            is WlckActivity -> {
                popupView.layout_cknum.visibility = View.VISIBLE
                popupView.layout_price.visibility = View.GONE
            }
            is KcpdActivity -> {
                popupView.layout_cknum.visibility = View.GONE
                popupView.layout_price.visibility = View.GONE
            }
        }
        popupView.button_close.setOnClickListener(this)
        popupView.button_commit.setOnClickListener(this)
        popupView.editText_tm.setOnEditorActionListener { _, code, _ ->
            when (code) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {  // 点击完成按钮或虚拟键
                    val barcode = popupView.editText_tm.text.toString()
                    when (activity) {
                        is WlrkActivity -> {
                            val ckId = activity.jhd.ck_id
                            LoadingDialog.show(activity)
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
                                                    popupView.textView_name.text = material.ma_name
                                                    popupView.textView_kcnum.text = material.kc_num.toString()
                                                    popupView.textView_kind.text = material.ma_kind_name
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
                        is KcpdActivity -> {
                            val ckId = activity.ckId
                            LoadingDialog.show(activity)
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
                                                    activity.pdmx.iid = material.ma_id
                                                    activity.pdmx.kc_num = material.kc_num
                                                    activity.pdmx.hao = material.ma_code
                                                    activity.pdmx.txm = material.ma_txm
                                                    activity.pdmx.name = material.ma_name
                                                    activity.pdmx.kind = material.ma_kind_name
                                                    activity.pdmx.spec = material.ma_spec ?: ""
                                                    activity.pdmx.hw = material.kc_hw_name
                                                    activity.pdmx.comment = material.comment ?: ""
                                                    popupView.editText_tm.setText(material.ma_txm)
                                                    popupView.textView_name.text = material.ma_name
                                                    popupView.textView_kcnum.text = material.kc_num.toString()
                                                    popupView.textView_kind.text = material.ma_kind_name
                                                    popupView.textView_hw.text = material.kc_hw_name
                                                    popupView.textView_remark.text = material.comment
                                                    popupView.editText_num.requestFocus()
                                                    popupView.editText_num.setSelection(popupView.editText_num.text.toString().trim().length)
                                                    activity.pdmx.ck_id = activity.ckId
                                                    // 先查询是否已盘点
                                                    if (activity.pdmx.pd_id != null) {
                                                        LoadingDialog.show(activity)
                                                        val json = Gson().toJson(activity.pdmx)
//                                                        val json = ObjectMapper().writeValueAsBytes(activity.pdmx)
                                                        val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                                                        RetrofitManager.instance.selPdmx(requestBody)
                                                                .enqueue(object : BaseCallback<Pdmx>(context) {
                                                                    override fun successData(data: Pdmx) {
                                                                        popupView.editText_num.setText(data.pd_num.toString())
                                                                        activity.pdmx.id = data.id   // 设置盘点明细ID，修改需要此参数
                                                                        popupView.editText_tm.isFocusable = false
                                                                        popupView.editText_tm.isFocusableInTouchMode = false
                                                                        popupView.button_commit.text = "修改"
                                                                    }
                                                                })
                                                    }
                                                }
                                            }
                                        }
                                    })
                        }
                    }
                }
                else -> Toast.makeText(context, code.toString(), Toast.LENGTH_SHORT).show()
            }
            return@setOnEditorActionListener false      // 防止响应两次
        }
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
            R.id.button_close -> dismiss()
            R.id.button_commit -> {
                if (popupView.button_commit.text == "确认") {     // 点击确认按钮执行
                    when (activity) {
                        is WlrkActivity -> {
                            if (popupView.editText_tm.text.toString().isBlank()) {
                                Toast.makeText(context, "请扫描物料", Toast.LENGTH_SHORT).show()
                                return
                            }
                            var hasMaterial = false
                            for (item in activity.jhd.jhmx!!) {
                                if (item.ma_id == activity.material?.ma_id) {
                                    val numStr = popupView.editText_num.text.toString()
                                    val priceStr = popupView.editText_price.text.toString()
                                    if (numStr.isBlank()) {
                                        Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                    if (priceStr.isBlank()) {
                                        Toast.makeText(context, "请输入进价", Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                    val num = numStr.toDouble()
                                    if (num > item.mx_wwcnum) {
                                        Toast.makeText(context, "不能超过未入库数量", Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                    Toast.makeText(context, "数量清点完毕", Toast.LENGTH_SHORT).show()
                                    item.mx_num = num
                                    item.mx_price = priceStr.toDouble()
                                    // 重新加载数据
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
                            if (popupView.editText_tm.text.toString().isBlank()) {
                                Toast.makeText(context, "请扫描物料", Toast.LENGTH_SHORT).show()
                                return
                            }
                            val numStr = popupView.editText_num.text.toString()
                            var hasMaterial = false
                            for (item in activity.wlckList) {
                                if (item.ma_id == activity.material!!.ma_id) {
                                    if (numStr.isBlank() || numStr.toDouble() == 0.0) {
                                        Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                    val num = numStr.toDouble()
                                    if (num != item.num) {
                                        Toast.makeText(context, "数量不一致", Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                    item.scanNum = num
                                    hasMaterial = true
                                    break
                                }
                            }
                            if (!hasMaterial) {
                                val ma = activity.material!!
                                val num = numStr.toDouble()
                                activity.wlckList.add(Wlck(ma.ma_id, ma.kc_num, ma.kc_hw_name, num, num, "", ma.ma_name, ma.ma_code,
                                        ma.ma_spec, ma.ma_kind_name, ma.ma_unit, 0, 0, null))
                            }
                            // 重新加载数据
                            activity.adapter.notifyDataSetChanged()
                            clearData()
                            dismiss()
                        }
                        is KcpdActivity -> {
                            if (popupView.editText_tm.text.toString().isBlank()) {
                                Toast.makeText(context, "请扫描物料", Toast.LENGTH_SHORT).show()
                                return
                            }
                            val numStr = popupView.editText_num.text.toString()
                            if (numStr.isBlank()) {
                                Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            LoadingDialog.show(context)
                            activity.pdmx.id = null
                            activity.pdmx.pd_num = numStr.toDouble()
                            val json = Gson().toJson(activity.pdmx)
//                            val json = ObjectMapper().writeValueAsBytes(activity.pdmx)
                            val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                            RetrofitManager.instance.updatePdmx(requestBody)
                                    .enqueue(object : Callback<PdResult> {
                                        override fun onResponse(call: Call<PdResult>?, response: Response<PdResult>?) {
                                            LoadingDialog.dismiss()
                                            val data = response?.body()
                                            when (data?.code) {
                                                0 -> {
//                                                    if (data.info.equals("此产品已盘点，如需要，请修改！")) {
//                                                        activity.pdAdapter.notifyDataSetChanged()
//                                                        activity.pdmx = Pdmx(0, activity.pdmx.pd_id, null, 0, 0, null, 0, "", "",
//                                                                "", "", "", "", "")
//                                                    } else {
                                                        Toast.makeText(context, data.info ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
//                                                    }
                                                }
                                                1 -> { // 正常
                                                    Toast.makeText(context, "已记录", Toast.LENGTH_SHORT).show()
                                                    activity.pdmx.pd_id = activity.pdmx.pd_id ?: data.pd_id
                                                    activity.pdmx.id = data.id
                                                    activity.pdmxList.add(activity.pdmx)
                                                    activity.pdAdapter.notifyDataSetChanged()
                                                    activity.pdmx = Pdmx(0, activity.pdmx.pd_id, null, 0.0, 0.0, null, activity.pdmx.ck_id, "", "",
                                                            "", "", "", "", "", "","","",0)
                                                    clearData()
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
                                                    Toast.makeText(context, data.info, Toast.LENGTH_SHORT).show()
                                                }
                                                else -> Toast.makeText(context, data?.info, Toast.LENGTH_SHORT).show()
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
                    val numStr = popupView.editText_num.text.toString()
                    if (numStr.trim().isBlank() || numStr.toDouble() == 0.0) {
                        Toast.makeText(context, "请输入数量", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val num = numStr.toDouble()
                    when (activity) {
                        is WlrkActivity -> {
                            if (num > activity.jhmx.mx_wwcnum) {
                                Toast.makeText(context, "不能超过未入库数量", Toast.LENGTH_SHORT).show()
                                return
                            }
                            activity.jhmx.mx_num = num
                            activity.wlAdapter.notifyDataSetChanged()
                        }
                        is WlckActivity -> {
//                            if (num > activity.ckdmx.mx_num - activity.ckdmx.wc_num) {
//                                Toast.makeText(context, "不能超过未领数量", Toast.LENGTH_SHORT).show()
//                                return
//                            }
//                            activity.ckdmx.ck_num = num
//                            activity.wlAdapter.notifyDataSetChanged()
                        }
                        is KcpdActivity -> {
                            LoadingDialog.show(context)
                            activity.pdmx.pd_num = num
                            val json = Gson().toJson(activity.pdmx)
//                            val json = ObjectMapper().writeValueAsBytes(activity.pdmx)
                            val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
                            RetrofitManager.instance.updatePdmx(requestBody)
                                    .enqueue(object : Callback<PdResult> {
                                        override fun onResponse(call: Call<PdResult>?, response: Response<PdResult>?) {
                                            LoadingDialog.dismiss()
                                            when (response?.body()?.code) {
                                                0 -> {
                                                    Toast.makeText(context, response.body()?.info, Toast.LENGTH_SHORT).show()
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
                                                    activity.pdmx = Pdmx(0, activity.pdmx.pd_id, null, 0.0, 0.0, null, activity.pdmx.ck_id, "", "",
                                                            "", "", "", "", "", "","","",0)
                                                    clearData()
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
                                                    Toast.makeText(context, response.body()?.info, Toast.LENGTH_SHORT).show()
                                                }
                                                else -> Toast.makeText(context, response?.body()?.info, Toast.LENGTH_SHORT).show()
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
                    dismiss()
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
        popupView.textView_cknum.text = ""
        popupView.textView_kcnum.text = ""
        popupView.editText_num.text.clear()
        popupView.editText_price.text.clear()
        popupView.textView_kind.text = ""
        popupView.textView_hw.text = ""
        popupView.textView_remark.text = ""
    }
}