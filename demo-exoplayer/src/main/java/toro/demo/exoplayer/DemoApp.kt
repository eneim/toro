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

package toro.demo.exoplayer

import android.app.Application
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import im.ene.toro.exoplayer.PlayerHub
import im.ene.toro.exoplayer.ToroExo
import java.io.File

/**
 * @author eneim (2018/01/26).
 */
class DemoApp : Application() {

    companion object {
        var cacheFile = 8 * 1024 * 1024.toLong()
        var demoApp: DemoApp? = null
        var playerHub: PlayerHub? = null
    }

    override fun onCreate() {
        super.onCreate()
        demoApp = this
        val cache = SimpleCache(File(filesDir.path + "/toro_cache"),
                LeastRecentlyUsedCacheEvictor(cacheFile))
        playerHub = ToroExo.with(this).builder().cache(cache).build()
    }
}