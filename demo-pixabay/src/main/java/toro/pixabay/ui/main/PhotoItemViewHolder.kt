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

import android.graphics.drawable.Drawable
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import toro.pixabay.R
import toro.pixabay.data.entity.PhotoItem
import toro.pixabay.ui.main.MainAdapter.ViewHolderListener

/**
 * @author eneim (2018/05/11).
 */
class PhotoItemViewHolder(val view: View, val listener: ViewHolderListener) : BaseViewHolder(
    view), OnClickListener {
  val imageView = itemView.findViewById<ImageView>(R.id.imageView)

  init {
    itemView.setOnClickListener(this)
  }

  override fun bind(item: Any?) {
    val photo = item as PhotoItem?
    if (photo != null) {
      imageView.transitionName = photo.pageURL
      Glide.with(imageView).load(photo.largeImageURL)
          .transition(DrawableTransitionOptions.withCrossFade())
          .thumbnail(0.25f)
          .apply(options.placeholder(R.drawable.side_nav_bar))
          .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?,
                isFirstResource: Boolean): Boolean {
              listener.onLoadCompleted(imageView, adapterPosition)
              return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?,
                target: Target<Drawable>?, dataSource: DataSource?,
                isFirstResource: Boolean): Boolean {
              listener.onLoadCompleted(imageView, adapterPosition)
              return false
            }
          })
          .into(imageView)
    }
  }

  override fun onClick(v: View?) {
    listener.onItemClicked(v!!, adapterPosition)
  }
}

fun PhotoItem.getRatio(): Float {
  return this.imageWidth / this.imageHeight.toFloat()
}