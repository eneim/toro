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

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import im.ene.toro.CacheManager

/**
 * @author eneim (6/26/17).
 *
 */
class VideoListAdapter : RecyclerView.Adapter<VideoViewHolder>(), CacheManager {

  private val mediaList = MediaList()

  override fun getItemCount() = mediaList.size

  override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
    holder.bind(mediaList[position])
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = //
      VideoViewHolder.newInstance(parent)

  override fun getKeyForOrder(order: Int) = order

  override fun getOrderForKey(key: Any) = key as Int?

}