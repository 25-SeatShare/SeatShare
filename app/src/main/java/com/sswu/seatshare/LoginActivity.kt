package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.view.LayoutInflater
import android.view.WindowManager
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.ForegroundColorSpan
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import edu.sswu.seatshare.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEt: EditText
    private lateinit var pwEt: EditText
    private lateinit var loginBtn: Button
    private lateinit var goSignup: TextView
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_main)

        // SharedPreferences 초기화
        prefs = getSharedPreferences("AppNoticePrefs", MODE_PRIVATE)

        // "다시 보지 않기" 상태 확인 후 필요할 때만 팝업창 표시
        val skipNotice = prefs.getBoolean("skip_notice", false)
        if (!skipNotice) {
            showAppNoticeDialog()
        }


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
                                startActivity(Intent(this, HomeActivity::class.java))
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

    // 안내사항 팝업
    private fun showAppNoticeDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.app_notice, null, false)
        val btnCheck = view.findViewById<Button>(R.id.bt_check)
        val cbSkip   = view.findViewById<CheckBox>(R.id.cb_skip)

        // TextView 찾기
        val textView = view.findViewById<TextView>(R.id.tv_subway)

        // Text 부분 색상 적용
        val text = "1. 7호선 군자역-이수역(총신대) 구간만 제한적으로 시행하고 있습니다."
        val spannable = SpannableString(text)
        val target = "7호선 군자역-이수역(총신대)"
        val start = text.indexOf(target)
        if (start >= 0) {
            val end = start + target.length

            // 부분만 카키색
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#556B2F")),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // 부분 볼드체
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        textView.text = spannable

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)   // 뒤로가기/바깥터치로 닫히지 않게
            .create()

        // 확인 버튼 누르면 체크 상태 저장 후 팝업 닫기
        btnCheck.setOnClickListener {
            val skip = cbSkip.isChecked
            prefs.edit().putBoolean("skip_notice", skip).apply()
            dialog.dismiss()
        }

        dialog.show()

        // 팝업 크기 조정
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
