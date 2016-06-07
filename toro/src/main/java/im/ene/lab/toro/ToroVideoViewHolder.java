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

package im.ene.lab.toro;

import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.view.View;
import im.ene.lab.toro.player.MediaSource;
import im.ene.lab.toro.player.PlaybackException;
import im.ene.lab.toro.player.TrMediaPlayer;
import im.ene.lab.toro.player.widget.VideoPlayerView;

/**
 * Created by eneim on 1/31/16.
 */
public abstract class ToroVideoViewHolder extends ToroViewHolder {

  protected final VideoPlayerView mVideoView;
  private boolean mPlayable = true; // normally true

  public ToroVideoViewHolder(View itemView) {
    super(itemView);
    mVideoView = findVideoView(itemView);

    if (mVideoView == null) {
      throw new NullPointerException("Unusable ViewHolder");
    }

    mVideoView.setOnPreparedListener(this);
    mVideoView.setOnCompletionListener(this);
    mVideoView.setOnErrorListener(this);
    mVideoView.setOnInfoListener(this);
  }

  protected abstract VideoPlayerView findVideoView(View itemView);

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

  @Override public int getBufferPercentage() {
    if (mVideoView != null) {
      return mVideoView.getBufferPercentage();
    }

    return 0;
  }

  @Override public int getAudioSessionId() {
    if (mVideoView != null) {
      return mVideoView.getAudioSessionId();
    }

    return 0;
  }

  @Override public boolean wantsToPlay() {
    // Default implementation
    return visibleAreaOffset() >= 0.75 && mPlayable;
  }

  @CallSuper @Override public void onVideoPrepared(TrMediaPlayer mp) {
    mPlayable = true;
  }

  @Override public boolean onPlaybackError(TrMediaPlayer mp, PlaybackException error) {
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

  @Override public void setBackgroundAudioEnabled(boolean enabled) {
    mVideoView.setBackgroundAudioEnabled(enabled);
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    mVideoView.setVolume(volume);
  }

  @Override public void setMediaSource(@NonNull MediaSource source) {
    mVideoView.setMediaSource(source);
  }

  @Override public void setMediaUri(Uri uri) {
    mVideoView.setMediaUri(uri);
  }
}
