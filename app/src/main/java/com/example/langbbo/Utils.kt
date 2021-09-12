package com.example.langbbo

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView


class Utils {
    companion object {

        val userInfo = "USER_INFO"
        val studyInfo = "STUDY_INFO"
        val completeInfo = "COMPLETE_INFO"
        val dateInfo = "DATE_INFO"

        val learnInfo = "LEARN_INFO"

        val questionInfo = "QUESTION_INFO"

        val langInfo = "LANG_INFO"

        val adminOutput = "ADMIN_OUTPUT"

        val adminError = "ADMIN_ERROR"
        val adminInfo = "ADMIN_INFO"
        val adminQna = "ADMIN_QNA"
        val adminAsk = "ADMIN_ASK"

        fun koreanToLang(korean: String): String{
            return when(korean){
                "영어" -> "en"
                "중국어" -> "zh"
                "일본어" -> "ja"
                "스페인어" -> "ca"
                else -> "en"
            }
        }

        fun langToKorean(lang: String): String{
            return when(lang){
                "en" -> "영어"
                "zh" -> "중국어"
                "ja" -> "일본어"
                "ca" -> "스페인어"
                else -> "기타"
            }
        }

        fun kindToKorean(lang: String): String{
            return when(lang){
                "speaking" -> "말하기"
                "listening" -> "듣기"
                "writing" -> "쓰기"
                "reading" -> "읽기"
                "word" -> "단어"
                "translator" -> "하고 싶은 말"
                else -> "기타"
            }
        }


        fun textHighlight(tv: TextView, word: String){
            val text = tv.text
            val ss = SpannableString(text)
            if (text.indexOf(word) > -1) {
                val start = text.indexOf(word)
                val end = start + word.length
                ss.setSpan(
                    ForegroundColorSpan(Color.parseColor("#ff8c00")),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                //ss.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            tv.text = ss
        }


        fun getDuration(duration: Int): String{
            val audioDuration : String
            val minute = ((duration) / 1000) / 60
            val second = ((duration) / 1000) % 60
            if (second < 10) {
                audioDuration = "$minute:0$second"
            } else {
                audioDuration = "$minute:$second"
            }
            return audioDuration
        }


        fun makePureArray(array: List<String>): java.util.ArrayList<String> {

            val pureArray = java.util.ArrayList<String>()
            for(i in 0 until array.size){
                if(array[i].trim() != "") {
                    pureArray.add(array[i].trim())
                }
            }
            return pureArray
        }


        fun bubbleAnim(ctx: Context, v: View, a: Double, f: Double){
            val bbAnim = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
            var interpolater = MyBounceInterpolator(a, f)
            bbAnim.interpolator = interpolater
            v.startAnimation(bbAnim)
        }




    }

}