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

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.transition.TransitionInflater
import android.support.v4.app.Fragment
import android.support.v4.app.SharedElementCallback
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import toro.pixabay.Injectable
import toro.pixabay.R
import toro.pixabay.data.entity.PixabayItem
import toro.pixabay.di.ViewModelFactory
import toro.pixabay.ui.main.MainActivity
import javax.inject.Inject


/**
 * @author eneim (2018/05/15).
 */
class ImagePagerFragment : Fragment(), Injectable {

  companion object {
    const val KEY_ITEM_URL = "toro.pixabay:item_detail:url"
    fun newInstance(item: PixabayItem): ImagePagerFragment {
      val fragment = ImagePagerFragment()
      fragment.arguments = Bundle().also {
        it.putString(KEY_ITEM_URL, item.pageUrl)
      }
      return fragment
    }
  }

  @Inject
  lateinit var factory: ViewModelFactory

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_pager, container, false)
  }

  var viewPager: ViewPager? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val viewModel = factory.create(DetailViewModel::class.java)
    val itemUrl = arguments?.getString(KEY_ITEM_URL) ?: ""

    viewPager = view.findViewById(R.id.viewPager)
    if (itemUrl.isNotEmpty()) viewModel.pageUrl.value = itemUrl
    viewModel.photoItem.observe(this, Observer {
      viewPager!!.adapter = PhotoPagerAdapter(this, it!!)
    })

    viewPager!!.currentItem = MainActivity.currentPosition
    viewPager!!.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
      override fun onPageSelected(position: Int) {
        MainActivity.currentPosition = position
      }
    })

    prepareSharedElementTransition()

    // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
    if (savedInstanceState == null) {
      postponeEnterTransition()
    }
  }

  /**
   * Prepares the shared element transition from and back to the grid fragment.
   */
  private fun prepareSharedElementTransition() {
    val transition = TransitionInflater.from(context)
        .inflateTransition(R.transition.image_shared_element_transition)
    transition.duration = 375
    sharedElementEnterTransition = transition

    val viewPager = viewPager!!
    // A similar mapping is set at the GridFragment with a setExitSharedElementCallback.
    setEnterSharedElementCallback(object : SharedElementCallback() {
      override fun onMapSharedElements(names: MutableList<String>?,
          sharedElements: MutableMap<String, View>?) {
        // Locate the image view at the primary fragment (the ImageFragment that is currently
        // visible). To locate the fragment, call instantiateItem with the selection position.
        // At this stage, the method will simply return the fragment at the position and will
        // not create a new one.
        val currentFragment = viewPager.adapter!!.instantiateItem(viewPager, 0) as Fragment
        val view = currentFragment.view

        // Map the first shared element name to the child ImageView.
        if (view !== null) {
          sharedElements?.put(names?.get(0)!!, view.findViewById(R.id.photoView))
        }
      }
    })
  }
}