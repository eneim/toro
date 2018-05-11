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

package toro.pixabay

import android.app.Activity
import android.app.Application
import com.squareup.moshi.Moshi
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

/**
 * @author eneim (2018/05/02).
 */
class MyApp : Application(), HasActivityInjector {

  @Inject
  lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

  @Inject
  lateinit var moshi: Moshi

  override fun activityInjector() = dispatchingAndroidInjector

  override fun onCreate() {
    super.onCreate()
    AppInjector.inject(this)
    app = this
    if (BuildConfig.API_KEY.isEmpty()) throw IllegalArgumentException("Pixabay api key is empty.")
  }

  companion object {
    lateinit var app: MyApp
  }
}