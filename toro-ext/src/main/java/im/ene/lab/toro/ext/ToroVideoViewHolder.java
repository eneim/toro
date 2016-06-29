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

package im.ene.lab.toro.ext;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.player.widget.VideoPlayerView;

/**
 * Created by eneim on 6/11/16.
 */
public abstract class ToroVideoViewHolder extends ToroViewHolder {

  protected final VideoPlayerView mVideoView;
  private boolean mPlayable = true; // normally true

  public ToroVideoViewHolder(View itemView) {
    super(itemView);
    mVideoView = findVideoView(itemView);

    if (mVideoView == null) {
      throw new NullPointerException("A valid VideoPlayerView is required.");
    }

    mVideoView.setOnPlayerStateChangeListener(mHelper);
  }

  protected abstract VideoPlayerView findVideoView(View itemView);

  @Override public void preparePlayer(boolean playWhenReady) {
    if (mVideoView != null) {
      mVideoView.preparePlayer(playWhenReady);
    }
  }

  @Override public void releasePlayer() {
    if (mVideoView != null) {
      mVideoView.releasePlayer();
    }
  }

  // Client could override this method for better practice
  @Override public void start() {
    if (mVideoView != null) {
      mVideoView.start();
    }
  }

  @Override public void pause() {
    if (mVideoView != null) {
      mVideoView.pause();
    }
  }

  @Override public long getDuration() {
    return mVideoView != null ? mVideoView.getDuration() : -1;
  }

  @Override public long getCurrentPosition() {
    return mVideoView != null ? mVideoView.getCurrentPosition() : 0;
  }

  @Override public void seekTo(long pos) {
    if (mVideoView != null) {
      mVideoView.seekTo(pos);
    }
  }

  @Override public boolean isPlaying() {
    return mVideoView != null && mVideoView.isPlaying();
  }

  @Override public boolean wantsToPlay() {
    // Default implementation
    return visibleAreaOffset() >= 0.75 && mPlayable;
  }

  @CallSuper @Override public void onVideoPrepared(Cineer mp) {
    mPlayable = true;
  }

  @Override public boolean onPlaybackError(Cineer mp, PlaybackException error) {
    mPlayable = false;
    return super.onPlaybackError(mp, error);
  }

  @Override public void stop() {
    if (mVideoView != null) {
      mVideoView.stop();
    }
  }

  @NonNull @Override public View getVideoView() {
    return mVideoView;
  }

}
