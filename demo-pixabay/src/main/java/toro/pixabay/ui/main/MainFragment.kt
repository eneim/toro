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

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.ene.toro.exoplayer.ExoCreator
import im.ene.toro.widget.Container
import toro.pixabay.Injectable
import toro.pixabay.R
import toro.pixabay.common.NetworkState
import toro.pixabay.di.ViewModelFactory
import javax.inject.Inject

class MainFragment : Fragment(), Injectable {

  companion object {
    const val STATE_QUERY = "toro:pixabay:query"
    fun newInstance() = MainFragment()
  }

  @Inject
  lateinit var creator: ExoCreator

  @Inject
  lateinit var factory: ViewModelFactory

  private lateinit var model: MainViewModel

  var container: Container? = null
  var swipeToRefresh: SwipeRefreshLayout? = null
  private var callback: Callback? = null

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    callback = context as Callback?
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.main_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    model = factory.create(MainViewModel::class.java)

    container = view.findViewById(R.id.container)
    container!!.setPlayerDispatcher { 250 }

    swipeToRefresh = view.findViewById(R.id.swipeToRefresh)

    val adapter = MainAdapter(creator)
    model.items.observe(this, Observer { adapter.submitList(it) })
    model.networkState.observe(this, Observer { adapter.setNetworkState(it) })

    container!!.adapter = adapter
    (container!!.layoutManager as GridLayoutManager).spanSizeLookup = object : SpanSizeLookup() {
      override fun getSpanSize(pos: Int): Int {
        val type = adapter.getItemViewType(pos)
        return if (type == R.layout.holder_video_item || type == R.layout.holder_loading) 3
        else 1
      }
    }

    prepareSwipeRefresh()
    model.query.observe(this, Observer { callback?.onSearchQuery(it!!) })
  }

  override fun onActivityCreated(state: Bundle?) {
    super.onActivityCreated(state)
    model.search(state?.getString(STATE_QUERY) ?: "Holiday")
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(STATE_QUERY, model.query.value)
  }

  private fun prepareSwipeRefresh() {
    model.refreshState.observe(this, Observer {
      swipeToRefresh?.isRefreshing = it == NetworkState.LOADING
    })

    swipeToRefresh?.setOnRefreshListener {
      model.refresh()
    }
  }

  fun onUserRequestSearch() {
    val dialogBuilder = AlertDialog.Builder(requireContext())
    val editText = AppCompatEditText(requireContext())
    editText.hint = "Enter your query here."
    dialogBuilder.setTitle("Search")
        .setPositiveButton("Submit",
            { _, _ -> model.search(editText.text.toString()) })
        .setNegativeButton("Cancel",
            { dialog, _ -> dialog.dismiss() })
    dialogBuilder.setView(editText)
    dialogBuilder.create().show()
  }

  fun scrollToTop() {
    container?.smoothScrollToPosition(0)
  }

  interface Callback {

    fun onSearchQuery(query: String)
  }
}
