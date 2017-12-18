package net.tiaozhua.wms

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.SimpleAdapter
import kotlinx.android.synthetic.main.activity_main.*

import java.util.ArrayList
import java.util.HashMap

class MainActivity : BaseActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isShowBacking = false
        toolbarTitle.text = "米兰仓库管理"

        val itemList = ArrayList<Map<String, Any>>()
        itemList.add(object : HashMap<String, Any>() {
            init {
                put("image", R.mipmap.wlrk)
                put("text", "物料入库")
            }
        })
        itemList.add(object : HashMap<String, Any>() {
            init {
                put("image", R.mipmap.cprk)
                put("text", "成品入库")
            }
        })
        itemList.add(object : HashMap<String, Any>() {
            init {
                put("image", R.mipmap.wlck)
                put("text", "物料出库")
            }
        })
        itemList.add(object : HashMap<String, Any>() {
            init {
                put("image", R.mipmap.cpck)
                put("text", "成品出库")
            }
        })
        itemList.add(object : HashMap<String, Any>() {
            init {
                put("image", R.mipmap.wlpd)
                put("text", "物料盘点")
            }
        })
        itemList.add(object : HashMap<String, Any>() {
            init {
                put("image", R.mipmap.cppd)
                put("text", "成品盘点")
            }
        })

        //生成适配器的ImageItem 与动态数组的元素相对应
        val saImageItems = SimpleAdapter(this,
                itemList, //数据源
                R.layout.gridview_item, //item的XML
                //动态数组与ImageItem对应的子项
                arrayOf("image", "text"),
                //ImageItem的XML文件里面的一个ImageView,TextView ID
                intArrayOf(R.id.img, R.id.txt))
        //添加并且显示
        gridview.adapter = saImageItems
        gridview.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val text = itemList[position]["text"].toString()
            when (text) {
                "物料入库" -> startActivity(Intent(this@MainActivity, WlrkActivity::class.java))
                "物料出库" -> startActivity(Intent(this@MainActivity, WlckActivity::class.java))
                "物料查询" -> {
                }
                "物料盘点" -> {
                }
                "成品入库" -> {
                }
                "成品出库" -> {
                }
                "成品查询" -> {
                }
                "成品盘点" -> {
                }
            }
        }
    }
}