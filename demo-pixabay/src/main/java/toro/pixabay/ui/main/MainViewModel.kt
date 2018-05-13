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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.Transformations.switchMap
import android.arch.lifecycle.ViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repo: MainRepository
) : ViewModel() {

  val query = MutableLiveData<String>()
  private val results = Transformations.map(query, { input -> repo.getItems(input) })

  val items = switchMap(results, { input -> input.items })!!
  val networkState = switchMap(results, { input -> input.networkState })!!
  val refreshState = switchMap(results, { it.refreshState })!!

  fun search(query: String): Boolean {
    if (this.query.value == query) return false
    this.query.value = query
    return true
  }

  fun refresh() {
    results.value?.refresh?.invoke()
  }
}
