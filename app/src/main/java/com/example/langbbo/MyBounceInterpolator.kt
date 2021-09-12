package com.example.langbbo

internal class MyBounceInterpolator(amplitude: Double, frequency: Double) : android.view.animation.Interpolator {
    private var mAmplitude = 1.0
    private var mFrequency = 10.0

    init {
        mAmplitude = amplitude
        mFrequency = frequency
    }

    override fun getInterpolation(time: Float): Float {
        return (-1.0 * Math.pow(Math.E, -(0.1+time) / mAmplitude) *
                Math.cos(mFrequency * (0.1+time)) + 1).toFloat()
    }
}