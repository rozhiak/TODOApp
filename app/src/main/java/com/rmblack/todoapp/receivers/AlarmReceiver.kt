package com.rmblack.todoapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        Toast.makeText(p0, "Alarm", Toast.LENGTH_LONG).show()

        // todo put null on alarm property of related task

    }

}