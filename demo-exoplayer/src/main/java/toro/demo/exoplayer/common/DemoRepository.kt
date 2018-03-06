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

package toro.demo.exoplayer.common

import android.content.Context
import com.squareup.moshi.JsonReader
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Observable
import okio.Okio

/**
 * @author eneim (2018/03/05).
 */
class DemoRepository {

    companion object {
        var listMyData = Types.newParameterizedType(List::class.java,
                DemoItem::class.java)
    }

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun loadDemoItems(context: Context): Observable<List<DemoItem>> {
        val fileData = context.assets.open("media.exolist.json")
        return Observable.just(moshi.adapter<List<DemoItem>>(listMyData).fromJson(
                JsonReader.of(Okio.buffer(Okio.source(fileData)))))
    }
}