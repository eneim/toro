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

package toro.v4;

import android.support.annotation.NonNull;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;

/**
 * @author eneim (2018/10/03).
 * @since 4.0.0
 */
final class PlayerHelper extends ToroPlayerHelper {

  final Media media;
  final Playable playable;
  @NonNull private final MyEventListeners listeners;

  PlayerHelper(@NonNull ToroPlayer player, Playable playable, Media media) {
    super(player);
    this.playable = playable;
    this.media = media;
    this.listeners = new MyEventListeners();
  }

  @Override protected void initialize(@NonNull PlaybackInfo playbackInfo) {
    playable.setPlaybackInfo(playbackInfo);
    playable.addEventListener(listeners);
    playable.prepare(false);
    playable.setPlayerView((PlayerView) player.getPlayerView());
  }

  @Override public void release() {
    super.release();
    playable.setPlayerView(null);
    playable.removeEventListener(listeners);
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
    playable.setVolume(volume);
  }

  @Override public float getVolume() {
    return playable.getVolume();
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

  @Override
  public void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
    playable.addOnVolumeChangeListener(listener);
  }

  @Override public void removeOnVolumeChangeListener(ToroPlayer.OnVolumeChangeListener listener) {
    playable.removeOnVolumeChangeListener(listener);
  }

  @Override public void addErrorListener(@NonNull ToroPlayer.OnErrorListener errorListener) {
    playable.addErrorListener(errorListener);
  }

  @Override public void removeErrorListener(ToroPlayer.OnErrorListener errorListener) {
    playable.removeErrorListener(errorListener);
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
}
