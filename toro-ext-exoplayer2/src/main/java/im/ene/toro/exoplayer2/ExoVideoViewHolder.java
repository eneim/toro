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
import android.view.View;

/**
 * Created by eneim on 6/11/16.
 */
public abstract class ExoVideoViewHolder extends BaseVideoViewHolder {

  @NonNull
  protected final ExoVideoView videoView;
  private boolean mPlayable = true; // normally true

  public ExoVideoViewHolder(View itemView) {
    super(itemView);
    videoView = findVideoView(itemView);
    if (videoView == null) {
      throw new NullPointerException("A valid DemoVideoView is required.");
    }

    videoView.setPlayerCallback(helper);
  }

  protected abstract ExoVideoView findVideoView(View itemView);

  @Override public void preparePlayer(boolean playWhenReady) {
    videoView.initializePlayer(playWhenReady);
  }

  @Override public void releasePlayer() {
    videoView.releasePlayer();
  }

  // Client could override this method for better practice
  @Override public void start() {
    videoView.start();
  }

  @Override public void pause() {
    videoView.pause();
  }

  @Override public long getDuration() {
    return videoView.getDuration();
  }

  @Override public long getCurrentPosition() {
    return videoView.getCurrentPosition();
  }

  @Override public void seekTo(long pos) {
    videoView.seekTo(pos);
  }

  @Override public boolean isPlaying() {
    return videoView.isPlaying();
  }

  @Override public boolean wantsToPlay() {
    // Default implementation
    return visibleAreaOffset() >= 0.75 && mPlayable;
  }

  @CallSuper @Override public void onVideoPrepared() {
    mPlayable = true;
  }

  @Override public void onVideoPreparing() {

  }

  @Override public int getBufferPercentage() {
    return videoView.getBufferPercentage();
  }

  @Override public boolean onPlaybackError(Exception error) {
    mPlayable = false;
    return super.onPlaybackError(error);
  }

  @Override public void stop() {
    videoView.stop();
  }

  @NonNull @Override public View getPlayerView() {
    return videoView;
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    this.videoView.setVolume(volume);
  }
}
