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

package toro.pixabay.common

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PagingRequestHelper
import android.arch.paging.PagingRequestHelper.Status

/**
 * @author eneim (2018/05/10).
 */

data class NetworkState constructor(
    val status: Status,
    val msg: String? = null /* error message */) {
  companion object {
    val LOADED = NetworkState(Status.SUCCESS)
    val LOADING = NetworkState(Status.RUNNING)
    fun error(msg: String?) = NetworkState(Status.FAILED, msg)
  }
}

private fun getErrorMessage(report: PagingRequestHelper.StatusReport): String {
  return PagingRequestHelper.RequestType.values().mapNotNull {
    report.getErrorFor(it)?.message
  }.first()
}

fun PagingRequestHelper.createStatusLiveData(): LiveData<NetworkState> {
  val liveData = MutableLiveData<NetworkState>()
  addListener { report ->
    when {
      report.hasRunning() -> liveData.postValue(NetworkState.LOADING)
      report.hasError() -> liveData.postValue(
          NetworkState.error(getErrorMessage(report)))
      else -> liveData.postValue(NetworkState.LOADED)
    }
  }
  return liveData
}