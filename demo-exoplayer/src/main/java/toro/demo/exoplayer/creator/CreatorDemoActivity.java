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

package toro.demo.exoplayer.creator;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.exoplayer.ExoCreator;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.exoplayer.ToroExo;
import toro.demo.exoplayer.R;

/**
 * @author eneim (2018/02/07).
 *
 *         Demo for {@link ExoCreator}, written in Java.
 */

public class CreatorDemoActivity extends AppCompatActivity {

  static final Uri videoUri = Uri.parse("file:///android_asset/bbb/video.mp4");

  PlayerView playerView;

  ExoCreator creator;
  SimpleExoPlayer exoPlayer;
  MediaSource mediaSource;

  final Playable.EventListener listener = new Playable.DefaultEventListener() {
    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      super.onPlayerStateChanged(playWhenReady, playbackState);
      boolean active = playbackState > Player.STATE_IDLE && playbackState < Player.STATE_ENDED;
      playerView.setKeepScreenOn(active);
    }
  };

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_single_player);

    playerView = findViewById(R.id.playerView);
    creator = ToroExo.with(this).getDefaultCreator();

    exoPlayer = ToroExo.with(this).requestPlayer(creator);
    mediaSource = creator.createMediaSource(videoUri);
    exoPlayer.addListener(listener);
    exoPlayer.prepare(mediaSource);
    playerView.setPlayer(exoPlayer);
  }

  @Override protected void onStart() {
    super.onStart();
    exoPlayer.setPlayWhenReady(true);
  }

  @Override protected void onStop() {
    super.onStop();
    exoPlayer.setPlayWhenReady(false);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    playerView.setPlayer(null);
    exoPlayer.removeListener(listener);
    exoPlayer.release();
  }
}