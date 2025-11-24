package edu.sswu.seatshare

import java.util.Date

data class PointItem(
    val delta: Long = 0,      // +1, -1, +5, +20 등
    val createdAt: Date = Date()
)
