package com.hank.bingo822

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton


class NumberButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attributeSet, defStyleAttr) {

    var number: Int = 0
    var picked: Boolean = false
    var pos: Int = 0
}


//class NumberButton(context: Context) : AppCompatButton(context) {
//    constructor(context: Context, attrs: AttributeSet) : this(context)
//
//    var number: Int = 0
//    var picked: Boolean = false
//    var pos: Int = 0
//}












