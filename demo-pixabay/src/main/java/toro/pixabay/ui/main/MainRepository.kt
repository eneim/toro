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

import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.arch.paging.PagingRequestHelper
import android.support.annotation.MainThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import toro.pixabay.common.ListModel
import toro.pixabay.common.createStatusLiveData
import toro.pixabay.data.Api
import toro.pixabay.data.PixabayDao
import toro.pixabay.data.entity.PhotoItem
import toro.pixabay.data.entity.PhotoSearchResult
import toro.pixabay.data.entity.PixabayItem
import toro.pixabay.data.entity.VideoSearchResult
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger
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

  private fun insertResultToDb(result: List<PhotoItem>?) {
    diskExecutor.execute { dao.insertPhotoItems(result!!) }
  }

  private fun insertToDb(result: List<PixabayItem>?) {
    diskExecutor.execute { dao.insertItems(result!!) }
  }

  fun getItems(query: String): ListModel<PixabayItem> {
    val boundaryCallback = PixabayItemsBoundaryCallback(
        query, api, ioExecutor, diskExecutor, this::insertToDb, DEFAULT_NETWORK_PAGE_SIZE
    )

    return ListModel<PixabayItem>(
        LivePagedListBuilder<Int, PixabayItem>(dao.getItems(),
            DEFAULT_NETWORK_PAGE_SIZE).setBoundaryCallback(boundaryCallback).build(),
        boundaryCallback.networkState
    )
  }

  fun searchPhotos(query: String): ListModel<PhotoItem> {
    val boundaryCallback = ItemsBoundaryCallback(
        query, api, ioExecutor, diskExecutor, this::insertResultToDb, DEFAULT_NETWORK_PAGE_SIZE
    )

    return ListModel<PhotoItem>(
        LivePagedListBuilder<Int, PhotoItem>(dao.getAllPhotos(),
            DEFAULT_NETWORK_PAGE_SIZE).setBoundaryCallback(boundaryCallback).build(),
        boundaryCallback.networkState
    )
  }
}

class ItemsBoundaryCallback(
    private val query: String,
    private val api: Api,
    private val ioExecutor: Executor,
    private val diskExecutor: Executor,
    private val handleResponse: (List<PhotoItem>?) -> Unit,
    private val networkPageSize: Int
) : PagedList.BoundaryCallback<PhotoItem>() {

  private val helper = PagingRequestHelper(ioExecutor)
  private val pageNumber = AtomicInteger(1)
  val networkState = helper.createStatusLiveData()

  /**
   * every time it gets new items, boundary callback simply inserts them into the database and
   * paging library takes care of refreshing the list if necessary.
   */
  private fun insertItemsIntoDb(response: List<PhotoItem>,
      callback: PagingRequestHelper.Request.Callback) {
    diskExecutor.execute {
      handleResponse(response)
      callback.recordSuccess()
    }
  }

  /**
   * Database returned 0 items. We should query the backend for more items.
   */
  @MainThread
  override fun onZeroItemsLoaded() {
    pageNumber.set(1)
    helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
      ioExecutor.execute {
        api.searchPhoto(query, pageNumber.get(), networkPageSize) //
            .enqueue(object : Callback<PhotoSearchResult> {
              override fun onFailure(call: Call<PhotoSearchResult>?, t: Throwable?) {
                it.recordFailure(t!!)
              }

              override fun onResponse(call: Call<PhotoSearchResult>?,
                  response: Response<PhotoSearchResult>?) {
                insertItemsIntoDb(response!!.body()!!.hits, it)
                pageNumber.incrementAndGet()
              }
            })
      }
    }
  }

  /**
   * User reached to the end of the list.
   */
  @MainThread
  override fun onItemAtEndLoaded(item: PhotoItem) {
    helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
      ioExecutor.execute {
        api.searchPhoto(query, pageNumber.get(), networkPageSize) //
            .enqueue(object : Callback<PhotoSearchResult> {
              override fun onFailure(call: Call<PhotoSearchResult>?, t: Throwable?) {
                it.recordFailure(t!!)
              }

              override fun onResponse(call: Call<PhotoSearchResult>?,
                  response: Response<PhotoSearchResult>?) {
                val result = response?.body()
                if (result != null) {
                  insertItemsIntoDb(result.hits, it)
                  pageNumber.incrementAndGet()
                }
              }
            })
      }
    }
  }

  override fun onItemAtFrontLoaded(itemAtFront: PhotoItem) {
    // ignored, since we only ever append to what's in the DB
  }
}

class PixabayItemsBoundaryCallback(
    private val query: String,
    private val api: Api,
    private val ioExecutor: Executor,
    private val diskExecutor: Executor,
    private val handleResponse: (List<PixabayItem>?) -> Unit,
    private val networkPageSize: Int
) : PagedList.BoundaryCallback<PixabayItem>() {

  private val helper = PagingRequestHelper(ioExecutor)
  private val pageNumber = AtomicInteger(1)
  private val photoPageSize = networkPageSize * 4
  private val videoPageSize = networkPageSize

  val networkState = helper.createStatusLiveData()

  private fun insertIntoDb(photoSearchResult: PhotoSearchResult,
      videoSearchResult: VideoSearchResult,
      callback: PagingRequestHelper.Request.Callback) {
    diskExecutor.execute {
      val photos = photoSearchResult.hits
      val videos = videoSearchResult.hits
      val itemCount = photos.size + videos.size
      val limit = Math.min(itemCount / 5, videos.size)
      var index = 0
      val result = arrayListOf<PixabayItem>()
      while (index < limit) {
        result.add(PixabayItem.fromPhotoItem(photos[index * 4 + 0]).also { it.query = query })
        result.add(PixabayItem.fromPhotoItem(photos[index * 4 + 1]).also { it.query = query })
        result.add(PixabayItem.fromPhotoItem(photos[index * 4 + 2]).also { it.query = query })
        result.add(PixabayItem.fromPhotoItem(photos[index * 4 + 3]).also { it.query = query })
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
    helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
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
    helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
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