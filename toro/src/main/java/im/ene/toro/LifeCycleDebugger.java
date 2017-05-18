/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by eneim on 1/20/17.
 */
// TODO Remove this when release
final class LifeCycleDebugger implements Application.ActivityLifecycleCallbacks {

  private static final String TAG = "Toro:Life";

  @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    Log.i(TAG, "onActivityCreated() called with: activity = ["
        + activity.getClass().getSimpleName()
        + "], savedInstanceState = ["
        + savedInstanceState
        + "]");
  }

  @Override public void onActivityStarted(Activity activity) {
    Log.w(TAG, "onActivityStarted() called with: activity = ["
        + activity.getClass().getSimpleName()
        + "]");
  }

  @Override public void onActivityResumed(Activity activity) {
    Log.e(TAG, "onActivityResumed() called with: activity = ["
        + activity.getClass().getSimpleName()
        + "]");
  }

  @Override public void onActivityPaused(Activity activity) {
    Log.e(TAG,
        "onActivityPaused() called with: activity = [" + activity.getClass().getSimpleName() + "]");
  }

  @Override public void onActivityStopped(Activity activity) {
    Log.w(TAG, "onActivityStopped() called with: activity = ["
        + activity.getClass().getSimpleName()
        + "]");
  }

  @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    Log.d(TAG, "onActivitySaveInstanceState() called with: activity = [" + activity.getClass()
        .getSimpleName() + "], outState = [" + outState + "]");
  }

  @Override public void onActivityDestroyed(Activity activity) {
    Log.i(TAG, "onActivityDestroyed() called with: activity = ["
        + activity.getClass().getSimpleName()
        + "]");
  }
}
