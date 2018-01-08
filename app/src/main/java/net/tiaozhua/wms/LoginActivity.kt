package net.tiaozhua.wms

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import net.tiaozhua.wms.bean.ApiBean
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
* Created by ldp on 2017/11/7.
*/
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener {
            if (textView_no.text.isEmpty() || editText_password.text.isEmpty()) {
                return@setOnClickListener
            }
            LoadingDialog.show(this@LoginActivity)
            val call = RetrofitManager.instance.login(textView_no.text.toString(), editText_password.text.toString())
            call.enqueue(object : Callback<ApiBean<String>> {
                override fun onResponse(call: Call<ApiBean<String>>?, response: Response<ApiBean<String>>?) {
                    LoadingDialog.dismiss()
                    if (response?.body()?.code == 1) {
                        editText_password.text.clear()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        Toast.makeText(this@LoginActivity, response?.body()?.info ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiBean<String>>?, t: Throwable?) {
                    LoadingDialog.dismiss()
                    Toast.makeText(this@LoginActivity, "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

}
