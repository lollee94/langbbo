package com.example.langbbo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_tutorial3.view.*


class TutorialFragment3 : Fragment() {

    var fView : View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fView = inflater.inflate(R.layout.fragment_tutorial3, container, false)

        fView!!.tutorial_finish_lo.setOnClickListener {

            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            // 이전 액티비티 기록을 다 지우기 위함.

            startActivity(intent)
            activity?.finish()
        }

        return fView
    }

}