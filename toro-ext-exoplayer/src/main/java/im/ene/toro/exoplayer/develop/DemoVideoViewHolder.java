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

package im.ene.toro.exoplayer.develop;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;
import im.ene.toro.exoplayer.BaseVideoViewHolder;

/**
 * Created by eneim on 6/11/16.
 */
public abstract class DemoVideoViewHolder extends BaseVideoViewHolder {

  @NonNull
  protected final DemoVideoView mVideoView;
  private boolean mPlayable = true; // normally true

  public DemoVideoViewHolder(View itemView) {
    super(itemView);
    mVideoView = findVideoView(itemView);
    if (mVideoView == null) {
      throw new NullPointerException("A valid DemoVideoView is required.");
    }

    // mVideoView.setOnPlayerStateChangeListener(mHelper);
  }

  protected abstract DemoVideoView findVideoView(View itemView);

  @Override public void preparePlayer(boolean playWhenReady) {
    mVideoView.preparePlayer(playWhenReady);
  }

  @Override public void releasePlayer() {
    mVideoView.releasePlayer();
  }

  // Client could override this method for better practice
  @Override public void start() {
    mVideoView.start();
  }

  @Override public void pause() {
    mVideoView.pause();
  }

  @Override public long getDuration() {
    return mVideoView.getDuration();
  }

  @Override public long getCurrentPosition() {
    return mVideoView.getCurrentPosition();
  }

  @Override public void seekTo(long pos) {
    mVideoView.seekTo(pos);
  }

  @Override public boolean isPlaying() {
    return mVideoView.isPlaying();
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

  @Override public boolean onPlaybackError(Exception error) {
    mPlayable = false;
    return super.onPlaybackError(error);
  }

  @Override public void stop() {
    mVideoView.stop();
  }

  @NonNull @Override public View getPlayerView() {
    return mVideoView;
  }

}
