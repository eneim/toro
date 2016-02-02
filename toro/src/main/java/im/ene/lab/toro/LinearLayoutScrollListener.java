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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by eneim on 1/31/16.
 *
 * @hide
 */
public final class LinearLayoutScrollListener extends ToroScrollListener {

  private int mLastVideoPosition;

  private Rect mParentRect;
  private Rect mChildRect;

  public LinearLayoutScrollListener(@NonNull ToroManager manager) {
    super(manager);
    mLastVideoPosition = -1;
    mParentRect = new Rect();
    mChildRect = new Rect();
  }

  @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
      return;
    }
    // We require current layout manager to be a LinearLayoutManager.
    if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
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

    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    int firstPosition = layoutManager.findFirstVisibleItemPosition();
    int lastPosition = layoutManager.findLastVisibleItemPosition();
    int videoPosition = -1;
    if ((firstPosition != RecyclerView.NO_POSITION || lastPosition != RecyclerView.NO_POSITION)
        && firstPosition <= lastPosition) { // for sure
      ToroPlayer video;
      for (int i = firstPosition; i <= lastPosition; i++) {
        // detected a view holder for video player
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
        if (viewHolder != null && viewHolder instanceof ToroPlayer) {
          video = (ToroPlayer) viewHolder;
          // Re-calculate the rectangles
          recyclerView.getGlobalVisibleRect(mParentRect, new Point());
          viewHolder.itemView.getGlobalVisibleRect(mChildRect, new Point());
          // check that view position
          if (video.wantsToPlay(mParentRect, mChildRect)) {
            if (!candidates.contains(video)) {
              candidates.add(video);
            }
          }
        }
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
  }
}
