package com.example.langbbo.modelDTO

data class StudyDTO(
    var studyId : String = "",
    var uid : String = "",
    var timestamp : Long = 0,

    var lang: String = "",

    var catData : HashMap<String, Boolean> = HashMap(),

    var idxReading: Int = 0,
    var idxListening: Int = 0,
    var idxSpeaking: Int = 0,
    var idxWriting: Int = 0,
    var idxWord: Int = 0,

    var sentenceList : ArrayList<String> = ArrayList(), // 질문
    var sentenceNum : Int = 0,
    var sentenceKorean : String = "", // 대답
    var situationExplain : String = "", // 한줄 상황 설명
    var sentenceExplainList:  ArrayList<String> = ArrayList(),

    var makeSituation:  String = "",
    var coreExpression : ArrayList<String> = ArrayList(),

    var youtubeUrl : String = "",
    var youtubeMainTime : ArrayList<Int> = ArrayList(),
    var youtubeSubtitle : ArrayList<String> = ArrayList(),
    var youtubeSubtitleTime : ArrayList<Int> = ArrayList(),
    var youtubeThumbnail : Int = 0,

    var audioUrl : String = "",
    var audioFile : String = "",

    var imageUrl : String = "",
    var imageFile : String = "",

    var example : ArrayList<String> = ArrayList(),
    var exampleHighlight : ArrayList<String> = ArrayList(),

    var completeId: String = "",
    var completeCategory:  String = "",
    var completeTimestamp: Long = 0,
    var isComplete: Boolean = false

)