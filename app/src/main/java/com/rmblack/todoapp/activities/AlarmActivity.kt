package com.rmblack.todoapp.activities

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.ActivityAlarmBinding
import com.rmblack.todoapp.utils.PersianNum
import com.rmblack.todoapp.viewmodels.AlarmViewModel
import java.util.UUID

class AlarmActivity : AppCompatActivity() {

    private lateinit var viewModel: AlarmViewModel

    private lateinit var binding: ActivityAlarmBinding

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        val taskIDString = intent.getStringExtra(ALARM_ID)
        viewModel.setData(UUID.fromString(taskIDString))
        setUI()
        mediaPlayer = playSound()
    }

    private fun setUI() {
        viewModel.task.observe(this) {
            it?.let {
                binding.title.text = it.title

                var hourStr = PersianNum.convert(it.deadLine.hourOfDay.toString())
                if (hourStr.length == 1) {
                    hourStr = "۰$hourStr"
                }
                var minuteStr = PersianNum.convert(it.deadLine.minute.toString())
                if (minuteStr.length == 1) {
                    minuteStr = "۰$minuteStr"
                }
                val formattedText = String.format(getString(R.string.clock_format), hourStr, minuteStr)
                binding.clockTv.text = formattedText
            }
        }

        binding.closeBtn.setOnClickListener {
            finish()
        }
    }

    private fun playSound(): MediaPlayer {
        val mediaPlayer = MediaPlayer.create(this, R.raw.soft_alarm_2010)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }
        return mediaPlayer
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetAlarmState()
        mediaPlayer.release()
    }

    companion object {
        const val ALARM_ID = "alarm_title"
    }
}