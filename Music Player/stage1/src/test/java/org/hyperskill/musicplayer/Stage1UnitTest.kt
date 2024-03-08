package org.hyperskill.musicplayer

import android.app.AlertDialog
import org.hyperskill.musicplayer.internals.CustomMediaPlayerShadow
import org.hyperskill.musicplayer.internals.CustomShadowAsyncDifferConfig
import org.hyperskill.musicplayer.internals.MusicPlayerBaseScreen
import org.hyperskill.musicplayer.internals.MusicPlayerBaseScreen.Companion.mainMenuItemIdAddPlaylist
import org.hyperskill.musicplayer.internals.MusicPlayerBaseScreen.Companion.mainMenuItemIdDeletePlaylist
import org.hyperskill.musicplayer.internals.MusicPlayerBaseScreen.Companion.mainMenuItemIdLoadPlaylist
import org.hyperskill.musicplayer.internals.MusicPlayerUnitTests
import org.hyperskill.musicplayer.internals.PlayMusicScreen
import org.hyperskill.musicplayer.internals.PlayMusicScreen.Companion.ID_CONTROLLER_BTN_PLAY_PAUSE
import org.hyperskill.musicplayer.internals.PlayMusicScreen.Companion.ID_CONTROLLER_BTN_STOP
import org.hyperskill.musicplayer.internals.PlayMusicScreen.Companion.ID_CONTROLLER_SEEKBAR
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
    fun checkMainActivityComponentsExist() = testActivity {
        MusicPlayerBaseScreen(this).apply {
            mainButtonSearch
            mainSongList
            mainFragmentContainer
        }
        Unit
    }

    @Test
    fun checkPlayerControllerFragmentComponentsExist() = testActivity {
        PlayMusicScreen(this, initAssertions = true).apply {
            controllerTvCurrentTime // assert exists
            controllerTvTotalTime
            controllerSeekBar
            controllerBtnPlayPause
            controllerBtnStop
        }
        Unit
    }

    @Test
    fun checkSearchButtonNoSongsFound() = testActivity {
        PlayMusicScreen(this).apply {
            mainButtonSearch.clickAndRun()
            assertLastToastMessageEquals(
                "wrong toast message after click to mainButtonSearch",
                "no songs found"
            )
        }
        Unit
    }

    @Test
    fun checkMenuItemAddPlaylist() = testActivity {
        PlayMusicScreen(this).apply {
            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            assertLastToastMessageEquals(
                "wrong toast message after click to mainMenuItemIdAddPlaylist",
                "no songs loaded, click search to load songs"
            )
        }
        Unit
    }

    @Test
    fun checkMenuItemLoadPlaylist() = testActivity {
        PlayMusicScreen(this).apply {
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
        Unit
    }

    @Test
    fun checkMenuItemDeletePlaylist() = testActivity {
        PlayMusicScreen(this).apply {
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
        Unit
    }

    @Test
    fun checkControllerStopButtonBeforeSearch() = testActivity {
        PlayMusicScreen(this).apply {
            try { controllerBtnStop.clickAndRun() }
            catch (t: Throwable) {
                throw AssertionError("Click on $ID_CONTROLLER_BTN_STOP before " +
                        "search should not throw exception",
                    t
                )
            }
        }
        Unit
    }

    @Test
    fun checkControllerSeekBarBeforeSearch() = testActivity {
        PlayMusicScreen(this).apply {
            if (Shadows.shadowOf(controllerSeekBar).onSeekBarChangeListener != null) {
                try { controllerSeekBar.setProgressAsUser(1) }
                catch (t: Throwable) {
                    throw AssertionError("Dragging $ID_CONTROLLER_SEEKBAR before " +
                            "search should not throw exception",
                        t
                    )
                }
            } else {
                // ok
            }
        }
        Unit
    }

    @Test
    fun checkControllerPlayPauseButtonBeforeSearch() = testActivity {
        PlayMusicScreen(this).apply {
            try { controllerBtnPlayPause.clickAndRun() }
            catch (t: Throwable) {
                throw AssertionError("Click on $ID_CONTROLLER_BTN_PLAY_PAUSE before " +
                        "search should not throw exception",
                    t
                )
            }
        }
        Unit
    }
}