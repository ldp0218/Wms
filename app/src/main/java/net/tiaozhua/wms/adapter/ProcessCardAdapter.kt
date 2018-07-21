package net.tiaozhua.wms.adapter

import android.content.Context
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import net.tiaozhua.wms.R
import net.tiaozhua.wms.bean.Scdpl
import net.tiaozhua.wms.databinding.ListviewProcessCardItemBinding
import net.tiaozhua.wms.utils.DialogUtil
import net.tiaozhua.wms.utils.findViewOften

/**
* Created by ldp on 2017/11/7.
*/
class ProcessCardAdapter(private val list: MutableList<Scdpl>, private val context: Context) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // 适配器中数据集中数据的个数
    override fun getCount(): Int {
        return list.size
    }

    // 获取数据集中与指定索引对应的数据项
    override fun getItem(position: Int): Any {
        return list[position]
    }

    // 获取指定行对应的ID
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 获取每一个Item显示的内容
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = list[position]
        val binding: ListviewProcessCardItemBinding?
        val view: View?
        if (convertView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.listview_process_card_item, parent, false)
            view = binding.root
        } else {
            binding = DataBindingUtil.bind(convertView)
            view = convertView
        }
        binding?.lld = item
        view.findViewOften<Button>(R.id.btnDelete).setOnClickListener {
            DialogUtil.showDialog(context, null, "是否删除?",
                    null,
                    DialogInterface.OnClickListener { _, _ ->

                    }
            )
            (view as SwipeMenuLayout).quickClose()
            list.removeAt(position)
            notifyDataSetChanged()
        }

//        val item: Scdpl = list[position]
//        val name: TextView = view.findViewOften(R.id.textView_name)
//        name.text = item.ma_name
//        val kcNum: TextView = view.findViewOften(R.id.textView_kcnum)
//        kcNum.text = nf.format(item.kc_num).toString()
//        val ckNum: EditText = view.findViewOften(R.id.editText_cknum)
//        item.num = item.mx_num - item.mx_wcnum
//        ckNum.setText(nf.format(item.num).toString())
//        val unit: TextView = view.findViewOften(R.id.textView_unit)
//        unit.text = item.ma_unit
//        val cb: CheckBox = view.findViewOften(viewId = R.id.checkBox)
//        cb.isChecked = item.checked
        return view
    }
}