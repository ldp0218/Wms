package net.tiaozhua.wms

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : BaseActivity(R.layout.activity_main) {
    private var mExitTime: Long = 0

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
                put("image", R.mipmap.wlck)
                put("text", "物料出库")
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
                put("image", R.mipmap.cprk)
                put("text", "成品入库")
            }
        })
        itemList.add(object : HashMap<String, Any>() {
            init {
                put("image", R.mipmap.cprk)
                put("text", "半成品入库")
            }
        })
        itemList.add(object : HashMap<String, Any>() {
            init {
                put("image", R.mipmap.cppd)
                put("text", "成品备货")
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
                "物料盘点" -> startActivity(Intent(this@MainActivity, WlpdActivity::class.java))
                "成品入库" -> startActivity(Intent(this@MainActivity, CprkActivity::class.java))
                "半成品入库" -> startActivity(Intent(this@MainActivity, BcprkActivity::class.java))
                "成品备货" -> startActivity(Intent(this@MainActivity, CpbhActivity::class.java))
                "成品出库" -> startActivity(Intent(this@MainActivity, CpckActivity::class.java))
                "成品盘点" -> startActivity(Intent(this@MainActivity, CppdActivity::class.java))
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        //1.点击返回键条件成立
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.action == KeyEvent.ACTION_DOWN
                && event.repeatCount == 0) {
            //2.点击的时间差如果大于2000，则提示用户点击两次退出
            if (System.currentTimeMillis() - mExitTime > 2000) {
                //3.保存当前时间
                mExitTime = System.currentTimeMillis()
                //4.提示
                Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show()
            } else {
                //5.点击在两秒内，退出。
                removeALLActivity();//执行移除所有Activity方法
            }
            return true
        }
        return false
    }

//    override fun onBackPressed() {
        //1.点击的时间差如果大于2000，则提示用户点击两次退出
//        if(System.currentTimeMillis() - mExitTime > 2000) {
//            //2.保存当前时间
//            mExitTime  = System.currentTimeMillis()
//            //3.提示
//            Toast.makeText(this, "再按一次退出登录", Toast.LENGTH_SHORT).show()
//        } else {
//            //4.点击的时间差小于2000，调用父类onBackPressed方法执行退出。
//            super.onBackPressed()
//        }
//    }
}