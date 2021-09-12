package com.example.langbbo

import android.content.Context
import android.icu.util.Calendar
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class TodayDecorator(context: Context): DayViewDecorator {
    private var date = CalendarDay.today()
    val drawable = context.resources.getDrawable(R.drawable.circle_border_gray, null)

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.equals(date)!! // 원하는 날짜
    }
    override fun decorate(view: DayViewFacade?) {
        view?.setBackgroundDrawable(drawable)
    }
}