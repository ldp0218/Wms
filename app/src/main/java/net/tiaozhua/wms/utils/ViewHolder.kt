package net.tiaozhua.wms.utils

import android.support.annotation.IdRes
import android.util.SparseArray
import android.view.View

/**
 * Created by ldp on 2017/11/7.
 */
inline fun <reified T : View> View.findViewOften(@IdRes viewId: Int): T {
    val viewHolder: SparseArray<View> = tag as? SparseArray<View> ?: SparseArray()
    tag = viewHolder
    var childView: View? = viewHolder.get(viewId)
    if (null == childView) {
        childView = findViewById(viewId)
        viewHolder.put(viewId, childView)
    }
    return childView as T
}