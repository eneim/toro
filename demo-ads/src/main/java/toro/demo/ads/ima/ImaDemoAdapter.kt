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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import toro.demo.ads.R
import toro.demo.ads.common.BaseViewHolder

/**
 * @author eneim (2018/08/22).
 */
class ImaDemoAdapter(private val builder: ImaAdsLoader.Builder) : Adapter<BaseViewHolder>() {
  companion object {
    const val ITEM_COUNT = 150
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.vh_video_player, parent,
        false)
    return ImaVideoViewHolder(itemView, builder)
  }

  override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
    holder.onBind(position)
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getItemCount(): Int {
    return ITEM_COUNT
  }

}