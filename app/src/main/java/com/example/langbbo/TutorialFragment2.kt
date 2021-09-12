package com.example.langbbo

import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment


class TutorialFragment2 : Fragment() {

    var fView : View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fView = inflater.inflate(R.layout.fragment_tutorial2, container, false)
        return fView
    }


}