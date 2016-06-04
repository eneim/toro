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

package im.ene.lab.toro.player.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import im.ene.lab.toro.player.MediaSource;
import im.ene.lab.toro.player.internal.ExoMediaPlayer;

/**
 * Created by eneim on 6/3/16.
 */
public class PlayerPresenterImpl
    implements PlayerPresenter, ExoMediaPlayer.Listener, AudioCapabilitiesReceiver.Listener {

  private final Context context;
  @NonNull private final PlayerView view;

  private ExoMediaPlayer player;
  private ExoMediaPlayer.RendererBuilder rendererBuilder;
  private AudioCapabilitiesReceiver audioCapabilitiesReceiver;

  boolean playerNeedsPrepare = false;

  public PlayerPresenterImpl(Context context, @NonNull PlayerView view) {
    this.context = context;
    this.view = view;
    this.audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(context, this);
  }

  @Override public void onCreate() {
    addListener(this);
    this.audioCapabilitiesReceiver.register();
  }

  @Override public void setMediaSource(MediaSource source) {
    if (this.rendererBuilder != null) {
      this.rendererBuilder.cancel();
    }

    this.rendererBuilder = RendererBuilderFactory.createRendererBuilder(context, source);

    if (this.player == null) {
      playerNeedsPrepare = true;
      this.player = new ExoMediaPlayer(rendererBuilder);
      this.player.addListener(this);
    }

    if (playerNeedsPrepare) {
      this.player.prepare();
      playerNeedsPrepare = false;
    }
  }

  @Override public void startPlayback(int position) {
    if (view.getSurface() != null) {
      player.setSurface(view.getSurface());
      player.seekTo(position);
      player.start();
    }
  }

  @Override public void pausePlayback() {
    player.pause();
  }

  @Override public void stopPlayback() {
    player.stop();
  }

  @Override public void release() {
    player.release();
  }

  @Override public void onDestroy() {
    this.rendererBuilder = null;
    this.player = null;
    this.audioCapabilitiesReceiver.unregister();
    this.audioCapabilitiesReceiver = null;
  }

  @Override public void addListener(ExoMediaPlayer.Listener listener) {
    if (player != null) {
      player.addListener(listener);
    }
  }

  @Override public void start() {
    startPlayback(0);
  }

  @Override public void start(long position) {
    startPlayback((int) position);
  }

  @Override public void pause() {
    pausePlayback();
  }

  @Override public long getDuration() {
    return player != null ? player.getDuration() : ExoPlayer.UNKNOWN_TIME;
  }

  @Override public long getCurrentPosition() {
    return player != null ? player.getCurrentPosition() : ExoPlayer.UNKNOWN_TIME;
  }

  @Override public void seekTo(long pos) {
    if (player != null) {
      player.seekTo(pos);
    }
  }

  @Override public boolean isPlaying() {
    return player != null && player.isPlaying();
  }

  @Override public int getBufferPercentage() {
    return player != null ? player.getBufferedPercentage() : 0;
  }

  @Override public boolean canPause() {
    return true;
  }

  @Override public boolean canSeekBackward() {
    return true;
  }

  @Override public boolean canSeekForward() {
    return true;
  }

  @Override public int getAudioSessionId() {
    return player != null ? player.getAudioSessionId() : 0;
  }

  @Override public void onStateChanged(boolean playWhenReady, int playbackState) {
    view.updatePlaybackState(playWhenReady, playbackState);
  }

  @Override public void onError(Exception e) {
    view.showError(e);
  }

  @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
      float pixelWidthHeightRatio) {
    this.view.updateSize(width, height, unAppliedRotationDegrees, pixelWidthHeightRatio);
  }

  @Override public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
    Log.d(TAG, "onAudioCapabilitiesChanged() called with: "
        + "audioCapabilities = ["
        + audioCapabilities
        + "]");
  }
}
