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

package toro.demo.ads.ima

import android.content.Context
import android.net.Uri
import android.view.View
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.ui.PlayerView
import im.ene.toro.ToroPlayer
import im.ene.toro.ToroUtil
import im.ene.toro.helper.ToroPlayerHelper
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.widget.Container
import toro.demo.ads.R
import toro.demo.ads.common.BaseViewHolder
import toro.v4.Media
import toro.v4.MediaItem
import toro.v4.exo.MediaHub

/**
 * @author eneim (2018/08/22).
 */
@Suppress("MemberVisibilityCanBePrivate")
class ImaVideoViewHolder( //
    itemView: View, builder: ImaAdsLoader.Builder //
) : BaseViewHolder(itemView), ToroPlayer {

  var helper: ToroPlayerHelper? = null
  val exoPlayerView: PlayerView = itemView.findViewById(R.id.playerView)

  val mediaUri = Uri.parse(itemView.context.getString(R.string.ima_content_url))!!
  val media = MediaItem(mediaUri, null) as Media
  val adTagUri = Uri.parse(itemView.context.getString(R.string.ima_ad_tag_url))!!
  val adLoader = builder.buildForAdTag(adTagUri)!!

  override fun getPlayerView() = this.exoPlayerView

  override fun getCurrentPlaybackInfo(): PlaybackInfo {
    return helper?.latestPlaybackInfo ?: PlaybackInfo()
  }

  override fun initialize(container: Container, playbackInfo: PlaybackInfo) {
    (helper ?: createPlayerHelper(container.context).also { helper = it }) //
        .initialize(container, playbackInfo)
  }

  fun createPlayerHelper(context: Context): ToroPlayerHelper {
    val playable = MediaHub.get(context).createAdsPlayable(this.media, adLoader,
        exoPlayerView.overlayFrameLayout)
    return MediaHub.get(context).requestHelper(this, playable)
  }

  override fun play() {
    helper?.play()
  }

  override fun pause() {
    helper?.pause()
  }

  override fun isPlaying(): Boolean {
    return helper?.isPlaying ?: false
  }

  override fun release() {
    helper?.run {
      this.release()
    }
    helper = null
  }

  override fun wantsToPlay() = ToroUtil.visibleAreaOffset(this, itemView.parent) >= 0.75

  override fun getPlayerOrder() = adapterPosition
}