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
import im.ene.toro.exoplayer.ToroExo.with
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.widget.Container
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_demo_items.recyclerView
import toro.demo.exoplayer.common.DemoItem
import toro.demo.exoplayer.common.DemoMediaDrm
import toro.demo.exoplayer.common.DemoRepository

/**
 * @author eneim (2018/03/05).
 */
class DemoItemsFragment : Fragment() {

    companion object {
        fun newInstance() = DemoItemsFragment()
        val configMap = HashMap<DemoMediaDrm, Config>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_demo_items, container, false)
    }

    private val adapter: DemoAdapter by lazy { DemoAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val handler = Handler()
        recyclerView.adapter = adapter
        val demoItemMap = HashMap<DemoItem, List<DemoMediaDrm>>()
        DemoRepository().loadDemoItems(requireContext()).doOnNext {
            it.forEach {
                val drmList = arrayListOf<DemoMediaDrm>()
                it.samples.filter { it.drmScheme != null }.map { item ->
                    DemoMediaDrm(item.drmScheme!!, item.drmLicenseUrl, false)
                }.distinctBy { it.hashCode() }.forEach {
                    drmList.add(it)
                    val manager = if (Util.SDK_INT >= 18) with(
                            requireContext()).createDrmSessionManager(it, handler)
                    else null
                    if (manager != null) configMap[it] = DemoApp.config!!.newBuilder()
                            .setDrmSessionManagers(arrayOf(manager)).build()
                }
                demoItemMap[it] = drmList
            }
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { adapter.setItems(it) }
    }

    //// ViewHolder

    class DemoItemViewHolder(itemView: View) : ViewHolder(itemView), ToroPlayer {
        override fun getPlayerView() = playerView

        override fun getCurrentPlaybackInfo() = helper?.latestPlaybackInfo ?: PlaybackInfo()

        override fun initialize(container: Container, playbackInfo: PlaybackInfo?) {
            if (listener == null) {
                listener = object : DefaultEventListener() {
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        super.onPlayerStateChanged(playWhenReady, playbackState)
                        playerStatus.text = "State: $playbackState, play: $playWhenReady"
                    }

                    override fun onPlayerError(error: ExoPlaybackException?) {
                        super.onPlayerError(error)
                        playerStatus.text = "Error: ${error?.localizedMessage}"
                    }
                }
                helper?.addEventListener(listener!!)
            }
            helper?.initialize(container, playbackInfo)
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

        fun bind(config: Config, item: DemoItem) {
            demoItem = item
            if (demoItem?.samples?.isNotEmpty()!!) {
                val videoUri = demoItem?.samples!![0].uri
                if (videoUri != null) helper = ExoPlayerViewHelper(this, Uri.parse(videoUri),
                        demoItem?.samples!![0].extension, config)
            }

            if (demoItem?.name?.isNotEmpty()!!) {
                itemName.text = demoItem?.name
            }

            if (demoItem!!.samples.isEmpty()) {
                itemVideos.visibility = View.GONE
            } else itemVideos.visibility = View.VISIBLE
        }
    }

    class DemoAdapter : Adapter<DemoItemViewHolder>() {

        private val items = arrayListOf<DemoItem>()
        private var configMap: HashMap<DemoMediaDrm, Config>? = null
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
            return DemoItemViewHolder(
                    inflater(parent.context).inflate(R.layout.view_holder_demo_item, parent, false)
            )
        }

        fun setConfigMap(configMap: HashMap<DemoMediaDrm, Config>) {
            this.configMap = configMap
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: DemoItemViewHolder, position: Int) {
            // holder.bind(config!!, items[position])
        }
    }

}