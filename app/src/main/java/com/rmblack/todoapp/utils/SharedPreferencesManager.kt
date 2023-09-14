package com.rmblack.todoapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.rmblack.todoapp.models.server.success.User

private const val USER_KEY = "user"

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    private val gson = Gson()

    fun saveUser(user: User) {
        val serializedUser = gson.toJson(user)
        sharedPreferences.edit().putString(USER_KEY, serializedUser).apply()
    }

    fun getUser(): User? {
        val serializedUser = sharedPreferences.getString(USER_KEY, null)
        return if (serializedUser != null) {
            gson.fromJson(serializedUser, User::class.java)
        } else {
            null
        }
    }

}