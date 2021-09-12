package com.example.langbbo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_form1.view.*

class FormFragment1 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_form1, container, false)

        var isReading = false
        var isListening = false
        var isSpeaking = false
        var isWriting = false
        var isWord = false

        val catData = arguments?.getSerializable("catData")
        //println("kkkk ${catData}")


        catData?.let {
            val catDataHashMap = it as HashMap<String, Boolean>
            if (catDataHashMap["reading"] == true) {
                isReading = true
            }
            if (catDataHashMap["listening"] == true) {
                isListening = true
            }
            if (catDataHashMap["speaking"] == true) {
                isSpeaking = true
            }
            if (catDataHashMap["writing"] == true) {
                isWriting = true
            }
            if (catDataHashMap["word"] == true) {
                isWord = true
            }

        }


        turnButton(isReading, view.form_reading_tv, view.form_reading_et, "reading")
        turnButton(isListening, view.form_listening_tv, view.form_listening_et, "listening")
        turnButton(isSpeaking, view.form_speaking_tv, view.form_speaking_et, "speaking")
        turnButton(isWriting, view.form_writing_tv, view.form_writing_et, "writing")
        turnButton(isWord, view.form_word_tv, view.form_word_et, "word")

        // revise가 있다면 group spinner 삭제
        //val revise = arguments?.getString("revise")
        //revise?.let{view.form_group_lo.visibility = View.GONE}

        val sentenceList = arguments?.getStringArrayList("sentenceList")
        sentenceList?.let{
            view.form_sentence1_et.setText(it[0])
            view.form_sentence2_et.setText(it[1])
            view.form_sentence3_et.setText(it[2])
            view.form_sentence4_et.setText(it[3])
        }

        val sentenceNum = arguments?.getInt("sentenceNum")
        sentenceNum?.let{
            view.form_sentenceNum_et.setText(it.toString())

            when(sentenceNum){
                1 -> view.form_sentence1_cb.isChecked = true
                2 -> view.form_sentence2_cb.isChecked = true
                3 -> view.form_sentence3_cb.isChecked = true
                4 -> view.form_sentence4_cb.isChecked = true
                else -> null
            }

        }

        val sentenceKorean = arguments?.getString("sentenceKorean")
        sentenceKorean?.let{view.form_sentenceKorean_et.setText(it)}

        val coreExpression = arguments?.getStringArrayList("coreExpression")
        coreExpression?.let{
            view.form_core1_et.setText(it[0])
            view.form_core2_et.setText(it[1])
        }


        val situationExplain = arguments?.getString("situationExplain")
        situationExplain?.let{view.form_situationExplain_et.setText(it)}

        val sentenceExplainList = arguments?.getStringArrayList("sentenceExplainList")
        sentenceExplainList?.let{
            view.form_sentenceExplain1_et.setText(it[0])
            view.form_sentenceExplain2_et.setText(it[1])
            view.form_sentenceExplain3_et.setText(it[2])
            view.form_sentenceExplain4_et.setText(it[3])
        }

        val makeSituation = arguments?.getString("makeSituation")
        makeSituation?.let{view.form_makeSituation_et.setText(it)}


        view.form_sentence1_cb.setOnCheckedChangeListener{
            buttonView: CompoundButton?, isChecked: Boolean ->

            if(isChecked){
                view.form_sentence2_cb.isChecked = false
                view.form_sentence3_cb.isChecked = false
                view.form_sentence4_cb.isChecked = false

                view.form_sentenceNum_et.setText("1")
            }
            else{
                view.form_sentenceNum_et.setText("0")
            }
        }

        view.form_sentence2_cb.setOnCheckedChangeListener{
                buttonView: CompoundButton?, isChecked: Boolean ->

            if(isChecked){
                view.form_sentence1_cb.isChecked = false
                view.form_sentence3_cb.isChecked = false
                view.form_sentence4_cb.isChecked = false

                view.form_sentenceNum_et.setText("2")
            }
            else{
                view.form_sentenceNum_et.setText("0")
            }
        }

        view.form_sentence3_cb.setOnCheckedChangeListener{
                buttonView: CompoundButton?, isChecked: Boolean ->

            if(isChecked){
                view.form_sentence1_cb.isChecked = false
                view.form_sentence2_cb.isChecked = false
                view.form_sentence4_cb.isChecked = false

                view.form_sentenceNum_et.setText("3")
            }
            else{
                view.form_sentenceNum_et.setText("0")
            }
        }

        view.form_sentence4_cb.setOnCheckedChangeListener{
                buttonView: CompoundButton?, isChecked: Boolean ->

            if(isChecked){
                view.form_sentence1_cb.isChecked = false
                view.form_sentence2_cb.isChecked = false
                view.form_sentence3_cb.isChecked = false

                view.form_sentenceNum_et.setText("4")
            }
            else{
                view.form_sentenceNum_et.setText("0")
            }
        }
        return view
    }

    private fun turnButton(isTurnOn: Boolean, tv: TextView, et:EditText, category: String) {

        var turnOn = isTurnOn
        if(turnOn) {
            tv.setBackgroundResource(R.drawable.border_rounded_green)
            et.setText(category)
        }
        else{
            tv.setBackgroundResource(R.drawable.border_rounded_gray)
            et.setText("")
        }

        tv.setOnClickListener {
            if(!turnOn){
                tv.setBackgroundResource(R.drawable.border_rounded_green)
                et.setText(category)
                turnOn = true
            }
            else{
                tv.setBackgroundResource(R.drawable.border_rounded_gray)
                et.setText("")
                turnOn = false
            }
        }
    }
}