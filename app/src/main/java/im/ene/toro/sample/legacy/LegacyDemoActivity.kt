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

import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import im.ene.toro.sample.R
import im.ene.toro.sample.common.BaseActivity
import im.ene.toro.widget.Container

/**
 * @author eneim (6/26/17).
 *
 *  Demo using Android official [android.widget.VideoView]. In which we will use [ToroVideoView]
 *  which is an extension of [android.widget.VideoView] to provide on top of it an "event listener".
 */
class LegacyDemoActivity : BaseActivity() {

  var container: Container? = null
  var adapter: VideoListAdapter? = null
  var layoutManager: LinearLayoutManager? = null

  var statusDebug: TextView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_legacy)
    container = findViewById(R.id.player_container)
    statusDebug = findViewById(R.id.status_debug)

    adapter = VideoListAdapter()
    layoutManager = LinearLayoutManager(this)

    container!!.layoutManager = layoutManager
    container!!.adapter = adapter
  }

  internal var handler = Handler(Handler.Callback {
    val player = container?.activePlayers!!.elementAtOrNull(0)
    if (player != null) {
      statusDebug?.text = "Order: ${player.playerOrder}, Pos: ${player.currentPlaybackInfo.resumePosition}"
    }
    return@Callback true
  })

  override fun onStart() {
    super.onStart()
    val runnableCode = object : Runnable {
      override fun run() {
        handler.obtainMessage().sendToTarget()
        handler.postDelayed(this, 500)
      }
    }
    handler.postDelayed(runnableCode, 500)
  }

  override fun onStop() {
    super.onStop()
    handler.removeCallbacksAndMessages(null)
  }

  override fun onDestroy() {
    adapter = null
    layoutManager = null
    container = null
    super.onDestroy()
  }
}