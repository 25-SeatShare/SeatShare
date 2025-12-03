package edu.sswu.seatshare

import java.util.Date

data class PointItem(
    val delta: Long = 0,
    val message: String = "",
    val createdAt: Date = Date()
)
