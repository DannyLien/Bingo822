package com.hank.bingo822

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

class NumberButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context) {

    var number: Int = 0
    var picked: Boolean = false
    var pos: Int = 0

}