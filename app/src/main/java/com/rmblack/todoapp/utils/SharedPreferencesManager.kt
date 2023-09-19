package com.rmblack.todoapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rmblack.todoapp.models.server.requests.AddTaskRequest
import com.rmblack.todoapp.models.server.requests.DeleteTaskRequest
import com.rmblack.todoapp.models.server.requests.EditTaskRequest
import com.rmblack.todoapp.models.server.success.User

private const val USER_KEY = "USER_KEY"

private const val ENTRANCE_STATE_KEY = "ENTRANCE_STATE_KEY"

private const val FAILED_ADD_REQUESTS_KEY = "FAILED_ADD_REQUESTS_KEY"

private const val FAILED_DELETE_REQUESTS_KEY = "FAILED_DELETE_REQUESTS_KEY"

private const val FAILED_EDIT_REQUESTS_KEY = "FAILED_EDIT_REQUESTS_KEY"


class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    private val editor = sharedPreferences.edit()

    private val gson = Gson()

    fun getFailedAddRequests(): List<AddTaskRequest> {
        val serializedRequests = sharedPreferences.getString(FAILED_ADD_REQUESTS_KEY, null)

        return if (serializedRequests != null) {
            gson.fromJson(serializedRequests, object : TypeToken<List<AddTaskRequest>>() {}.type)
        } else {
            listOf()
        }
    }

    fun insertFailedAddRequest(request: AddTaskRequest) {
        val existingList: MutableList<AddTaskRequest> =
            getFailedAddRequests().toMutableList()

        existingList.add(request)

        val serializedRequests = gson.toJson(existingList.toList())
        editor.putString(FAILED_ADD_REQUESTS_KEY, serializedRequests).apply()
    }

    fun removeFailedAddRequest(request: AddTaskRequest) {
        val existingList: MutableList<AddTaskRequest> =
            getFailedAddRequests().toMutableList()

        existingList.remove(request)

        val serializedRequests = gson.toJson(existingList.toList())
        editor.putString(FAILED_ADD_REQUESTS_KEY, serializedRequests).apply()
    }

    fun getFailedDeleteRequests(): List<DeleteTaskRequest> {
        val serializedRequests = sharedPreferences.getString(FAILED_DELETE_REQUESTS_KEY, null)

        return if (serializedRequests != null) {
            gson.fromJson(serializedRequests, object : TypeToken<List<DeleteTaskRequest>>() {}.type)
        } else {
            listOf()
        }
    }

    fun insertFailedDeleteRequest(request: DeleteTaskRequest) {
        val existingList: MutableList<DeleteTaskRequest> =
            getFailedDeleteRequests().toMutableList()

        existingList.add(request)

        val serializedRequests = gson.toJson(existingList.toList())
        editor.putString(FAILED_DELETE_REQUESTS_KEY, serializedRequests).apply()
    }

    fun removeFailedDeleteRequest(request: DeleteTaskRequest) {
        val existingList: MutableList<DeleteTaskRequest> =
            getFailedDeleteRequests().toMutableList()

        existingList.remove(request)

        val serializedRequests = gson.toJson(existingList.toList())
        editor.putString(FAILED_DELETE_REQUESTS_KEY, serializedRequests).apply()
    }

    fun getFailedEditRequests(): List<EditTaskRequest> {
        val serializedRequests = sharedPreferences.getString(FAILED_EDIT_REQUESTS_KEY, null)

        return if (serializedRequests != null) {
            gson.fromJson(serializedRequests, object : TypeToken<List<EditTaskRequest>>() {}.type)
        } else {
            listOf()
        }
    }

    fun insertFailedEditRequest(request: EditTaskRequest) {
        val existingList: MutableList<EditTaskRequest> =
            getFailedEditRequests().toMutableList()

        existingList.add(request)

        val serializedRequests = gson.toJson(existingList.toList())
        editor.putString(FAILED_EDIT_REQUESTS_KEY, serializedRequests).apply()
    }

    fun removeFailedEditRequest(request: EditTaskRequest) {
        val existingList: MutableList<EditTaskRequest> =
            getFailedEditRequests().toMutableList()

        existingList.remove(request)

        val serializedRequests = gson.toJson(existingList.toList())
        editor.putString(FAILED_EDIT_REQUESTS_KEY, serializedRequests).apply()
    }

    fun saveUser(user: User) {
        val serializedUser = gson.toJson(user)
        editor.putString(USER_KEY, serializedUser).apply()
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
        editor.putBoolean(ENTRANCE_STATE_KEY, state).apply()
    }

    fun getEntranceState(): Boolean {
        return sharedPreferences.getBoolean(ENTRANCE_STATE_KEY, false)
    }

}