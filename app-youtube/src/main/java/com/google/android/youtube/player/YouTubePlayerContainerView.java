/*
 * Copyright (c) 2019 Nam Nguyen, nam@ene.im
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

package com.google.android.youtube.player;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public final class YouTubePlayerContainerView extends FrameLayout
    implements YouTubePlayer.Provider, LifecycleObserver {

  public YouTubePlayerContainerView(@NonNull Context context) {
    this(context, null);
  }

  public YouTubePlayerContainerView(@NonNull Context context,
      @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public YouTubePlayerContainerView(@NonNull Context context, @Nullable AttributeSet attrs,
      int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    if (!(context instanceof Activity)) throw new IllegalArgumentException("Need Activity");
    this.activity = (Activity) context;
  }

  Activity activity;
  YouTubePlayerView playerView;

  public final void initPlayer(LifecycleOwner lifecycleOwner, Bundle playerState) {
    this.playerView = new YouTubePlayerView(activity, null, 0,
        new YouTubePlayerView.b() {
          @Override public void a(YouTubePlayerView view, String apiKey,
              YouTubePlayer.OnInitializedListener onInitializedListener) {
            view.a(activity, view, apiKey, onInitializedListener, playerState);
          }

          @Override public void a(YouTubePlayerView youTubePlayerView) {
          }
        });
    LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
    params.gravity = Gravity.CENTER;
    super.addView(this.playerView, 0, params);
    lifecycleOwner.getLifecycle().addObserver(this);
  }

  @Override
  public void initialize(String apiKey, YouTubePlayer.OnInitializedListener onInitializedListener) {
    if (this.playerView != null) this.playerView.initialize(apiKey, onInitializedListener);
  }

  public Bundle getPlayerState() {
    if (playerView != null) {
      return playerView.e();
    } else {
      return null;
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  protected void onStart() {
    if (this.playerView != null) playerView.a();
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  protected void onResume() {
    if (this.playerView != null) playerView.b();
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  protected void onPause() {
    if (this.playerView != null) playerView.c();
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  protected void onStop() {
    if (this.playerView != null) playerView.d();
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  protected void onDestroy(LifecycleOwner owner) {
    owner.getLifecycle().removeObserver(this);
    if (this.playerView != null) playerView.c(activity.isFinishing());
  }

  @Override public void addView(View child) {
    //
  }

  @Override public void addView(View child, int index) {
    //
  }

  @Override public void addView(View child, int width, int height) {
    //
  }

  @Override public void addView(View child, ViewGroup.LayoutParams params) {
    //
  }

  @Override public void addView(View child, int index, ViewGroup.LayoutParams params) {
    //
  }

  @Override public void onViewAdded(View child) {
    super.onViewAdded(child);
  }
}
