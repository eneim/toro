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
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

/**
 * @author eneim (2018/01/24).
 */

public class ExoPlayerViewHelper extends ToroPlayerHelper {

  @NonNull private final Playable helper;
  @NonNull private final MyEventListeners listeners;

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri uri) {
    this(container, player, uri, ToroExo.with(container.getContext()).getDefaultCreator());
  }

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri uri, @NonNull ExoCreator creator) {
    this(container, player, uri, null, creator);
  }

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri uri, @Nullable Playable.EventListener eventListener,
      @NonNull ExoCreator creator) {
    super(container, player);
    if (!(player.getPlayerView() instanceof SimpleExoPlayerView)) {
      throw new IllegalArgumentException("Require SimpleExoPlayerView");
    }

    listeners = new MyEventListeners();
    if (eventListener != null) listeners.add(eventListener);
    helper = creator.createPlayable(uri);
  }

  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    helper.addEventListener(listeners);
    helper.prepare();
    helper.attachView((SimpleExoPlayerView) player.getPlayerView());
    if (playbackInfo != null) helper.setPlaybackInfo(playbackInfo);
  }

  @Override public void release() {
    super.release();
    helper.detachView();
    helper.removeEventListener(listeners);
    helper.release();
  }

  @Override public void play() {
    helper.play();
  }

  @Override public void pause() {
    helper.pause();
  }

  @Override public boolean isPlaying() {
    return helper.isPlaying();
  }

  @Override public void setVolume(float volume) {
    helper.setVolume(volume);
  }

  @Override public float getVolume() {
    return helper.getVolume();
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    return helper.getPlaybackInfo();
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
