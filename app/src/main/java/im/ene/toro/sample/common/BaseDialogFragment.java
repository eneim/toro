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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import im.ene.toro.sample.BuildConfig;

/**
 * @author eneim | 6/6/17.
 */

public class BaseDialogFragment extends AppCompatDialogFragment {

  private static final boolean D = BuildConfig.DEBUG;

  protected String TAG = "Toro:BaseDialogFragment";

  @Override public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    if (D) Log.d(TAG, "onCreate() called with: bundle = [" + bundle + "]");
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    TAG = "Toro:" + getClass().getSimpleName();
    if (D) Log.d(TAG, "onAttach() called with: context = [" + context + "]");
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    if (D) {
      Log.d(TAG, "onCreateView() called with: inflater = ["
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
      Log.d(TAG, "onViewCreated() called with: view = [" + view + "], bundle = [" + bundle + "]");
    }
    unbinder = ButterKnife.bind(this, view);
  }

  @Override public void onActivityCreated(@Nullable Bundle bundle) {
    super.onActivityCreated(bundle);
    if (D) Log.d(TAG, "onActivityCreated() called with: bundle = [" + bundle + "]");
  }

  @Override public void onViewStateRestored(@Nullable Bundle bundle) {
    super.onViewStateRestored(bundle);
    if (D) Log.d(TAG, "onViewStateRestored() called with: bundle = [" + bundle + "]");
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (D) Log.d(TAG, "onSaveInstanceState() called with: outState = [" + outState + "]");
  }

  @Override public void onStart() {
    super.onStart();
    if (D) Log.d(TAG, "onStart() called");
  }

  @Override public void onResume() {
    super.onResume();
    if (D) Log.d(TAG, "onResume() called");
  }

  @Override public void onPause() {
    super.onPause();
    if (D) Log.d(TAG, "onPause() called");
  }

  @Override public void onStop() {
    super.onStop();
    if (D) Log.d(TAG, "onStop() called");
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    if (D) Log.d(TAG, "onDestroyView() called");
    if (unbinder != null) unbinder.unbind();
  }

  @Override public void onDetach() {
    super.onDetach();
    if (D) Log.d(TAG, "onDetach() called");
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if (D) Log.d(TAG, "onDestroy() called");
  }

  @Override public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    if (D) Log.d(TAG, "onDismiss() called with: dialog = [" + dialog + "]");
  }

  @Override public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
    super.onMultiWindowModeChanged(isInMultiWindowMode);
    if (D) {
      Log.d(TAG, "onMultiWindowModeChanged() called with: isInMultiWindowMode = ["
          + isInMultiWindowMode
          + "]");
    }
  }

  @Override public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
    super.onPictureInPictureModeChanged(isInPictureInPictureMode);
    if (D) {
      Log.d(TAG, "onPictureInPictureModeChanged() called with: isInPictureInPictureMode = ["
          + isInPictureInPictureMode
          + "]");
    }
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (D) {
      Log.d(TAG, "onActivityResult() called with: requestCode = ["
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
}
