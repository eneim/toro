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

package toro.demo.exoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import im.ene.toro.ToroPlayer
import im.ene.toro.ToroUtil
import im.ene.toro.exoplayer.Config
import im.ene.toro.exoplayer.ExoPlayerViewHelper
import im.ene.toro.exoplayer.Playable.DefaultEventListener
import im.ene.toro.exoplayer.Playable.EventListener
import im.ene.toro.exoplayer.ToroExo
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.widget.Container
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_demo_items.recyclerView
import toro.demo.exoplayer.common.DemoItem
import toro.demo.exoplayer.common.DemoMediaDrm
import toro.demo.exoplayer.common.DemoRepository
import toro.demo.exoplayer.common.VideoItem

/**
 * @author eneim (2018/03/05).
 */
class DemoItemsFragment : Fragment(), (DemoItem) -> Config {
  companion object {
    fun newInstance() = DemoItemsFragment()
  }

  private val configCache = HashMap<DemoMediaDrm, Config>()

  // (DemoItem) -> Config
  override fun invoke(item: DemoItem): Config {
    val defaultConfig = DemoApp.config!!
    if (Util.SDK_INT < 18) return defaultConfig

    // Step 1: process 1st level Videos.
    val drmList = item.samples.filter { it.drmScheme != null }.map { it ->
      DemoMediaDrm(it.drmScheme!!, it.drmLicenseUrl, false)
    }.distinctBy { it.hashCode() }

    var config = configCache[drmList.firstOrNull()]
    if (config == null) {
      config = drmList.map { it ->
        ToroExo.with(requireContext()).createDrmSessionManager(it, Handler())
      }.run {
        defaultConfig.newBuilder().setDrmSessionManagers(this.toTypedArray()).build()
      }
      drmList.forEach { configCache[it] = config }
    }

    return config!!
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, st: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_demo_items, container, false)
  }

  private val adapter: DemoAdapter by lazy { DemoAdapter(this) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recyclerView.adapter = adapter
    DemoRepository().loadDemoItems(requireContext())
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { adapter.setItems(it) }
  }

}

//// ViewHolder

class DemoItemViewHolder(itemView: View) : ViewHolder(itemView), ToroPlayer {
  override fun getPlayerView() = playerView

  override fun getCurrentPlaybackInfo() = helper?.latestPlaybackInfo ?: PlaybackInfo()

  override fun initialize(container: Container, playbackInfo: PlaybackInfo?) {
    val videoUri = videoItem!!.uri
    if (videoUri != null && helper == null) {
      helper = ExoPlayerViewHelper(this, Uri.parse(videoUri),
          videoItem!!.extension, act!!.invoke(demoItem!!))
    }

    if (listener == null) {
      listener = object : DefaultEventListener() {
        @SuppressLint("SetTextI18n")
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
          super.onPlayerStateChanged(playWhenReady, playbackState)
          playerStatus.text = "State: $playbackState, play: $playWhenReady"
        }

        @SuppressLint("SetTextI18n")
        override fun onPlayerError(error: ExoPlaybackException?) {
          super.onPlayerError(error)
          playerStatus.text = "Error: ${error?.localizedMessage}"
        }
      }
      helper!!.addEventListener(listener!!)
    }
    helper!!.initialize(container, playbackInfo)
  }

  override fun play() {
    helper?.play()
  }

  override fun pause() {
    helper?.pause()
  }

  override fun isPlaying() = helper?.isPlaying ?: false

  override fun release() {
    if (listener != null) {
      helper?.removeEventListener(listener)
      listener = null
    }
    helper?.release()
    helper = null
  }

  override fun wantsToPlay() = ToroUtil.visibleAreaOffset(this, itemView.parent) > 0.85

  override fun getPlayerOrder() = adapterPosition

  override fun onSettled(container: Container?) {

  }

  private val playerView: PlayerView = itemView.findViewById(R.id.itemVideo)
  private val itemName: TextView = itemView.findViewById(R.id.itemName)
  private val itemVideos: View = itemView.findViewById(R.id.itemVideos)
  private val playerStatus: TextView = itemView.findViewById(R.id.playerStatus)

  private var demoItem: DemoItem? = null
  private var helper: ExoPlayerViewHelper? = null
  private var listener: EventListener? = null
  private var videoItem: VideoItem? = null
  private var act: ((DemoItem) -> Config)? = null

  fun bind(item: DemoItem, act: (DemoItem) -> Config) {
    this.act = act
    this.demoItem = item
    if (demoItem?.samples?.isNotEmpty()!!) {
      videoItem = demoItem?.samples!![0]
    }

    if (demoItem?.name?.isNotEmpty()!!) {
      itemName.text = demoItem?.name
    }

    if (demoItem!!.samples.isEmpty()) itemVideos.visibility = View.GONE
    else itemVideos.visibility = View.VISIBLE

    if (videoItem?.uri == null) {
      if (videoItem!!.playlist?.isNotEmpty()!!) {
        videoItem = videoItem!!.playlist!![0]
      }
    }

    playerStatus.text = "Idle"
  }
}

class DemoAdapter(private val act: (DemoItem) -> Config) : Adapter<DemoItemViewHolder>() {

  private val items = arrayListOf<DemoItem>()
  private var inflater: LayoutInflater? = null

  private fun inflater(context: Context): LayoutInflater {
    if (inflater == null || inflater!!.context !== context) {
      inflater = LayoutInflater.from(context)
    }
    return inflater!!
  }

  fun setItems(newItems: List<DemoItem>) {
    items.clear()
    items.addAll(newItems)
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoItemViewHolder {
    return DemoItemViewHolder(inflater(parent.context)
        .inflate(R.layout.view_holder_demo_item, parent, false)
    )
  }

  override fun getItemCount() = items.size

  override fun onBindViewHolder(holder: DemoItemViewHolder, position: Int) {
    holder.bind(items[position], act)
  }
}