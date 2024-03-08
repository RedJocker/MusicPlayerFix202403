package org.hyperskill.musicplayer

import android.Manifest
import android.app.AlertDialog
import android.graphics.Color
import android.widget.*
import org.hyperskill.musicplayer.internals.AddPlaylistScreen

import org.hyperskill.musicplayer.internals.CustomMediaPlayerShadow
import org.hyperskill.musicplayer.internals.CustomShadowAsyncDifferConfig
import org.hyperskill.musicplayer.internals.MusicPlayerBaseScreen.Companion.ID_MAIN_BUTTON_SEARCH
import org.hyperskill.musicplayer.internals.MusicPlayerBaseScreen.Companion.mainMenuItemIdAddPlaylist
import org.hyperskill.musicplayer.internals.MusicPlayerBaseScreen.Companion.mainMenuItemIdDeletePlaylist
import org.hyperskill.musicplayer.internals.MusicPlayerBaseScreen.Companion.mainMenuItemIdLoadPlaylist
import org.hyperskill.musicplayer.internals.MusicPlayerUnitTests
import org.hyperskill.musicplayer.internals.PlayMusicScreen
import org.hyperskill.musicplayer.internals.PlayMusicScreen.Companion.ID_CONTROLLER_BTN_PLAY_PAUSE
import org.hyperskill.musicplayer.internals.PlayMusicScreen.Companion.ID_CONTROLLER_BTN_STOP
import org.hyperskill.musicplayer.internals.PlayMusicScreen.Companion.ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE
import org.hyperskill.musicplayer.internals.SongFake
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

// version 1.4
@Config(shadows = [CustomMediaPlayerShadow::class, CustomShadowAsyncDifferConfig::class])
@RunWith(RobolectricTestRunner::class)
class Stage2UnitTest : MusicPlayerUnitTests<MainActivity>(MainActivity::class.java){

    companion object {
        val songFakeList = (1..10).map { idNum ->
            SongFake(
                    id = idNum,
                    artist = "artist$idNum",
                    title = "title$idNum",
                    duration = 215_000
            )
        }
    }

    @Before
    fun setUp() {
        setupContentProvider(songFakeList)
        shadowActivity.grantPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
        CustomMediaPlayerShadow.setFakeSong(songFakeList[0])
    }


