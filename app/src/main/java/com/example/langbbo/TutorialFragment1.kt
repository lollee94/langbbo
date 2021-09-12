package com.example.langbbo

import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment


class TutorialFragment1 : Fragment() {
    // Store instance variables
    private var title: String? = null
    private var page: Int = 0
    var fView : View? = null

    // Inflate the view for the fragment based on layout XML
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        page = arguments?.getInt("someInt", 0)!!
        title = arguments?.getString("someTitle")

        //val tvLabel = view.findViewById(R.id.editText1) as EditText
        //tvLabel.setText("$page -- $title")

        fView = inflater.inflate(R.layout.fragment_tutorial1, container, false)
        return fView


    }

    companion object {
        // newInstance constructor for creating fragment with arguments
        fun newInstance(page: Int, title: String): TutorialFragment1 {
            val fragment = TutorialFragment1()
            val args = Bundle()
            args.putInt("someInt", page)
            args.putString("someTitle", title)
            fragment.arguments = args
            return fragment
        }
    }
}