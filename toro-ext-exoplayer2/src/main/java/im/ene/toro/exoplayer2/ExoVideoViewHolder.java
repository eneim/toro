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

package im.ene.toro.exoplayer2;

import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import im.ene.toro.Toro;
import im.ene.toro.ToroAdapter;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;

/**
 * Created by eneim on 6/11/16.
 *
 * @deprecated from 2.2.0, use {@link ExoPlayerViewHolder} instead.
 */
@Deprecated
public abstract class ExoVideoViewHolder extends ToroAdapter.ViewHolder implements ToroPlayer {

  @NonNull protected final ExoVideoView playerView;
  protected final ExoPlayerViewHelper helper;
  private boolean playable = false; // normally false

  public ExoVideoViewHolder(View itemView) {
    super(itemView);
    playerView = findVideoView(itemView);
    if (playerView == null) {
      throw new NullPointerException("A valid ExoVideoView is required.");
    }
    helper = new ExoPlayerViewHelper(this, itemView);
  }

  protected abstract ExoVideoView findVideoView(View itemView);

  protected abstract void onBind(RecyclerView.Adapter adapter, @Nullable Object object);

  @Override public final void bind(RecyclerView.Adapter adapter, @Nullable Object object) {
    playerView.setPlayerCallback(helper);
    onBind(adapter, object);
    helper.onBound();
  }

  @CallSuper @Override protected void onRecycled() {
    playerView.setPlayerCallback(null);
    helper.onRecycled();
  }

  @CallSuper @Override public void onAttachedToWindow() {
    helper.onAttachedToWindow();
  }

  @CallSuper @Override public void onDetachedFromWindow() {
    helper.onDetachedFromWindow();
  }

  @CallSuper @Override public void onActivityActive() {

  }

  @CallSuper @Override public void onActivityInactive() {

  }

  @Override public void preparePlayer(boolean playWhenReady) {
    playerView.preparePlayer(playWhenReady);
  }

  @Override public void releasePlayer() {
    playerView.releasePlayer();
    playable = false;
  }

  // Client could override this method for better practice
  @Override public void start() {
    playerView.start();
  }

  @Override public void pause() {
    playerView.pause();
  }

  @Override public long getDuration() {
    return playerView.getDuration();
  }

  @Override public long getCurrentPosition() {
    return playerView.getCurrentPosition();
  }

  @Override public void seekTo(long pos) {
    playerView.seekTo(pos);
  }

  @Override public boolean isPlaying() {
    return playerView.isPlaying();
  }

  @Override public boolean wantsToPlay() {
    // Default implementation
    return visibleAreaOffset() >= Toro.DEFAULT_OFFSET;
  }

  @CallSuper @Override public void onVideoPrepared() {
    playable = true;
  }

  @Override public int getBufferPercentage() {
    return playerView.getBufferPercentage();
  }

  @Override public boolean onPlaybackError(Exception error) {
    playable = false;
    return true;
  }

  @Override public void stop() {
    playerView.stop();
  }

  @NonNull @Override public View getPlayerView() {
    return playerView;
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    this.playerView.setVolume(volume);
  }

  @Override public int getPlayOrder() {
    return getAdapterPosition();
  }

  @Override public void onVideoPreparing() {

  }

  @Override public void onPlaybackStarted() {

  }

  @Override public void onPlaybackPaused() {

  }

  @Override public void onPlaybackCompleted() {
    playable = false;
    this.playerView.stop();
  }

  @Override public boolean isPrepared() {
    return playable && playerView.getPlayer() != null;
  }

  @Override public float visibleAreaOffset() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent());
  }
}
