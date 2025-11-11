package com.example.seatshare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var nameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var pwEt: EditText
    private lateinit var pw2Et: EditText
    private lateinit var sendBtn: Button
    private lateinit var checkBtn: Button
    private lateinit var doneBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nameEt = findViewById(R.id.sign_up_name)
        emailEt = findViewById(R.id.sign_up_email)
        pwEt = findViewById(R.id.sign_up_pw)
        pw2Et = findViewById(R.id.sign_up_pw_check)
        sendBtn = findViewById(R.id.certi_button)
        checkBtn = findViewById(R.id.certi_check_button)
        doneBtn = findViewById(R.id.signup_done)

        // 1) 인증메일 보내기 (= 계정 생성 + 인증메일 발송)
        sendBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pw = pwEt.text.toString()
            val pw2 = pw2Et.text.toString()

            if (nameEt.text.isNullOrBlank()) { toast("이름을 입력하세요."); return@setOnClickListener }
            if (email.isEmpty()) { toast("이메일을 입력하세요."); return@setOnClickListener }
            if (pw.length < 6) { toast("비밀번호는 6자 이상이어야 합니다."); return@setOnClickListener }
            if (pw != pw2) { toast("비밀번호가 일치하지 않습니다."); return@setOnClickListener }

            auth.createUserWithEmailAndPassword(email, pw)
                .addOnSuccessListener { result ->
                    result.user?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            toast("인증 메일을 보냈어요. 메일의 [Verify]를 누르고 [인증 확인]을 눌러주세요.")
                            auth.signOut()
                        }
                        ?.addOnFailureListener { e ->
                            toast("인증 메일 전송 실패: ${e.localizedMessage}")
                        }
                }
                .addOnFailureListener { e ->
                    toast("계정 생성 실패: ${e.localizedMessage}")
                }
        }

        // 2) 인증 확인
        checkBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pw = pwEt.text.toString()

            if (email.isEmpty() || pw.isEmpty()) {
                toast("이메일/비밀번호를 입력하세요.")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pw)
                .addOnSuccessListener {
                    val user = auth.currentUser
                    user?.reload()?.addOnCompleteListener {
                        if (user?.isEmailVerified == true) {
                            toast("이메일 인증이 확인되었습니다!")
                            sendBtn.isEnabled = false      // 인증메일 버튼 비활성화
                            doneBtn.isEnabled = true       // 최종 완료 버튼 활성화
                        } else {
                            toast("아직 인증되지 않았어요. 메일에서 [Verify]를 눌러주세요.")
                            auth.signOut()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    toast("로그인 실패: ${e.localizedMessage}")
                }
        }

        // 3) 최종 완료
        doneBtn.setOnClickListener {
            val user = auth.currentUser
            if (user == null || user.isEmailVerified != true) {
                toast("인증이 완료된 계정으로 로그인되어야 합니다.")
                return@setOnClickListener
            }

            val profile = mapOf(
                "uid" to user.uid,
                "name" to nameEt.text.toString(),
                "email" to (user.email ?: ""),
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("users").document(user.uid)
                .set(profile)
                .addOnSuccessListener {
                    toast("회원가입이 완료되었어요!")
                    // TODO: 메인으로 이동
                    // startActivity(Intent(this, MainActivity::class.java))
                    // finish()
                }
                .addOnFailureListener { e ->
                    toast("프로필 저장 실패: ${e.localizedMessage}")
                }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}