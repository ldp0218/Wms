package net.tiaozhua.wms.utils

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.widget.Toast
import net.tiaozhua.wms.bean.ApiBean
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import net.tiaozhua.wms.LoginActivity
import net.tiaozhua.wms.RkdActivity
import net.tiaozhua.wms.bean.Jhrk


/**
 * Created by ldp on 2017/11/8.
 */
abstract class BaseCallback<T>(context: Context) : Callback<ApiBean<T>> {

    private val context: Context = context

    override fun onResponse(call: Call<ApiBean<T>>?, response: Response<ApiBean<T>>?) {
        response?.body()?.let {
            when (it.code) {
                1 -> { // 正常
                    success(it.data)
                }
                2 -> { // 登录超时
                    DialogUtil.showAlert(context, "请重新登录",
                            DialogInterface.OnClickListener { _, _ ->
                                context.startActivity(Intent(context, LoginActivity::class.java))
                            }
                    )
                }
                3 -> { // 无权限
                    Toast.makeText(context, it.info, Toast.LENGTH_SHORT).show()
                }
                else -> Toast.makeText(context, it.info, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onFailure(call: Call<ApiBean<T>>?, t: Throwable?) {
        Toast.makeText(context, "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
    }

    abstract fun success(data: T)


}