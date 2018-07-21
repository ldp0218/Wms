package net.tiaozhua.wms.view

import android.content.Context
import android.view.View
import android.widget.Checkable
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.TextView
import kotlinx.android.synthetic.main.listview_user_item.view.*
import net.tiaozhua.wms.R
import net.tiaozhua.wms.bean.User

/**
* Created by ldp on 2017/11/10.
*/
class UsersItemView(context: Context) : FrameLayout(context), Checkable {

    private val rb: RadioButton
    private val name: TextView
    private val bm: TextView
    private val gz: TextView

    init {
        View.inflate(context, R.layout.listview_user_item, this)
        rb = rb_item
        name = textView_name
        bm = textView_bm
        gz = textView_gz
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

    fun setValue(user: User) {
        name.text = user.a_name
        bm.text = user.bm_name
        gz.text = user.gz_name
    }
}