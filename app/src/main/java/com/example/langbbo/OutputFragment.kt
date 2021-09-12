package com.example.langbbo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class OutputFragment: Fragment() {

    var fView : View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fView = inflater.inflate(R.layout.fragment_output, container, false)
        return fView

    }
}
