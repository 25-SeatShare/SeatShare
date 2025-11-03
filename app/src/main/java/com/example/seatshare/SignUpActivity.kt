package com.example.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_main)

        val confirmButton = findViewById<Button>(R.id.signup_done)
        confirmButton.setOnClickListener {
            // LoginActivity로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // SignUpActivity 종료 (뒤로가기 눌러도 안 돌아오게)
        }
    }
}