    @Test
    fun checkSongListAfterInitialClickOnSearch() = testActivity {
        PlayMusicScreen(this).apply {
            mainButtonSearch.clickAndRun()

            mainSongList.assertListItems(
                songFakeList,
                "on init after clicking on $ID_MAIN_BUTTON_SEARCH"
            ) { itemViewSupplier, index, songFake ->
                val itemView = itemViewSupplier()
                assertSongItem("Wrong data after search.", itemView, songFake)

                val songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemView)

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "When a song from the song list is stopped " +
                            "the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should be R.drawable.ic_play",
                    R.drawable.ic_play
                )
            }
        }
        Unit
    }

    @Test
    fun checkSongListItemChangesImageOnImageButtonClick() = testActivity {
        PlayMusicScreen(this).apply {
            mainButtonSearch.clickAndRun()
            val songFakeIndex = 3
            CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndex])

            mainSongList.assertSingleListItem(
                songFakeIndex,
                "on init after clicking on $ID_MAIN_BUTTON_SEARCH"
            ) { itemViewSupplier ->
                var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "When a song from the song list is stopped " +
                            "the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should be R.drawable.ic_play",
                    R.drawable.ic_play
                )

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a stopped song " +
                            "the image displayed should change to R.drawable.ic_pause",
                    R.drawable.ic_pause
                )

                songItemImgBtnPlayPause.clickAndRun()
                songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                    "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a playing song " +
                            "the image displayed should change to R.drawable.ic_play",
                    R.drawable.ic_play
                )
            }
        }
        Unit
    }

    @Test
    fun checkWhenCurrentTrackChangesAndOldCurrentTrackIsPlayingImageChangesToPaused() {

        testActivity {
            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                val songFakeIndexBefore = 5
                val songFakeIndexAfter = 7

                CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndexBefore])
                mainSongList.assertSingleListItem(songFakeIndexBefore) { itemViewSupplierBefore ->
                    var songItemImgBtnPlayPauseBefore = songItemImgBtnPlayPauseSupplier(itemViewSupplierBefore)
                    songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                        "When a song from the song list is stopped " +
                                "the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should be R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPauseBefore.clickAndRun()
                    songItemImgBtnPlayPauseBefore = songItemImgBtnPlayPauseSupplier(itemViewSupplierBefore)

                    songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a stopped song" +
                                " the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )

                    CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndexAfter])
                    shadowLooper.idleFor(10_000L, TimeUnit.MILLISECONDS)
                    mainSongList.assertSingleListItem(songFakeIndexAfter) { itemViewSupplierAfter ->
                        var songItemImgBtnPlayPauseAfter = songItemImgBtnPlayPauseSupplier(itemViewSupplierAfter)
                        songItemImgBtnPlayPauseAfter.drawable.assertCreatedFromResourceId(
                            "When a song from the song list is stopped " +
                                    "the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should be R.drawable.ic_play",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPauseAfter.clickAndRun()
                        songItemImgBtnPlayPauseAfter = songItemImgBtnPlayPauseSupplier(itemViewSupplierAfter)

                        songItemImgBtnPlayPauseAfter.drawable.assertCreatedFromResourceId(
                            "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a paused song " +
                                    "the image displayed should change to R.drawable.ic_pause",
                            R.drawable.ic_pause
                        )

                    }
                    songItemImgBtnPlayPauseBefore = songItemImgBtnPlayPauseSupplier(itemViewSupplierBefore)

                    songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                        "After changing the currentTrack with the old currentTrack playing" +
                                "the image displayed on the old currentTrack should change to R.drawable.ic_play",
                        R.drawable.ic_play
                    )
                }
            }
        }
    }

    @Test
    fun checkWhenCurrentTrackChangesAndOldCurrentTrackIsNotPlayingImageRemains() {

        testActivity {
            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                val songFakeIndexBefore = 5
                val songFakeIndexAfter = 7

                CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndexBefore])
                mainSongList.assertSingleListItem(songFakeIndexBefore) { itemViewSupplierBefore ->
                    var songItemImgBtnPlayPauseBefore = songItemImgBtnPlayPauseSupplier(itemViewSupplierBefore)

                    songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                        "When a song from the song list is paused the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE " +
                                "should be R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPauseBefore.clickAndRun()
                    songItemImgBtnPlayPauseBefore = songItemImgBtnPlayPauseSupplier(itemViewSupplierBefore)

                    songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a paused song " +
                                "the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )

                    songItemImgBtnPlayPauseBefore.clickAndRun()
                    songItemImgBtnPlayPauseBefore = songItemImgBtnPlayPauseSupplier(itemViewSupplierBefore)

                    songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a playing song " +
                                "the image displayed should change to R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndexAfter])
                    mainSongList.assertSingleListItem(songFakeIndexAfter) { itemViewSupplierAfter ->
                        var songItemImgBtnPlayPauseAfter = songItemImgBtnPlayPauseSupplier(itemViewSupplierAfter)

                        songItemImgBtnPlayPauseAfter.drawable.assertCreatedFromResourceId(
                            "When a song from the song list is paused " +
                                    "the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should be R.drawable.ic_play",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPauseAfter.clickAndRun()
                        songItemImgBtnPlayPauseAfter = songItemImgBtnPlayPauseSupplier(itemViewSupplierAfter)

                        songItemImgBtnPlayPauseAfter.drawable.assertCreatedFromResourceId(
                            "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a paused song " +
                                    "the image displayed should change to R.drawable.ic_pause",
                            R.drawable.ic_pause
                        )
                    }

                    songItemImgBtnPlayPauseBefore = songItemImgBtnPlayPauseSupplier(itemViewSupplierBefore)
                    songItemImgBtnPlayPauseBefore.drawable.assertCreatedFromResourceId(
                        "After changing the currentTrack with the old currentTrack not playing " +
                                "the image displayed should remain being R.drawable.ic_play",
                        R.drawable.ic_play
                    )
                }
            }
        }
    }

    @Test
    fun checkAfterInitialSearchFirstListItemIsCurrentTrackAndRespondToControllerPlayPauseButton() {

        testActivity {
            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()

                mainSongList.assertSingleListItem(0) { itemViewSupplier ->

                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "When a song from the song list is paused " +
                                "the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should be R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()

                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a paused song" +
                                " the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )

                    controllerBtnPlayPause.clickAndRun()

                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a playing song" +
                                " the image displayed should change to R.drawable.ic_play",
                        R.drawable.ic_play
                    )
                }
            }
        }
    }

    @Test
    fun checkCurrentTrackImgChangeAfterControllerStopButtonClickWithCurrentTrackPlaying() {

        testActivity {
            PlayMusicScreen(this).apply {
                val songFakeIndex = 4
                mainButtonSearch.clickAndRun()

                CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndex])
                mainSongList.assertSingleListItem(songFakeIndex) { itemViewSupplier ->

                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)


                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "When a song from the song list is stopped " +
                                "the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should be R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a stopped song " +
                                "the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )

                    controllerBtnStop.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_CONTROLLER_BTN_STOP on a playing song " +
                                "the image displayed should change to R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a stopped song " +
                                "the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a playing song" +
                                "the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should change to R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    controllerBtnStop.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_CONTROLLER_BTN_STOP on a paused song " +
                                "the image displayed should remain R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    controllerBtnStop.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_CONTROLLER_BTN_STOP on a stopped song " +
                                "the image displayed should remain R.drawable.ic_play",
                        R.drawable.ic_play
                    )
                }
            }
        }
    }

    @Test
    fun checkListItemImgChangeMixedClicks() {

        testActivity {
            PlayMusicScreen(this).apply {
                mainButtonSearch
                mainFragmentContainer

                val songFakeIndex = 6

                mainButtonSearch.clickAndRun()

                CustomMediaPlayerShadow.setFakeSong(songFakeList[songFakeIndex])
                mainSongList.assertSingleListItem(songFakeIndex) { itemViewSupplier ->

                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    for (i in 1..10) {
                        /*
                        1 - paused -> songItemImgBtnPlayPause -> playing
                        2 - playing -> controllerBtnPlayPause -> paused
                        3 - paused -> controllerBtnPlayPause -> playing
                        4 - playing -> songItemImgBtnPlayPause -> paused
                        5 - paused -> controllerBtnPlayPause -> playing
                        6 - playing -> controllerBtnStop -> paused
                        7 - paused -> songItemImgBtnPlayPause -> playing
                        8 - playing -> controllerBtnPlayPause -> paused
                        9 - paused -> controllerBtnPlayPause -> playing
                        10 - playing -> songItemImgBtnPlayPause -> paused
                     */

                        val buttonClickedId = if (i == 6) {
                            controllerBtnStop.clickAndRun()
                            songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                            songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                                "After clicking on $ID_CONTROLLER_BTN_STOP on a playing song the image displayed should change to R.drawable.ic_play",
                                R.drawable.ic_play
                            )
                            continue
                        } else if (i % 3 == 1) {
                            songItemImgBtnPlayPause.clickAndRun()
                            songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                            ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE
                        } else {
                            controllerBtnPlayPause.clickAndRun()
                            songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                            ID_CONTROLLER_BTN_PLAY_PAUSE
                        }

                        if (i % 2 == 1) {
                            songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                                "After clicking on $buttonClickedId on a paused song the image displayed should change to R.drawable.ic_pause",
                                R.drawable.ic_pause
                            )
                        } else {
                            songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                                "After clicking on $buttonClickedId on a playing song the image displayed should change to R.drawable.ic_play",
                                R.drawable.ic_play
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun checkAddPlaylistStateTriggeredByMenuItem() {

        testActivity {
            PlayMusicScreen(this).apply {
                mainButtonSearch
                mainSongList

                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, index, itemSongFake ->
                    val listItemView = itemViewSupplier()
                    // check songSelector items
                    val songSelectorItemTvTitle: TextView =
                        listItemView.findViewByString("songSelectorItemTvTitle")
                    val songSelectorItemTvArtist: TextView =
                        listItemView.findViewByString("songSelectorItemTvArtist")
                    val songSelectorItemTvDuration: TextView =
                        listItemView.findViewByString("songSelectorItemTvDuration")
                    val songSelectorItemCheckBox: CheckBox =
                        listItemView.findViewByString("songSelectorItemCheckBox")

                    val actualSongTitle = songSelectorItemTvTitle.text.toString()
                    assertEquals(
                        "songSelectorItemTvTitle with incorrect text",
                        actualSongTitle,
                        itemSongFake.title
                    )

                    val actualSongArtist = songSelectorItemTvArtist.text.toString()
                    assertEquals(
                        "songSelectorItemTvArtist with incorrect text",
                        actualSongArtist,
                        itemSongFake.artist
                    )

                    val actualSongDuration = songSelectorItemTvDuration.text.toString()
                    val expectedSongDuration = itemSongFake.duration.timeString()
                    assertEquals(
                        "songSelectorItemTvDuration with incorrect text",
                        expectedSongDuration,
                        actualSongDuration,
                    )

                    assertEquals(
                        "No songSelectorItemCheckBox should be checked after click on $mainMenuItemIdAddPlaylist",
                        false,
                        songSelectorItemCheckBox.isChecked
                    )

                    listItemView.assertBackgroundColor(
                        errorMessage = "The backgroundColor for all songSelectorItems should be Color.WHITE after click on $mainMenuItemIdAddPlaylist",
                        expectedBackgroundColor = Color.WHITE
                    )
                    //
                }

                mainFragmentContainer.findViewByString<Button>("addPlaylistBtnCancel")
                    .also { addPlaylistBtnCancel ->
                        assertEquals(
                            "Wrong text for addPlaylistBtnCancel",
                            "cancel",
                            addPlaylistBtnCancel.text.toString().lowercase()
                        )
                    }
                mainFragmentContainer.findViewByString<Button>("addPlaylistBtnOk")
                    .also { addPlaylistBtnOk ->
                        assertEquals(
                            "Wrong text for addPlaylistBtnOk",
                            "ok",
                            addPlaylistBtnOk.text.toString().lowercase()
                        )
                    }
                mainFragmentContainer.findViewByString<EditText>("addPlaylistEtPlaylistName")
                    .also { addPlaylistEtPlaylistName ->
                        assertEquals(
                            "Wrong hint for addPlaylistEtPlaylistName",
                            "playlist name",
                            addPlaylistEtPlaylistName.hint.toString().lowercase()
                        )
                    }
            }
        }
    }

    @Test
    fun checkAddingPlaylistWithEmptyListAddedToastErrorEmptyListMessage() {
        testActivity {
            PlayMusicScreen(this).apply {
                mainButtonSearch
                mainSongList

                mainButtonSearch.clickAndRun()
            }
            AddPlaylistScreen(this).apply {
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

                val playlistName = "My Playlist"

                val addPlaylistButtonOk =
                    mainFragmentContainer.findViewByString<Button>("addPlaylistBtnOk")

                val addPlaylistEtPlaylistName =
                    mainFragmentContainer.findViewByString<EditText>("addPlaylistEtPlaylistName")
                addPlaylistEtPlaylistName.setText(playlistName)

                addPlaylistButtonOk.clickAndRun()

                assertLastToastMessageEquals(
                    errorMessage = "When there is no song selected a toast message is expected after click on addPlaylistBtnOk",
                    expectedMessage = "Add at least one song to your playlist"
                )
            }
        }
    }

    @Test
    fun checkAddingPlaylistWithBothEmptyListAndEmptyPlaylistNameToastErrorEmptyListMessage() {
        testActivity {
            PlayMusicScreen(this).apply {
                mainButtonSearch
                mainSongList

                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {

                val addPlaylistButtonOk =
                    mainFragmentContainer.findViewByString<Button>("addPlaylistBtnOk")

                addPlaylistButtonOk.clickAndRun()

                assertLastToastMessageEquals(
                    errorMessage = "When there is no song selected a toast message is expected after click on addPlaylistBtnOk",
                    expectedMessage = "Add at least one song to your playlist"
                )
            }
        }
    }

    @Test
    fun checkAddingPlaylistWithReservedPlaylistNameAllSongsToastErrorReservedNameMessage() {
        testActivity {
            PlayMusicScreen(this).apply {
                mainButtonSearch
                mainSongList

                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                val addPlaylistButtonOk =
                    mainFragmentContainer.findViewByString<Button>("addPlaylistBtnOk")

                val playlistName = "All Songs"
                val addPlaylistEtPlaylistName =
                    mainFragmentContainer.findViewByString<EditText>("addPlaylistEtPlaylistName")

                mainSongList.assertSingleListItem(0) {
                    it().clickAndRun()
                }

                addPlaylistEtPlaylistName.setText(playlistName)
                addPlaylistButtonOk.clickAndRun()

                assertLastToastMessageEquals(
                    errorMessage = "All Songs should be a reserve name. A toast with message",
                    expectedMessage = "All Songs is a reserved name choose another playlist name"
                )
            }
        }
    }

    @Test
    fun checkLoadPlaylistInPlayMusicStateAfterAddingPlaylistWithMainMenuItem() {

        testActivity {
            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val testedItemsOneBasedIndexes = listOf(2, 4, 7)
            val playlistName = "My Playlist"

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
                    testEmptyName = true
                )
            }
            PlayMusicScreen(this).apply {
                CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes[0]])
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                val playlistSongFake = songFakeList.filter { it.id in testedItemsOneBasedIndexes }

                mainSongList.assertListItems(playlistSongFake) { itemViewSupplier, position, song ->

                    assertSongItem(
                        "Wrong list item after playlist loaded",
                        itemViewSupplier(),
                        song
                    )
                    CustomMediaPlayerShadow.setFakeSong(song)

                    // check image changes after playlist loaded
                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "When a song from the song list is paused the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should be R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a paused song the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )


                    controllerBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_CONTROLLER_BTN_PLAY_PAUSE on a playing song the image displayed should change to R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    controllerBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_CONTROLLER_BTN_PLAY_PAUSE on a paused song the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )

                    controllerBtnStop.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_CONTROLLER_BTN_STOP on a playing song the image displayed should change to R.drawable.ic_play",
                        R.drawable.ic_play
                    )
                }
            }
        }
    }

    @Test
    fun checkLoadPlaylistInPlayMusicStateAfterAddingPlaylistWithLongClick() {

        testActivity {
            val testedItemsZeroBasedIndexes = listOf(4, 7, 8)
            val testedItemsOneBasedIndexes = testedItemsZeroBasedIndexes.map { it + 1 }
            val longClickItemZeroBasedIndex = 5
            val longClickItemOneBasedIndex = longClickItemZeroBasedIndex + 1
            val playlistName = "My Playlist"

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                mainSongList.assertSingleListItem(longClickItemZeroBasedIndex) {
                    it().clickLongAndRun()
                }
            }

            AddPlaylistScreen(this).apply {
                // check long click item is checked and deselect item
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                    when (item.id) {
                        longClickItemOneBasedIndex -> {
                            val itemView = itemViewSupplier()
                            val songSelectorItemCheckBox =
                                itemView.findViewByString<CheckBox>("songSelectorItemCheckBox")

                            assertEquals(
                                "On the item that received a long click songSelectorItemCheckBox should be check.",
                                true,
                                songSelectorItemCheckBox.isChecked
                            )

                            itemView.assertBackgroundColor(
                                "On the item that received a long click background color should be Color.LT_GRAY.",
                                Color.LTGRAY
                            )

                            itemView.clickAndRun()  // deselect
                        }

                        else -> {}
                    }
                }
                //

                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
                    testEmptyName = true
                )
            }
            PlayMusicScreen(this).apply {
                CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes.first()])
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                val playlistSongFake = songFakeList.filter { it.id in testedItemsOneBasedIndexes }

                mainSongList.assertListItems(playlistSongFake) { itemViewSupplier, position, song ->
                    assertSongItem(
                        "Wrong list item after playlist loaded",
                        itemViewSupplier(),
                        song
                    )
                    CustomMediaPlayerShadow.setFakeSong(song)

                    // check image changes after load
                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "When a song from the song list is paused the image of $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE should be R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE on a paused song the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )

                    controllerBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_CONTROLLER_BTN_PLAY_PAUSE on a playing song the image displayed should change to R.drawable.ic_play",
                        R.drawable.ic_play
                    )

                    controllerBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_CONTROLLER_BTN_PLAY_PAUSE on a paused song the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )

                    controllerBtnStop.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_CONTROLLER_BTN_STOP on a playing song the image displayed should change to R.drawable.ic_play",
                        R.drawable.ic_play
                    )
                    //
                }
            }
        }
    }

    @Test
    fun checkLoadPlaylistOnPlayMusicStateWithCurrentTrackKeepsCurrentTrack() {

        testActivity {
            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val selectedSongZeroIndex = testedItemsZeroBasedIndexes[1]
            val playlistName = "My Playlist"

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()

                CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedSongZeroIndex])
                mainSongList.assertSingleListItem(selectedSongZeroIndex) { itemViewSupplier ->
                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )
                }

                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer
                )
            }
            PlayMusicScreen(this).apply {

                // check item keeps selected state after list add
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    if (item.id == selectedSongZeroIndex + 1) {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain selected after adding a playlist",
                            R.drawable.ic_pause
                        )

                        val controllerUi = mainFragmentContainer.getControllerViews()

                        controllerUi.btnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_CONTROLLER_BTN_PLAY_PAUSE clicks after adding a playlist",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE clicks after adding a playlist",
                            R.drawable.ic_pause
                        )

                        controllerUi.btnStop.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_CONTROLLER_BTN_STOP clicks after adding a playlist",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE clicks after adding a playlist",
                            R.drawable.ic_pause
                        )

                    } else {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "A unselected song should remain unselected after adding a playlist",
                            R.drawable.ic_play
                        )
                    }
                }
                //

                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                // check item keeps selected state after list load
                mainSongList.assertListItems(
                    testedItemsZeroBasedIndexes.map { songFakeList[it] }) { itemViewSupplier, position, item ->
                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    if (item.id == selectedSongZeroIndex + 1) {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain selected after loading a playlist",
                            R.drawable.ic_pause
                        )

                        val controllerUi = mainFragmentContainer.getControllerViews()

                        controllerUi.btnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_CONTROLLER_BTN_PLAY_PAUSE clicks after loading a playlist",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE clicks after loading a playlist",
                            R.drawable.ic_pause
                        )

                        controllerUi.btnStop.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_CONTROLLER_BTN_STOP clicks after loading a playlist",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE clicks after loading a playlist",
                            R.drawable.ic_pause
                        )

                    } else {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "A unselected song should remain unselected after loading a playlist",
                            R.drawable.ic_play
                        )
                    }
                }
                //
            }
        }
    }

    @Test
    fun checkLoadPlaylistOnPlayMusicStateWithoutCurrentTrackChangesCurrentTrack() {

        testActivity {
            val playlistName = "My Playlist"
            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val selectedSongZeroIndex = 8

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedSongZeroIndex])
                mainSongList.assertSingleListItem(selectedSongZeroIndex) { itemViewSupplier ->
                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )
                }

                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {

                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer
                )
            }
            PlayMusicScreen(this).apply {
                // check item keeps selected state after list add
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    if (item.id == selectedSongZeroIndex + 1) {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain selected after adding a playlist",
                            R.drawable.ic_pause
                        )

                        val controllerUi = mainFragmentContainer.getControllerViews()

                        controllerUi.btnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_CONTROLLER_BTN_PLAY_PAUSE clicks after adding a playlist",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE clicks after adding a playlist",
                            R.drawable.ic_pause
                        )

                        controllerUi.btnStop.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_CONTROLLER_BTN_STOP clicks after adding a playlist",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The selected song should remain responding to $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE clicks after adding a playlist",
                            R.drawable.ic_pause
                        )

                    } else {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "A unselected song should remain unselected after adding a playlist",
                            R.drawable.ic_play
                        )
                    }
                }
                //

                CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes.first()])
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                // check default item selected after list load
                mainSongList.assertListItems(
                    testedItemsZeroBasedIndexes.map { songFakeList[it] }) { itemViewSupplier, position, item ->
                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    if (position == 0) {
                        val controllerUi = mainFragmentContainer.getControllerViews()

                        controllerUi.btnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The first song should be the currentTrack after loading a playlist " +
                                    "without the old currentTrack and respond to $ID_CONTROLLER_BTN_PLAY_PAUSE clicks",
                            R.drawable.ic_pause
                        )

                        controllerUi.btnStop.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The currentTrack should remain responding " +
                                    "to $ID_CONTROLLER_BTN_STOP clicks after loading a playlist",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The currentTrack should remain responding " +
                                    "to $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE clicks after loading a playlist",
                            R.drawable.ic_pause
                        )

                    } else {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "A track that is not the currentTrack should remain not being the currentTrack",
                            R.drawable.ic_play
                        )
                    }
                }
                //
            }
        }
    }

    @Test
    fun checkLoadPlaylistInAddPlaylistStateKeepsSelectedItemsById() {

        testActivity {
            val playlistAItemsZeroBasedIndexes = listOf(0, 3, 6, 7, 8, 9)
            val playlistAItemsOneBasedIndexes = playlistAItemsZeroBasedIndexes.map { it + 1 }
            val playlistBItemsZeroBasedIndexes =
                playlistAItemsZeroBasedIndexes.filter { it % 3 == 0 }
            val playlistBItemsOneBasedIndexes = playlistBItemsZeroBasedIndexes.map { it + 1 }
            val playlistName = "Weird Sounds"

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = playlistAItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
                )
            }
            PlayMusicScreen(this).apply {
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                // check default playlist "All Songs" in ADD_PLAYLIST state and select items
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                    var itemView = itemViewSupplier()
                    var checkBox =
                        itemView.findViewByString<CheckBox>("songSelectorItemCheckBox")

                    assertEquals(
                        "No songSelectorItemCheckBox should be checked after click on mainMenuItemIdAddPlaylist",
                        false,
                        checkBox.isChecked
                    )

                    if (item.id in playlistBItemsOneBasedIndexes) {
                        itemView.clickAndRun()
                        itemView = itemViewSupplier()
                        checkBox = itemView.findViewByString<CheckBox>("songSelectorItemCheckBox")


                        assertEquals(
                            "songSelectorItemCheckBox should be checked after click on list item",
                            true,
                            checkBox.isChecked
                        )
                    }
                }
                //

                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                // check loaded playlist in ADD_PLAYLIST state keeps selected items
                mainSongList.assertListItems(
                    songFakeList.filter { it.id in playlistAItemsOneBasedIndexes }
                ) { itemViewSupplier, position, item ->

                    val checkBox =
                        itemViewSupplier().findViewByString<CheckBox>("songSelectorItemCheckBox")

                    if (item.id in playlistBItemsOneBasedIndexes) {
                        assertEquals(
                            "songSelectorItemCheckBox should remain isChecked value" +
                                    " after list loaded on ADD_PLAYLIST state",
                            true,
                            checkBox.isChecked
                        )
                    } else {
                        assertEquals(
                            "songSelectorItemCheckBox should remain isChecked value" +
                                    " after list loaded on ADD_PLAYLIST state",
                            false,
                            checkBox.isChecked
                        )
                    }
                }
                //
            }
        }
    }

    @Test
    fun checkLoadPlaylistInAddPlaylistStateKeepsCurrentTrackWhenReturningToPlayMusicState() {

        testActivity {
            val playlistItemsZeroBasedIndexes = listOf(0, 3, 6, 7, 8, 9)
            val selectedItem = 1
            val playlistName = "Party Songs"

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()

                CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedItem])
                mainSongList.assertSingleListItem(selectedItem) { itemViewSupplier ->
                    songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        .clickAndRun()
                }

                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = playlistItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
                )
            }
            PlayMusicScreen(this).apply {
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                mainFragmentContainer
                    .findViewByString<Button>("addPlaylistBtnCancel")
                    .clickAndRun()
            }
            PlayMusicScreen(this).apply {

                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                    assertSongItem(
                        "The currentPlaylist should not change " +
                                "after loading a playlist in ADD_PLAYLIST state",
                        itemViewSupplier(), song
                    )
                    val songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    if (position == selectedItem) {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The currentTrack should keep its playing state " +
                                    "after loading a playlist on ADD_PLAYLIST state and returning to PLAY_MUSIC state",
                            R.drawable.ic_pause
                        )
                    } else {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "A track that is not the currentTrack should not be playing",
                            R.drawable.ic_play
                        )
                    }
                }
            }
        }
    }

    @Test
    fun checkPlaylistSavedAfterSelectingSongsAfterLoadingPlaylistInAddPlaylistState() {

        testActivity {
            val playlistOne = listOf(1, 2, 3)
            val selectItemsOne = listOf(0, 1)
            val selectItemsTwo = listOf(2, 3)
            val playlistName1 = "playlist1"
            val playlistName2 = "playlist2"
            val loadPlaylistOneSongs = songFakeList.filter { it.id - 1 in playlistOne }

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = playlistName1,
                    selectedItemsIndex = playlistOne,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer
                )
            }
            PlayMusicScreen(this).apply {
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                    if (item.id - 1 in selectItemsOne) {
                        itemViewSupplier().clickAndRun()
                    }
                }
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName1),
                    playlistToLoadIndex = 1
                )


                mainSongList.assertListItems(loadPlaylistOneSongs) { itemViewSupplier, position, item ->
                    if (item.id - 1 in selectItemsTwo) {
                        itemViewSupplier().clickAndRun()
                    }
                }
                mainFragmentContainer.findViewByString<EditText>("addPlaylistEtPlaylistName")
                    .apply {
                        setText(playlistName2)
                    }
                mainFragmentContainer.findViewByString<Button>("addPlaylistBtnOk").clickAndRun()
            }
            PlayMusicScreen(this).apply {
                CustomMediaPlayerShadow.setFakeSong(songFakeList[playlistOne.first()])
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName1, playlistName2),
                    playlistToLoadIndex = 2
                )
                val messageItemsSaved =
                    "The playlist saved should contain the selected items when clicking addPlaylistBtnOk"
                mainSongList.assertListItems(loadPlaylistOneSongs) { itemViewSupplier, position, song ->
                    assertSongItem(messageItemsSaved, itemViewSupplier(), song)
                }
            }
        }
    }

    @Test
    fun checkCancellingAddPlaylistKeepsCurrentPlaylist() {

        testActivity {
            val playlistAItemsZeroBasedIndexes = listOf(3, 7, 8)
            val playlistAItemsOneBasedIndexes = playlistAItemsZeroBasedIndexes.map { it + 1 }
            val playlistName = "Cool Songs"
            val playlistSongFake =
                songFakeList.filter { it.id in playlistAItemsOneBasedIndexes }

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = playlistAItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
                    testEmptyName = true
                )
            }
            PlayMusicScreen(this).apply {
                CustomMediaPlayerShadow.setFakeSong(songFakeList[playlistAItemsZeroBasedIndexes[0]])
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                // check loaded items
                mainSongList.assertListItems(playlistSongFake) { itemViewSupplier, position, song ->
                    assertSongItem(
                        "Wrong list item after playlist loaded",
                        itemViewSupplier(),
                        song
                    )
                }
                //
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                // canceling an add playlist
                mainFragmentContainer.findViewByString<Button>("addPlaylistBtnCancel").clickAndRun()
            }
            PlayMusicScreen(this).apply {

                // check loaded items remains
                mainSongList.assertListItems(playlistSongFake) { itemViewSupplier, position, item ->
                    val messageWrongListItemAfterCancel =
                        "Playlist loaded should remain after addPlaylistBtnCancel clicked"
                    assertSongItem(messageWrongListItemAfterCancel, itemViewSupplier(), item)
                }
                //
            }
        }
    }

    @Test
    fun checkCancelingAddPlaylistKeepsCurrentTrackPlayingState() {

        testActivity {
            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val selectedSongZeroIndex = testedItemsZeroBasedIndexes[1]

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()

                CustomMediaPlayerShadow.setFakeSong(songFakeList[selectedSongZeroIndex])
                mainSongList.assertSingleListItem(selectedSongZeroIndex) { itemViewSupplier ->
                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.clickAndRun()
                    songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                        "After clicking on $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE " +
                                "the image displayed should change to R.drawable.ic_pause",
                        R.drawable.ic_pause
                    )
                }

                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                mainFragmentContainer.findViewByString<Button>("addPlaylistBtnCancel").clickAndRun()
            }
            PlayMusicScreen(this).apply {
                // check item keeps selected state after cancel add list
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                    var songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)

                    if (item.id == selectedSongZeroIndex + 1) {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The currentTrack should remain being the currentTrack " +
                                    "after canceling adding a playlist",
                            R.drawable.ic_pause
                        )

                        val controllerUi = mainFragmentContainer.getControllerViews()

                        controllerUi.btnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The currentTrack should remain responding to $ID_CONTROLLER_BTN_PLAY_PAUSE clicks " +
                                    "after canceling adding a playlist",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The currentTrack should remain responding to $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE clicks " +
                                    "after canceling adding a playlist",
                            R.drawable.ic_pause
                        )

                        controllerUi.btnStop.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The currentTrack should remain responding to $ID_CONTROLLER_BTN_STOP clicks " +
                                    "after canceling adding a playlist",
                            R.drawable.ic_play
                        )

                        songItemImgBtnPlayPause.clickAndRun()
                        songItemImgBtnPlayPause = songItemImgBtnPlayPauseSupplier(itemViewSupplier)
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "The currentTrack should remain responding to $ID_SONG_ITEM_IMG_BTN_PLAY_PAUSE clicks " +
                                    "after canceling adding a playlist",
                            R.drawable.ic_pause
                        )

                    } else {
                        songItemImgBtnPlayPause.drawable.assertCreatedFromResourceId(
                            "A track that is not the currentTrack should remain not being " +
                                    "the currentTrack after canceling adding a playlist",
                            R.drawable.ic_play
                        )
                    }
                }
                //
            }
        }
    }

    @Test
    fun checkDeletePlaylistOnPlayMusicStateDeletingPlaylistThatIsNotCurrentPlaylist() {

        testActivity {
            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()

                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = "My Playlist",
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer
                )
            }
            PlayMusicScreen(this).apply {
                // delete playlist
                activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                        listOf("My Playlist"),
                        dialogItems
                    )
                    shadowDialog.clickAndRunOnItem(0)
                }
                //

                // check delete dialog don't display deleted playlist
                activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                        listOf<String>(),
                        dialogItems
                    )

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
                }
                //

                // check load dialog don't display deleted playlist
                activity.clickMenuItemAndRun(mainMenuItemIdLoadPlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemLoadPlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemLoadPlaylist",
                        listOf("All Songs"),
                        dialogItems
                    )

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
                }
                //

                //check currentPlaylist remains
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                    assertSongItem(
                        "Deleting a playlist that is not the currentPlaylist " +
                                "should not change the currentPlaylist",
                        itemViewSupplier(), song
                    )
                }
            }
        }
    }

    @Test
    fun checkDeletePlaylistOnPlayMusicStateWithCurrentPlaylistBeingDeleted() {

        testActivity {
            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val testedItemsOneBasedIndexes = testedItemsZeroBasedIndexes.map { it + 1 }
            val playlistName = "My Playlist"

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {

                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer
                )
            }
            PlayMusicScreen(this).apply {
                CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes.first()])
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                // check loaded items
                val playlistSongFake = songFakeList.filter { it.id in testedItemsOneBasedIndexes }

                mainSongList.assertListItems(playlistSongFake) { itemViewSupplier, position, item ->
                    val messageWrongListItemAfterPlaylistLoaded =
                        "Wrong list item after playlist loaded"
                    assertSongItem(
                        messageWrongListItemAfterPlaylistLoaded,
                        itemViewSupplier(),
                        item
                    )
                }
                //

                // delete playlist
                activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                        listOf(playlistName),
                        dialogItems
                    )
                    shadowDialog.clickAndRunOnItem(0)
                }
                //

                //check items
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, item ->
                    val messageWrongItem =
                        "Wrong list item found after deleting current playlist, " +
                                "expected \"All songs\" playlist to be loaded"
                    assertSongItem(messageWrongItem, itemViewSupplier(), item)
                }

                // check delete dialog don't display deleted playlist
                activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                        listOf<String>(),
                        dialogItems
                    )

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
                }
                //

                // check load dialog don't display deleted playlist
                activity.clickMenuItemAndRun(mainMenuItemIdLoadPlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemLoadPlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemLoadPlaylist",
                        listOf("All Songs"),
                        dialogItems
                    )

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
                }
                //
            }
        }
    }

    @Test
    fun checkDeletePlaylistOnAddPlaylistStateDeletingPlaylistThatIsNotDisplayingAndNotCurrentPlaylist() {

        testActivity {
            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = "My Playlist",
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer
                )
            }
            PlayMusicScreen(this).apply {
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                // delete playlist
                activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                        listOf("My Playlist"),
                        dialogItems
                    )
                    shadowDialog.clickAndRunOnItem(0)
                }
                //

                // check delete dialog don't display deleted playlist
                activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                        listOf<String>(),
                        dialogItems
                    )

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
                }
                //

                // check load dialog don't display deleted playlist
                activity.clickMenuItemAndRun(mainMenuItemIdLoadPlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemLoadPlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemLoadPlaylist",
                        listOf("All Songs"),
                        dialogItems
                    )

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
                }
                //

                // check SongSelector items remains
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                    val itemView = itemViewSupplier()
                    val actualArtist = itemView
                        .findViewByString<TextView>("songSelectorItemTvArtist")
                        .text.toString().lowercase()
                    val actualTitle = itemView
                        .findViewByString<TextView>("songSelectorItemTvTitle")
                        .text.toString().lowercase()
                    val actualDuration = itemView
                        .findViewByString<TextView>("songSelectorItemTvDuration")
                        .text.toString().lowercase()
                    val errorMessage =
                        "After deleting in ADD_PLAYLIST state a playlist that is not displaying " +
                                "the playlist that is displaying should remain"

                    assertEquals(errorMessage, song.artist, actualArtist)
                    assertEquals(errorMessage, song.title, actualTitle)
                    assertEquals(errorMessage, song.duration.timeString(), actualDuration)
                }

                mainFragmentContainer
                    .findViewByString<Button>("addPlaylistBtnCancel")
                    .clickAndRun()
            }
            PlayMusicScreen(this).apply {
                //check currentPlaylist remains
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                    val errorMessage =
                        "After deleting in ADD_PLAYLIST state a playlist that is not the currentPlaylist" +
                                "the currentPlaylist should remain"

                    assertSongItem(errorMessage, itemViewSupplier(), song)
                }
            }
        }
    }

    @Test
    fun checkDeletePlaylistOnAddPlaylistStateWithCurrentDisplayingAndCurrentPlaylistBeingDeleted() {

        testActivity {
            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val playlistName = "My Playlist"
            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer
                )
            }
            PlayMusicScreen(this).apply {
                // load list in PLAY_MUSIC state
                CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes.first()])
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                // load list in ADD_PLAYLIST state
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                CustomMediaPlayerShadow.setFakeSong(songFakeList.first())
                // delete playlist
                activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                        listOf(playlistName),
                        dialogItems
                    )
                    shadowDialog.clickAndRunOnItem(0)
                }
                //

                // check delete dialog don't display deleted playlist
                activity.clickMenuItemAndRun(mainMenuItemIdDeletePlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemDeletePlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemDeletePlaylist",
                        listOf<String>(),
                        dialogItems
                    )

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
                }
                //

                // check load dialog don't display deleted playlist
                activity.clickMenuItemAndRun(mainMenuItemIdLoadPlaylist)

                getLastAlertDialogWithShadow(
                    "An AlertDialog should be displayed after click on mainMenuItemLoadPlaylist"
                ).also { (dialog, shadowDialog) ->
                    val dialogItems = shadowDialog.items.map { it.toString() }

                    assertEquals(
                        "Wrong list displayed on AlertDialog after click on mainMenuItemLoadPlaylist",
                        listOf("All Songs"),
                        dialogItems
                    )

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).clickAndRun()
                }
                //

                // check SongSelector changes to "All Songs"
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                    val itemView = itemViewSupplier()
                    val actualArtist = itemView
                        .findViewByString<TextView>("songSelectorItemTvArtist")
                        .text.toString().lowercase()
                    val actualTitle = itemView
                        .findViewByString<TextView>("songSelectorItemTvTitle")
                        .text.toString().lowercase()
                    val actualDuration = itemView
                        .findViewByString<TextView>("songSelectorItemTvDuration")
                        .text.toString().lowercase()
                    val errorMessage =
                        "After deleting in ADD_PLAYLIST state a playlist that is displaying " +
                                "the playlist that is displaying should change to \"All Songs\""

                    assertEquals(errorMessage, song.artist, actualArtist)
                    assertEquals(errorMessage, song.title, actualTitle)
                    assertEquals(errorMessage, song.duration.timeString(), actualDuration)
                }

                mainFragmentContainer
                    .findViewByString<Button>("addPlaylistBtnCancel")
                    .clickAndRun()
            }
            PlayMusicScreen(this).apply {
                //check currentPlaylist changes to "All Songs"
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                    val errorMessage =
                        "After deleting in ADD_PLAYLIST state a playlist that is the currentPlaylist" +
                                "the currentPlaylist should change to \"All Songs\""

                    assertSongItem(errorMessage, itemViewSupplier(), song)
                }
            }
        }
    }

    @Test
    fun checkSearchInPlayMusicStateChangeCurrentPlaylistToAllSongs() {
        val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
        val playlistName = "My Playlist"

        testActivity {
            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
                    testEmptyName = true
                )
            }
            PlayMusicScreen(this).apply {
                CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes[0]])
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                mainButtonSearch.clickAndRun()
                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                    assertSongItem(
                        "Wrong list item after search button clicked",
                        itemViewSupplier(),
                        song
                    )
                }
            }
        }
    }

    @Test
    fun checkSearchInAddPlaylistStateDisplaysAllSongsOnAddPlaylistStateAndKeepsCurrentPlaylistInPlayMusicState() {

        testActivity {
            val testedItemsZeroBasedIndexes = listOf(1, 3, 6)
            val playlistName = "My Playlist"
            val playlist = testedItemsZeroBasedIndexes.map { songFakeList[it] }

            PlayMusicScreen(this).apply {
                mainButtonSearch.clickAndRun()
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)

            }
            AddPlaylistScreen(this).apply {
                addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = testedItemsZeroBasedIndexes,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
                    testEmptyName = true
                )
            }
            PlayMusicScreen(this).apply {
                CustomMediaPlayerShadow.setFakeSong(songFakeList[testedItemsZeroBasedIndexes[0]])
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )
                activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            }
            AddPlaylistScreen(this).apply {
                loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf("All Songs", playlistName),
                    playlistToLoadIndex = 1
                )

                mainButtonSearch.clickAndRun()

                mainSongList.assertListItems(songFakeList) { itemViewSupplier, position, song ->
                    val itemView = itemViewSupplier()
                    val actualArtist = itemView
                        .findViewByString<TextView>("songSelectorItemTvArtist")
                        .text.toString().lowercase()
                    val actualTitle = itemView
                        .findViewByString<TextView>("songSelectorItemTvTitle")
                        .text.toString().lowercase()
                    val actualDuration = itemView
                        .findViewByString<TextView>("songSelectorItemTvDuration")
                        .text.toString().lowercase()
                    val errorMessage =
                        "After mainButtonSearch is clicked on ADD_PLAYLIST state " +
                                "the \"All Songs\" playlist should be displaying"

                    assertEquals(errorMessage, song.artist, actualArtist)
                    assertEquals(errorMessage, song.title, actualTitle)
                    assertEquals(errorMessage, song.duration.timeString(), actualDuration)
                }

                mainFragmentContainer
                    .findViewByString<Button>("addPlaylistBtnCancel")
                    .clickAndRun()
            }
            PlayMusicScreen(this).apply {
                mainSongList.assertListItems(playlist) { itemViewSupplier, position, song ->
                    assertSongItem(
                        "After mainButtonSearch is clicked on ADD_PLAYLIST state " +
                                "the currentPlaylist in PLAY_MUSIC state should not change",
                        itemViewSupplier(), song
                    )
                }
            }
        }
    }
}