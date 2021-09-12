package com.example.langbbo.modelDTO

data class AlarmDTO(
        var alarmId : String = "",
        var hour : Int = 0,
        var minute : Int = 0,
        var isAlarmOn : Boolean = false,
        var requestCode : Int = 0
)