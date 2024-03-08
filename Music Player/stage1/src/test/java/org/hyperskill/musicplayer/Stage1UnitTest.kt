package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import org.hyperskill.musicplayer.internals.CustomMediaPlayerShadow
import org.hyperskill.musicplayer.internals.CustomShadowAsyncDifferConfig
import org.hyperskill.musicplayer.internals.MusicPlayerActivityScreen
import org.hyperskill.musicplayer.internals.MusicPlayerActivityScreen.Companion.mainMenuItemIdAddPlaylist
import org.hyperskill.musicplayer.internals.MusicPlayerActivityScreen.Companion.mainMenuItemIdDeletePlaylist
import org.hyperskill.musicplayer.internals.MusicPlayerActivityScreen.Companion.mainMenuItemIdLoadPlaylist
import org.hyperskill.musicplayer.internals.MusicPlayerUnitTests
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

// version 2.0
@RunWith(RobolectricTestRunner::class)
@Config(shadows = [CustomMediaPlayerShadow::class, CustomShadowAsyncDifferConfig::class])
class Stage1UnitTest : MusicPlayerUnitTests<MainActivity>(MainActivity::class.java){


    @Test
    fun checkMainActivityComponentsExist() {
        testActivity {
            MusicPlayerActivityScreen(this).apply {
                mainButtonSearch
                mainSongList
                mainFragmentContainer
            }
        }
    }

    @Test
    fun checkPlayerControllerFragmentComponentsExist() {
        testActivity {
            MusicPlayerActivityScreen(this).apply {
                mainFragmentContainer

                val controllerTvCurrentTime =
                    mainFragmentContainer.findViewByString<TextView>("controllerTvCurrentTime")


                val actualCurrentTime = controllerTvCurrentTime.text.toString()
                val expectedCurrentTime = "00:00"
                val messageWrongInitialCurrentTime = "Wrong initial value for controllerTvCurrentTime"
                assertEquals(messageWrongInitialCurrentTime, expectedCurrentTime, actualCurrentTime)

                val controllerTvTotalTime =
                    mainFragmentContainer.findViewByString<TextView>("controllerTvTotalTime")


                val actualTotalTime = controllerTvTotalTime.text.toString()
                val expectedTotalTime = "00:00"
                val messageWrongInitialTotalTime = "Wrong initial value for controllerTvTotalTime"
                assertEquals(messageWrongInitialTotalTime, expectedTotalTime, actualTotalTime)

                mainFragmentContainer.findViewByString<SeekBar>("controllerSeekBar")

                val controllerBtnPlayPause =
                    mainFragmentContainer.findViewByString<Button>("controllerBtnPlayPause")

                val actualBtnPlayPauseText = controllerBtnPlayPause.text.toString().lowercase()
                val expectedBtnPlayPauseText = "play/pause"
                val messageWrongInitialBtnPlayPauseText = "Wrong initial value for controllerBtnPlayPause"
                assertEquals(messageWrongInitialBtnPlayPauseText, expectedBtnPlayPauseText, actualBtnPlayPauseText)

                val controllerBtnStop =
                    mainFragmentContainer.findViewByString<Button>("controllerBtnStop")
                val actualBtnStopText = controllerBtnStop.text.toString().lowercase()
                val expectedBtnStopText = "stop"
                val messageWrongInitialBtnStopText = "Wrong initial value for controllerBtnStop"
                assertEquals(messageWrongInitialBtnStopText, expectedBtnStopText, actualBtnStopText)
            }

        }
    }

    @Test
    fun checkSearchButtonNoSongsFound() {
        testActivity {
            MusicPlayerActivityScreen(this).apply {
                mainButtonSearch

                mainButtonSearch.clickAndRun()
                assertLastToastMessageEquals(
                    "wrong toast message after click to mainButtonSearch",
                    "no songs found"
                )
            }
        }
    }

    @Test
    fun checkMenuItemAddPlaylist() {
        testActivity {
            MusicPlayerActivityScreen(this).apply {
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
                assertLastToastMessageEquals(
                    "wrong toast message after click to mainMenuItemIdAddPlaylist",
                    "no songs loaded, click search to load songs"
                )
            }
        }
    }

    @Test
    fun checkMenuItemLoadPlaylist() {
        testActivity {
            MusicPlayerActivityScreen(this).apply {
                activity.clickMenuItemAndRun(mainMenuItemIdLoadPlaylist)

                val (alertDialog, shadowAlertDialog) = getLastAlertDialogWithShadow(
                    errorMessageNotFound = "No Dialog was shown after click on mainMenuLoadPlaylist."
                )

                val actualTitle = shadowAlertDialog.title.toString().lowercase()
                val messageWrongTitle =
                    "Wrong title found on dialog shown after click on mainMenuLoadPlaylist"
                val expectedTitle = "choose playlist to load"
                assertEquals(messageWrongTitle, expectedTitle, actualTitle)


                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
            }
        }
    }

    @Test
    fun checkMenuItemDeletePlaylist() {
        testActivity {
            MusicPlayerActivityScreen(this).apply {
                activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)


                val (alertDialog, shadowAlertDialog) = getLastAlertDialogWithShadow(
                    errorMessageNotFound = "No Dialog was shown after click on mainMenuDeletePlaylist."
                )

                val actualTitle = shadowAlertDialog.title.toString().lowercase()
                val messageWrongTitle =
                    "Wrong title found on dialog shown after click on mainMenuDeletePlaylist"
                val expectedTitle = "choose playlist to delete"
                assertEquals(messageWrongTitle, expectedTitle, actualTitle)


                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
            }
        }
    }

    @Test
    fun checkControllerStopButtonBeforeSearch() {

        testActivity {
            MusicPlayerActivityScreen(this).apply {
                mainFragmentContainer

                val controllerBtnStop =
                    mainFragmentContainer.findViewByString<Button>("controllerBtnStop")

                controllerBtnStop.clickAndRun()
                // should not throw Exception
            }
        }
    }

    @Test
    fun checkControllerSeekBarBeforeSearch() {

        testActivity {
            MusicPlayerActivityScreen(this).apply {
                mainFragmentContainer

                val controllerSeekBar =
                    mainFragmentContainer.findViewByString<SeekBar>("controllerSeekBar")

                if (Shadows.shadowOf(controllerSeekBar).onSeekBarChangeListener != null) {
                    controllerSeekBar.setProgressAsUser(1)
                    //should not throw exception
                } else {
                    // ok
                }
            }
        }
    }

    @Test
    fun checkControllerPlayPauseButtonBeforeSearch() {

        testActivity {
            MusicPlayerActivityScreen(this).apply {
                mainFragmentContainer

                val controllerBtnPlayPause =
                    mainFragmentContainer.findViewByString<Button>("controllerBtnPlayPause")

                controllerBtnPlayPause.clickAndRun()
                // should not throw Exception
            }
        }
    }
}