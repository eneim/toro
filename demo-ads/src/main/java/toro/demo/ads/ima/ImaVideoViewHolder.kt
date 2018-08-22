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

import android.net.Uri
import android.view.View
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.ui.PlayerView
import im.ene.toro.ToroPlayer
import im.ene.toro.ToroUtil
import im.ene.toro.exoplayer.AdsExoPlayerViewHelper
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.widget.Container
import toro.demo.ads.R
import toro.demo.ads.common.BaseViewHolder

/**
 * @author eneim (2018/08/22).
 */
@Suppress("MemberVisibilityCanBePrivate")
class ImaVideoViewHolder(itemView: View) : BaseViewHolder(itemView), ToroPlayer {

  var helper: AdsExoPlayerViewHelper? = null
  val exoPlayerView: PlayerView = itemView.findViewById(R.id.playerView)

  val mediaUri = Uri.parse(itemView.context.getString(R.string.ima_content_url))!!
  val adTagUri = Uri.parse(itemView.context.getString(R.string.ima_ad_tag_url))!!
  val adLoader = ImaAdsLoader(itemView.context, adTagUri)

  override fun getPlayerView() = this.exoPlayerView

  override fun getCurrentPlaybackInfo(): PlaybackInfo {
    return helper?.latestPlaybackInfo ?: PlaybackInfo()
  }

  override fun initialize(container: Container, playbackInfo: PlaybackInfo) {
    (helper ?: AdsExoPlayerViewHelper(this, mediaUri, null,
        adLoader).also { helper = it }).initialize(container, playbackInfo)
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
  }

  override fun wantsToPlay() = ToroUtil.visibleAreaOffset(this, itemView.parent) >= 0.75

  override fun getPlayerOrder() = adapterPosition
}