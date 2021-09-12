package com.example.langbbo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.example.langbbo.modelDTO.LangDTO
import com.example.langbbo.modelDTO.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_signup.*

class SignUpActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        signUp_back_lo.setOnClickListener {
            finish()
        }

        signUp_finish_lo.setOnClickListener {
            if(currentFocus != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            }

            signUp_black_lo.bringToFront()
            signUp_black_lo.visibility = View.VISIBLE

            val email = signUp_email_et.text.toString().trim()
            val pwd = signUp_pwd_et.text.toString().trim()
            val pwdcheck = signUp_pwdCheck_et.text.toString().trim()

            if(email.contains("@")){
                if(pwd.length >= 6){
                    if(pwd == pwdcheck){
                        createAndLoginEmail(email, pwd) // 성공!
                    }
                    else{
                        signUp_black_lo.visibility = View.GONE
                        Toast.makeText(this, "비밀번호와 비밀번호 확인이 서로 다릅니다.", Toast.LENGTH_LONG).show()
                    }
                }
                else{
                    signUp_black_lo.visibility = View.GONE
                    Toast.makeText(this, "비밀번호를 6자 이상 입력해주세요. ", Toast.LENGTH_LONG).show()
                }
            }
            else{
                signUp_black_lo.visibility = View.GONE
                Toast.makeText(this, "이메일 형식이 유효하지 않습니다.", Toast.LENGTH_LONG).show()
            }
        }

        editTextChanged(signUp_email_et)
        editTextChanged(signUp_pwd_et)
        editTextChanged(signUp_pwdCheck_et)

    }


    fun createAndLoginEmail(email: String, pwd: String) {

        auth?.createUserWithEmailAndPassword(email, pwd)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userData(auth?.currentUser, pwd)
            } else {
                signUp_black_lo.visibility = View.GONE
                Toast.makeText(this, "일시적 오류가 발생했습니다. 완료 버튼을 다시 눌러주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }


    fun userData(user : FirebaseUser?, pwd: String){

        val email = user?.email!!
        val uid = user?.uid!!

        val u = UserDTO()
        u.email = email

        firestore?.collection(Utils.userInfo)?.document(uid)?.set(u)?.addOnSuccessListener {

            val langList = ArrayList<String>()
            langList.add("en")
            langList.add("ja")
            langList.add("zh")
            langList.add("ca")


            //lang_info -> study_info로 바로 연결

            for(lang in langList) {
                val l = LangDTO()
                l.lang = lang


                firestore?.collection(Utils.userInfo)?.document(uid)?.collection(Utils.langInfo)?.document(lang)?.set(l)?.addOnSuccessListener {
                    Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_LONG).show()
                    signUp_black_lo.visibility = View.GONE
                    finish()

                    /*
                    val groupHashMap = HashMap<String, Int>()
                    groupHashMap["${lang}_all"] = 0

                    for(group in groupHashMap.keys){

                        val g = GroupDTO()
                        g.lang = lang
                        g.group = group
                        g.index = groupHashMap[group]!!

                        firestore?.collection(Utils.userInfo)?.document(uid)?.collection(Utils.langInfo)?.document(lang)
                                ?.collection(Utils.groupInfo)?.document(group)?.set(g)?.addOnSuccessListener {

                            Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_LONG).show()
                            signUp_black_lo.visibility = View.GONE
                            finish()
                        }
                    }
                     */
                }


            }



        }
    }

    fun editTextChanged(et: EditText){

        with(et) {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {

                    val length_email = signUp_email_et.text.toString().trim().length
                    val length_pwd = signUp_pwd_et.text.toString().trim().length
                    val length_pwdcheck = signUp_pwdCheck_et.text.toString().trim().length


                    if(length_email > 0 && length_pwd > 0 && length_pwdcheck > 0) {
                        signUp_finish_lo.setBackgroundResource(R.drawable.button_on)
                    }
                    else{
                        signUp_finish_lo.setBackgroundResource(R.drawable.button_off)
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }
    }

}