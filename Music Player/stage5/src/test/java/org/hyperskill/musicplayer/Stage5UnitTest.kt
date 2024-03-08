package org.hyperskill.musicplayer

import android.Manifest
import android.content.ContentValues
import android.widget.Button
import androidx.core.database.sqlite.transaction
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import org.hyperskill.musicplayer.internals.CustomMediaPlayerShadow
import org.hyperskill.musicplayer.internals.CustomShadowAsyncDifferConfig
import org.hyperskill.musicplayer.internals.CustomShadowCountDownTimer
import org.hyperskill.musicplayer.internals.MusicPlayerUnitTests
import org.hyperskill.musicplayer.internals.TestDatabaseFactory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Duration

// version 1.4
@Config(shadows = [CustomMediaPlayerShadow::class, CustomShadowCountDownTimer::class, CustomShadowAsyncDifferConfig::class])
@RunWith(RobolectricTestRunner::class)
class Stage5UnitTest : MusicPlayerUnitTests<MainActivity>(MainActivity::class.java) {


    companion object {
        const val tableName = "playlist"
        const val columnSongId = "songId"
        const val columnPlaylistName = "playlistName"
        const val createQuery = "CREATE TABLE IF NOT EXISTS $tableName(" +
                "$columnPlaylistName TEXT, " +
                "$columnSongId INTEGER, " +
                "PRIMARY KEY($columnPlaylistName, $columnSongId));"

        const val mainMenuItemIdAddPlaylist = "mainMenuAddPlaylist"
        const val mainMenuItemIdLoadPlaylist = "mainMenuLoadPlaylist"
        const val mainMenuItemIdDeletePlaylist = "mainMenuDeletePlaylist"

        const val ALL_SONGS = "All Songs"

    }

    private val mainButtonSearch by lazy {
        val view = activity.findViewByString<Button>("mainButtonSearch")

        val expectedText = "search"
        val actualText = view.text.toString().lowercase()
        assertEquals("wrong text for mainButtonSearch", expectedText, actualText)

        view
    }

    private val mainSongList by lazy {
        activity.findViewByString<RecyclerView>("mainSongList")
    }

    private val mainFragmentContainer by lazy {
        activity.findViewByString<FragmentContainerView>("mainFragmentContainer")
    }

    @Before
    fun setUp() {
        setupContentProvider(SongFakeRepository.fakeSongData)
        shadowActivity.grantPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
        CustomMediaPlayerShadow.setFakeSong(SongFakeRepository.fakeSongData[0])
    }

    @Test
    fun checkDatabaseDataAfterPlaylistSave() {

        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            val playlistName = "cool songs"
            val selectedItemsIndex = listOf(2, 5, 6)
            val expectedDatabaseData = selectedItemsIndex.map {
                playlistName to SongFakeRepository.fakeSongData[it].id
            }

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = selectedItemsIndex,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
            )

            shadowLooper.idleFor(Duration.ofSeconds(3))

