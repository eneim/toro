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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

import static im.ene.toro.ToroUtil.checkNotNull;
import static im.ene.toro.exoplayer.ToroExo.with;

/**
 * An implementation of {@link ToroPlayerHelper} where the actual Player is an {@link ExoPlayer}
 * implementation. This is a bridge between ExoPlayer's callback and ToroPlayerHelper behaviors.
 *
 * @author eneim (2018/01/24).
 * @since 3.4.0
 */

public class ExoPlayerViewHelper extends ToroPlayerHelper {

  @NonNull private final Playable<SimpleExoPlayerView> playable;
  @NonNull private final MyEventListeners listeners;

  @Deprecated //
  public ExoPlayerViewHelper(@SuppressWarnings("unused") @NonNull Container container,
      @NonNull ToroPlayer player, @NonNull Uri uri) {
    this(player, uri, with(player.getPlayerView().getContext()).getDefaultCreator());
  }

  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull Uri uri) {
    this(player, uri, with(checkNotNull(player.getPlayerView()).getContext()).getDefaultCreator());
  }

  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull Uri uri,
      @NonNull ExoCreator creator) {
    this(player, uri, null, creator, null);
  }

  public ExoPlayerViewHelper(@NonNull ToroPlayer player, @NonNull Uri uri, String extension,
      @NonNull ExoCreator creator, Playable.EventListener eventListener) {
    super(player);
    //noinspection ConstantConditions
    if (player.getPlayerView() == null || !(player.getPlayerView() instanceof SimpleExoPlayerView)) {
      throw new IllegalArgumentException("Require non-null SimpleExoPlayerView");
    }

    listeners = new MyEventListeners();
    if (eventListener != null) listeners.add(eventListener);
    playable = creator.createPlayable(uri, extension);
  }

  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    playable.addEventListener(listeners);
    playable.prepare(false);
    playable.setPlayerView((SimpleExoPlayerView) player.getPlayerView());
    if (playbackInfo != null) playable.setPlaybackInfo(playbackInfo);
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

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    return playable.getPlaybackInfo();
  }

  @SuppressWarnings("WeakerAccess") //
  public void addEventListener(@NonNull Playable.EventListener listener) {
    //noinspection ConstantConditions
    if (listener != null) this.listeners.add(listener);
  }

  @SuppressWarnings("WeakerAccess") //
  public void removeEventListener(Playable.EventListener listener) {
    this.listeners.remove(listener);
  }

  // A proxy, to also hook into ToroPlayerHelper's state change event.
  private class MyEventListeners extends Playable.EventListeners {

    MyEventListeners() {
    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      ExoPlayerViewHelper.super.onPlayerStateUpdated(playWhenReady, playbackState); // important
      super.onPlayerStateChanged(playWhenReady, playbackState);
    }
  }
}
