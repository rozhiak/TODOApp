package com.rmblack.todoapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.rmblack.todoapp.models.server.success.User

private const val USER_KEY = "USER_KEY"

private const val ENTRANCE_STATE_KEY = "ENTRANCE_STATE_KEY"

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

    fun saveEntranceState(state: Boolean) {
        sharedPreferences.edit().putBoolean(ENTRANCE_STATE_KEY, state).apply()
    }

    fun getEntranceState(): Boolean {
        return sharedPreferences.getBoolean(ENTRANCE_STATE_KEY, false)
    }

}