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

import android.media.MediaPlayer;
import android.support.annotation.CallSuper;
import android.view.View;

/**
 * Created by eneim on 1/31/16.
 */
public abstract class ToroViewHolder extends ToroAdapter.ViewHolder implements ToroPlayer {

  private final ToroItemViewHelper mHelper;

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
  public void setOnItemViewLongClickListener(final View.OnLongClickListener listener) {
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

    super.setOnItemViewLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View v) {
        if (mLongClickListener != null) {
          mLongClickListener.onLongClick(v);  // we can ignore this boolean result
        }
        return listener.onLongClick(v);
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
   * Implement from {@link MediaPlayer.OnPreparedListener#onPrepared(MediaPlayer)}
   */
  @CallSuper @Override public void onPrepared(MediaPlayer mp) {
    Toro.onPrepared(this, itemView, itemView.getParent(), mp);
  }

  /**
   * Implement from {@link MediaPlayer.OnCompletionListener#onCompletion(MediaPlayer)}. This method
   * is closed and called only by {@link Toro#onCompletion(ToroPlayer, MediaPlayer)}, Client
   * should use {@link ToroPlayer#onPlaybackStopped()}
   */
  @Override public final void onCompletion(MediaPlayer mp) {
    Toro.onCompletion(this, mp);
  }

  /**
   * Implement from {@link MediaPlayer.OnErrorListener#onError(MediaPlayer, int, int)}. This method
   * is closed and called only by {@link Toro#onError(ToroPlayer, MediaPlayer, int, int)}, Client
   * should use {@link ToroPlayer#onPlaybackError(MediaPlayer, int, int)}
   */
  @Override public final boolean onError(MediaPlayer mp, int what, int extra) {
    return Toro.onError(this, mp, what, extra);
  }

  /**
   * Implement from {@link MediaPlayer.OnInfoListener#onInfo(MediaPlayer, int, int)}
   */
  @CallSuper @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
    return Toro.onInfo(this, mp, what, extra);
  }

  /**
   * Implement from {@link MediaPlayer.OnSeekCompleteListener#onSeekComplete(MediaPlayer)}
   */
  @CallSuper @Override public void onSeekComplete(MediaPlayer mp) {
    Toro.onSeekComplete(this, mp);
  }

  @Override public int getPlayOrder() {
    return getAdapterPosition();
  }

  /**
   * Allow long press to play support or not
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

  @Override public void onPlaybackError(MediaPlayer mp, int what, int extra) {

  }

  @Override public void onPlaybackProgress(int position, int duration) {

  }
}
