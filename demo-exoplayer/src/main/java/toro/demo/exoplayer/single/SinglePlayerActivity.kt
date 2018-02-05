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

package toro.demo.exoplayer.single

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import im.ene.toro.exoplayer.Playback.DefaultEventListener
import im.ene.toro.exoplayer.Playback.Helper
import kotlinx.android.synthetic.main.activity_single_player.app_bar
import kotlinx.android.synthetic.main.activity_single_player.playerView
import kotlinx.android.synthetic.main.activity_single_player.toolbar
import toro.demo.exoplayer.DemoApp
import toro.demo.exoplayer.R

class SinglePlayerActivity : AppCompatActivity() {

    companion object {
        private val videoUri = Uri.parse("file:///android_asset/bbb/video.mp4")
    }

    private var helper: Helper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_player)
        setSupportActionBar(toolbar)

        helper = DemoApp.playerHub!!.createHelper(playerView, videoUri, DefaultEventListener())
        helper!!.prepare()
        app_bar.addOnOffsetChangedListener { _, offset ->
            val rate = ((app_bar.height + offset.toFloat()) / app_bar.height.toFloat())
            if (rate < 0.75) helper!!.pause() else if (!helper!!.isPlaying) helper!!.play()
        }
    }

    override fun onStart() {
        super.onStart()
        helper!!.play()
    }

    override fun onStop() {
        super.onStop()
        helper!!.pause()
    }
}
