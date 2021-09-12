package com.example.langbbo


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // 밑줄 치기
        /*
        val content = SpannableString(login_signup_tv.text.toString())
        content.setSpan(UnderlineSpan(), 0, content.length,0)
        login_signup_tv.text = content
         */

        login_email_lo.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        login_signup_tv.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    override fun onResume() { // 자동로그인
        super.onResume()
        if(auth?.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish() // 뒤로가기 했을 때, 전 페이지로 돌아가지 못 하게 막는 것
        }
    }

}





