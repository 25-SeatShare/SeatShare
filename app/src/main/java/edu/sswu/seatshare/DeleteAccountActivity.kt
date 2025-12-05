package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DeleteAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.delete_account)

        //뒤로가기
        findViewById<TextView>(R.id.delete_account_back_button).setOnClickListener {
            startActivity(Intent(this,MyInfo2Activity::class.java))
        }


    }
}
