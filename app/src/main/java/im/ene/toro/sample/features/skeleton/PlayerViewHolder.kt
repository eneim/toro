/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.features.skeleton

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import im.ene.toro.ToroPlayer
import im.ene.toro.ToroUtil
import im.ene.toro.helper.SimpleExoPlayerViewHelper
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.sample.R
import im.ene.toro.widget.Container
// Better naming after import
import android.view.LayoutInflater.from as inflater

class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ToroPlayer {

  companion object {
    internal val LAYOUT_RES = R.layout.vh_skeleton_exoplayer

    // Static Factory method for Adapter to create this ViewHolder
    fun createNew(parent: ViewGroup) = PlayerViewHolder(inflater(parent.context).inflate(
        LAYOUT_RES, parent, false))
  }

  internal var playerView = itemView.findViewById<SimpleExoPlayerView>(R.id.player)
  internal var playerViewHelper: SimpleExoPlayerViewHelper? = null
  internal lateinit var mediaUri: Uri

  // Called by Adapter to pass a valid media uri here.
  fun bind(uri: Uri) {
    this.mediaUri = uri
  }

  override fun getPlayerView() = playerView!!

  override fun getCurrentPlaybackInfo(): PlaybackInfo {
    return playerViewHelper?.updatePlaybackInfo() ?: PlaybackInfo()
  }

  override fun initialize(container: Container, playbackInfo: PlaybackInfo) {
    if (playerViewHelper == null) {
      playerViewHelper = SimpleExoPlayerViewHelper(container, this, mediaUri)
    }
    playerViewHelper!!.initialize(playbackInfo)
  }

  override fun play() {
    playerViewHelper?.play()
  }

  override fun pause() {
    playerViewHelper?.pause()
  }

  override fun isPlaying() = playerViewHelper != null && playerViewHelper!!.isPlaying

  override fun release() {
    try {
      playerViewHelper?.cancel()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    playerViewHelper = null
  }

  override fun wantsToPlay(): Boolean {
    val parent = itemView.parent
    var offset = 0f
    if (parent is View) {
      offset = ToroUtil.visibleAreaOffset(playerView, parent)
    }
    return offset >= 0.85
  }

  override fun getPlayerOrder() = adapterPosition
}
