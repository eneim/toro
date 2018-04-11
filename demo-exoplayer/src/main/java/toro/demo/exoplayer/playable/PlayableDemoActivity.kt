/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package toro.demo.exoplayer.playable

import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.exoplayer2.ui.PlayerView
import im.ene.toro.exoplayer.Playable
import im.ene.toro.exoplayer.Playable.DefaultEventListener
import kotlinx.android.synthetic.main.activity_single_player.toolbar
import kotlinx.android.synthetic.main.activity_single_player_landscape.player_view
import kotlinx.android.synthetic.main.content_single_player.playerView
import toro.demo.exoplayer.DemoApp
import toro.demo.exoplayer.R

/**
 * Demo for @see [Playable]. Written in Kotlin.
 */
class PlayableDemoActivity : AppCompatActivity() {

    companion object {
        private val videoUri = Uri.parse("file:///android_asset/bbb/video.mp4")
    }

    private var playable: Playable? = null
    private var exoPlayerView: PlayerView? = null

    private val listener = object : DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            exoPlayerView!!.keepScreenOn = playWhenReady
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val windowSize = Point()
        window.windowManager.defaultDisplay.getSize(windowSize)
        val landscape = windowSize.y < windowSize.x
        exoPlayerView = //
                if (landscape) {
                    setContentView(R.layout.activity_single_player_landscape)
                    player_view
                } else {
                    setContentView(R.layout.activity_single_player)
                    setSupportActionBar(toolbar)
                    playerView
                }

        playable = lastCustomNonConfigurationInstance as Playable?
        if (playable == null) {
            playable = DemoApp.exoCreator!!.createPlayable(videoUri, null)
            playable!!.prepare(true)
        }
        playable!!.addEventListener(listener)
    }

    override fun onStart() {
        super.onStart()
        playable!!.playerView = exoPlayerView
        if (!playable!!.isPlaying) playable!!.play()
    }

    override fun onStop() {
        super.onStop()
        // If the activity is not finishing, we keep it playing.
        if (isFinishing) playable!!.pause()
        playable!!.playerView = null
    }

    override fun onDestroy() {
        super.onDestroy()
        playable!!.removeEventListener(listener)
        if (isFinishing) playable!!.release()
    }

    override fun onRetainCustomNonConfigurationInstance(): Any {
        return playable!!
    }
}
