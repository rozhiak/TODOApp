package com.rmblack.todoapp.utils

object PersianNum {
    fun convert(numToConvert: String): String {
        var faNumbers = numToConvert
        val mChars = arrayOf(
            arrayOf("0", "۰"),
            arrayOf("1", "۱"),
            arrayOf("2", "۲"),
            arrayOf("3", "۳"),
            arrayOf("4", "۴"),
            arrayOf("5", "۵"),
            arrayOf("6", "۶"),
            arrayOf("7", "۷"),
            arrayOf("8", "۸"),
            arrayOf("9", "۹")
        )
        for (num in mChars) {
            faNumbers = faNumbers.replace(num[0], num[1])
        }
        return faNumbers
    }
}