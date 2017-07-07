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

package im.ene.toro.sample.legacy

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import im.ene.toro.ToroPlayer
import im.ene.toro.ToroUtil
import im.ene.toro.helper.ToroPlayerHelper
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.sample.R
import im.ene.toro.widget.Container

import android.view.LayoutInflater.from as inflater

/**
 * @author eneim (6/26/17).
 */
class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ToroPlayer {

  companion object {
    fun newInstance(viewGroup: ViewGroup) = VideoViewHolder(
        inflater(viewGroup.context).inflate(R.layout.view_holder_legacy_basic, viewGroup, false))
  }

  var videoView: ToroVideoView = itemView.findViewById(R.id.video_view)
  var mediaUri: Uri? = null
  var playerHelper: ToroPlayerHelper? = null

  override fun getPlayerView() = videoView

  override fun getCurrentPlaybackInfo() = playerHelper?.latestPlaybackInfo ?: PlaybackInfo()

  override fun initialize(container: Container, playbackInfo: PlaybackInfo?) {
    if (mediaUri == null) throw NullPointerException("MediaUri is null.")
    if (playerHelper == null) {
      playerHelper = LegacyVideoViewHelper(container, this, mediaUri!!)
    }
    playerHelper!!.initialize(playbackInfo)
  }

  override fun play() {
    playerHelper?.play()
  }

  override fun pause() {
    playerHelper?.pause()
  }

  override fun isPlaying() = playerHelper != null && playerHelper!!.isPlaying

  override fun release() {
    playerHelper?.release()
    playerHelper = null
  }

  override fun wantsToPlay(): Boolean {
    return ToroUtil.visibleAreaOffset(this, itemView.parent) >= 0.85
  }

  override fun getPlayerOrder() = adapterPosition

  // API to call from Adapter
  fun bind(media: Media?) {
    if (media != null) {
      this.mediaUri = media.mediaUri
    }
  }
}