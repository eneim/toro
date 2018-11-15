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

package toro.v4.exo;

import android.support.annotation.NonNull;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.annotations.Beta;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import toro.v4.Media;
import toro.v4.exo.factory.ExoPlayerManager;
import toro.v4.exo.factory.MediaSourceFactoryProvider;

/**
 * @author eneim (2018/10/03).
 * @since 3.7.0
 */
@SuppressWarnings("WeakerAccess") //
public class PlayerHelper extends ToroPlayerHelper {

  @NonNull //
  private final MyEventListeners listeners;
  protected final boolean lazyPrepare;
  protected final Playable playable;

  public PlayerHelper(@NonNull ToroPlayer player, @NonNull Media media,
      ExoPlayerManager playerProvider, MediaSourceFactoryProvider mediaSourceFactoryProvider,
      boolean lazyPrepare) {
    super(player);
    this.playable = new DefaultPlayable( //
        player.getPlayerView().getContext().getApplicationContext(), //
        media, mediaSourceFactoryProvider, playerProvider);
    this.listeners = new MyEventListeners();
    this.lazyPrepare = lazyPrepare;
  }

  public PlayerHelper(@NonNull ToroPlayer player, Playable playable, boolean lazyPrepare) {
    super(player);
    this.playable = playable;
    this.listeners = new MyEventListeners();
    this.lazyPrepare = lazyPrepare;
  }

  public PlayerHelper(@NonNull ToroPlayer player, Playable playable) {
    this(player, playable, true);
  }

  @Override protected void initialize(@NonNull PlaybackInfo playbackInfo) {
    playable.addOnVolumeChangeListener(this.volumeChangeListeners);
    playable.addEventListener(this.listeners);
    playable.addErrorListener(this.errorListeners);

    playable.setPlaybackInfo(playbackInfo);
    playable.setPlayerView((PlayerView) player.getPlayerView());
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
    if (playable != null) playable.play();
  }

  @Override public void pause() {
    if (playable != null) playable.pause();
  }

  @Override public boolean isPlaying() {
    return playable != null && playable.isPlaying();
  }

  @Override public void setVolume(float volume) {
    throw new UnsupportedOperationException("Deprecated");
  }

  @Override public float getVolume() {
    throw new UnsupportedOperationException("Deprecated");
  }

  @Override public void setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
    if (playable != null) playable.setVolumeInfo(volumeInfo);
  }

  @NonNull @Override public VolumeInfo getVolumeInfo() {
    return playable != null ? playable.getVolumeInfo() : new VolumeInfo(false, 1.0f);
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    return playable != null ? playable.getPlaybackInfo() : PlaybackInfo.SCRAP;
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

  @Beta public Playable getPlayable() {
    return playable;
  }
}
