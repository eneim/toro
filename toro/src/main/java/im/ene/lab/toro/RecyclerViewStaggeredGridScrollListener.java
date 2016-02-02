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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by eneim on 1/31/16.
 *
 * @hide
 */
public final class RecyclerViewStaggeredGridScrollListener extends RecyclerViewScrollListener {

  private int mLastVideoPosition = -1;
  private int mSpanCount;
  private ToroPlayer mLastVideo = null;

  private Rect mParentRect = new Rect();
  private Rect mChildRect = new Rect();

  public RecyclerViewStaggeredGridScrollListener(@NonNull ToroManager manager) {
    super(manager);
  }

  @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
      return;
    }

    if (!(recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager)) {
      return;
    }

    List<ToroPlayer> candidates = new ArrayList<>();
    // Check current playing position
    ToroPlayer lastVideo = mManager.getPlayer();
    if (lastVideo != null) {
      mLastVideoPosition = lastVideo.getPlayerPosition();
      RecyclerView.ViewHolder viewHolder =
          recyclerView.findViewHolderForLayoutPosition(mLastVideoPosition);
      // Re-calculate the rectangles
      if (viewHolder != null) {
        recyclerView.getLocalVisibleRect(mParentRect);
        viewHolder.itemView.getLocalVisibleRect(mChildRect);
        if (lastVideo.wantsToPlay(mParentRect, mChildRect)) {
          candidates.add(lastVideo);
        }
      }
    }

    StaggeredGridLayoutManager layoutManager =
        (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
    int[] firstVisibleItemPositions = layoutManager.findFirstVisibleItemPositions(null);
    int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null);

    layoutManager.findFirstCompletelyVisibleItemPositions(null);

    List<Integer> firstVisiblePositions = Utils.asList(firstVisibleItemPositions);
    List<Integer> lastVisiblePositions = Utils.asList(lastVisibleItemPositions);
    int videoPosition = -1;
    ToroPlayer video;
    try {
      int minPosition = Collections.min(firstVisiblePositions);
      int maxPosition = Collections.max(lastVisiblePositions);
      Log.i(TAG, "onScrollStateChanged: " + minPosition + " | " + maxPosition);
      for (int i = minPosition; i <= maxPosition; i++) {
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
        if (viewHolder != null && viewHolder instanceof ToroPlayer) {
          video = (ToroPlayer) viewHolder;
          // Re-calculate the rectangles
          recyclerView.getHitRect(mParentRect);
          viewHolder.itemView.getGlobalVisibleRect(mChildRect, new Point());
          // check that view position
          if (video.wantsToPlay(mParentRect, mChildRect)) {
            if (!candidates.contains(video)) {
              candidates.add(video);
            }
          }
        }
      }
    } catch (NullPointerException er) {
      er.printStackTrace();
    }

    if (Toro.getStrategy().requireCompletelyVisible()) {
      for (Iterator<ToroPlayer> iterator = candidates.iterator(); iterator.hasNext(); ) {
        ToroPlayer player = iterator.next();
        if (player.visibleAreaOffset() < 1.f) {
          iterator.remove();
        }
      }
    }

    video = Toro.getStrategy().getPlayer(candidates);

    if (video == null) {
      return;
    }

    for (ToroPlayer player : candidates) {
      if (player == video) {
        videoPosition = player.getPlayerPosition();
        break;
      }
    }

    if (videoPosition == mLastVideoPosition) {  // Nothing changes, keep going
      if (lastVideo != null && !lastVideo.isPlaying()) {
        mManager.startVideo(lastVideo);
      }
      return;
    }

    // Pause last video
    if (lastVideo != null) {
      mManager.saveVideoState(lastVideo.getVideoId(), lastVideo.getCurrentPosition(),
          lastVideo.getDuration());
      if (lastVideo.isPlaying()) {
        mManager.pauseVideo(lastVideo);
      }
    }

    // Switch video
    lastVideo = video;
    mLastVideoPosition = videoPosition;

    mManager.setPlayer(lastVideo);
    mManager.restoreVideoState(lastVideo, lastVideo.getVideoId());
    mManager.startVideo(lastVideo);
  }

  private static final String TAG = "Staggered";
}
