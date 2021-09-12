package com.example.langbbo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signup.*

class SignInActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        auth = FirebaseAuth.getInstance()

        editTextChanged(signIn_email_et)
        editTextChanged(signIn_pwd_et)

        signIn_finish_lo.setOnClickListener {
            val email = signIn_email_et.text.toString().trim()
            val pwd = signIn_pwd_et.text.toString().trim()

            if(email.length > 0) {
                if(pwd.length > 0) {
                    moveMainPage(email, pwd)
                }
                else{
                    Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show()
                }
            }
            else{
                Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun editTextChanged(et: EditText){

        with(et) {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {

                    val length_email = signIn_email_et.text.toString().trim().length
                    val length_pwd = signIn_pwd_et.text.toString().trim().length

                    if(length_email > 0 && length_pwd > 0) {
                        signIn_finish_lo.setBackgroundResource(R.drawable.button_on)
                    }
                    else{
                        signIn_finish_lo.setBackgroundResource(R.drawable.button_off)
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }
    }


    fun moveMainPage(email: String, pwd: String){

        auth?.signInWithEmailAndPassword(email, pwd)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            else{
                Toast.makeText(this, "일치하는 회원이 없습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

}