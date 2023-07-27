package com.rmblack.todoapp.models

data class User(var name: String,
                val phoneNumber: String,
                val privateTasksId: String,
                var sharedTasksId: String)
