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

package im.ene.toro.extended;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.VideoPlayerManager;
import im.ene.lab.toro.VideoPlayerManagerImpl;

/**
 * Created by eneim on 10/7/16.
 */

public abstract class ExtToroAdapter<VH extends ToroAdapter.ViewHolder> extends ToroAdapter<VH>
    implements VideoPlayerManager {

  public static final int INVALID_VIDEO_POSITION = -1;

  private RecyclerView parent;
  private final VideoPlayerManagerImpl delegate;

  public ExtToroAdapter() {
    delegate = new VideoPlayerManagerImpl();
  }

  @Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    this.parent = recyclerView;
  }

  @Override public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    // TODO Do some tearing down jobs before setting the parent View to null
    this.parent = null;
  }

  @Nullable public final RecyclerView.ViewHolder findViewHolderForPosition(int position) {
    return parent == null ? null : parent.findViewHolderForAdapterPosition(position);
  }

  public final void scrollToPosition(int position) {
    if (parent != null) {
      parent.smoothScrollToPosition(position);
    }
  }

  public final int findNextVideoPosition() {
    int currentVideoPosition =
        getPlayer() == null ? INVALID_VIDEO_POSITION : getPlayer().getPlayOrder();
    do {
      currentVideoPosition++;
    } while (currentVideoPosition < getItemCount() && !(findViewHolderForPosition(
        currentVideoPosition) instanceof ToroPlayer));

    return currentVideoPosition < getItemCount() ? currentVideoPosition : INVALID_VIDEO_POSITION;
  }

  public final ToroPlayer findNextPlayer() {
    int nextVideoPosition = findNextVideoPosition();
    return nextVideoPosition == INVALID_VIDEO_POSITION ? null
        : (ToroPlayer) findViewHolderForPosition(nextVideoPosition);
  }

  public final void scrollToNextVideo() {
    int nextVideoPosition = findNextVideoPosition();
    if (nextVideoPosition != INVALID_VIDEO_POSITION) {
      scrollToPosition(findNextVideoPosition());
    }
  }

  // VideoPlayerManager implementation

  @Nullable @Override public ToroPlayer getPlayer() {
    return delegate.getPlayer();
  }

  @Override public void setPlayer(ToroPlayer player) {
    delegate.setPlayer(player);
  }

  @Override public void onRegistered() {
    delegate.onRegistered();
  }

  @Override public void onUnregistered() {
    delegate.onUnregistered();
  }

  @Override public void startPlayback() {
    delegate.startPlayback();
  }

  @Override public void pausePlayback() {
    delegate.pausePlayback();
  }

  @Override public void stopPlayback() {
    if (getPlayer() != null) {
      Log.e("PVH:" + getPlayer().getPlayOrder() + ":" + getPlayer().hashCode(), "STOP");
    }
    delegate.stopPlayback();
  }

  @Override public void saveVideoState(String videoId, @Nullable Long position, long duration) {
    if (getPlayer() != null) {
      Log.w("PVH:" + getPlayer().getPlayOrder() + ":" + getPlayer().hashCode(),
          "saveVideoState() called with: videoId = ["
              + videoId
              + "], position = ["
              + position
              + "], duration = ["
              + duration
              + "]");
    }
    delegate.saveVideoState(videoId, position, duration);
  }

  @Override public void restoreVideoState(String videoId) {
    if (getPlayer() != null) {
      Log.w("PVH:" + getPlayer().getPlayOrder() + ":" + getPlayer().hashCode(),
          "restoreVideoState() called with: videoId = [" + videoId + "]");
    }

    delegate.restoreVideoState(videoId);
  }

  @Nullable @Override public Long getSavedPosition(String videoId) {
    return delegate.getSavedPosition(videoId);
  }
}
