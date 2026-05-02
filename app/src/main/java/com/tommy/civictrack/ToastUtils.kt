package com.tommy.civictrack

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

fun Context.showTextToast(
    message: String,
    duration: Int = Toast.LENGTH_SHORT
) {
    val textView = TextView(this).apply {
        text = message
        gravity = Gravity.CENTER_VERTICAL
        setTextColor(ContextCompat.getColor(context, R.color.civic_ink))
        textSize = 14f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(dp(16), dp(12), dp(16), dp(12))
        background = GradientDrawable().apply {
            setColor(ContextCompat.getColor(context, R.color.civic_surface_muted))
            cornerRadius = dp(18).toFloat()
        }
    }

    Toast(this).apply {
        this.duration = duration
        view = textView
        setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, dp(96))
    }.show()
}

private fun Context.dp(value: Int): Int =
    (value * resources.displayMetrics.density).toInt()
