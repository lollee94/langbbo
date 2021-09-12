package com.example.langbbo

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager

class PracticeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)

    }


    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle("컨텐츠 만들기를 종료하시겠습니까?")
            setPositiveButton("확인") { dialogInterface, i ->
                super.onBackPressed()
            }
            setNegativeButton("취소") { dialogInterface, i -> }
            show()
        }
    }
}