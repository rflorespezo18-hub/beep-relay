package com.example.beeprelay

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private val sampleRate = 44100
    private val freqHz = 1000
    private val toneMs = 100
    private val pauseMs = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            while (true) {
                playTwoTones()
            }
        }
    }

    private suspend fun playTwoTones() {
        withContext(Dispatchers.Default) {
            playTone(freqHz, toneMs)
            delay(pauseMs.toLong())
            playTone(freqHz, toneMs)
            delay(1500L) // brief pause before repeating
        }
    }

    private suspend fun playTone(freqHz: Int, durationMs: Int) {
        val samples = buildSineWave(freqHz, durationMs)

        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = maxOf(samples.size * 2, minBuffer)
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STATIC
        )

        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()
        delay(durationMs.toLong())
        audioTrack.stop()
        audioTrack.release()
    }

    private fun buildSineWave(freqHz: Int, durationMs: Int): ShortArray {
        val numSamples = (sampleRate * durationMs / 1000.0).toInt()
        val result = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val phase = 2.0 * PI * i * freqHz / sampleRate
            val amplitude = sin(phase) * Short.MAX_VALUE
            result[i] = amplitude.toInt().toShort()
        }
        return result
    }
}
