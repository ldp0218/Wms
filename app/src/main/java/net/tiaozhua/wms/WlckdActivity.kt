package net.tiaozhua.wms

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_dj.*
import net.tiaozhua.wms.adapter.WlckdAdapter
import net.tiaozhua.wms.bean.Scdmx

class WlckdActivity : BaseActivity(R.layout.activity_dj) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "流程卡"

        listView_dj.emptyView = empty
        listView_dj.adapter = WlckdAdapter(WlckActivity.self.scdmxList, this@WlckdActivity)
        listView_dj.setOnItemClickListener { _, _, position, _ ->
            val mIntent = Intent(this@WlckdActivity, LldInfoActivity::class.java)
            mIntent.putExtra("scdmx", WlckActivity.self.scdmxList[position])
            startActivityForResult(mIntent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val scdmx = data.getParcelableExtra<Scdmx>("scdmx")
                WlckActivity.updateWlckAdapter(scdmx)
            }
        }
    }
}