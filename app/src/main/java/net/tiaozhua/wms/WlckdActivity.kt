package net.tiaozhua.wms

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_dj.*
import net.tiaozhua.wms.adapter.WlckdAdapter
import net.tiaozhua.wms.bean.Ckd
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager

class WlckdActivity : BaseActivity(R.layout.activity_dj) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "领料单"

        LoadingDialog.show(this@WlckdActivity)
        RetrofitManager.instance.wlckd().enqueue(object : BaseCallback<ResponseList<Ckd>>(context = this@WlckdActivity) {
            override fun successData(data: ResponseList<Ckd>) {
                val items = data.items
                listView_dj.emptyView = empty
                listView_dj.adapter = WlckdAdapter(items, this@WlckdActivity)
                listView_dj.setOnItemClickListener { _, _, i, _ ->
                    DialogUtil.showDialog(this@WlckdActivity, null, "是否选择该领料单?",
                            null,
                            DialogInterface.OnClickListener {_, _ ->
                                val intent = Intent()
                                intent.putExtra("ckd", items[i])
                                setResult(Activity.RESULT_OK, intent) // 放入回传的值,并添加一个Code,方便区分返回的数据
                                finish()
                            }
                    )
                }
            }
        })
    }
}