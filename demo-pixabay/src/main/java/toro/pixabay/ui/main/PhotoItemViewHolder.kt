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

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import toro.pixabay.R
import toro.pixabay.data.entity.PhotoItem

/**
 * @author eneim (2018/05/11).
 */
class PhotoItemViewHolder(view: View) : BaseViewHolder(view) {

  private val imageView = itemView.findViewById(R.id.imageView) as ImageView

  override fun bind(item: Any?) {
    val photo = item as PhotoItem?
    if (photo != null) {
      Glide.with(imageView).load(photo.largeImageURL)
          .transition(DrawableTransitionOptions.withCrossFade())
          .thumbnail(0.15f)
          .apply(options).into(imageView)
    }
  }
}

fun PhotoItem.ratio(): Float {
  return this.imageWidth / (this.imageHeight.toFloat())
}