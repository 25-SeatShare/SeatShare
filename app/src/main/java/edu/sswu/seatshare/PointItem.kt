package edu.sswu.seatshare
//pointItem 클래스 정의
data class PointItem(
    val date: String,
    val time: String,
    val type: String   // "+1 적립" 또는 "-1 차감"
)
