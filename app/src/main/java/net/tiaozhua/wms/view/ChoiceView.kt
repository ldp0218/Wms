package net.tiaozhua.wms.view

import android.content.Context
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Checkable
import android.widget.FrameLayout
import net.tiaozhua.wms.R

/**
* Created by ldp on 2017/11/10.
*/
class ChoiceView(context: Context) : FrameLayout(context), Checkable {

    private val tv: TextView
    private val rb: RadioButton

    init {
        View.inflate(context, R.layout.listview_material, this)
        tv = findViewById(R.id.textView_name) as TextView
        rb = findViewById(R.id.rb_item) as RadioButton
    }

    override fun setChecked(checked: Boolean) {
        rb.isChecked = checked
    }

    override fun isChecked(): Boolean {
        return rb.isChecked
    }

    override fun toggle() {
        rb.toggle()
    }

    fun setTextView(text: String) {
        tv.text = text
    }
}