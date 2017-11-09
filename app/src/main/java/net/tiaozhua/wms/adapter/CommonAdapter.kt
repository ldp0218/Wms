package net.tiaozhua.wms.adapter

import android.content.Context
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.BaseAdapter

/**
 * 通用ListView/GridView Adapter
 * Created by ldp on 2017/11/9.
*/
abstract class CommonAdapter<T>(private var mContext: Context, private var mDatas: MutableList<T>?, private val layoutId: Int) : BaseAdapter() {
    protected var mInflater: LayoutInflater = LayoutInflater.from(mContext)

    /**
     * 刷新数据，初始化数据
     */
    var datas: MutableList<T>?
        get() = mDatas
        set(list) {
            if (this.mDatas != null) {
                if (null != list) {
                    val temp = arrayListOf<T>()
                    temp.addAll(list)
                    this.mDatas!!.clear()
                    this.mDatas!!.addAll(temp)
                } else {
                    this.mDatas!!.clear()
                }
            } else {
                this.mDatas = list
            }
            notifyDataSetChanged()
        }

    override fun getCount(): Int {
        return if (mDatas != null) mDatas!!.size else 0
    }

    override fun getItem(position: Int): T {
        return mDatas!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder = ViewHolder[mContext, convertView, parent, layoutId, position]
        convert(holder, getItem(position), position)
        return holder.getConvertView()
    }

    abstract fun convert(holder: ViewHolder, t: T, position: Int)

    /**
     * 删除数据
     *
     * @param i
     */
    fun remove(i: Int) {
        if (null != mDatas && mDatas!!.size > i && i > -1) {
            mDatas!!.removeAt(i)
            notifyDataSetChanged()
        }
    }

    /**
     * 加载更多数据
     *
     * @param list
     */
    fun addDatas(list: List<T>?) {
        if (null != list) {
            val temp = arrayListOf<T>()
            temp.addAll(list)
            if (this.mDatas != null) {
                this.mDatas!!.addAll(temp)
            } else {
                this.mDatas = temp
            }
            notifyDataSetChanged()
        }

    }

}