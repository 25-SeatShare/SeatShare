//커스텀 뷰
package edu.sswu.seatshare

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.sswu.seatshare.PointItem

data class PointLog(
    val date: String = "",
    val time: String = "",
    val type: String = ""
)

class PointAdapter(private val items: List<PointItem>) :
    RecyclerView.Adapter<PointAdapter.PointViewHolder>() {

    inner class PointViewHolder(val pointView: PointItemView)
        : RecyclerView.ViewHolder(pointView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        val view = PointItemView(parent.context)
        return PointViewHolder(view)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        val item = items[position]
        holder.pointView.bind(item.date, item.time, item.type)
    }

    override fun getItemCount(): Int = items.size
}
