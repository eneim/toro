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

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.View;
import android.widget.VideoView;
import com.sprylab.android.widget.TextureVideoView;

/**
 * Created by eneim on 2/3/16.
 *
 * ViewHolder to support {@link VideoView}
 */
public abstract class AbsVideoViewHolder extends ToroViewHolder {

  protected final VideoView mVideoView;

  private static final String TAG = "ToroVideoViewHolder";

  private boolean mPlayable = true; // normally true

  public AbsVideoViewHolder(View itemView) {
    super(itemView);
    mVideoView = findVideoView(itemView);

    if (mVideoView == null) {
      throw new NullPointerException("Unusable ViewHolder");
    }

    mVideoView.setOnPreparedListener(this);
    mVideoView.setOnCompletionListener(this);
    mVideoView.setOnErrorListener(this);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      mVideoView.setOnInfoListener(this);
    }
    // This is unsupported
    // mVideoView.setOnSeekCompleteListener(this);
  }

  protected abstract VideoView findVideoView(View itemView);

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

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) @Override public int getAudioSessionId() {
    if (mVideoView != null) {
      return mVideoView.getAudioSessionId();
    }

    return 0;
  }

  @Override public float visibleAreaOffset() {
    Rect videoRect = getVideoRect();
    Rect parentRect = getRecyclerViewRect();
    if (parentRect != null && !parentRect.contains(videoRect) && !parentRect.intersect(videoRect)) {
      return 0.f;
    }

    return mVideoView.getHeight() <= 0 ? 1.f : videoRect.height() / (float) mVideoView.getHeight();
  }

  @Override public boolean wantsToPlay() {
    return false;
  }

  @Override public boolean isAbleToPlay() {
    return mPlayable;
  }

  @Override public void onPrepared(MediaPlayer mp) {
    super.onPrepared(mp);
    mPlayable = true;
  }

  /**
   * This method is unsupported by {@link TextureVideoView}
   */
  /** @hide */
  @Deprecated @Override public final void onSeekComplete(MediaPlayer mp) {
    super.onSeekComplete(mp);
  }

  @Override public void onPlaybackError(MediaPlayer mp, int what, int extra) {
    mPlayable = false;
  }

  private Rect getVideoRect() {
    Rect rect = new Rect();
    mVideoView.getGlobalVisibleRect(rect, new Point());
    return rect;
  }

  private Rect getRecyclerViewRect() {
    if (itemView.getParent() == null) {
      return null;
    }

    Rect rect = new Rect();
    rect.contains(0, 0, 0, 0);
    ((View) itemView.getParent()).getGlobalVisibleRect(rect, new Point());
    return rect;
  }

  @Override public View getVideoView() {
    return mVideoView;
  }
}
