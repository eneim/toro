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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import im.ene.toro.widget.Container
import toro.demo.ads.R
import toro.demo.ads.common.BaseFragment

/**
 * @author eneim (2018/08/22).
 */
class ImaDemoFragment : BaseFragment(), AdEventListener {

  companion object {
    fun newInstance() = ImaDemoFragment()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_recycler_view, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    val container = view.findViewById<Container>(R.id.native_recycler_view)
    container.layoutManager = LinearLayoutManager(requireContext())
    val adapter = ImaDemoAdapter(ImaAdsLoader.Builder(requireContext()).setAdEventListener(this))
    container.adapter = adapter
  }

  override fun onAdEvent(event: AdEvent?) {
    Log.d("Toro:Ads", "ev: $event")
  }
}