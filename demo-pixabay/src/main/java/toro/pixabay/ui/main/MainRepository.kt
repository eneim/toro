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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagingRequestHelper
import android.arch.paging.PagingRequestHelper.Request.Callback
import android.arch.paging.PagingRequestHelper.RequestType.AFTER
import toro.pixabay.common.ListModel
import toro.pixabay.common.NetworkState
import toro.pixabay.data.Api
import toro.pixabay.data.PixabayDao
import toro.pixabay.data.entity.PhotoSearchResult
import toro.pixabay.data.entity.PixabayItem
import toro.pixabay.data.entity.VideoSearchResult
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

/**
 * @author eneim (2018/05/10).
 */
class MainRepository @Inject constructor(
    private val api: Api,
    private val dao: PixabayDao,
    @Named("io.executor") private val ioExecutor: Executor,
    @Named("disk.executor") private val diskExecutor: Executor
) {

  companion object {
    private const val DEFAULT_NETWORK_PAGE_SIZE = 10
  }

  private val helper = PagingRequestHelper(ioExecutor)
  private val photoPageSize = DEFAULT_NETWORK_PAGE_SIZE * 3
  private val videoPageSize = DEFAULT_NETWORK_PAGE_SIZE

  private fun insertToDb(result: List<PixabayItem>?) {
    diskExecutor.execute { dao.insertItems(result!!) }
  }

  private fun insertResultToDb(photoSearchResult: PhotoSearchResult,
      videoSearchResult: VideoSearchResult, query: String, refresh: Boolean, callback: Callback) {
    diskExecutor.execute {
      if (refresh) dao.deleteItemsForQuery(query)
      val photos = photoSearchResult.hits
      val videos = videoSearchResult.hits
      val itemCount = photos.size + videos.size
      val limit = Math.min(itemCount / 3, videos.size)
      var index = 0
      val result = arrayListOf<PixabayItem>()
      while (index < limit) {
        result.add(PixabayItem.fromPhotoItem(photos[index * 3 + 0]).also { it.query = query })
        result.add(PixabayItem.fromPhotoItem(photos[index * 3 + 1]).also { it.query = query })
        result.add(PixabayItem.fromPhotoItem(photos[index * 3 + 2]).also { it.query = query })
        result.add(PixabayItem.fromVideoItem(videos[index]).also { it.query = query })
        index++
      }
      dao.insertItems(result)
      callback.recordSuccess()
    }
  }

  fun getItems(query: String): ListModel<PixabayItem> {
    val boundaryCallback = PixabayItemsBoundaryCallback(
        query, api, ioExecutor, diskExecutor, helper, this::insertToDb, DEFAULT_NETWORK_PAGE_SIZE
    )

    val refreshTrigger = MutableLiveData<Unit>()
    val refreshState = Transformations.switchMap(refreshTrigger, {
      refresh(query)
    })

    return ListModel<PixabayItem>(
        LivePagedListBuilder<Int, PixabayItem>(dao.getItemsForQuery(query),
            DEFAULT_NETWORK_PAGE_SIZE * 4).setBoundaryCallback(boundaryCallback).build(),
        boundaryCallback.networkState,
        refreshState,
        refresh = { refreshTrigger.value = null },
        retry = { helper.retryAllFailed() }
    )
  }

  private fun refresh(query: String): LiveData<NetworkState> {
    val networkState = MutableLiveData<NetworkState>()
    networkState.value = NetworkState.LOADING
    helper.runIfNotRunning(AFTER) {
      ioExecutor.execute {
        try {
          val photoResult = api.searchPhoto(query, 1,
              photoPageSize).execute().body()
          val videoResult = api.searchVideo(query, 1,
              videoPageSize).execute().body()
          insertResultToDb(photoResult!!, videoResult!!, query, true, it)
          networkState.postValue(NetworkState.LOADED)
        } catch (error: Throwable) {
          it.recordFailure(error)
          networkState.postValue(NetworkState.error(error.message))
        }
      }
    }
    return networkState
  }
}

