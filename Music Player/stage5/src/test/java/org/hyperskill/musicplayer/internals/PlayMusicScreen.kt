package org.hyperskill.musicplayer.internals

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import org.hyperskill.musicplayer.MainActivity
import org.junit.Assert.assertEquals

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
        const val ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE = "songItemImgBtnPlayPause"
        const val ID_SONG_ITEM_TV_ARTIST = "songItemTvArtist"
        const val ID_SONG_ITEM_TV_TITLE = "songItemTvTitle"
        const val ID_SONG_ITEM_TV_DURATION = "songItemTvDuration"
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
                assertEquals(
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
                assertEquals(
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
                assertEquals(
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
            assertEquals(
                messageWrongInitialBtnStopText,
                expectedBtnStopText,
                actualBtnStopText
            )
            controllerBtnStop
        }
    }

    fun songItemImgBtnPlayPauseSupplier(itemViewSupplier: () -> View) = with(test){
        itemViewSupplier().findViewByString<ImageButton>(ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE)
    }

    fun songItemImgBtnPlayPauseSupplier(itemView: View) = with(test){
        itemView.findViewByString<ImageButton>(ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE)
    }

    fun assertSongItem(errorMessage: String, itemView: View, song: SongFake) = with(test) {
        val songItemTvArtist = itemView.findViewByStringOrNull<TextView>(ID_SONG_ITEM_TV_ARTIST)
            ?: throw AssertionError("$errorMessage Could not find view $ID_SONG_ITEM_TV_ARTIST")
        val songItemTvTitle = itemView.findViewByStringOrNull<TextView>(ID_SONG_ITEM_TV_TITLE)
            ?: throw AssertionError("$errorMessage Could not find view $ID_SONG_ITEM_TV_TITLE")
        val songItemTvDuration = itemView.findViewByStringOrNull<TextView>(ID_SONG_ITEM_TV_DURATION)
            ?: throw AssertionError("$errorMessage Could not find view $ID_SONG_ITEM_TV_DURATION")

        assertEquals(errorMessage, song.artist, songItemTvArtist.text.toString())
        assertEquals(errorMessage, song.title, songItemTvTitle.text.toString())
        assertEquals(
            errorMessage,
            song.duration.timeString(),
            songItemTvDuration.text.toString()
        )
    }
}