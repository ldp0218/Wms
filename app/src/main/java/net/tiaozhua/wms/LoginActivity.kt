package net.tiaozhua.wms

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
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
    }

    fun login(view: View) {
        val editTextName = findViewById(R.id.editText_name) as EditText
        val editTextPassword = findViewById(R.id.editText_password) as EditText
        if (editTextName.text.isEmpty() || editTextPassword.text.isEmpty()) {
            return
        }
        LoadingDialog.show(this@LoginActivity, false)
        val call = RetrofitManager.instance.login(editTextName.text.toString(), editTextPassword.text.toString())
        call.enqueue(object : Callback<ApiBean<String>> {
            override fun onResponse(call: Call<ApiBean<String>>?, response: Response<ApiBean<String>>?) {
                LoadingDialog.dismiss()
                if (response?.body()?.code == 1) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else {
                    Toast.makeText(this@LoginActivity, response?.body()?.info, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiBean<String>>?, t: Throwable?) {
                LoadingDialog.dismiss()
                Toast.makeText(this@LoginActivity, "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
            }

        })
    }
}
