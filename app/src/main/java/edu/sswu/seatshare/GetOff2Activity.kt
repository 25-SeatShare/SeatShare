package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GetOff2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.get_off_2)

        val actualGetOff = intent.getStringExtra("actualGetOffStation") ?: ""
        val departure = intent.getStringExtra("departure") ?: ""
        val arrive = intent.getStringExtra("arrive") ?: ""
        val platform = intent.getStringExtra("platform") ?: ""
        val seatNumber = intent.getStringExtra("seatNumber") ?: ""

        findViewById<TextView>(R.id.departure_station_).text = departure
        findViewById<TextView>(R.id.arrive_station_).text = actualGetOff
        findViewById<TextView>(R.id.platform_number_).text = platform
        findViewById<TextView>(R.id.seat_number_).text = seatNumber

        findViewById<TextView>(R.id.get_off_2_back_button).setOnClickListener {
            startActivity(Intent(this, GetOff1Activity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.get_off_2_go_home_button).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}
