package net.tiaozhua.wms

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_user.*
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.bean.User
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager
import net.tiaozhua.wms.view.UsersItemView

class UserSelectActivity : BaseActivity(R.layout.activity_user), View.OnClickListener {

    private lateinit var responseList: ResponseList<User>
    private lateinit var items: MutableList<User>
    private lateinit var adapter: ArrayAdapter<User>
    lateinit var query: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        button_ok.setOnClickListener(this)
        button_cancel.setOnClickListener(this)
        button_search.setOnClickListener(this)
        query = editText_query.text.toString()
        getUsers(query)

        refreshLayout.isEnableRefresh = false
        refreshLayout.setOnLoadmoreListener { smartLayout ->
            if (responseList.totalPages > responseList.page) {
                smartLayout.layout.postDelayed({
                    RetrofitManager.instance.adminList(query,responseList.page + 1)
                            .enqueue(object : BaseCallback<ResponseList<User>>(this@UserSelectActivity) {
                                override fun successData(data: ResponseList<User>) {
                                    items.addAll(data.items)
                                    adapter.notifyDataSetChanged()
                                    responseList.page = data.page
                                    responseList.totalPages = data.totalPages
                                    smartLayout.finishLoadmore()
                                    if (responseList.totalPages == responseList.page) {
                                        smartLayout.isLoadmoreFinished = true//将不会再次触发加载更多事件
                                    }
                                }
                            })
                }, 1000)
            } else {
                smartLayout.finishLoadmore()
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.button_cancel -> finish()
            R.id.button_ok -> {
                if (listView_user.checkedItemPosition == ListView.INVALID_POSITION) {
                    Toast.makeText(this, "请选择领料人", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent()
                    intent.putExtra("user", items[listView_user.checkedItemPosition])
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            R.id.button_search -> {
                query = editText_query.text.toString()
                getUsers(query)
            }
        }
    }

    private fun getUsers(query: String) {
        LoadingDialog.show(this)
        RetrofitManager.instance.adminList(query)
                .enqueue(object : BaseCallback<ResponseList<User>>(this) {
                    override fun successData(data: ResponseList<User>) {
                        responseList = data
                        items = responseList.items.toMutableList()
                        adapter = object : ArrayAdapter<User>(this@UserSelectActivity, R.layout.listview_user_item, items) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = if (convertView == null) {
                                    UsersItemView(this@UserSelectActivity)
                                } else {
                                    convertView as UsersItemView
                                }
                                view.setValue(getItem(position))
                                return view
                            }
                        }
                        listView_user.adapter = adapter
                    }
                })
    }
}