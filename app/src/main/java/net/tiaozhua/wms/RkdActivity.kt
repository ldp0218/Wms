package net.tiaozhua.wms

import android.content.DialogInterface
import android.os.Bundle
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_rkd.*
import net.tiaozhua.wms.adapter.RkdAdapter
import net.tiaozhua.wms.bean.Orders
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.RetrofitManager
import android.content.Intent

class RkdActivity : BaseActivity(R.layout.activity_rkd) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "领料单"

        RetrofitManager.instance.wlrkd().enqueue(object : BaseCallback<ResponseList<Orders>>(context = this@RkdActivity) {
            override fun success(data: ResponseList<Orders>) {
                val listViewRkd = this@RkdActivity.listView_rkd
                val items = data.items
                listViewRkd.adapter = RkdAdapter(items, this@RkdActivity)
                listViewRkd.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    DialogUtil.showDialog(this@RkdActivity, null, "是否选择该领料单?",
                        null,
                        DialogInterface.OnClickListener {_, _ ->
                            val intent = Intent()
                            intent.putExtra("id", items[position].wid)// 放入返回值
                            setResult(0, intent)// 放入回传的值,并添加一个Code,方便区分返回的数据
                            finish()
                        }
                    )
                }
            }
        })
    }
}