package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch

class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var backButton: TextView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var pwEditText: EditText
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // XML 파일 이름에 맞게 수정
        setContentView(R.layout.delete_account)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        backButton = findViewById(R.id.delete_account_back_button)
        nameEditText = findViewById(R.id.delete_account_name)
        emailEditText = findViewById(R.id.delete_account_email)
        pwEditText = findViewById(R.id.delete_account_current_pw)
        deleteButton = findViewById(R.id.delete_account_button)

        // 뒤로가기
        backButton.setOnClickListener { finish() }

        // 탈퇴 버튼
        deleteButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = pwEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            verifyAndDeleteAccount(name, email, password)
        }
    }

    /**
     * 1) 현재 로그인 유저 확인
     * 2) Firestore users/{uid}에서 name, email 검증
     * 3) 재인증 + 실제 탈퇴 로직 호출
     */
    private fun verifyAndDeleteAccount(name: String, email: String, password: String) {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(this, "회원 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val savedName = document.getString("name") ?: ""
                val savedEmail = document.getString("email") ?: ""

                if (!savedName.equals(name, ignoreCase = true)) {
                    Toast.makeText(this, "이름이 회원 정보와 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                if (savedEmail != email) {
                    Toast.makeText(this, "이메일이 회원 정보와 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Auth에 저장된 이메일과도 한 번 더 체크 (optional)
                val currentEmail = user.email
                if (currentEmail != null && currentEmail != email) {
                    Toast.makeText(this, "현재 로그인된 계정의 이메일과 다릅니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 검증 끝 → 재인증 + 탈퇴
                reauthenticateAndDelete(email, password, uid)
            }
            .addOnFailureListener {
                Toast.makeText(this, "회원 정보 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * 이메일/비밀번호로 재인증 후 Firestore + Auth 삭제
     */
    private fun reauthenticateAndDelete(email: String, password: String, uid: String) {
        val user = auth.currentUser ?: run {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // 재인증 성공 → Firestore 데이터 삭제
                deleteUserData(uid) { success ->
                    if (!success) {
                        Toast.makeText(this, "사용자 데이터 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        return@deleteUserData
                    }

                    // Firestore 정리 후 Auth 계정 삭제
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                                // 로그인 화면으로 이동 (필요한 화면으로 변경)
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "회원 탈퇴 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "현재 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * users/{uid} 문서의 subcollection(pointLogs, seats) + trainSeats(해당 uid) + users/{uid} 삭제
     */
    private fun deleteUserData(uid: String, onComplete: (Boolean) -> Unit) {
        val userDocRef = db.collection("users").document(uid)

        // 1) pointLogs 삭제
        deleteSubcollection(userDocRef.collection("pointLogs")) { success1 ->
            if (!success1) {
                onComplete(false)
                return@deleteSubcollection
            }

            // 2) seats 삭제
            deleteSubcollection(userDocRef.collection("seats")) { success2 ->
                if (!success2) {
                    onComplete(false)
                    return@deleteSubcollection
                }

                // 3) trainSeats 컬렉션에서 uid가 같은 문서 삭제 (필요 없으면 이 호출 제거)
                deleteTrainSeatsByUid(uid) { success3 ->
                    if (!success3) {
                        onComplete(false)
                        return@deleteTrainSeatsByUid
                    }

                    // 4) 마지막으로 users/{uid} 문서 삭제
                    userDocRef.delete()
                        .addOnSuccessListener { onComplete(true) }
                        .addOnFailureListener { onComplete(false) }
                }
            }
        }
    }

    /**
     * 특정 subcollection 전체 문서 삭제 (batch)
     */
    private fun deleteSubcollection(
        collectionRef: CollectionReference,
        onComplete: (Boolean) -> Unit
    ) {
        collectionRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onComplete(true)
                    return@addOnSuccessListener
                }

                val batch: WriteBatch = db.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }

                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    /**
     * trainSeats 컬렉션에서 uid가 같은 문서들 삭제 (uid 필드가 있다고 가정)
     */
    private fun deleteTrainSeatsByUid(uid: String, onComplete: (Boolean) -> Unit) {
        db.collection("trainSeats")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onComplete(true)
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }

                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}
