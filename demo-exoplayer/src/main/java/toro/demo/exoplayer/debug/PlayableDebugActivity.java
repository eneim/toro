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
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.exoplayer.Playable;
import toro.demo.exoplayer.DemoApp;
import toro.demo.exoplayer.R;

/**
 * @author eneim (2018/02/25).
 */

public class PlayableDebugActivity extends AppCompatActivity {

  static final Uri video = Uri.parse("file:///android_asset/bbb/video.mp4");

  Playable playable;
  LayoutInflater inflater;

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

  @BindView(R.id.playerView) PlayerView playerView;
  @BindView(R.id.buttonBar) ViewGroup buttonBar;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_debug_playable);
    ButterKnife.bind(this);

    inflater = LayoutInflater.from(this);
    //noinspection ConstantConditions
    playable = DemoApp.Companion.getExoCreator().createPlayable(video);
    playable.prepare(false);
    playable.setPlayerView(playerView);

    buttonBar.removeAllViews();
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Play", v -> play());
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Pause", v -> pause());
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Reset", v -> reset());
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Release", v -> release());
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "Mute", v -> mute());
    addButton(inflater, buttonBar, R.layout.widget_debug_button, "UnMute", v -> unMute());
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    release();
  }

  static void addButton(LayoutInflater inflater, ViewGroup parent, int layoutId, String text,
      View.OnClickListener listener) {
    View view = inflater.inflate(layoutId, parent, false);
    if (view instanceof Button) ((Button) view).setText(text);
    view.setOnClickListener(listener);
    parent.addView(view);
  }
}
