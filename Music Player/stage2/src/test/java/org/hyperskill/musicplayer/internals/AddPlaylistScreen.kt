package org.hyperskill.musicplayer.internals

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import org.hyperskill.musicplayer.MainActivity
import org.junit.Assert
import org.junit.Assert.assertEquals

class AddPlaylistScreen(
    private val test: MusicPlayerUnitTests<MainActivity>,
    val initAssertions: Boolean = false
) : MusicPlayerBaseScreen(test) {
    companion object {

    }

}