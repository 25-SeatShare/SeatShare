package com.example.seatshare

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

data class UserData(
    val name: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val TAG = "SignUpActivity"

    // XML과 매핑 (id 그대로)
    private val nameEt by lazy { findViewById<EditText>(R.id.sign_up_name) }
    private val emailEt by lazy { findViewById<EditText>(R.id.sign_up_email) }
    private val codeEt by lazy { findViewById<EditText>(R.id.verify_code) }
    private val pwEt by lazy { findViewById<EditText>(R.id.sign_up_pw) }
    private val pwCheckEt by lazy { findViewById<EditText>(R.id.sign_up_pw_check) }
    private val sendCodeBtn by lazy { findViewById<Button>(R.id.certi_button) }
    private val checkCodeBtn by lazy { findViewById<Button>(R.id.certi_check_button) }
    private val doneBtn by lazy { findViewById<Button>(R.id.signup_done) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 인증번호 관련 버튼 (현재는 더미 동작)
        sendCodeBtn.setOnClickListener {
            toast("인증번호 전송 기능은 추후 연결 예정입니다.")
        }

        checkCodeBtn.setOnClickListener {
            toast("인증번호 확인 기능은 추후 연결 예정입니다.")
        }

        doneBtn.setOnClickListener {
            doSignUp()
        }
    }

    private fun doSignUp() {
        val name = nameEt.text.toString().trim()
        val email = emailEt.text.toString().trim().lowercase()
        val pw = pwEt.text.toString().trim()
        val pwCheck = pwCheckEt.text.toString().trim()

        // 기본 유효성 검사
        if (name.isEmpty() || email.isEmpty() || pw.isEmpty() || pwCheck.isEmpty()) {
            toast("모든 항목을 입력해주세요.")
            return
        }
        if (pw != pwCheck) {
            toast("비밀번호가 일치하지 않습니다.")
            return
        }
        if (pw.length < 6) {
            toast("비밀번호는 6자 이상이어야 합니다.")
            return
        }

        // Firebase Auth로 계정 생성
        auth.createUserWithEmailAndPassword(email, pw)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        Log.e(TAG, "회원가입은 되었지만 UID를 가져오지 못함")
                        toast("회원가입은 되었지만, 정보 저장에 실패했습니다.")
                        return@addOnCompleteListener
                    }

                    // Firestore에 사용자 정보 저장
                    saveUserToFirestore(uid, name, email)

                } else {
                    val e = task.exception
                    Log.w(TAG, "createUser 실패", e)
                    val msg = when (e) {
                        is FirebaseAuthWeakPasswordException ->
                            "비밀번호가 너무 약합니다."
                        is FirebaseAuthInvalidCredentialsException ->
                            "이메일 형식이 올바르지 않습니다."
                        is FirebaseAuthUserCollisionException ->
                            "이미 가입된 이메일입니다."
                        is FirebaseNetworkException ->
                            "네트워크 오류입니다. 인터넷 연결을 확인하세요."
                        else -> e?.message ?: "회원가입 실패. 다시 시도해주세요."
                    }
                    toast(msg)
                }
            }
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String) {
        val data = UserData(name = name, email = email)
        db.collection("users").document(uid)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Firestore에 저장 완료: users/$uid")
                toast("회원가입 및 정보 저장 완료! 🎉")
                finish() // 회원가입 후 종료 (필요시 로그인 화면으로 이동)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore 저장 실패: UID=$uid", e)
                toast("정보 저장 실패. 다시 시도해주세요.")
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
