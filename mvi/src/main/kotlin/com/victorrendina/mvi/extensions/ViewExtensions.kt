package com.victorrendina.mvi.extensions

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat

fun ViewGroup.inflate(@LayoutRes resId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(resId, this, attachToRoot)
}

fun ImageButton.setDrawableTint(@ColorRes colorRes: Int) {
    drawable.mutate().colorFilter = PorterDuffColorFilter(
        ContextCompat.getColor(context, colorRes),
        PorterDuff.Mode.MULTIPLY
    )
}

fun Drawable.setDrawableTint(context: Context, @ColorRes colorRes: Int) {
    mutate().colorFilter = PorterDuffColorFilter(
        ContextCompat.getColor(context, colorRes),
        PorterDuff.Mode.MULTIPLY
    )
}

fun ImageView.setDrawableTint(@ColorRes colorRes: Int) {
    drawable?.mutate()?.colorFilter =
        PorterDuffColorFilter(
            ContextCompat.getColor(context, colorRes),
            PorterDuff.Mode.MULTIPLY
        )
}
