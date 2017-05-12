/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample;

import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;

/**
 * Created by eneim on 10/8/16.
 */

public abstract class BaseToroFragment extends Fragment {

  @CallSuper @Override public void onStart() {
    super.onStart();
    if (Build.VERSION.SDK_INT >= 24) {
      dispatchFragmentActive();
    }
  }

  @CallSuper @Override public void onStop() {
    if (Build.VERSION.SDK_INT >= 24) {
      dispatchFragmentInactive();
    }
    super.onStop();
  }

  @CallSuper @Override public void onResume() {
    super.onResume();
    if (Build.VERSION.SDK_INT < 24) {
      dispatchFragmentActive();
    }
  }

  @CallSuper @Override public void onPause() {
    if (Build.VERSION.SDK_INT < 24) {
      dispatchFragmentInactive();
    }
    super.onPause();
  }

  protected void dispatchFragmentActive() {
  }

  protected void dispatchFragmentInactive() {
  }
}
