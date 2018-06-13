package net.tiaozhua.wms

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import net.tiaozhua.wms.bean.ApiBean
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.EditText
import me.jessyan.retrofiturlmanager.RetrofitUrlManager

/**
* Created by ldp on 2017/11/7.
*/
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
