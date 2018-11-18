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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import im.ene.toro.BuildConfig;
import im.ene.toro.exoplayer.ExoCreator;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.media.PlaybackInfo;
import toro.demo.exoplayer.R;
import toro.v4.Media;
import toro.v4.MediaItem;
import toro.v4.exo.DefaultBandwidthMeterFactory;
import toro.v4.exo.DefaultExoPlayerManager;
import toro.v4.exo.DefaultMediaSourceFactoryProvider;
import toro.v4.exo.factory.BandwidthMeterFactory;
import toro.v4.exo.factory.ExoPlayerManager;
import toro.v4.exo.factory.MediaSourceFactoryProvider;

import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;

/**
 * @author eneim (2018/02/07).
 *
 * Demo for {@link ExoPlayerManager} and {@link MediaSourceFactoryProvider}, written in Java.
 */

public class CreatorDemoActivity extends AppCompatActivity {

  private static final String TAG = "Toro:Demo:Provider";
  static final Uri videoUri =
      // Uri.parse("https://storage.googleapis.com/material-design/publish/material_v_12/assets/0B14F_FSUCc01SWc0N29QR3pZT2s/materialmotionhero-spec-0505.mp4");
      Uri.parse("file:///android_asset/bbb/video.mp4");
  static final Media videoMedia = new MediaItem(videoUri, "mp4");

  PlayerView playerView;

  MediaSource mediaSource;
  SimpleExoPlayer player;

  final Playable.EventListener listener = new Playable.DefaultEventListener() {
    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      super.onPlayerStateChanged(playWhenReady, playbackState);
      boolean active = playbackState > Player.STATE_IDLE && playbackState < Player.STATE_ENDED;
      playerView.setKeepScreenOn(active);
    }
  };

  // These instance should live in a ViewModel
  ExoPlayerManager playerManager;
  MediaSourceFactoryProvider factoryProvider;
  PlaybackInfo playbackInfo = PlaybackInfo.SCRAP;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo_creator);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    playerView = findViewById(R.id.playerView);

    BandwidthMeterFactory meterFactory = new DefaultBandwidthMeterFactory();
    playerManager = new DefaultExoPlayerManager(this, meterFactory);
    DataSource.Factory upstreamFactory = new DefaultDataSourceFactory(this,
        new DefaultHttpDataSourceFactory(BuildConfig.LIB_NAME));
    factoryProvider =
        new DefaultMediaSourceFactoryProvider(this, upstreamFactory, null);
  }

  @Override protected void onStart() {
    super.onStart();
    mediaSource = factoryProvider.provideMediaSourceFactory(videoMedia)
        .createMediaSource(videoMedia.getUri());
    player = playerManager.acquireExoPlayer(videoMedia);
    Log.i(TAG, "onStart: " + player); // Make sure the player is reused.
    player.addListener(listener);
    player.prepare(mediaSource);
    player.setRepeatMode(Player.REPEAT_MODE_ONE);
    playerView.setPlayer(player);
    boolean haveResumePosition = this.playbackInfo.getResumeWindow() != INDEX_UNSET;
    if (haveResumePosition) {
      player.seekTo(this.playbackInfo.getResumeWindow(), this.playbackInfo.getResumePosition());
    }
    player.setPlayWhenReady(true);
  }

  @Override protected void onStop() {
    super.onStop();
    playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
    playbackInfo.setResumePosition(player.getCurrentPosition());
    player.setPlayWhenReady(false);

    playerView.setPlayer(null);
    player.removeListener(listener);
    if (playerManager != null) playerManager.releasePlayer(videoMedia, player);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (playerManager != null) playerManager.cleanUp();
  }
}