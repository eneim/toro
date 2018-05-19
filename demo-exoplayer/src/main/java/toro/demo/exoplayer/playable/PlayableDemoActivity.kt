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

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import butterknife.ButterKnife
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.DebugTextViewHelper
import im.ene.toro.exoplayer.ExoPlayable
import im.ene.toro.exoplayer.Playable
import im.ene.toro.exoplayer.ToroExo
import im.ene.toro.media.VolumeInfo
import kotlinx.android.synthetic.main.activity_demo_playable.debugText
import kotlinx.android.synthetic.main.activity_demo_playable.playerView
import toro.demo.exoplayer.R

/**
 * @author eneim (2018/02/25).
 */

class PlayableDemoActivity : AppCompatActivity() {
  companion object {
    private val video =
    // Uri.parse("https://cdn.jwplayer.com/videos/SMd5tDhS-cSpmBcaY.mp4");
        Uri.parse("file:///android_asset/bbb/video.mp4")
  }

  var playable: Playable? = null
  var helper: DebugTextViewHelper? = null

  private fun release() {
    if (playable != null) {
      playable!!.pause()
      playable!!.release()
      playable = null
    }

    ToroExo.with(this).cleanUp()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_demo_playable)
    ButterKnife.bind(this)

    playable = lastCustomNonConfigurationInstance as Playable?
    if (playable == null) {
      playable = ExoPlayable(
          /* DemoApp.exoCreator */ // un-comment to use custom ExoCreator
          ToroExo.with(this).defaultCreator, video, "mp4")
          .also {
            it.prepare(true)
            it.play()
          }
    }

    playable!!.playerView = playerView
    helper = DebugTextViewHelper(playerView!!.player as SimpleExoPlayer, debugText)
    preparePlayableControlButtons()
  }

  override fun onStart() {
    super.onStart()
    helper?.start()
  }

  override fun onStop() {
    super.onStop()
    helper?.stop()
  }

  override fun onDestroy() {
    super.onDestroy()
    if (isFinishing) release()
  }

  // Save this for config change.
  override fun onRetainCustomNonConfigurationInstance(): Any? {
    return playable
  }

  // Dynamically add buttons to control the Playable instance.
  private fun preparePlayableControlButtons() {
    val buttonContainer = findViewById<ViewGroup>(R.id.buttonContainer)
    buttonContainer.removeAllViews()
    val inflater = LayoutInflater.from(buttonContainer.context)

    buttonContainer.addCustomButton(inflater, "Play", { playable!!.play() })
    buttonContainer.addCustomButton(inflater, "Pause", { playable!!.pause() })
    buttonContainer.addCustomButton(inflater, "Mute",
        { playable!!.volumeInfo = VolumeInfo(true, 0.0F) })
    buttonContainer.addCustomButton(inflater, "Un Mute",
        { playable!!.volumeInfo = VolumeInfo(false, 1.0F) })

    // Immediately reset current playback and start it all over again
    buttonContainer.addCustomButton(inflater, "Replay", {
      if (playable != null) (playable as Playable).apply {
        this.pause()
        this.reset()
        this.play()
      }
    })
  }
}

// A extension function for our ViewGroup.
fun ViewGroup.addCustomButton(inflater: LayoutInflater, name: String,
    action: (View) -> Unit) {
  (inflater.inflate(R.layout.widget_debug_button, this, false) as Button)
      .apply {
        this.text = name
        this.setOnClickListener { action.invoke(it) }
      }
      .run { this@addCustomButton.addView(this) }
}