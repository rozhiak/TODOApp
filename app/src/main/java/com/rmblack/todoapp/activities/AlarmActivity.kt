package com.rmblack.todoapp.activities

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.ActivityAlarmBinding
import com.rmblack.todoapp.viewmodels.AlarmViewModel
import java.util.UUID

class AlarmActivity : AppCompatActivity() {

    private lateinit var viewModel: AlarmViewModel

    private lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        val taskIDString = intent.getStringExtra(ALARM_ID)
        viewModel.setData(UUID.fromString(taskIDString))
        setUI()
        playSound()
        viewModel.resetAlarmState()
    }

    private fun setUI() {
        viewModel.task.observe(this) {

        }
    }

    private fun playSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.soft_alarm_2010)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }
    }

    companion object {
        const val ALARM_ID = "alarm_title"
    }
}