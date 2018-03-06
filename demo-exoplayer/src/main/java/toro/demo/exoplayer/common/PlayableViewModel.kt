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

package toro.demo.exoplayer.common

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider.AndroidViewModelFactory
import android.net.Uri
import com.google.android.exoplayer2.ui.PlayerView
import im.ene.toro.exoplayer.Config
import im.ene.toro.exoplayer.Playable
import im.ene.toro.exoplayer.ToroExo
import im.ene.toro.media.PlaybackInfo

/**
 * @author eneim (2018/03/05).
 */

class PlayableViewModel(application: Application, config: Config?, mediaUri: Uri) :
        AndroidViewModel(application) {

    private val playable: Playable

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val config: Config?,
            private val uri: Uri) : AndroidViewModelFactory(application) {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayableViewModel(application, config, uri) as T
        }
    }

    init {
        val toroExo = ToroExo.with(application)
        val creator = if (config == null) toroExo.defaultCreator else toroExo.getCreator(config)
        playable = creator.createPlayable(mediaUri, null)
        playable.prepare(true)
    }

    fun play() {
        playable.play()
    }

    fun pause() {
        playable.pause()
    }

    fun setPlayerView(playerView: PlayerView?) {
        playable.playerView = playerView
    }

    fun setPlaybackInfo(playbackInfo: PlaybackInfo) {
        playable.playbackInfo = playbackInfo
    }

    fun mute(mute: Boolean) {
        if (mute) {
            playable.volume = 0f
        } else {
            playable.volume = 1f
        }
    }

    override fun onCleared() {
        super.onCleared()
        playable.release()
    }
}
