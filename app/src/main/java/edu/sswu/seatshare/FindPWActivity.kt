package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindPWActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var nameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var newPwEt: EditText
    private lateinit var newPwCheckEt: EditText

    private lateinit var sendEmailBtn: Button
    private lateinit var checkEmailBtn: Button
    private lateinit var doneBtn: Button

    private lateinit var backTv: TextView
    private lateinit var loginTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_pw)   // xml 이름에 맞게 수정

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 뷰 연결
        backTv = findViewById(R.id.find_pw_back)
        loginTv = findViewById(R.id.find_pw_login)

        nameEt = findViewById(R.id.find_pw_name)
        emailEt = findViewById(R.id.find_pw_email)
        newPwEt = findViewById(R.id.find_pw_new_pw)
        newPwCheckEt = findViewById(R.id.find_pw_new_pw_check)

        sendEmailBtn = findViewById(R.id.find_pw_send_email_btn)
        doneBtn = findViewById(R.id.find_pw_btn_done)

        // 뒤로가기
        backTv.setOnClickListener {
            finish()
        }

        // 로그인으로 이동
        loginTv.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 인증 메일 보내기
        sendEmailBtn.setOnClickListener {
            sendResetEmail()
        }


        doneBtn.setOnClickListener {
            Toast.makeText(
                this,
                "비밀번호 재설정을 완료했다면 다시 로그인해 주세요.",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun sendResetEmail() {
        val name = nameEt.text.toString().trim()
        val email = emailEt.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1) Firestore에서 이름 + 이메일 확인 (컬렉션/필드 이름은 실제 사용 중인 걸로 맞춰줘)
        db.collection("users")
            .whereEqualTo("name", name)
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(
                        this,
                        "입력한 이름과 이메일에 해당하는 계정을 찾을 수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                // 2) Firebase Auth 비밀번호 재설정 메일 전송
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "비밀번호 재설정 메일을 보냈습니다.\n메일함을 확인해 주세요.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val msg = task.exception?.message ?: "알 수 없는 오류가 발생했습니다."
                            Toast.makeText(this, "메일 전송 실패: $msg", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "서버 통신 중 오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}


