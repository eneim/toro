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

@file:Suppress("HasPlatformType")

package toro.pixabay.di

import android.app.Application
import android.arch.persistence.room.Room
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import dagger.Module
import dagger.Provides
import im.ene.toro.exoplayer.MediaSourceBuilder
import im.ene.toro.exoplayer.ToroExo
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import toro.pixabay.BuildConfig
import toro.pixabay.data.Api
import toro.pixabay.data.PixabayDb
import java.io.File
import java.util.Date
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton

/**
 * @author eneim (2018/05/02).
 */
@Module
class AppModule {

  companion object {
    const val CACHE_SIZE = 16 * 1024 * 1024.toLong()
  }

  @Provides
  @Singleton
  fun provideCacheFile(app: Application) = app.cacheDir

  @Singleton
  @Provides
  fun provideApiKeyQueryInterceptor() = Interceptor { chain ->
    val original = chain.request()
    val originalHttpUrl = original.url()
    val url = originalHttpUrl.newBuilder()
        .addQueryParameter("key", BuildConfig.API_KEY)
        .build()
    // Request customization: add request headers
    val requestBuilder = original.newBuilder().url(url)
    val request = requestBuilder.build()
    chain.proceed(request)
  }

  @Provides
  @Singleton
  fun provideOkHttpClient(cacheFolder: File, interceptor: Interceptor): OkHttpClient =
      OkHttpClient.Builder()
          .cache(Cache(cacheFolder, CACHE_SIZE))
          .addInterceptor(interceptor)
          .build()

  @Provides
  @Singleton
  fun provideMoshi() = Moshi.Builder()
      .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe()).build()

  @Provides
  @Singleton
  fun provideRetrofitBuilder(client: OkHttpClient): Retrofit.Builder =
      Retrofit.Builder().baseUrl(Api.BASE_URL) //
          .addConverterFactory(MoshiConverterFactory.create())
          .client(client)

  @Provides
  @Singleton
  fun provideRetrofit(builder: Retrofit.Builder): Retrofit = builder.build()

  @Provides
  @Singleton
  fun provideApi(retrofit: Retrofit): Api = retrofit.create(Api::class.java)

  @Provides
  @Singleton
  fun provideDb(app: Application): PixabayDb {
    return Room.databaseBuilder(app, PixabayDb::class.java, "pixabay.db")
        .fallbackToDestructiveMigration()
        .build()
  }

  @Provides
  @Singleton
  @Named("io.executor")
  fun provideIoExecutor() = Executors.newFixedThreadPool(5) as Executor

  @Provides
  @Singleton
  @Named("disk.executor")
  fun provideDiskExecutor() = Executors.newSingleThreadExecutor() as Executor

  @Provides
  @Singleton
  fun provideDao(db: PixabayDb) = db.getDao()

  @Provides
  @Singleton
  fun provideExoCreator(app: Application) = ToroExo.with(app).getCreator(
      ToroExo.with(app).defaultConfig.newBuilder()
          .setMediaSourceBuilder(MediaSourceBuilder.LOOPING)
          .setCache(SimpleCache(File(app.cacheDir, "toro_cache"),
              LeastRecentlyUsedCacheEvictor(8 * 1024 * 1024)))
          .build()
  )
}