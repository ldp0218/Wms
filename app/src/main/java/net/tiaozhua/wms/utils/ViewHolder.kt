package net.tiaozhua.wms.utils

import android.content.Context
import android.support.annotation.IdRes
import android.util.SparseArray
import android.view.View
import android.widget.Toast
import net.tiaozhua.wms.bean.Bzmx

/**
 * Created by ldp on 2017/11/7.
 */
inline fun <reified T : View> View.findViewOften(@IdRes viewId: Int): T {
    val viewHolder: SparseArray<View> = tag as? SparseArray<View> ?: SparseArray()
    tag = viewHolder
    var childView = viewHolder.get(viewId)
    if (null == childView) {
        childView = findViewById(viewId)
        viewHolder.put(viewId, childView)
    }
    return childView as T
}

fun parseProductQRCode(
        context: Context,
        barcodeStr: String,
        productList: HashSet<String>
) : Bzmx {
    if (productList.contains(barcodeStr)) {
        Toast.makeText(context, "请勿重复扫描", Toast.LENGTH_SHORT).show()
        return Bzmx(0, 0, 0)
    }
    if (barcodeStr.indexOf("_") == -1 || barcodeStr.indexOf("*") == -1 || barcodeStr.indexOf("#") == -1) {
        Toast.makeText(context, "未识别的二维码", Toast.LENGTH_SHORT).show()
        return Bzmx(0, 0, 0)
    }
    val xsdBzId = try {
        barcodeStr.substring(0, barcodeStr.indexOf("_")).toInt()
    } catch (e: NumberFormatException) { 0 }
    val bzId = try {
        barcodeStr.substring(barcodeStr.indexOf("_") + 1, barcodeStr.indexOf("*")).toInt()
    } catch (e: NumberFormatException) { 0 }
    val num = try {
        barcodeStr.substring(barcodeStr.indexOf("*") + 1, barcodeStr.indexOf("#")).toInt()
    } catch (e: NumberFormatException) { 0 }
    if (xsdBzId == 0 || bzId == 0 || num == 0) {
        Toast.makeText(context, "未识别的二维码", Toast.LENGTH_SHORT).show()
    }
    return Bzmx(xsdBzId, bzId, num)
}