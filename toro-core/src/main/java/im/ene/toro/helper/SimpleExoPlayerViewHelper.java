/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.helper;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author eneim | 6/11/17.
 *
 *         Extension of {@link ToroPlayerHelper}, aims to support {@link ExoPlayer} via its
 *         components {@link SimpleExoPlayer} and {@link SimpleExoPlayerView}.
 */

public final class SimpleExoPlayerViewHelper extends ToroPlayerHelper {

  private final ExoPlayerHelper.EventListener internalListener =
      new ExoPlayerHelper.EventListener() {
        @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
          SimpleExoPlayerViewHelper.super.onPlayerStateUpdated(playWhenReady, playbackState);
          super.onPlayerStateChanged(playWhenReady, playbackState);
        }
      };

  private final AtomicInteger counter = new AtomicInteger(0); // initialize count
  private final ExoPlayerHelper helper;
  private final Uri mediaUri;

  public SimpleExoPlayerViewHelper(Container container, ToroPlayer player, Uri mediaUri) {
    super(container, player);
    if (!(player.getPlayerView() instanceof SimpleExoPlayerView)) {
      throw new IllegalArgumentException("Only SimpleExoPlayerView is supported.");
    }
    this.helper = new ExoPlayerHelper((SimpleExoPlayerView) player.getPlayerView());
    this.mediaUri = mediaUri;
  }

  public final void setEventListener(ExoPlayer.EventListener eventListener) {
    this.internalListener.setDelegate(eventListener);
  }

  /**
   * {@inheritDoc}
   *
   * @param playbackInfo the initial playback info. {@code null} if no such info available.
   */
  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    if (counter.getAndIncrement() == 0) { // prevent the multiple time init
      this.helper.addEventListener(internalListener);
      try {
        this.helper.prepare(this.mediaUri);
      } catch (ParserException e) {
        e.printStackTrace();
      }
    }

    this.helper.setPlaybackInfo(playbackInfo);
  }

  @Override public void play() {
    this.helper.play();
  }

  @Override public void pause() {
    this.helper.pause();
  }

  @Override public boolean isPlaying() {
    return this.helper.isPlaying();
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    return this.helper.getPlaybackInfo();
  }

  public SimpleExoPlayer getPlayer() {
    return this.helper.getPlayer();
  }

  @Override public void release() {
    counter.set(0); // reset
    this.helper.removeEventListener(internalListener);
    this.helper.release();
    super.release();
  }
}
