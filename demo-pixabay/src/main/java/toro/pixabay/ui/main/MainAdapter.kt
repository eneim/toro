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

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import toro.pixabay.R
import toro.pixabay.data.entity.PhotoItem

/**
 * @author eneim (2018/05/11).
 */
class MainAdapter : PagedListAdapter<PhotoItem, BaseViewHolder>(DIFF_CALLBACK) {

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PhotoItem>() {
      override fun areItemsTheSame(oldItem: PhotoItem?, newItem: PhotoItem?): Boolean {
        return newItem?.id == oldItem?.id
      }

      override fun areContentsTheSame(oldItem: PhotoItem?, newItem: PhotoItem?): Boolean {
        return newItem?.toString() == oldItem?.toString()
      }

    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.holder_photo_item,
        parent, false)
    return PhotoItemViewHolder(view)
  }

  override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

}