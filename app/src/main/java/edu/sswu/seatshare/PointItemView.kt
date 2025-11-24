package edu.sswu.seatshare

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView

class PointItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val dateText: TextView
    private val timeText: TextView
    private val typeText: TextView

    init {
        inflate(context, R.layout.item_point_log, this)

        dateText = findViewById(R.id.dateText)
        timeText = findViewById(R.id.timeText)
        typeText = findViewById(R.id.typeText)

        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

    }

    fun bind(date: String, time: String, type: String) {
        dateText.text = date
        timeText.text = time
        typeText.text = type
    }
}
