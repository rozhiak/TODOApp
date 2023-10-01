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

private const val CONNECTED_PHONE_KEY = "CONNECTED_PHONE_KEY"

private const val FAILED_ADD_REQUESTS_KEY = "FAILED_ADD_REQUESTS_KEY"

private const val FAILED_DELETE_REQUESTS_KEY = "FAILED_DELETE_REQUESTS_KEY"

private const val FAILED_EDIT_REQUESTS_KEY = "FAILED_EDIT_REQUESTS_KEY"


class SharedPreferencesManager(private val context: Context) {

    private val gson = Gson()

    fun getFailedAddRequests(): List<AddTaskRequest> {
        val serializedRequests = getSharedPreferences(context).getString(FAILED_ADD_REQUESTS_KEY, null)

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
        getEditor(context).putString(FAILED_ADD_REQUESTS_KEY, serializedRequests).apply()
    }

    fun removeFailedAddRequest(request: AddTaskRequest) {
        val existingList: MutableList<AddTaskRequest> =
            getFailedAddRequests().toMutableList()

        existingList.remove(request)

        val serializedRequests = gson.toJson(existingList.toList())
        getEditor(context).putString(FAILED_ADD_REQUESTS_KEY, serializedRequests).apply()
    }

    fun getFailedDeleteRequests(): List<DeleteTaskRequest> {
        val serializedRequests = getSharedPreferences(context).getString(FAILED_DELETE_REQUESTS_KEY, null)

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
        getEditor(context).putString(FAILED_DELETE_REQUESTS_KEY, serializedRequests).apply()
    }

    fun removeFailedDeleteRequest(request: DeleteTaskRequest) {
        val existingList: MutableList<DeleteTaskRequest> =
            getFailedDeleteRequests().toMutableList()

        existingList.remove(request)

        val serializedRequests = gson.toJson(existingList.toList())
        getEditor(context).putString(FAILED_DELETE_REQUESTS_KEY, serializedRequests).apply()
    }

    fun getFailedEditRequests(): List<EditTaskRequest> {
        val serializedRequests = getSharedPreferences(context).getString(FAILED_EDIT_REQUESTS_KEY, null)

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
        getEditor(context).putString(FAILED_EDIT_REQUESTS_KEY, serializedRequests).apply()
    }

    fun removeFailedEditRequest(request: EditTaskRequest) {
        val existingList: MutableList<EditTaskRequest> =
            getFailedEditRequests().toMutableList()

        existingList.remove(request)

        val serializedRequests = gson.toJson(existingList.toList())
        getEditor(context).putString(FAILED_EDIT_REQUESTS_KEY, serializedRequests).apply()
    }

    fun saveUser(user: User) {
        val serializedUser = gson.toJson(user)
        getEditor(context).putString(USER_KEY, serializedUser).apply()
    }

    fun getUser(): User? {
        val serializedUser = getSharedPreferences(context).getString(USER_KEY, null)
        return if (serializedUser != null) {
            gson.fromJson(serializedUser, User::class.java)
        } else {
            null
        }
    }

    fun saveEntranceState(state: Boolean) {
        getEditor(context).putBoolean(ENTRANCE_STATE_KEY, state).apply()
    }

    fun getEntranceState(): Boolean {
        return getSharedPreferences(context).getBoolean(ENTRANCE_STATE_KEY, false)
    }

    fun saveConnectedPhone(phone: String) {
        getEditor(context).putString(CONNECTED_PHONE_KEY ,phone).apply()
    }

    fun getConnectedPhone(): String? {
        return getSharedPreferences(context).getString(CONNECTED_PHONE_KEY, null)
    }

    companion object {
        private var sharedPreferences: SharedPreferences? = null
        private val spLOCK = Any()
        private fun getSharedPreferences(context: Context): SharedPreferences {
            synchronized(spLOCK) {
                if (sharedPreferences == null) {
                    sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                }
                return sharedPreferences!!
            }
        }

        private var editor : SharedPreferences.Editor? = null
        private val editorLOCK = Any()
        private fun getEditor(context: Context): SharedPreferences.Editor {
            synchronized(editorLOCK) {
                if (editor == null) {
                    editor = getSharedPreferences(context).edit()
                }
                return editor!!
            }
        }


    }

}