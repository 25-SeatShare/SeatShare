package com.example.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEt: EditText
    private lateinit var pwEt: EditText
    private lateinit var loginBtn: Button
    private lateinit var goSignup: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)

        // FirebaseAuth 인스턴스
        auth = FirebaseAuth.getInstance()

        // View 연결
        emailEt = findViewById(R.id.email)
        pwEt = findViewById(R.id.password)
        loginBtn = findViewById(R.id.login_button)
        goSignup = findViewById(R.id.go_signup)

        // 회원가입 화면 이동
        goSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // 로그인 버튼 클릭 시
        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pw = pwEt.text.toString().trim()

            if (email.isBlank() || pw.isBlank()) {
                toast("이메일과 비밀번호를 모두 입력하세요.")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            if (user.isEmailVerified) {
                                // 로그인 성공
                                toast("로그인 성공!")
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                // 이메일 미인증
                                toast("이메일 인증 후 로그인 가능합니다.")
                                auth.signOut()
                            }
                        } else {
                            toast("사용자 정보를 불러올 수 없습니다.")
                        }
                    } else {
                        // 로그인 실패 (Firebase에 계정이 없거나 비밀번호 오류)
                        toast("로그인 실패! 이메일 또는 비밀번호를 확인하세요.")
                    }
                }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
