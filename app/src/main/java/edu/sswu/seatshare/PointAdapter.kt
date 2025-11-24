package edu.sswu.seatshare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class PointLogAdapter : RecyclerView.Adapter<PointLogAdapter.PointLogViewHolder>() {

    private var items: List<PointItem> = emptyList()

    fun submitList(list: List<PointItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_point_log, parent, false)
        return PointLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: PointLogViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PointLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val typeText: TextView = itemView.findViewById(R.id.typeText)

        private val dateFormat = SimpleDateFormat("yy.MM.dd", Locale.KOREA)
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)

        fun bind(log: PointItem) {
            val date = log.createdAt

            dateText.text = dateFormat.format(date)     // 예: 25.10.07
            timeText.text = timeFormat.format(date)     // 예: 15:36

            // delta > 0 → 적립 , delta < 0 → 차감
            typeText.text = if (log.delta > 0) {
                "+${log.delta} 적립"
            } else {
                "${log.delta} 차감"     // log.delta가 -1이면 "-1 차감"
            }
        }
    }
}

