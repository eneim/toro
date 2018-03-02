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

package toro.demo.exoplayer.debug;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.exoplayer.Playable;
import toro.demo.exoplayer.DemoApp;
import toro.demo.exoplayer.R;

import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM;

/**
 * @author eneim (2018/02/25).
 */

public class PlayableDebugActivity extends AppCompatActivity {

  static final Uri video = Uri.parse("file:///android_asset/bbb/video.mp4");
  // Uri.parse("https://storage.googleapis.com/material-design/publish/material_v_12/assets/0B14F_FSUCc01a05pM2FXWEN0b0U/responsive-01-durations-v1.mp4");

  Playable playable;
  LayoutInflater inflater;
  static int currentResizeMode = 0; // static, so it won't reset.

  void play() {
    if (playable != null && !playable.isPlaying()) playable.play();
  }

  void pause() {
    if (playable != null) playable.pause();
  }

  void reset() {
    if (playable != null) {
      if (playable.isPlaying()) playable.pause();
      playable.reset();
    }
  }

  void release() {
    if (playable != null) {
      pause();
      playable.release();
    }
  }

  void mute() {
    if (playable != null) playable.setVolume(0.f);
  }

  void unMute() {
    if (playable != null) playable.setVolume(1.f);
  }

  void changeResizeMode() {
    playerView.setResizeMode(MODES.values()[currentResizeMode].mode);
    resizeMode.setText("Resize mode: " + MODES.values()[currentResizeMode].name());
  }

  @BindView(R.id.playerView) PlayerView playerView;
  @BindView(R.id.buttonBar) ViewGroup buttonBar;
  @BindView(R.id.resize_mode) TextView resizeMode;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_debug_playable);
    ButterKnife.bind(this);

    inflater = LayoutInflater.from(this);
    playable = (Playable) getLastCustomNonConfigurationInstance();
    if (playable == null) {
      //noinspection ConstantConditions
      playable = DemoApp.Companion.getExoCreator().createPlayable(video);
      playable.prepare(true);
    }

    playable.setPlayerView(playerView);

    buttonBar.removeAllViews();
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Play", v -> play());
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Pause", v -> pause());
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Reset", v -> reset());
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Release", v -> release());
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Resize mode", v -> {
      currentResizeMode++;
      currentResizeMode = currentResizeMode % MODES.values().length;
      changeResizeMode();
    });
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Mute", v -> mute());
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "UnMute", v -> unMute());

    //playerView.setResizeMode(MODES.values()[currentResizeMode].mode);
    changeResizeMode();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (isFinishing()) release();
  }

  static void addButton(LayoutInflater inflater, ViewGroup parent, int layoutId, String text,
      View.OnClickListener listener) {
    View view = inflater.inflate(layoutId, parent, false);
    if (view instanceof Button) ((Button) view).setText(text);
    view.setOnClickListener(listener);
    parent.addView(view);
  }

  enum MODES {
    RESIZE_FIT(RESIZE_MODE_FIT),  //
    RESIZE_FILL(RESIZE_MODE_FILL),  //
    RESIZE_FIXED_HEIGHT(RESIZE_MODE_FIXED_HEIGHT),  //
    RESIZE_FIXED_WIDTH(RESIZE_MODE_FIXED_WIDTH),  //
    RESIZE_ZOOM(RESIZE_MODE_ZOOM);

    @AspectRatioFrameLayout.ResizeMode final int mode;

    MODES(int mode) {
      this.mode = mode;
    }
  }

  @Override public Object onRetainCustomNonConfigurationInstance() {
    return playable;
  }
}
