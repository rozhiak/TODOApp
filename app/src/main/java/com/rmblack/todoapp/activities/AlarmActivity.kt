package com.rmblack.todoapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.PersistableBundle

class AlarmActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        // TODO show title and description

        // TODO play sound

    }

    private fun showAlarmDialog() {
        val title = intent.getStringExtra(ALARM_TITLE)
        val description = intent.getStringExtra(ALARM_DESCRIPTION)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(description)
            .setCancelable(false)
            .setPositiveButton("باشه") { dialog, _ ->
                dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun playSound(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, com.rmblack.todoapp.R.raw.soft_alarm_2010)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }
    }

    companion object {
        const val ALARM_TITLE = "alarm_title"
        const val ALARM_DESCRIPTION = "alarm_description"
    }

}