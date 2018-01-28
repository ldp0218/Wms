package net.tiaozhua.wms

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_dj.*
import net.tiaozhua.wms.adapter.WlrkdAdapter
import net.tiaozhua.wms.bean.Orders
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.RetrofitManager
import android.content.Intent
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.LoadingDialog

class WlrkdActivity : BaseActivity(R.layout.activity_dj) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "入库单"

        LoadingDialog.show(this)
        RetrofitManager.instance.wlrkd().enqueue(object : BaseCallback<ResponseList<Orders>>(context = this) {
            override fun successData(data: ResponseList<Orders>) {
                val items = data.items
                listView_dj.emptyView = empty
                listView_dj.adapter = WlrkdAdapter(items, this@WlrkdActivity)
                listView_dj.setOnItemClickListener { _, _, i, _ ->
                    DialogUtil.showDialog(this@WlrkdActivity, null, "是否选择该入库单?",
                        null,
                        DialogInterface.OnClickListener {_, _ ->
                            val intent = Intent()
                            intent.putExtra("dj_id", items[i].dj_id)// 放入返回值
                            intent.putExtra("id", items[i].wid)// 放入返回值
                            setResult(Activity.RESULT_OK, intent) // 放入回传的值,并添加一个Code,方便区分返回的数据
                            finish()
                        }
                    )
                }
            }
        })
    }
}