package edu.sswu.seatshare

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent


class MyInfo2Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var nameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var currentPwEt: EditText
    private lateinit var newPwEt: EditText
    private lateinit var newPwCheckEt: EditText
    private lateinit var doneBtn: Button
    private lateinit var logoutTv: TextView
    private lateinit var backTv: TextView   // 뒤로가기 텍스트뷰

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_info2)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // XML 뷰 연결
        backTv = findViewById(R.id.tv_back)
        nameEt = findViewById(R.id.et_name)
        emailEt = findViewById(R.id.et_email)
        currentPwEt = findViewById(R.id.et_current_pw)
        newPwEt = findViewById(R.id.et_new_pw)
        newPwCheckEt = findViewById(R.id.et_new_pw_check)
        doneBtn = findViewById(R.id.btn_done)
        logoutTv = findViewById(R.id.tv_logout)

        // 이메일은 수정 불가
        emailEt.isEnabled = false

        // Firebase에서 유저 정보 불러오기
        loadUserInfoFromFirestore()

        // 완료 버튼 클릭 → 비밀번호 변경 처리
        doneBtn.setOnClickListener { changePasswordIfNeeded() }

        // 로그아웃
        logoutTv.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()

            // LoginActivity로 이동
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 뒤로가기 → 무조건 MyInfo1Activity로 이동
        backTv.setOnClickListener {
            val intent = Intent(this, MyInfo1Activity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        //회원 탈퇴
        findViewById<TextView>(R.id.tv_delete_account).setOnClickListener {
            startActivity(Intent(this,DeleteAccountActivity::class.java))
        }
    }

    // 🔹 Firestore에서 유저 정보 읽어오기
    private fun loadUserInfoFromFirestore() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: (user.email ?: "")

                    // 이름 마스킹 (예: 지*철)
                    nameEt.setText(maskName(name))

                    emailEt.setText(email)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "내 정보 불러오기 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔹 비밀번호 변경 (현재 비번 → 새 비번)
    private fun changePasswordIfNeeded() {
        val currentPw = currentPwEt.text.toString()
        val newPw = newPwEt.text.toString()
        val newPwCheck = newPwCheckEt.text.toString()

        if (currentPw.isEmpty() && newPw.isEmpty() && newPwCheck.isEmpty()) {
            Toast.makeText(this, "변경할 내용이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPw.length < 6) {
            Toast.makeText(this, "새 비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPw != newPwCheck) {
            Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        val email = user?.email ?: return

        // 현재 비밀번호로 재인증
        val credential = EmailAuthProvider.getCredential(email, currentPw)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // 재인증 성공 → 비밀번호 변경
                user.updatePassword(newPw)
                    .addOnSuccessListener {
                        Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()

                        // 입력창 초기화
                        currentPwEt.text.clear()
                        newPwEt.text.clear()
                        newPwCheckEt.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "비밀번호 변경 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "현재 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔸 이름 마스킹 기능
    private fun maskName(name: String): String {
        if (name.length <= 1) return name
        if (name.length == 2) return "${name[0]}*"
        val hidden = "*".repeat(name.length - 2)
        return "${name.first()}$hidden${name.last()}"
    }
}
