package com.occ.orca

import java.io.Serializable


class KeyExt(val name: String) : Serializable{

    var value: String = ""
    fun value(value: String) {
        this.value = value
    }

    override fun toString(): String {
        return "KeyExt[$name = $value]"
    }
}