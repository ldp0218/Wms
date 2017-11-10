package net.tiaozhua.wms

import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_materialselect.*
import net.tiaozhua.wms.bean.Material
import net.tiaozhua.wms.view.ChoiceView
import android.view.ViewGroup
import android.widget.ArrayAdapter
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.RetrofitManager

class MaterialSelectActivity : BaseActivity(R.layout.activity_materialselect) {
    private lateinit var responseList: ResponseList<Material>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val code = intent.getStringExtra("code")
        val ckId = intent.getIntExtra("ckId", 0)
        @Suppress("UNCHECKED_CAST")
        responseList = intent.getSerializableExtra("data") as ResponseList<Material>
        val items = responseList.items.toMutableList()
        val adapter = object : ArrayAdapter<Material>(this, R.layout.listview_material, items) {
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
            smartLayout.layout.postDelayed({
                if (responseList.totalPages > responseList.page) {
                    RetrofitManager.instance.materialList(code, ckId, responseList.page + 1)
                            .enqueue(object : BaseCallback<ResponseList<Material>>(this@MaterialSelectActivity) {
                                override fun success(data: ResponseList<Material>) {
                                    items.addAll(data.items)
                                    responseList.page = data.page
                                    responseList.totalPages = data.totalPages
                                    smartLayout.finishLoadmore()
                                    if (responseList.totalPages == responseList.page) {
                                        smartLayout.isLoadmoreFinished = true//将不会再次触发加载更多事件
                                    }
                                }
                            })
                }
            }, 1000)
        }
    }
}