package edu.sswu.seatshare

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class PointAdapter : RecyclerView.Adapter<PointAdapter.PointViewHolder>() {

    private val items: MutableList<PointItem> = mutableListOf()

    // 외부에서 리스트 넣을 때 호출
    fun submitList(list: List<PointItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        // 🔹 커스텀 뷰(PointItemView)를 직접 생성해서 ViewHolder에 전달
        val itemView = PointItemView(parent.context)
        return PointViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PointViewHolder(private val pointItemView: PointItemView) :
        RecyclerView.ViewHolder(pointItemView) {

        private val dateFormat = SimpleDateFormat("yy.MM.dd", Locale.KOREA)
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)

        fun bind(item: PointItem) {
            val date = item.createdAt

            val dateStr = dateFormat.format(date)        // 예: 25.10.07
            val timeStr = timeFormat.format(date)        // 예: 15:36
            val typeStr = if (item.delta > 0) {
                "+${item.delta} 적립"
            } else {
                "${item.delta} 차감"                     // -1 차감 등
            }

            // 🔹 커스텀 뷰의 bind 이용해서 텍스트 세팅
            pointItemView.bind(dateStr, timeStr, typeStr)
        }
    }
}
