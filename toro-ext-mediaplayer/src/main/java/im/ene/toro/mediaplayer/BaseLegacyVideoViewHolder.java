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

package im.ene.toro.mediaplayer;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.VideoView;
import im.ene.toro.Toro;
import im.ene.toro.ToroAdapter;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;

/**
 * Created by eneim on 9/29/16.
 */

public abstract class BaseLegacyVideoViewHolder extends ToroAdapter.ViewHolder
    implements ToroPlayer {

  protected final LegacyVideoViewHelper helper;
  private boolean isPlayable = false;
  @NonNull
  protected final VideoView videoView;

  public BaseLegacyVideoViewHolder(View itemView) {
    super(itemView);
    videoView = findVideoView(itemView);
    if (videoView == null) {
      throw new NullPointerException("A valid VideoView is required");
    }

    helper = new LegacyVideoViewHelper(this, itemView);
    videoView.setOnPreparedListener(helper);
    videoView.setOnCompletionListener(helper);
    videoView.setOnErrorListener(helper);
  }

  protected abstract VideoView findVideoView(View itemView);

  /* BEGIN: ToroViewHolder callbacks */
  @Override public void onAttachedToWindow() {
    helper.onAttachedToWindow();
  }

  @Override public void onDetachedFromWindow() {
    helper.onDetachedFromWindow();
  }
  /* END: ToroViewHolder callbacks */

  /* BEGIN: ToroPlayer callbacks (partly) */
  @Override public void onActivityActive() {

  }

  @Override public void onActivityInactive() {

  }

  @Override public void onVideoPreparing() {

  }

  @Override public void onVideoPrepared() {
    this.isPlayable = true;
  }

  @Override public void onPlaybackStarted() {

  }

  @Override public void onPlaybackPaused() {

  }

  @Override public void onPlaybackCompleted() {
    isPlayable = false;
  }

  @Override public boolean isPrepared() {
    return isPlayable;
  }

  @Override public boolean onPlaybackError(Exception error) {
    this.isPlayable = false;
    return true;
  }

  @Override public boolean wantsToPlay() {
    return isPlayable && visibleAreaOffset() >= Toro.DEFAULT_OFFSET;
  }

  @Override public float visibleAreaOffset() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent());
  }

  @Override public int getPlayOrder() {
    return getAdapterPosition();
  }

  @Override public void preparePlayer(boolean playWhenReady) {
    helper.preparePlayer(playWhenReady);
  }

  @Override public void start() {
    videoView.start();
  }

  @Override public void pause() {
    videoView.pause();
  }

  @Override public void stop() {
    videoView.stopPlayback();
  }

  @Override public void releasePlayer() {
    // Do nothing here
    helper.releasePlayer();
  }

  @Override public long getDuration() {
    return videoView.getDuration();
  }

  @Override public long getCurrentPosition() {
    return videoView.getCurrentPosition();
  }

  @Override public void seekTo(long pos) {
    videoView.seekTo((int) pos);
  }

  @Override public boolean isPlaying() {
    return videoView.isPlaying();
  }

  @Override public void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume) {
    this.helper.setVolume(volume);
  }

  @NonNull @Override public View getPlayerView() {
    return videoView;
  }

  @Override public int getBufferPercentage() {
    return videoView.getBufferPercentage();
  }

  /* END: ToroPlayer callbacks (partly) */
}
