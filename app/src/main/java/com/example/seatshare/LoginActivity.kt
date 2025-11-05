package com.example.seatshare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_main)
        val database = FirebaseDatabase.getInstance("https://seatshare-385d2-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = database.getReference("message")

        myRef.setValue("Hello, Firebase!")
            .addOnSuccessListener {
                Log.d("FirebaseTest", "✅ 데이터 저장 성공!")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseTest", "❌ 데이터 저장 실패: ${e.message}")
            }




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 버튼 눌렀을 때 SignUpActivity로 이동
        val tvSignup = findViewById<TextView>(R.id.go_signup)

        tvSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }
}
