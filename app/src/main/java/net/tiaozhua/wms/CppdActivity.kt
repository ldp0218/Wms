package net.tiaozhua.wms

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.BaseAdapter
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import kotlinx.android.synthetic.main.activity_pd.*
import net.tiaozhua.wms.adapter.CommonAdapter
import net.tiaozhua.wms.adapter.ViewHolder
import net.tiaozhua.wms.bean.Pd
import net.tiaozhua.wms.bean.ResponseList
import net.tiaozhua.wms.utils.BaseCallback
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.LoadingDialog
import net.tiaozhua.wms.utils.RetrofitManager

class CppdActivity : BaseActivity(R.layout.activity_pd) {
    internal lateinit var pdAdapter: BaseAdapter
    internal var pdList: MutableList<Pd>? = null
    internal var page = 1
    private var totalPages = 0
    private val type = 1    // 设置类型为成品

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarTitle.text = "成品盘点"

        CppdActivity.self = this
        LoadingDialog.show(this)
        RetrofitManager.instance.pdList(type)
                .enqueue(object : BaseCallback<ResponseList<Pd>>(this@CppdActivity) {
                    override fun successData(data: ResponseList<Pd>) {
                        page = data.page
                        totalPages = data.totalPages
                        pdList = data.items.toMutableList()
                        pdAdapter = object : CommonAdapter<Pd>(pdList, R.layout.listview_pd_item) {
                            override fun convert(holder: ViewHolder, t: Pd, position: Int) {
                                holder.setText(R.id.textView_no, t.pd_hao)
                                holder.setText(R.id.textView_status, if (t.pd_state == 0) "盘点中" else "已完成")
                                holder.setText(R.id.textView_date, t.pd_date)
                                holder.setText(R.id.textView_pdr, t.handler)
                                holder.setOnClickListener(R.id.btnDelete, View.OnClickListener { _ ->
                                    //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                                    (holder.getConvertView() as SwipeMenuLayout).quickClose()
                                    DialogUtil.showDialog(this@CppdActivity, null, "是否删除?",
                                            null,
                                            DialogInterface.OnClickListener { _, _ ->
                                                RetrofitManager.instance.delPd(type, t.id)
                                                        .enqueue(object : BaseCallback<String>(this@CppdActivity) {
                                                            override fun successInfo(info: String) {
                                                                pdList!!.removeAt(position)
                                                                notifyDataSetChanged()
                                                            }
                                                        })
                                            }
                                    )
                                })
                                holder.setOnClickListener(R.id.layout_pd, View.OnClickListener { _ ->
                                    // 点击查看详情
                                    val intent = Intent(this@CppdActivity, CppdmxActivity::class.java)
                                    intent.putExtra("type", type)  // 设置标识
                                    intent.putExtra("pdId", t.id)
                                    intent.putExtra("ckId", t.ck_id)
                                    intent.putExtra("ckName", t.ck_name)
                                    intent.putExtra("pdDate", t.pd_date)
                                    intent.putExtra("finish", t.pd_state)
                                    startActivity(intent)
                                })
                            }
                        }
                        listView_pd.adapter = pdAdapter
                        refreshLayout.setOnRefreshListener { smartLayout ->     // 刷新
                            smartLayout.layout.postDelayed({
                                RetrofitManager.instance.pdList(type, 1, page * 10)
                                        .enqueue(object : BaseCallback<ResponseList<Pd>>(this@CppdActivity) {
                                            override fun successData(data: ResponseList<Pd>) {
                                                pdList!!.clear()
                                                pdList!!.addAll(data.items)
                                                pdAdapter.notifyDataSetChanged()
                                                smartLayout.finishRefresh()
                                            }
                                        })
                            }, 1000)
                        }
                        refreshLayout.setOnLoadmoreListener { smartLayout ->    // 加载更多
                            if (totalPages > page) {
                                smartLayout.layout.postDelayed({
                                    RetrofitManager.instance.pdList(type, page + 1)
                                            .enqueue(object : BaseCallback<ResponseList<Pd>>(this@CppdActivity) {
                                                override fun successData(data: ResponseList<Pd>) {
                                                    pdList!!.addAll(data.items)
                                                    pdAdapter.notifyDataSetChanged()
                                                    page = data.page
                                                    totalPages = data.totalPages
                                                    smartLayout.finishLoadmore()
                                                    if (totalPages == page) {
                                                        smartLayout.isLoadmoreFinished = true//将不会再次触发加载更多事件
                                                    }
                                                }
                                            })
                                }, 1000)
                            } else {
                                smartLayout.finishLoadmore()
                            }
                        }
                    }
                })

        // 开始新盘点
        pandian.setOnClickListener {
            val intent = Intent(this@CppdActivity, CppdmxActivity::class.java)
            intent.putExtra("type", type)  // 设置标识
            startActivity(intent)
        }
    }
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var self: CppdActivity
    }
}
