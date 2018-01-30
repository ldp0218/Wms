package net.tiaozhua.wms

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_dj.*
import net.tiaozhua.wms.adapter.WlrkdAdapter
import net.tiaozhua.wms.bean.Jhd
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager

class WljhdActivity : BaseActivity(R.layout.activity_dj) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "进货单"

        LoadingDialog.show(this)
        RetrofitManager.instance.wljhd().enqueue(object : BaseCallback<ResponseList<Jhd>>(context = this) {
            override fun successData(data: ResponseList<Jhd>) {
                val items = data.items
                listView_dj.emptyView = empty
                listView_dj.adapter = WlrkdAdapter(items, this@WljhdActivity)
                listView_dj.setOnItemClickListener { _, _, i, _ ->
                    DialogUtil.showDialog(this@WljhdActivity, null, "是否选择该进货单?",
                        null,
                        DialogInterface.OnClickListener {_, _ ->
                            val intent = Intent()
                            intent.putExtra("jhd_id", items[i].jhd_id)// 放入返回值
                            setResult(Activity.RESULT_OK, intent) // 放入回传的值,并添加一个Code,方便区分返回的数据
                            finish()
                        }
                    )
                }
            }
        })
    }
}