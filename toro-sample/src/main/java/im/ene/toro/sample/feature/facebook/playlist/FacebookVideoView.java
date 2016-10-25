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

package im.ene.toro.sample.feature.facebook.playlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import im.ene.toro.exoplayer2.ExoVideoView;
import im.ene.toro.sample.R;

/**
 * Created by eneim on 10/15/16.
 */

public class FacebookVideoView extends ExoVideoView {

  PlaybackControlView controller;

  public FacebookVideoView(Context context) {
    this(context, null);
  }

  public FacebookVideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FacebookVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    LayoutInflater.from(context).inflate(R.layout.facebook_video_view, this);
    controller = (PlaybackControlView) findViewById(R.id.controller);
  }

  @Override protected void setPlayer(SimpleExoPlayer player) {
    super.setPlayer(player);
    controller.setPlayer(player);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
      if (controller.isVisible()) {
        controller.hide();
      } else {
        controller.show();
      }
    }
    return true;
  }

  @Override public boolean onTrackballEvent(MotionEvent ev) {
    controller.show();
    return true;
  }

  @Override public boolean dispatchKeyEvent(KeyEvent event) {
    return controller.dispatchKeyEvent(event);
  }
}
