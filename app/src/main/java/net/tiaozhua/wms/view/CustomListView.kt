package net.tiaozhua.wms.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AbsListView
import android.widget.ListView
import android.view.LayoutInflater
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_FLING
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import net.tiaozhua.wms.R


/**
* Created by ldp on 2017/10/26.
*/
class CustomListView : ListView, AbsListView.OnScrollListener {

    private var isLoading = false

    private lateinit var mFooterView: View

    private var mFooterHeight = 0

    private lateinit var inflater: LayoutInflater

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mFooterView = inflater.inflate(R.layout.listview_footer, null);
        mFooterView.measure(0, 0);
        mFooterHeight = mFooterView.measuredHeight;
        mFooterView.setPadding(0, -mFooterHeight, 0, 0);
        this.addFooterView(mFooterView);
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }

    override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
        if(this.lastVisiblePosition == this.adapter.count - 1
                && !isLoading && (scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_IDLE)){
            setLoadState(true);
        }
    }

    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

    }

    /**
     * 设置状态
     * @param s
     */
    fun setLoadState(s: Boolean) {
        this.isLoading = s
        if (isLoading) {
            mFooterView.setPadding(0, 0, 0, 0)
            this.setSelection(this.adapter.count + 1)
        } else {
            mFooterView.setPadding(0, -mFooterHeight, 0, 0)
        }
    }
}
