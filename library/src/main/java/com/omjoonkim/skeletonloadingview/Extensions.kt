package com.omjoonkim.skeletonloadingview

import android.content.Context
import android.util.TypedValue


internal fun Int.toDp(context : Context) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)