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

package im.ene.toro.exoplayer;

import android.support.annotation.NonNull;
import android.view.View;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.annotations.Beta;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;

/**
 * @author eneim (2018/10/03).
 * @since 3.7.0.2900
 */
@SuppressWarnings("WeakerAccess") //
public class PlayerHelper extends ToroPlayerHelper {

  private static final String TAG = "ToroLib:X:Helper";

  @NonNull protected final Playable playable;
  @NonNull protected final PlayerView playerView;
  protected final boolean lazyPrepare;

  @NonNull
  private final MyEventListeners listeners;

  // Build this Helper with custom Playable. This can be used with DefaultAdsPlayable to
  // support Ads.
  public PlayerHelper(@NonNull ToroPlayer player, @NonNull Playable playable, boolean lazyPrepare) {
    super(player);
    View view = player.getPlayerView();
    if (!(view instanceof PlayerView)) {
      throw new IllegalArgumentException("Require PlayerView, found: " + view);
    }
    this.playerView = (PlayerView) view;
    this.playable = playable;
    this.lazyPrepare = lazyPrepare;
    this.listeners = new MyEventListeners();
  }

  public PlayerHelper(@NonNull ToroPlayer player, Playable playable) {
    this(player, playable, true);
  }

  @Override protected void initialize(@NonNull PlaybackInfo playbackInfo) {
    playable.addOnVolumeChangeListener(this.volumeChangeListeners);
    playable.addEventListener(this.listeners);
    playable.addErrorListener(this.errorListeners);

    playable.setPlaybackInfo(playbackInfo);
    playable.setPlayerView(this.playerView);
    playable.prepare(!this.lazyPrepare);
  }

  @Override public void release() {
    super.release();
    playable.setPlayerView(null);

    playable.removeErrorListener(this.errorListeners);
    playable.removeEventListener(this.listeners);
    playable.removeOnVolumeChangeListener(this.volumeChangeListeners);
    playable.release();
  }

  @Override public void play() {
    playable.play();
  }

  @Override public void pause() {
    playable.pause();
  }

  @Override public boolean isPlaying() {
    return playable.isPlaying();
  }

  @Override public void setVolume(float volume) {
    throw new UnsupportedOperationException("Deprecated");
  }

  @Override public float getVolume() {
    throw new UnsupportedOperationException("Deprecated");
  }

  @Override public void setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
    playable.setVolumeInfo(volumeInfo);
  }

  @NonNull @Override public VolumeInfo getVolumeInfo() {
    return playable.getVolumeInfo();
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    return playable.getPlaybackInfo();
  }

  @Override public void setRepeatMode(int repeatMode) {
    this.playable.setRepeatMode(repeatMode);
  }

  @Override public int getRepeatMode() {
    return this.playable.getRepeatMode();
  }

  // A proxy, to also hook into ToroPlayerHelper's state change event.
  class MyEventListeners extends Playable.EventListeners {

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      PlayerHelper.super.onPlayerStateUpdated(playWhenReady, playbackState); // important
      super.onPlayerStateChanged(playWhenReady, playbackState);
    }

    @Override public void onRenderedFirstFrame() {
      super.onRenderedFirstFrame();
      internalListener.onFirstFrameRendered();
      for (ToroPlayer.EventListener listener : eventListeners) {
        listener.onFirstFrameRendered();
      }
    }
  }

  @NonNull @Beta public Playable getPlayable() {
    return playable;
  }
}
