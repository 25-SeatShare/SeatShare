package com.example.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_main)

        // Firebase 인증 객체 초기화
        auth = FirebaseAuth.getInstance()

        // 입력칸 참조
        val emailInput = findViewById<EditText>(R.id.sign_up_email)
        val pwInput = findViewById<EditText>(R.id.sign_up_pw)
        val pwCheckInput = findViewById<EditText>(R.id.sign_up_pw_check)
        val confirmButton = findViewById<Button>(R.id.signup_done)

        // 회원가입 버튼 클릭 시
        confirmButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = pwInput.text.toString()
            val pwCheck = pwCheckInput.text.toString()

            // 비밀번호 확인 검사
            if (password != pwCheck) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase 회원가입 요청
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "회원가입 실패: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
