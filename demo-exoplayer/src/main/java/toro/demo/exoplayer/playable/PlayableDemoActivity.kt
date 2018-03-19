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

@file:Suppress("RedundantOverride")

package toro.demo.exoplayer.playable

import android.arch.lifecycle.ViewModelProviders
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.exoplayer2.ui.PlayerView
import im.ene.toro.exoplayer.Playable
import toro.demo.exoplayer.DemoItemsFragment
import toro.demo.exoplayer.common.PlayableViewModel

/**
 * Demo for @see [Playable]. Written in Kotlin.
 */
class PlayableDemoActivity : AppCompatActivity() {

    companion object {
        private val videoUri = Uri.parse("file:///android_asset/bbb/video.mp4")
    }

    private var exoPlayerView: PlayerView? = null

    private val viewModel: PlayableViewModel by lazy {
        ViewModelProviders.of(this,
                PlayableViewModel.Factory(application, null, videoUri)).get(
                PlayableViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val windowSize = Point()
//        window.windowManager.defaultDisplay.getSize(windowSize)
//        val landscape = windowSize.y < windowSize.x
//        exoPlayerView = //
//                if (landscape) {
//                    setContentView(R.layout.activity_single_player_landscape)
//                    player_view
//                } else {
//                    setContentView(R.layout.activity_single_player)
//                    setSupportActionBar(toolbar)
//                    playerView
//                }
//
//        viewModel.setPlayerView(exoPlayerView!!)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(android.R.id.content,
                    DemoItemsFragment.newInstance()).commit()
        }
    }

    override fun onStart() {
        super.onStart()
        // viewModel.play()
    }

    override fun onStop() {
        super.onStop()
        // If the activity is not finishing, we keep it playing.
//        if (isFinishing) viewModel.pause()
//        viewModel.setPlayerView(null)
    }
}
