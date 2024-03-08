package org.hyperskill.musicplayer.internals

import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import org.hyperskill.musicplayer.MainActivity
import org.junit.Assert

class PlayMusicScreen(
    private val test: MusicPlayerUnitTests<MainActivity>,
    val initAssertions: Boolean = false
) : MusicPlayerBaseScreen(test) {
    companion object {
        const val ID_CONTROLLER_TV_CURRENT_TIME = "controllerTvCurrentTime"
        const val ID_CONTROLLER_TV_TOTAL_TIME = "controllerTvTotalTime"
        const val ID_CONTROLLER_SEEKBAR = "controllerSeekBar"
        const val ID_CONTROLLER_BTN_PLAY_PAUSE = "controllerBtnPlayPause"
        const val ID_CONTROLLER_BTN_STOP = "controllerBtnStop"
    }

    val controllerTvCurrentTime by lazy {
        with(test) {
            val controllerTvCurrentTime = mainFragmentContainer
                .findViewByString<TextView>(ID_CONTROLLER_TV_CURRENT_TIME)
            if(initAssertions) {
                val actualCurrentTime = controllerTvCurrentTime.text.toString()
                val expectedCurrentTime = "00:00"
                val messageWrongInitialCurrentTime =
                    "Wrong initial value for $ID_CONTROLLER_TV_CURRENT_TIME"
                Assert.assertEquals(
                    messageWrongInitialCurrentTime,
                    expectedCurrentTime,
                    actualCurrentTime
                )
            }
            controllerTvCurrentTime
        }
    }

    val controllerTvTotalTime by lazy {
        with(test) {
            val controllerTvTotalTime = mainFragmentContainer
                .findViewByString<TextView>(ID_CONTROLLER_TV_TOTAL_TIME)
            if(initAssertions) {
                val actualTotalTime = controllerTvTotalTime.text.toString()
                val expectedTotalTime = "00:00"
                val messageWrongInitialTotalTime =
                    "Wrong initial value for $ID_CONTROLLER_TV_TOTAL_TIME"
                Assert.assertEquals(
                    messageWrongInitialTotalTime,
                    expectedTotalTime,
                    actualTotalTime
                )
            }
            controllerTvTotalTime
        }
    }

    val controllerSeekBar by lazy {
        with(test) {
            mainFragmentContainer
                .findViewByString<SeekBar>(ID_CONTROLLER_SEEKBAR)
        }
    }

    val controllerBtnPlayPause by lazy {
        with(test) {
            val controllerBtnPlayPause = mainFragmentContainer
                .findViewByString<Button>(ID_CONTROLLER_BTN_PLAY_PAUSE)
            if(initAssertions) {
                val actualBtnPlayPauseText = controllerBtnPlayPause.text.toString().lowercase()
                val expectedBtnPlayPauseText = "play/pause"
                val messageWrongInitialBtnPlayPauseText =
                    "Wrong initial value for $ID_CONTROLLER_BTN_PLAY_PAUSE"
                Assert.assertEquals(
                    messageWrongInitialBtnPlayPauseText,
                    expectedBtnPlayPauseText,
                    actualBtnPlayPauseText
                )
            }
            controllerBtnPlayPause
        }
    }

    val controllerBtnStop by lazy {
        with(test) {
            val controllerBtnStop = mainFragmentContainer
                .findViewByString<Button>(ID_CONTROLLER_BTN_STOP)
            val actualBtnStopText = controllerBtnStop.text.toString().lowercase()
            val expectedBtnStopText = "stop"
            val messageWrongInitialBtnStopText = "Wrong initial value for $ID_CONTROLLER_BTN_STOP"
            Assert.assertEquals(
                messageWrongInitialBtnStopText,
                expectedBtnStopText,
                actualBtnStopText
            )
            controllerBtnStop
        }
    }
}