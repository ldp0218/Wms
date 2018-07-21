package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_wlmx.*
import net.tiaozhua.wms.adapter.WlmxAdapter
import net.tiaozhua.wms.bean.Wlck

class WlmxActivity : BaseActivity(R.layout.activity_wlmx) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "物料明细"
        self = this

        val wl = intent.getParcelableExtra<Wlck>("wl")
        textView_maname.text = wl.ma_name
        textView_manum.text = wl.num.toString()
        listView_wl.adapter = WlmxAdapter(wl.mxList!!, this@WlmxActivity)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var self: WlmxActivity
    }
}