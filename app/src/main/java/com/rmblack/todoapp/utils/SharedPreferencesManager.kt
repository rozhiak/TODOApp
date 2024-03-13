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

private const val CONNECTED_PHONES_KEY = "CONNECTED_PHONES_KEY"

private const val CASHED_ADD_REQUESTS_KEY = "CASHED_ADD_REQUESTS_KEY"

private const val CASHED_DELETE_REQUESTS_KEY = "CASHED_DELETE_REQUESTS_KEY"

private const val CASHED_EDIT_REQUESTS_KEY = "CASHED_EDIT_REQUESTS_KEY"

private const val DO_NOT_SHOW_DONE_TASKS_KEY = "DO_NOT_SHOW_DONE_TASKS_KEY"

private const val AUTO_START_PERMISSION_CHECK_KEY = "AUTO_START_PERMISSION_CHECK_KEY"

class SharedPreferencesManager(private val context: Context) {

    private val gson = Gson()

    fun setDoNotShowDoneTasks(state: Boolean) {
        getEditor(context).putBoolean(DO_NOT_SHOW_DONE_TASKS_KEY, state).apply()
    }

    fun getDoNotShowDoneTasksState(): Boolean {
        return getSharedPreferences(context).getBoolean(DO_NOT_SHOW_DONE_TASKS_KEY, false)
    }

    fun getCashedAddRequests(): List<AddTaskRequest> {
        val serializedRequests =
            getSharedPreferences(context).getString(CASHED_ADD_REQUESTS_KEY, null)
        return if (serializedRequests != null) {
            gson.fromJson(serializedRequests, object : TypeToken<List<AddTaskRequest>>() {}.type)
        } else {
            listOf()
        }
    }

    fun insertCashedAddRequest(request: AddTaskRequest) {
        val existingList: MutableList<AddTaskRequest> = getCashedAddRequests().toMutableList()

        existingList.add(request)

        val serializedRequests = gson.toJson(existingList.toList())
        getEditor(context).putString(CASHED_ADD_REQUESTS_KEY, serializedRequests).apply()
    }

    fun removeCashedAddRequest(request: AddTaskRequest) {
        val existingList: MutableList<AddTaskRequest> = getCashedAddRequests().toMutableList()

        existingList.remove(request)

        val serializedRequests = gson.toJson(existingList.toList())
        getEditor(context).putString(CASHED_ADD_REQUESTS_KEY, serializedRequests).apply()
    }

    fun getCashedDeleteRequests(): List<DeleteTaskRequest> {
        val serializedRequests =
            getSharedPreferences(context).getString(CASHED_DELETE_REQUESTS_KEY, null)

        return if (serializedRequests != null) {
            gson.fromJson(serializedRequests, object : TypeToken<List<DeleteTaskRequest>>() {}.type)
        } else {
            listOf()
        }
    }

    fun insertCashedDeleteRequest(request: DeleteTaskRequest) {
        val existingList: MutableList<DeleteTaskRequest> = getCashedDeleteRequests().toMutableList()

        existingList.add(request)

        val serializedRequests = gson.toJson(existingList.toList())
        getEditor(context).putString(CASHED_DELETE_REQUESTS_KEY, serializedRequests).apply()
    }

    fun removeCashedDeleteRequest(request: DeleteTaskRequest) {
        val existingList: MutableList<DeleteTaskRequest> = getCashedDeleteRequests().toMutableList()

        existingList.remove(request)

        val serializedRequests = gson.toJson(existingList.toList())
        getEditor(context).putString(CASHED_DELETE_REQUESTS_KEY, serializedRequests).apply()
    }

    fun getCashedEditRequests(): List<EditTaskRequest> {
        val serializedRequests =
            getSharedPreferences(context).getString(CASHED_EDIT_REQUESTS_KEY, null)

        return if (serializedRequests != null) {
            gson.fromJson(serializedRequests, object : TypeToken<List<EditTaskRequest>>() {}.type)
        } else {
            listOf()
        }
    }

    fun insertCashedEditRequest(request: EditTaskRequest) {
        val existingList: MutableList<EditTaskRequest> = getCashedEditRequests().toMutableList()

        existingList.add(request)

        val serializedRequests = gson.toJson(existingList.toList())
        getEditor(context).putString(CASHED_EDIT_REQUESTS_KEY, serializedRequests).apply()
    }

    fun removeCashedEditRequest(request: EditTaskRequest) {
        val existingList: MutableList<EditTaskRequest> = getCashedEditRequests().toMutableList()

        existingList.remove(request)

        val serializedRequests = gson.toJson(existingList.toList())
        getEditor(context).putString(CASHED_EDIT_REQUESTS_KEY, serializedRequests).apply()
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

    fun saveConnectedPhones(phones: List<String>) {
        val serializedPhones = gson.toJson(phones)
        getEditor(context).putString(CONNECTED_PHONES_KEY, serializedPhones).apply()
    }

    //If it returned null => user is not connected and when it returned a list => user is connected
    fun getConnectedPhone(): List<String>? {
        val serializedPhones = getSharedPreferences(context).getString(CONNECTED_PHONES_KEY, null)
        return if (serializedPhones != null) {
            gson.fromJson(serializedPhones, object : TypeToken<List<String>>() {}.type)
        } else {
            null
        }
    }

    fun removeConnectedPhones() {
        getEditor(context).remove(CONNECTED_PHONES_KEY).apply()
    }

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun getAutoStartPermissionCheckState(): Boolean {
        return getSharedPreferences(context).getBoolean(AUTO_START_PERMISSION_CHECK_KEY, false)
    }

    fun setAutoStartPermissionCheckState(state: Boolean) {
        getEditor(context).putBoolean(AUTO_START_PERMISSION_CHECK_KEY, state).apply()
    }

    companion object {
        private var sharedPreferences: SharedPreferences? = null
        private val spLOCK = Any()
        private fun getSharedPreferences(context: Context): SharedPreferences {
            synchronized(spLOCK) {
                if (sharedPreferences == null) {
                    sharedPreferences =
                        context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                }
                return sharedPreferences!!
            }
        }

        private var editor: SharedPreferences.Editor? = null
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