package net.tiaozhua.wms

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_materialselect.*
import net.tiaozhua.wms.bean.Material
import net.tiaozhua.wms.view.ChoiceView
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager

class MaterialSelectActivity : BaseActivity(R.layout.activity_materialselect) {
    private lateinit var responseList: ResponseList<Material>
    private lateinit var adapter: ArrayAdapter<Material>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val code = intent.getStringExtra("code")
        val ckId = intent.getIntExtra("ckId", 0)
        @Suppress("UNCHECKED_CAST")
        responseList = intent.getSerializableExtra("data") as ResponseList<Material>
        val items = responseList.items.toMutableList()
        adapter = object : ArrayAdapter<Material>(this, R.layout.listview_material, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = if (convertView == null) {
                    ChoiceView(this@MaterialSelectActivity)
                } else {
                    convertView as ChoiceView
                }
                view.setTextView(getItem(position).ma_name)
                return view
            }
        }
        listview.adapter = adapter
        refreshLayout.isEnableRefresh = false
        refreshLayout.setOnLoadmoreListener { smartLayout ->
            if (responseList.totalPages > responseList.page) {
                smartLayout.layout.postDelayed({
                    RetrofitManager.instance.materialList(code, ckId, responseList.page + 1)
                            .enqueue(object : BaseCallback<ResponseList<Material>>(this@MaterialSelectActivity) {
                                override fun successData(data: ResponseList<Material>) {
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

        btn_return.setOnClickListener({ _ ->
            finish()
        })

        btn_confirm.setOnClickListener({ _ ->
            if (listview.checkedItemPosition == ListView.INVALID_POSITION) {
                Toast.makeText(this, "请选择物料", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent()
                intent.putExtra("material", items[listview.checkedItemPosition])
                setResult(1, intent)
                finish()
            }
        })
    }
}