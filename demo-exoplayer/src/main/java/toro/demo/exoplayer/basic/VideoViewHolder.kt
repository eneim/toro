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

package toro.demo.exoplayer.basic

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import im.ene.toro.ToroPlayer
import im.ene.toro.ToroPlayer.EventListener
import im.ene.toro.ToroUtil.visibleAreaOffset
import im.ene.toro.exoplayer.ExoPlayerViewHelper
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.widget.Container
import org.jsoup.nodes.Element
import toro.demo.exoplayer.DemoApp
import toro.demo.exoplayer.R
import java.util.regex.Pattern

/**
 * @author eneim (2018/01/23).
 */
internal class VideoViewHolder(inflater: LayoutInflater?, parent: ViewGroup?) :
        BaseViewHolder(inflater!!.inflate(R.layout.exo_article_part_video, parent, false)),
        ToroPlayer {

    companion object {
        val regex = Pattern.compile("(\\d)+\\.(\\d+)")!!
        const val defaultRatio = 100 * 165.78F / 360F
        const val TAG = "ToroExo:Basic"
    }

    private val playerFrame by lazy { itemView as AspectRatioFrameLayout }
    private val player = itemView.findViewById(R.id.player) as SimpleExoPlayerView
    private var helper: ExoPlayerViewHelper? = null
    private var videoUri: Uri? = null

    private val listener = object: EventListener {
        override fun onBuffering() {
            Log.w(TAG, "onBuffering")
        }

        override fun onPlaying() {
            Log.d(TAG, "onPlaying")
        }

        override fun onPaused() {
            Log.d(TAG, "onPaused")
        }

        override fun onCompleted() {
            Log.d(TAG, "onCompleted")
        }
    }

    override fun bind(item: Any?) {
        super.bind(item)
        val videoUrl = (item as Element).select("video > source[type=video/mp4]").attr("src")
        if (videoUrl !== null) videoUri = Uri.parse(videoUrl)
        val style = item.getElementsByClass("qp-ui-video-player-mouse").attr("style")
        if (style !== null) {
            val match = regex.matcher(style)
            var ratio = if (match.find()) match.group().toFloat() else null
            if (ratio === null) ratio = defaultRatio
            playerFrame.setAspectRatio(100F / ratio)
        }
    }

    override fun getPlayerView() = player

    override fun getCurrentPlaybackInfo() = helper?.latestPlaybackInfo ?: PlaybackInfo()

    override fun initialize(container: Container, playbackInfo: PlaybackInfo?) {
        if (helper === null) {
            helper = ExoPlayerViewHelper(container, this,
                    videoUri!!, DemoApp.exoCreator!!, null)
            helper!!.addPlayerEventListener(listener)
        }
        helper!!.initialize(playbackInfo)
    }

    override fun play() {
        Log.d(TAG, hashCode().toString() + "#play()")
        helper!!.play()
    }

    override fun pause() {
        Log.d(TAG, hashCode().toString() + "#pause()")
        helper!!.pause()
    }

    override fun isPlaying() = helper?.isPlaying ?: false

    override fun release() {
        helper?.removePlayerEventListener(listener)
        helper?.release()
        helper = null
    }

    override fun wantsToPlay() = visibleAreaOffset(this, itemView.parent) >= 0.65

    override fun getPlayerOrder() = adapterPosition

    override fun onSettled(container: Container?) {}
}