            TestDatabaseFactory(activity).readableDatabase.use { readableDatabase ->
                val cursor = readableDatabase.query(
                        tableName, null, null,
                        null, null, null, null
                )

                val columnSongIdIndex = cursor.getColumnIndex(columnSongId)
                val columnPlaylistNameIndex = cursor.getColumnIndex(columnPlaylistName)

                val actualDatabaseData = generateSequence {
                    cursor.moveToNext().let { hasNext ->
                        if (hasNext) {
                            val playlistName = cursor.getString(columnPlaylistNameIndex)
                            val songId = cursor.getInt(columnSongIdIndex)

                            playlistName to songId
                        } else {
                            cursor.close()
                            null
                        }
                    }
                }.toList()

                assertEquals(
                        "The number of rows in table $tableName is incorrect,",
                        expectedDatabaseData.size,
                        actualDatabaseData.size
                )

                expectedDatabaseData.forEachIndexed { i, expectedData ->
                    val actualData = actualDatabaseData[i]


                    val messageWrongData =
                            "Incorrect data stored in table $tableName,"
                    assertEquals(messageWrongData, expectedData, actualData)
                }
            }
        }
    }

    @Test
    fun checkSamePlaylistSaveTwiceNoDuplicatesOnDatabase() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            val playlistName = "cool songs"
            val selectedItemsIndex = listOf(2, 5, 6)
            val expectedDatabaseData = selectedItemsIndex.map {
                playlistName to SongFakeRepository.fakeSongData[it].id
            }

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = selectedItemsIndex,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
            )

            shadowLooper.idleFor(Duration.ofSeconds(3))

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = selectedItemsIndex,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
            )

            shadowLooper.idleFor(Duration.ofSeconds(3))

            TestDatabaseFactory(activity).readableDatabase.use { readableDatabase ->
                val cursor = readableDatabase.query(
                        tableName, null, null,
                        null, null, null, null
                )

                val columnSongIdIndex = cursor.getColumnIndex(columnSongId)
                val columnPlaylistNameIndex = cursor.getColumnIndex(columnPlaylistName)

                val actualDatabaseData = generateSequence {
                    cursor.moveToNext().let { hasNext ->
                        if (hasNext) {
                            val playlistName = cursor.getString(columnPlaylistNameIndex)
                            val songId = cursor.getInt(columnSongIdIndex)

                            playlistName to songId
                        } else {
                            cursor.close()
                            null
                        }
                    }
                }.toList()

                assertEquals(
                        "The number of rows in table $tableName is incorrect,",
                        expectedDatabaseData.size,
                        actualDatabaseData.size
                )

                expectedDatabaseData.forEachIndexed { i, expectedData ->
                    val actualData = actualDatabaseData[i]


                    val messageWrongData =
                            "Incorrect data stored in table $tableName,"
                    assertEquals(messageWrongData, expectedData, actualData)
                }
            }
        }
    }

    @Test
    fun checkDatabaseDataAfterPlaylistSaveWithExistingPlaylistNameAndDifferentSongs() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()


            val playlistName = "cool songs"
            val selectedOldItemsIndex = listOf(2, 5, 6)
            val selectedNewItemsIndex = listOf(1, 5, 7, 8)
            val expectedDatabaseData = selectedNewItemsIndex.map {
                playlistName to SongFakeRepository.fakeSongData[it].id
            }

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = selectedOldItemsIndex,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
            )

            shadowLooper.idleFor(Duration.ofSeconds(3))

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            addPlaylist(
                    playlistName = playlistName,
                    selectedItemsIndex = selectedNewItemsIndex,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
            )

            shadowLooper.idleFor(Duration.ofSeconds(3))

            TestDatabaseFactory(activity).readableDatabase.use { readableDatabase ->
                val cursor = readableDatabase.query(
                        tableName, null, null,
                        null, null, null, null
                )

                val columnSongIdIndex = cursor.getColumnIndex(columnSongId)
                val columnPlaylistNameIndex = cursor.getColumnIndex(columnPlaylistName)

                val actualDatabaseData = generateSequence {
                    cursor.moveToNext().let { hasNext ->
                        if (hasNext) {
                            val playlistName = cursor.getString(columnPlaylistNameIndex)
                            val songId = cursor.getInt(columnSongIdIndex)

                            playlistName to songId
                        } else {
                            cursor.close()
                            null
                        }
                    }
                }.toList()

                assertEquals(
                        "The number of rows in table $tableName is incorrect,",
                        expectedDatabaseData.size,
                        actualDatabaseData.size
                )

                expectedDatabaseData.forEachIndexed { i, expectedData ->
                    val actualData = actualDatabaseData[i]

                    val messageWrongData =
                            "Incorrect data stored in table $tableName,"
                    assertEquals(messageWrongData, expectedData, actualData)
                }
            }
        }
    }

    @Test
    fun checkDatabaseDifferentPlaylistSaves() {
        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            val playlistNameA = "cool songs"
            val playlistNameB = "ok songs"
            val selectedPlaylistAItemsIndex = listOf(2, 5, 6)
            val selectedPlaylistBItemsIndex = listOf(3, 4, 7, 8)

            val expectedDatabaseData = selectedPlaylistAItemsIndex.map {
                playlistNameA to SongFakeRepository.fakeSongData[it].id
            } + selectedPlaylistBItemsIndex.map {
                playlistNameB to SongFakeRepository.fakeSongData[it].id
            }

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            addPlaylist(
                    playlistName = playlistNameA,
                    selectedItemsIndex = selectedPlaylistAItemsIndex,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
            )

            shadowLooper.idleFor(Duration.ofSeconds(3))

            activity.clickMenuItemAndRun(mainMenuItemIdAddPlaylist)
            addPlaylist(
                    playlistName = playlistNameB,
                    selectedItemsIndex = selectedPlaylistBItemsIndex,
                    songListView = mainSongList,
                    fragmentContainer = mainFragmentContainer,
            )

            shadowLooper.idleFor(Duration.ofSeconds(3))

            TestDatabaseFactory(activity).readableDatabase.use { readableDatabase ->
                val cursor = readableDatabase.query(
                        tableName, null, null,
                        null, null, null, null
                )

                val columnSongIdIndex = cursor.getColumnIndex(columnSongId)
                val columnPlaylistNameIndex = cursor.getColumnIndex(columnPlaylistName)

                val actualDatabaseData = generateSequence {
                    cursor.moveToNext().let { hasNext ->
                        if (hasNext) {
                            val playlistName = cursor.getString(columnPlaylistNameIndex)
                            val songId = cursor.getInt(columnSongIdIndex)

                            playlistName to songId
                        } else {
                            cursor.close()
                            null
                        }
                    }
                }.toList()

                assertEquals(
                        "The number of rows in table $tableName is incorrect,",
                        expectedDatabaseData.size,
                        actualDatabaseData.size
                )

                expectedDatabaseData.forEachIndexed { i, expectedData ->
                    val actualData = actualDatabaseData[i]

                    val messageWrongData =
                            "Incorrect data stored in table $tableName,"
                    assertEquals(messageWrongData, expectedData, actualData)
                }
            }
        }
    }

    @Test
    fun checkLoadPlaylistFromDatabase() {
        val playlistName = "fake songs"
        val fakeData = listOf(9, 10, 11).map { index ->
            playlistName to index
        }

        TestDatabaseFactory(activity).writableDatabase.use { writableDatabase ->
            writableDatabase.execSQL(createQuery)

            writableDatabase.transaction {
                fakeData.forEach { (playlistName, index) ->
                    val values = ContentValues()
                    values.put(columnPlaylistName, playlistName)
                    values.put(columnSongId, index + 1)

                    insert(tableName, null, values)
                }
            }
        }

        testActivity {
            mainButtonSearch

            mainButtonSearch.clickAndRun()

            val expectedItems = fakeData.map { SongFakeRepository.fakeSongData[it.second] }
            CustomMediaPlayerShadow.setFakeSong(expectedItems.first())

            loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf(ALL_SONGS, playlistName),
                    playlistToLoadIndex = 1
            )

            mainSongList.assertListItems(expectedItems) { itemViewSupplier, position, song ->
                assertSongItem("Incorrect item after loading a list from database", itemViewSupplier(), song)
            }
        }
    }

    @Test
    fun checkAutomaticSearchOnPlaylistLoad() {
        val playlistName = "Party Songs"
        val fakeData = listOf(3, 12, 13).map { index ->
            playlistName to index
        }

        TestDatabaseFactory(activity).writableDatabase.use { writableDatabase ->
            writableDatabase.execSQL(createQuery)

            writableDatabase.transaction {
                fakeData.forEach { (playlistName, index) ->
                    val values = ContentValues()
                    values.put(columnPlaylistName, playlistName)
                    values.put(columnSongId, index + 1)

                    insert(tableName, null, values)
                }
            }
        }

        testActivity {
            val expectedItems = fakeData.map { SongFakeRepository.fakeSongData[it.second] }
            CustomMediaPlayerShadow.setFakeSong(expectedItems.first())

            loadPlaylist(
                    menuItemIdLoadPlaylist = mainMenuItemIdLoadPlaylist,
                    expectedPlaylistNameList = listOf(ALL_SONGS, playlistName),
                    playlistToLoadIndex = 1
            )

            mainSongList.assertListItems(expectedItems) { itemViewSupplier, position, song ->
                assertSongItem(
                        "If a list is loaded before mainButtonSearch is clicked " +
                                "then the search should be done automatically before loading the list",
                        itemViewSupplier(),
                        song
                )
            }
        }
    }
}