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

import android.arch.paging.PagedList.BoundaryCallback
import android.arch.paging.PagingRequestHelper
import android.arch.paging.PagingRequestHelper.Request.Callback
import android.arch.paging.PagingRequestHelper.RequestType.AFTER
import android.arch.paging.PagingRequestHelper.RequestType.INITIAL
import android.support.annotation.MainThread
import toro.pixabay.common.createStatusLiveData
import toro.pixabay.data.Api
import toro.pixabay.data.entity.PhotoSearchResult
import toro.pixabay.data.entity.PixabayItem
import toro.pixabay.data.entity.VideoSearchResult
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

@Suppress("CanBeParameter")
class PixabayItemsBoundaryCallback(
    private val query: String,
    private val api: Api,
    private val ioExecutor: Executor,
    private val diskExecutor: Executor,
    private val helper: PagingRequestHelper,
    private val handleResponse: (List<PixabayItem>?) -> Unit,
    private val networkPageSize: Int
) : BoundaryCallback<PixabayItem>() {

  private val pageNumber = AtomicInteger(1)
  // Combine result from photo search and video search, and every 3 photo item I insert 1 video item.
  private val photoPageSize = networkPageSize * 3
  private val videoPageSize = networkPageSize

  val networkState = helper.createStatusLiveData()

  private fun insertIntoDb(photoSearchResult: PhotoSearchResult,
      videoSearchResult: VideoSearchResult, callback: Callback) {
    diskExecutor.execute {
      val photos = photoSearchResult.hits
      val videos = videoSearchResult.hits
      val itemCount = photos.size + videos.size
      val limit = Math.min(itemCount / 4, videos.size)
      var index = 0
      val result = arrayListOf<PixabayItem>()
      while (index < limit) {
        result.add(PixabayItem.fromPhotoItem(photos[index * 3 + 0]).also { it.query = query })
        result.add(PixabayItem.fromPhotoItem(photos[index * 3 + 1]).also { it.query = query })
        result.add(PixabayItem.fromPhotoItem(photos[index * 3 + 2]).also { it.query = query })
        result.add(PixabayItem.fromVideoItem(videos[index]).also { it.query = query })
        index++
      }
      handleResponse(result)
      callback.recordSuccess()
    }
  }

  /**
   * Database returned 0 items. We should query the backend for more items.
   */
  @MainThread
  override fun onZeroItemsLoaded() {
    pageNumber.set(1)
    helper.runIfNotRunning(INITIAL) {
      ioExecutor.execute {
        try {
          val photoResult = api.searchPhoto(query, pageNumber.get(),
              photoPageSize).execute().body()
          val videoResult = api.searchVideo(query, pageNumber.get(),
              videoPageSize).execute().body()
          insertIntoDb(photoResult!!, videoResult!!, it)
          pageNumber.incrementAndGet()
        } catch (error: Throwable) {
          it.recordFailure(error)
        }
      }
    }
  }

  /**
   * User reached to the end of the list.
   */
  @MainThread
  override fun onItemAtEndLoaded(item: PixabayItem) {
    helper.runIfNotRunning(AFTER) {
      ioExecutor.execute {
        try {
          val photoResult = api.searchPhoto(query, pageNumber.get(),
              photoPageSize).execute().body()
          val videoResult = api.searchVideo(query, pageNumber.get(),
              videoPageSize).execute().body()
          insertIntoDb(photoResult!!, videoResult!!, it)
          pageNumber.incrementAndGet()
        } catch (error: Throwable) {
          it.recordFailure(error)
        }
      }
    }
  }

  override fun onItemAtFrontLoaded(itemAtFront: PixabayItem) {
    // ignored, since we only ever append to what's in the DB
  }
}