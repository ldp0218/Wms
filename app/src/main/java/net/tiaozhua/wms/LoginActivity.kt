package net.tiaozhua.wms

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import net.tiaozhua.wms.bean.ApiBean
import net.tiaozhua.wms.bean.User
import net.tiaozhua.wms.utils.App
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
* Created by ldp on 2017/11/7.
*/
class LoginActivity : BaseActivity(R.layout.activity_login) {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        editText_name.setText(sharedPreferences.getString("userName", ""))

        // 修改ip地址
//        editText_ip.setText("192.168.99.137")
//        var ipText = editText_ip.text.toString()
//        editText_ip.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
//            val editText = v as EditText
//            if (ipText != editText.text.toString()) {
//                ipText = editText.text.toString()
//                Toast.makeText(this@LoginActivity, "IP已修改:" + ipText, Toast.LENGTH_SHORT).show()
//                // 可在 App 运行时,随时切换 BaseUrl (指定了 Domain-Name header 的接口)
//                RetrofitUrlManager.getInstance().putDomain("domain", "http://" + ipText);
//            }
//        }

//        editText_ip.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                ip = s.toString()
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//            }
//
//        })

        button_login.setOnClickListener {
            if (editText_name.text.isEmpty() || editText_password.text.isEmpty()) {
                return@setOnClickListener
            }
            LoadingDialog.show(this@LoginActivity)
            val call = RetrofitManager.instance.login(editText_name.text.toString(), editText_password.text.toString())
            call.enqueue(object : Callback<ApiBean<User>> {
                override fun onResponse(call: Call<ApiBean<User>>?, response: Response<ApiBean<User>>?) {
                    LoadingDialog.dismiss()
                    if (response?.body()?.code == 1) {
                        (application as App).user = response.body()?.data!!
                        val user = (application as App).user
                        val editor = sharedPreferences.edit()
                        editor.putString("userName", user!!.a_name)
                        editor.apply()
                        editText_password.text.clear()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        Toast.makeText(this@LoginActivity, response?.body()?.info ?: "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiBean<User>>?, t: Throwable?) {
                    LoadingDialog.dismiss()
                    Toast.makeText(this@LoginActivity, "服务繁忙，请稍后重试", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

}
