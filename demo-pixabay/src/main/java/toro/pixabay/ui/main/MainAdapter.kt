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
import android.support.transition.TransitionSet
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import im.ene.toro.exoplayer.ExoCreator
import toro.pixabay.R
import toro.pixabay.common.NetworkState
import toro.pixabay.data.entity.PixabayItem
import toro.pixabay.ui.detail.ImagePagerFragment
import java.util.concurrent.atomic.AtomicBoolean


/**
 * @author eneim (2018/05/11).
 */
class MainAdapter(private val creator: ExoCreator, private val fragment: Fragment) :  //
    PagedListAdapter<PixabayItem, BaseViewHolder>(DIFF_CALLBACK) {

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PixabayItem>() {
      override fun areItemsTheSame(oldItem: PixabayItem?, newItem: PixabayItem?): Boolean {
        return newItem?.pageUrl == oldItem?.pageUrl
      }

      override fun areContentsTheSame(oldItem: PixabayItem?, newItem: PixabayItem?): Boolean {
        return newItem?.toString() == oldItem?.toString()
      }
    }
  }

  private var inflater: LayoutInflater? = null
  private var networkState: NetworkState? = null

  private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

  fun setNetworkState(newNetworkState: NetworkState?) {
    val previousState = this.networkState
    val hadExtraRow = hasExtraRow()
    this.networkState = newNetworkState
    val hasExtraRow = hasExtraRow()
    if (hadExtraRow != hasExtraRow) {
      if (hadExtraRow) {
        notifyItemRemoved(super.getItemCount())
      } else {
        notifyItemInserted(super.getItemCount())
      }
    } else if (hasExtraRow && previousState != newNetworkState) {
      notifyItemChanged(itemCount - 1)
    }
  }

  override fun getItemCount(): Int {
    return super.getItemCount() + if (hasExtraRow()) 1 else 0
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    if (inflater?.context != parent.context) {
      inflater = LayoutInflater.from(parent.context)
    }

    val view = inflater!!.inflate(viewType, parent, false)
    return when (viewType) {
      R.layout.holder_photo_item -> PhotoItemViewHolder(view,
          ViewHolderListenerImpl(fragment, this))
      R.layout.holder_video_item -> VideoItemViewHolder(view, creator)
      R.layout.holder_loading -> LoadingViewHolder(view)
      else -> throw IllegalArgumentException("unknown view type $viewType")
    }
  }

  override fun onBindViewHolder(holder: BaseViewHolder, pos: Int) {
    val type = getItemViewType(pos)
    when (type) {
      R.layout.holder_photo_item -> (holder as? PhotoItemViewHolder)?.bind(getItem(pos)?.photoItem)
      R.layout.holder_video_item -> (holder as? VideoItemViewHolder)?.bind(getItem(pos)?.videoItem)
      R.layout.holder_loading -> (holder as? LoadingViewHolder)?.bind(null)
    }
  }

  override fun getItemViewType(position: Int): Int {
    return if (hasExtraRow() && position == itemCount - 1) {
      R.layout.holder_loading
    } else {
      val itemType = getItem(position)?.type ?: R.layout.holder_loading
      when (itemType) {
        1 -> R.layout.holder_photo_item
        2 -> R.layout.holder_video_item
        else -> itemType
      }
    }
  }

  /**
   * A listener that is attached to all ViewHolders to handle image loading events and clicks.
   */
  interface ViewHolderListener {

    fun onLoadCompleted(view: ImageView, adapterPosition: Int)

    fun onItemClicked(view: View, adapterPosition: Int)
  }

  /**
   * Default [ViewHolderListener] implementation.
   */
  private class ViewHolderListenerImpl(
      private val fragment: Fragment,
      private val adapter: MainAdapter) : ViewHolderListener {
    private val enterTransitionStarted: AtomicBoolean = AtomicBoolean()

    override fun onLoadCompleted(view: ImageView, adapterPosition: Int) {
      // Call startPostponedEnterTransition only when the 'selected' image loading is completed.
      if (MainActivity.currentPosition != adapterPosition) {
        return
      }
      if (enterTransitionStarted.getAndSet(true)) {
        return
      }
      fragment.startPostponedEnterTransition()
    }

    /**
     * Handles a view click by setting the current position to the given `position` and
     * starting a [ImagePagerFragment] which displays the image at the position.
     *
     * @param view the clicked [ImageView] (the shared element view will be re-mapped at the
     * GridFragment's SharedElementCallback)
     * @param adapterPosition the selected view position
     */
    override fun onItemClicked(view: View, adapterPosition: Int) {
      // Update the position.
      MainActivity.currentPosition = adapterPosition

      // Exclude the clicked card from the exit transition (e.g. the card will disappear immediately
      // instead of fading out with the rest to prevent an overlapping animation of fade and move).
      (fragment.exitTransition as TransitionSet).excludeTarget(view, true)

      val transitioningView = view.findViewById<ImageView>(R.id.imageView)
      fragment.fragmentManager!!.beginTransaction()
          .setReorderingAllowed(true) // Optimize for shared element transition
          .addSharedElement(transitioningView, transitioningView.transitionName)
          .replace(R.id.fragmentContainer,
              ImagePagerFragment.newInstance(adapter.getItem(adapterPosition)!!),
              ImagePagerFragment::class.java.simpleName)
          .addToBackStack(null)
          .commit()
    }
  }
}