package com.example.langbbo

import android.content.Context
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class DateDecorator(context: Context, day: CalendarDay): DayViewDecorator {
    private var date = day
    val drawable = context.resources.getDrawable(R.drawable.circle_border_red, null)

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.equals(date)!! // 원하는 날짜
    }
    override fun decorate(view: DayViewFacade?) {
        view?.setBackgroundDrawable(drawable)
    }
}