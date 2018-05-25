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
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import toro.pixabay.di.DaggerAppComponent

/**
 * @author eneim (2018/05/02).
 */
interface Injectable

object AppInjector {

  fun inject(app: MyApp) {
    DaggerAppComponent.builder().app(app).build().inject(app)
    app.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
      override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        handleActivity(activity)
      }

      override fun onActivityPaused(activity: Activity?) {

      }

      override fun onActivityResumed(activity: Activity?) {

      }

      override fun onActivityStarted(activity: Activity?) {

      }

      override fun onActivityDestroyed(activity: Activity?) {

      }

      override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {

      }

      override fun onActivityStopped(activity: Activity?) {

      }
    })
  }

  fun handleActivity(activity: Activity?) {
    if (activity is Injectable || activity is HasSupportFragmentInjector) {
      AndroidInjection.inject(activity)
    }

    if (activity is FragmentActivity) {
      activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
          object : FragmentLifecycleCallbacks() {
            override fun onFragmentPreCreated(fm: FragmentManager?, f: Fragment?,
                savedInstanceState: Bundle?) {
              super.onFragmentPreCreated(fm, f, savedInstanceState)
              if (f is Injectable) AndroidSupportInjection.inject(f)
            }
          }, true)
    }
  }
}