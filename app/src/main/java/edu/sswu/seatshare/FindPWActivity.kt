package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FindPWActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_pw)

        // 뒤로가기
        val backBtn = findViewById<TextView>(R.id.find_pw_back)
        backBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 로그인 이동
        val loginBtn = findViewById<TextView>(R.id.find_pw_login)
        loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

