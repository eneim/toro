/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import im.ene.toro.sample.BuildConfig;

/**
 * @author eneim | 6/6/17.
 */

public class BaseFragment extends Fragment {

  private static final boolean D = BuildConfig.DEBUG;

  // Save viewPagerMode flag on config change
  private static final String STATE_VIEW_PAGER_MODE = "toro:fragment:state:view_pager_mode";

  protected String TAG = "Toro:" + getClass().getSimpleName();

  public static final String RESULT_EXTRA_PLAYER_ORDER = "toro:demo:player:result:order";
  public static final String RESULT_EXTRA_PLAYBACK_INFO = "toro:demo:player:result:playback";

  /**
   * The following flag is used for {@link Fragment} that is inside a ViewPager.
   * Default is {@code false} for non-ViewPager use.
   */
  protected boolean viewPagerMode = false;

  @SuppressWarnings("SameParameterValue") public void setViewPagerMode(boolean viewPagerMode) {
    this.viewPagerMode = viewPagerMode;
  }

  @Override public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    if (D) Log.wtf(TAG, "onCreate() called with: bundle = [" + bundle + "]");
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (D) Log.wtf(TAG, "onAttach() called with: context = [" + context + "]");
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    if (D) {
      Log.wtf(TAG, "onCreateView() called with: inflater = ["
          + inflater
          + "], container = ["
          + container
          + "], bundle = ["
          + bundle
          + "]");
    }
    return super.onCreateView(inflater, container, bundle);
  }

  private Unbinder unbinder;

  @CallSuper @Override public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    if (D) {
      Log.wtf(TAG, "onViewCreated() called with: view = [" + view + "], bundle = [" + bundle + "]");
    }
    unbinder = ButterKnife.bind(this, view);
    if (bundle != null && bundle.containsKey(STATE_VIEW_PAGER_MODE)) {
      this.viewPagerMode = bundle.getBoolean(STATE_VIEW_PAGER_MODE);
    }
  }

  @Override public void onActivityCreated(@Nullable Bundle bundle) {
    super.onActivityCreated(bundle);
    if (D) Log.wtf(TAG, "onActivityCreated() called with: bundle = [" + bundle + "]");
  }

  @Override public void onViewStateRestored(@Nullable Bundle bundle) {
    super.onViewStateRestored(bundle);
    if (D) Log.wtf(TAG, "onViewStateRestored() called with: bundle = [" + bundle + "]");
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (D) Log.wtf(TAG, "onSaveInstanceState() called with: outState = [" + outState + "]");
    outState.putBoolean(STATE_VIEW_PAGER_MODE, this.viewPagerMode);
  }

  @Override public void onStart() {
    super.onStart();
    if (D) Log.wtf(TAG, "onStart() called");
  }

  @Override public void onResume() {
    super.onResume();
    if (D) Log.wtf(TAG, "onResume() called");
  }

  @Override public void onPause() {
    super.onPause();
    if (D) Log.wtf(TAG, "onPause() called");
  }

  @Override public void onStop() {
    super.onStop();
    if (D) Log.wtf(TAG, "onStop() called");
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    if (D) Log.wtf(TAG, "onDestroyView() called");
    if (unbinder != null) unbinder.unbind();
  }

  @Override public void onDetach() {
    super.onDetach();
    if (D) Log.wtf(TAG, "onDetach() called");
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if (D) Log.wtf(TAG, "onDestroy() called");
  }

  @Override public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
    super.onMultiWindowModeChanged(isInMultiWindowMode);
    if (D) {
      Log.wtf(TAG, "onMultiWindowModeChanged() called with: isInMultiWindowMode = ["
          + isInMultiWindowMode
          + "]");
    }
  }

  @Override public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
    super.onPictureInPictureModeChanged(isInPictureInPictureMode);
    if (D) {
      Log.wtf(TAG, "onPictureInPictureModeChanged() called with: isInPictureInPictureMode = ["
          + isInPictureInPictureMode
          + "]");
    }
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (D) {
      Log.wtf(TAG, "onActivityResult() called with: requestCode = ["
          + requestCode
          + "], resultCode = ["
          + resultCode
          + "], data = ["
          + data
          + "]");
    }
  }

  @Override public void onAttachFragment(Fragment childFragment) {
    super.onAttachFragment(childFragment);
    if (D) Log.wtf(TAG, "onAttachFragment() called with: childFragment = [" + childFragment + "]");
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (D) {
      Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
    }
  }
}
