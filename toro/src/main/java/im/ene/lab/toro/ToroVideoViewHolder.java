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

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import im.ene.lab.toro.widget.ToroVideoView;

/**
 * Created by eneim on 1/31/16.
 */
public abstract class ToroVideoViewHolder extends ToroViewHolder {

  protected final ToroVideoView mVideoView;

  private static final String TAG = "ToroVideoViewHolder";

  public ToroVideoViewHolder(View itemView) {
    super(itemView);
    mVideoView = getVideoView(itemView);

    if (mVideoView == null) {
      throw new NullPointerException("Unusable ViewHolder");
    }

    mVideoView.setOnPreparedListener(this);
    mVideoView.setOnCompletionListener(this);
    mVideoView.setOnErrorListener(this);
    mVideoView.setOnInfoListener(this);
    mVideoView.setOnSeekCompleteListener(this);

    // TODO: setup long press support on demand
    itemView.setOnLongClickListener(this);
  }

  protected abstract ToroVideoView getVideoView(View itemView);

  // Client needs to implement this method
  // @Override public boolean wantsToPlay(Rect parentRect, Rect childRect) {
  //   return false;
  // }

  // Client needs to implement this method
  // @Nullable @Override public Long getVideoId() {
  //   return null;
  // }

  @Override public void onActivityPaused() {

  }

  @Override public void onActivityResumed() {

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

  @Override public int getDuration() {
    return mVideoView != null ? mVideoView.getDuration() : -1;
  }

  @Override public int getCurrentPosition() {
    return mVideoView != null ? mVideoView.getCurrentPosition() : 0;
  }

  @Override public void seekTo(int pos) {
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

  @Override public boolean canPause() {
    return mVideoView != null && mVideoView.canPause();
  }

  @Override public boolean canSeekBackward() {
    return mVideoView != null && mVideoView.canSeekBackward();
  }

  @Override public boolean canSeekForward() {
    return mVideoView != null && mVideoView.canSeekForward();
  }

  @Override public int getAudioSessionId() {
    if (mVideoView != null) {
      return mVideoView.getAudioSessionId();
    }

    return 0;
  }

  @Override public float visibleAreaOffset() {
    return 0;
  }

  protected Rect getVideoRect() {
    Rect rect = new Rect();
    mVideoView.getGlobalVisibleRect(rect, new Point());
    return rect;
  }

  protected Rect getRecyclerViewRect() {
    if (itemView.getParent() == null) {
      return null;
    }

    Rect rect = new Rect();
    rect.contains(0, 0, 0, 0);
    ((View) itemView.getParent()).getGlobalVisibleRect(rect, new Point());
    return rect;
  }

  @Override public void onStartPlayback() {

  }

  @Override public void onPausePlayback() {

  }

  @Override public void onStopPlayback() {

  }

  @Override public void onPlaybackProgress(int position, int duration) {

  }
}
