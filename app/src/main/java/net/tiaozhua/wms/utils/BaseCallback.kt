package net.tiaozhua.wms.utils

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.widget.Toast
import net.tiaozhua.wms.LoginActivity
import net.tiaozhua.wms.bean.ApiBean
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
* Created by ldp on 2017/11/8.
*/
abstract class BaseCallback<T>(private val context: Context) : Callback<ApiBean<T>> {

    override fun onResponse(call: Call<ApiBean<T>>?, response: Response<ApiBean<T>>?) {
        LoadingDialog.dismiss()
        if (response?.code() == 200) {
            response.body()?.let {
                when (it.code) {
                    0 -> {
                        if (it.data != null) {
                            failureData(it.data)
                        } else {
                            Toast.makeText(context, it.info ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> { // 正常
                        if (it.data != null) {
                            successData(it.data)
                        } else {
                            successInfo(it.info ?: "1")
                        }
                    }
                    2 -> { // 登录超时
                        DialogUtil.showAlert(context, "请重新登录",
                                DialogInterface.OnClickListener { _, _ ->
                                    context.startActivity(Intent(context, LoginActivity::class.java))
                                }
                        )
                    }
                    3 -> Toast.makeText(context, it.info, Toast.LENGTH_SHORT).show()    // 无权限
                    5 -> Toast.makeText(context, "已完成盘点，请勿重复提交！", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(context, it.info ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFailure(call: Call<ApiBean<T>>?, t: Throwable?) {
        LoadingDialog.dismiss()
        Toast.makeText(context, "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
    }

    protected open  fun successInfo(info: String) {}

    protected open  fun successData(data: T) {}

    protected open fun failureData(data: T) {}
}