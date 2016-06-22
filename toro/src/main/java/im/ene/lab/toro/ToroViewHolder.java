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
import android.util.Log;
import android.view.View;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.media.PlaybackInfo;
import im.ene.lab.toro.media.State;

/**
 * Created by eneim on 1/31/16.
 *
 * Abstract implementation of {@link ToroPlayer}.
 */
public abstract class ToroViewHolder extends ToroAdapter.ViewHolder implements ToroPlayer {

  private final VideoViewItemHelper mHelper;

  private View.OnLongClickListener mLongClickListener;

  public ToroViewHolder(View itemView) {
    super(itemView);
    mHelper = RecyclerViewItemHelper.getInstance();
    if (allowLongPressSupport()) {
      if (mLongClickListener == null) {
        mLongClickListener = new View.OnLongClickListener() {
          @Override public boolean onLongClick(View v) {
            return mHelper.onItemLongClick(ToroViewHolder.this, ToroViewHolder.this.itemView,
                ToroViewHolder.this.itemView.getParent());
          }
        };
      }

      super.setOnItemLongClickListener(mLongClickListener);
    } else {
      mLongClickListener = null;
    }
  }

  @CallSuper @Override public void onActivityActive() {

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
        boolean longClickHandled = false;

        if (mLongClickListener != null) {
          longClickHandled = mLongClickListener.onLongClick(v); // we can ignore this boolean result
        }

        return listener.onLongClick(v) && longClickHandled;
      }
    });
  }

  @CallSuper @Override public void onActivityInactive() {
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

  @Override
  public void onPlayerStateChanged(Cineer player, boolean playWhenReady, @State int playbackState) {
    switch (playbackState) {
      case Cineer.PLAYER_PREPARED:
        mHelper.onPrepared(this, itemView, itemView.getParent(), player);
        break;
      case Cineer.PLAYER_ENDED:
        mHelper.onCompletion(this, player);
        break;
      // TODO Support buffering update.
      case Cineer.PLAYER_BUFFERING:
        Log.i(TAG, "onPlayerStateChanged: " + player.getBufferedPercentage());
        break;
      case Cineer.PLAYER_IDLE:
        break;
      case Cineer.PLAYER_PREPARING:
        break;
      case Cineer.PLAYER_READY:
        break;
      default:
        break;
    }
  }

  @Override public boolean onPlayerError(Cineer player, PlaybackException error) {
    return mHelper.onError(this, player, error);
  }

  /**
   * Implement from {@link OnInfoListener}
   */
  @Override public final boolean onInfo(Cineer mp, PlaybackInfo info) {
    return mHelper.onInfo(this, mp, info);
  }

  @Override public int getPlayOrder() {
    return getAdapterPosition();
  }

  /**
   * Allow long press to play support or not. {@code false} by default
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

  @Override public boolean onPlaybackError(Cineer mp, PlaybackException error) {
    return true;  // don't want to see the annoying dialog
  }

  @Override public void onPlaybackInfo(Cineer mp, PlaybackInfo info) {

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

    if (!(itemView.getParent() instanceof View)) {
      return null;
    }

    Rect rect = new Rect();
    Point offset = new Point();
    ((View) itemView.getParent()).getGlobalVisibleRect(rect, offset);
    return rect;
  }

  @Override public void onVideoPrepared(Cineer mp) {

  }

  @Override public int getBufferPercentage() {
    return 0;
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
