package com.occ.orca

import java.util.*

object StringUtils {




    @JvmStatic
    fun substring(str:String):String =  str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1)

}