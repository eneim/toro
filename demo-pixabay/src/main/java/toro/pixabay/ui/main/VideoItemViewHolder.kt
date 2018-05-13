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

package toro.pixabay.ui.main

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import im.ene.toro.ToroPlayer
import im.ene.toro.ToroPlayer.EventListener
import im.ene.toro.ToroUtil
import im.ene.toro.exoplayer.ExoCreator
import im.ene.toro.exoplayer.ExoPlayable
import im.ene.toro.exoplayer.ExoPlayerViewHelper
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.widget.Container
import toro.pixabay.R
import toro.pixabay.data.entity.VideoItem
import toro.pixabay.data.entity.VideoSize

/**
 * @author eneim (2018/05/12).
 */
class VideoItemViewHolder(view: View, val creator: ExoCreator) : BaseViewHolder(view), ToroPlayer {

  val container = itemView.findViewById<AspectRatioFrameLayout>(R.id.videoContainer)
  val playerView = itemView.findViewById<PlayerView>(R.id.playerView)
  val thumbnail = itemView.findViewById<ImageView>(R.id.thumbnail)
  val status = itemView.findViewById<TextView>(R.id.status)

  val eventListener = object : EventListener {
    override fun onBuffering() {
      thumbnail.visibility = View.VISIBLE
      status.text = "Buffering"
    }

    override fun onPlaying() {
      thumbnail.visibility = View.GONE
      status.text = "Playing"
    }

    override fun onPaused() {
      thumbnail.visibility = View.VISIBLE
      status.text = "Paused"
    }

    override fun onCompleted() {
      thumbnail.visibility = View.VISIBLE
      status.text = "Ended"
    }
  }

  var playerHelper: ExoPlayerViewHelper? = null
  private var videoUri: Uri? = null

  override fun bind(item: Any?) {
    super.bind(item)
    val video = item as VideoItem?
    videoUri = video?.getVideo()
    if (video != null && videoUri != null) {
      container.setAspectRatio(video.ratio())
      Glide.with(itemView).load(video.thumbnailUri())
          .transition(DrawableTransitionOptions.withCrossFade())
          .thumbnail(0.15f)
          .apply(options).into(thumbnail)
    }
    status.text = "Bound"
  }

  override fun getPlayerView(): View {
    return playerView
  }

  override fun getCurrentPlaybackInfo(): PlaybackInfo {
    return playerHelper?.latestPlaybackInfo ?: PlaybackInfo.SCRAP
  }

  override fun initialize(container: Container, playbackInfo: PlaybackInfo) {
    if (playerHelper == null) {
      playerHelper = ExoPlayerViewHelper(this, ExoPlayable(creator, videoUri!!, null))
      playerHelper!!.addPlayerEventListener(eventListener)
    }
    playerHelper!!.initialize(container, playbackInfo)
    thumbnail.visibility = View.VISIBLE
    status.text = "Initialized"
  }

  override fun play() {
    playerHelper?.play()
  }

  override fun pause() {
    playerHelper?.pause()
  }

  override fun isPlaying(): Boolean {
    return playerHelper?.isPlaying ?: false
  }

  override fun release() {
    playerHelper?.apply {
      removePlayerEventListener(eventListener)
      release()
    }
    playerHelper = null
    thumbnail.visibility = View.VISIBLE
    status.text = "Released"
  }

  override fun wantsToPlay(): Boolean {
    return ToroUtil.visibleAreaOffset(this, itemView.parent) >= 0.84
  }

  override fun getPlayerOrder(): Int {
    return adapterPosition
  }
}

fun VideoItem.ratio(): Float {
  var size = videos.large
  if (size == null) size = videos.medium
  if (size == null) size = videos.small
  if (size == null) size = videos.tiny
  if (size == null) size = VideoSize().also {
    it.width = 100
    it.height = 100
  }
  return size.width / size.height.toFloat()
}

fun VideoItem.getVideo(): Uri? {
  var size = videos.large
  if (size == null) size = videos.medium
  if (size == null) size = videos.small
  if (size == null) size = videos.tiny
  return if (size != null) Uri.parse(size.url + "&download=1") else null
}

fun VideoItem.thumbnailUri(): String {
  return "https://i.vimeocdn.com/video/${this.pictureId}_640x360.jpg"
}