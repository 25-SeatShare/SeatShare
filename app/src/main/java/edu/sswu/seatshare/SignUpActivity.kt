package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp   // ✅ 추가
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
    private lateinit var birthEt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_main)

        // 유의사항 팝업창 표시
        showAgeNoticeDialog()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // View 초기화
        nameEt = findViewById(R.id.sign_up_name)
        emailEt = findViewById(R.id.sign_up_email)
        pwEt = findViewById(R.id.sign_up_pw)
        pw2Et = findViewById(R.id.sign_up_pw_check)
        sendBtn = findViewById(R.id.certi_button)
        checkBtn = findViewById(R.id.certi_check_button)
        doneBtn = findViewById(R.id.signup_done)
        birthEt = findViewById(R.id.birthEditText)   // 생년월일 입력칸

        val goLogin = findViewById<TextView>(R.id.sign_up_login)
        goLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 1) 인증메일 보내기 (계정 생성 + 인증메일 발송)
        sendBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pw = pwEt.text.toString()
            val pw2 = pw2Et.text.toString()

            if (nameEt.text.isNullOrBlank()) {
                toast("이름을 입력하세요.")
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                toast("이메일을 입력하세요.")
                return@setOnClickListener
            }
            if (pw.length < 6) {
                toast("비밀번호는 6자 이상이어야 합니다.")
                return@setOnClickListener
            }
            if (pw != pw2) {
                toast("비밀번호가 일치하지 않습니다.")
                return@setOnClickListener
            }

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
                            sendBtn.isEnabled = false   // 인증메일 버튼 비활성화
                            doneBtn.isEnabled = true    // 최종 완료 버튼 활성화
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

        // 3) 최종 완료 (프로필 저장 + 포인트/교통약자 계산)
        doneBtn.setOnClickListener {
            val user = auth.currentUser
            if (user == null || user.isEmailVerified != true) {
                toast("인증이 완료된 계정으로 로그인되어야 합니다.")
                return@setOnClickListener
            }

            // 생년월일 입력값 확인
            val birthStr = birthEt.text.toString().trim()
            if (birthStr.isEmpty()) {
                toast("생년월일을 입력하세요. (예: 2003-11-10)")
                return@setOnClickListener
            }

            // YYYY-MM-DD 형식 간단 체크
            val birthParts = birthStr.split("-")
            if (birthParts.size != 3) {
                toast("생년월일 형식을 확인해주세요. (예: 2003-11-10)")
                return@setOnClickListener
            }

            val year = birthParts[0].toIntOrNull()
            val month = birthParts[1].toIntOrNull()
            val day = birthParts[2].toIntOrNull()

            if (year == null || month == null || day == null) {
                toast("생년월일은 숫자로 입력해주세요.")
                return@setOnClickListener
            }

            // 나이 계산 (대략적인 방식: 올해 - 태어난 해)
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val age = currentYear - year

            // 교통약자(65세 이상) 여부
            val isTransportVulnerable = age >= 65

            // 초기 포인트: 교통약자면 20, 아니면 5
            val initialPoints = if (isTransportVulnerable) 20L else 5L

            // 프로필 맵 생성
            val profile = mapOf(
                "uid" to user.uid,
                "name" to nameEt.text.toString(),
                "email" to (user.email ?: ""),
                "birth" to birthStr,                          // 생년월일 저장
                "age" to age.toLong(),                        // 계산된 나이 저장
                "isTransportVulnerable" to isTransportVulnerable, // 교통약자 여부
                "points" to initialPoints,                    // 포인트 5 or 20
                "createdAt" to System.currentTimeMillis()
            )

            // 🔹 users/{uid} 문서 레퍼런스
            val userDocRef = db.collection("users").document(user.uid)

            // 1차: Firestore에 저장
            userDocRef
                .set(profile)
                .addOnSuccessListener {
                    // ✅ 회원가입 시 pointLogs에 초기 포인트 로그 남기기
                    val logData = mapOf(
                        "delta" to initialPoints,
                        "type" to "signup",
                        "message" to if (isTransportVulnerable)
                            "회원가입 기본 포인트(교통약자)"
                        else
                            "회원가입 기본 포인트",
                        "createdAt" to Timestamp.now()
                    )

                    userDocRef.collection("pointLogs").add(logData)

                    // 2차: Realtime Database에도 저장
                    val realtimeDb = FirebaseDatabase.getInstance()
                    val userRef = realtimeDb.getReference("users").child(user.uid)

                    userRef.setValue(profile)
                        .addOnSuccessListener {
                            toast("회원가입이 완료되었어요! (Firestore + Realtime DB 저장)")
                            // TODO: 메인으로 이동 등
                            // startActivity(Intent(this, MainActivity::class.java))
                            // finish()
                        }
                        .addOnFailureListener { e ->
                            toast("회원가입은 되었지만, Realtime DB 저장에 실패했어요: ${e.localizedMessage}")
                        }
                }
                .addOnFailureListener { e ->
                    toast("프로필 저장 실패(Firestore): ${e.localizedMessage}")
                }
        }
    }

    // 유의사항 팝업창
    private fun showAgeNoticeDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.age_notice, null, false)
        val checkBox = view.findViewById<CheckBox>(R.id.cb_agree)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        // 체크박스 선택 시 팝업창 닫기
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) dialog.dismiss()
        }

        dialog.show()

        // 팝업창 크기 조정
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
