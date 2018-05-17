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

package toro.pixabay.data

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import toro.pixabay.data.entity.PhotoItem
import toro.pixabay.data.entity.PixabayItem
import toro.pixabay.data.entity.VideoItem

/**
 * @author eneim (2018/05/10).
 */
@Dao
abstract class PixabayDao {

  @Query("SELECT * FROM photo_item")
  abstract fun getAllPhotos(): DataSource.Factory<Int, PhotoItem>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insertPhotoItems(items: List<PhotoItem>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insertPhotoItem(item: PhotoItem)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insertVideoItems(items: List<VideoItem>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insertVideoItem(item: VideoItem)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insertItems(items: List<PixabayItem>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insertItems(vararg item: PixabayItem)

  @Query("SELECT * FROM pixabay_item")
  abstract fun getItems(): DataSource.Factory<Int, PixabayItem>

  @Query("SELECT * FROM pixabay_item WHERE pixabay_item.`query` == :query ORDER BY timeStamp ASC")
  abstract fun getItemsForQuery(query: String): DataSource.Factory<Int, PixabayItem>

  @Query("DELETE FROM pixabay_item WHERE pixabay_item.`query` == :query")
  abstract fun deleteItemsForQuery(query: String)

  @Query("SELECT * FROM pixabay_item WHERE pageUrl == :pageUrl LIMIT 1")
  abstract fun getItem(pageUrl: String): LiveData<PixabayItem>
}