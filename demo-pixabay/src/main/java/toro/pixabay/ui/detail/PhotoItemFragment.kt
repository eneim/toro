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

package toro.pixabay.ui.detail

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import toro.pixabay.R
import toro.pixabay.data.entity.PixabayItem
import toro.pixabay.ui.main.getRatio

/**
 * @author eneim (2018/05/15).
 */
class PhotoItemFragment : Fragment() {

  companion object {
    const val KEY_IMAGE_RES = "com.google.samples.gridtopager.key.imageRes"
    const val KEY_IMAGE_RATIO = "toro.pixabay:item_detail:ratio"

    fun newInstance(item: PixabayItem): PhotoItemFragment {
      val fragment = PhotoItemFragment()
      val bundle = Bundle().also {
        it.putString(KEY_IMAGE_RES, item.photoItem.largeImageURL)
        it.putFloat(KEY_IMAGE_RATIO, item.photoItem.getRatio())
      }
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_photo_item, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val bundle = arguments
    val imageUrl = bundle?.getString(KEY_IMAGE_RES, "") ?: ""
    val imageRatio = bundle?.getFloat(KEY_IMAGE_RATIO) ?: 1.0f

    val imageView = view.findViewById<ImageView>(R.id.photoView)
    val container = view.findViewById<AspectRatioFrameLayout>(R.id.photoContainer)

    if (imageUrl.isNotEmpty()) {
      container.setAspectRatio(imageRatio)
      imageView.transitionName = imageUrl
      Glide.with(this).load(imageUrl)
          .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?,
                isFirstResource: Boolean): Boolean {
              // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
              // startPostponedEnterTransition() should also be called on it to get the transition
              // going in case of a failure.
              parentFragment?.startPostponedEnterTransition()
              return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?,
                target: Target<Drawable>?, dataSource: DataSource?,
                isFirstResource: Boolean): Boolean {
              // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
              // startPostponedEnterTransition() should also be called on it to get the transition
              // going in case of a failure.
              parentFragment?.startPostponedEnterTransition()
              return false
            }
          })
          .into(imageView)
    }
  }

}