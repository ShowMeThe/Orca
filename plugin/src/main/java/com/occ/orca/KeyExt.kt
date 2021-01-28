package com.occ.orca


class KeyExt(val name: String) {

    var value: String = ""
    fun value(value: String) {
        this.value = value
    }

    override fun toString(): String {
        return "KeyExt[$name = $value]"
    }
}