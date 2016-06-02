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
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.View;
import im.ene.lab.toro.player.PlaybackException;
import im.ene.lab.toro.player.PlaybackInfo;
import im.ene.lab.toro.player.TrMediaPlayer;
import im.ene.lab.toro.player.listener.OnCompletionListener;
import im.ene.lab.toro.player.listener.OnErrorListener;
import im.ene.lab.toro.player.listener.OnInfoListener;
import im.ene.lab.toro.player.listener.OnPreparedListener;
import im.ene.lab.toro.player.listener.OnSeekCompleteListener;

/**
 * Created by eneim on 1/31/16.
 */
public abstract class ToroViewHolder extends ToroAdapter.ViewHolder implements ToroPlayer {

  private final VideoViewItemHelper mHelper;

  private View.OnLongClickListener mLongClickListener;

  public ToroViewHolder(View itemView) {
    super(itemView);
    mHelper = Toro.RECYCLER_VIEW_HELPER;
    if (allowLongPressSupport()) {
      if (mLongClickListener == null) {
        mLongClickListener = new View.OnLongClickListener() {
          @Override public boolean onLongClick(View v) {
            return mHelper.onItemLongClick(ToroViewHolder.this, ToroViewHolder.this.itemView,
                ToroViewHolder.this.itemView.getParent());
          }
        };
      }

      itemView.setOnLongClickListener(mLongClickListener);
    } else {
      mLongClickListener = null;
    }
  }

  @CallSuper @Override public void onActivityResumed() {

  }

  @CallSuper @Override
  public void setOnItemLongClickListener(final View.OnLongClickListener listener) {
    if (allowLongPressSupport()) {
      // Client set different long click listener, but this View holder tends to support Long
      // press, so we must support it
      if (mLongClickListener == null) {
        mLongClickListener = new View.OnLongClickListener() {
          @Override public boolean onLongClick(View v) {
            return mHelper.onItemLongClick(ToroViewHolder.this, itemView, itemView.getParent());
          }
        };
      }
    } else {
      mLongClickListener = null;
    }

    super.setOnItemLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View v) {
        boolean longClicked = false;

        if (mLongClickListener != null) {
          longClicked = mLongClickListener.onLongClick(v);  // we can ignore this boolean result
        }
        return listener.onLongClick(v) && longClicked;
      }
    });
  }

  @CallSuper @Override public void onActivityPaused() {
    // Release listener to prevent memory leak
    mLongClickListener = null;
  }

  @CallSuper @Override public void onAttachedToParent() {
    super.onAttachedToParent();
    mHelper.onAttachedToParent(this, itemView, itemView.getParent());
  }

  @CallSuper @Override public void onDetachedFromParent() {
    super.onDetachedFromParent();
    mHelper.onDetachedFromParent(this, itemView, itemView.getParent());
  }

  /**
   * Implement from {@link OnPreparedListener}
   */
  @Override public final void onPrepared(TrMediaPlayer mp) {
    mHelper.onPrepared(this, itemView, itemView.getParent(), mp);
  }

  /**
   * Implement from {@link OnCompletionListener}. This method
   * is closed and called only by {@link Toro#onCompletion(ToroPlayer, TrMediaPlayer)}, Client
   * should use {@link ToroPlayer#onPlaybackStopped()}
   */
  @Override public final void onCompletion(TrMediaPlayer mp) {
    mHelper.onCompletion(this, mp);
  }

  /**
   * Implement from {@link OnErrorListener}. This method
   * is closed and called only by {@link Toro#onError(ToroPlayer, TrMediaPlayer,
   * PlaybackException)}, Client should use {@link ToroPlayer#onPlaybackError(TrMediaPlayer,
   * PlaybackException)}
   */
  @Override public final boolean onError(TrMediaPlayer mp, PlaybackException error) {
    return mHelper.onError(this, mp, error);
  }

  /**
   * Implement from {@link OnInfoListener}
   */
  @Override public final boolean onInfo(TrMediaPlayer mp, PlaybackInfo info) {
    return mHelper.onInfo(this, mp, info);
  }

  /**
   * Implement from {@link OnSeekCompleteListener}
   */
  @Override public final void onSeekComplete(TrMediaPlayer mp) {
    mHelper.onSeekComplete(this, mp);
  }

  @Override public int getPlayOrder() {
    return getAdapterPosition();
  }

  /**
   * Allow long press to play support or not. False by default
   */
  protected boolean allowLongPressSupport() {
    return false;
  }

  @Override public void onPlaybackStarted() {

  }

  @Override public void onPlaybackPaused() {

  }

  @Override public void onPlaybackStopped() {

  }

  @Override public boolean onPlaybackError(TrMediaPlayer mp, PlaybackException error) {
    return true;  // don't want to see the annoying dialog
  }

  @Override public void onPlaybackInfo(TrMediaPlayer mp, PlaybackInfo info) {

  }

  @Override public void onPlaybackProgress(long position, long duration) {

  }

  private static final String TAG = "ToroViewHolder";

  @Override public float visibleAreaOffset() {
    Rect videoRect = getVideoRect();
    Rect parentRect = getRecyclerViewRect();

    if (parentRect != null && (parentRect.contains(videoRect) || parentRect.intersect(videoRect))) {
      float visibleArea = videoRect.height() * videoRect.width();
      float viewArea = getVideoView().getWidth() * getVideoView().getHeight();
      return viewArea <= 0.f ? 1.f : visibleArea / viewArea;
    } else {
      return 0.f;
    }
  }

  protected final Rect getVideoRect() {
    Rect rect = new Rect();
    Point offset = new Point();
    getVideoView().getGlobalVisibleRect(rect, offset);
    return rect;
  }

  @Nullable protected final Rect getRecyclerViewRect() {
    if (itemView.getParent() == null) { // view is not attached to RecyclerView
      return null;
    }

    Rect rect = new Rect();
    Point offset = new Point();
    ((View) itemView.getParent()).getGlobalVisibleRect(rect, offset);
    return rect;
  }

  @Override public void onVideoPrepared(TrMediaPlayer mp) {

  }

  @Override public int getBufferPercentage() {
    return 0;
  }

  @Override public boolean canPause() {
    return true;
  }

  @Override public boolean canSeekBackward() {
    return true;
  }

  @Override public boolean canSeekForward() {
    return true;
  }

  @Override public int getAudioSessionId() {
    return 0;
  }

  /**
   * Indicate that this Player is able to replay right after it stops (loop-able) or not.
   *
   * @return true if this Player is loop-able, false otherwise
   */
  @Override public boolean isLoopAble() {
    return false;
  }
}
