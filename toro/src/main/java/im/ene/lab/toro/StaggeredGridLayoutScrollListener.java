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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by eneim on 1/31/16.
 *
 * @hide
 */
public final class StaggeredGridLayoutScrollListener extends ToroScrollListener {

  private static final String TAG = "Staggered";
  private int mLastVideoPosition = -1;

  public StaggeredGridLayoutScrollListener(@NonNull ToroManager manager) {
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
      mLastVideoPosition = lastVideo.getPlayOrder();
      RecyclerView.ViewHolder viewHolder =
          recyclerView.findViewHolderForLayoutPosition(mLastVideoPosition);
      // Re-calculate the rectangles
      if (viewHolder != null) {
        if (lastVideo.wantsToPlay() && lastVideo.isAbleToPlay() &&
            Toro.getStrategy().allowsToPlay(lastVideo, recyclerView)) {
          candidates.add(lastVideo);
        } else {
          mManager.saveVideoState(lastVideo.getVideoId(), lastVideo.getCurrentPosition(),
              lastVideo.getDuration());
          if (lastVideo.isPlaying()) {
            mManager.pausePlayback();
          }
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
          // check that view position
          if (video.wantsToPlay() && video.isAbleToPlay() &&
              Toro.getStrategy().allowsToPlay(video, recyclerView)) {
            if (!candidates.contains(video)) {
              candidates.add(video);
            }
          }
        }
      }
    } catch (NullPointerException er) {
      er.printStackTrace();
    }

    video = Toro.getStrategy().findBestPlayer(candidates);

    if (video == null) {
      return;
    }

    for (ToroPlayer player : candidates) {
      if (player == video) {
        videoPosition = player.getPlayOrder();
        break;
      }
    }

    if (videoPosition == mLastVideoPosition) {  // Nothing changes, keep going
      if (lastVideo != null && !lastVideo.isPlaying()) {
        mManager.startPlayback();
      }
      return;
    }

    // Pause last video
    if (lastVideo != null) {
      mManager.saveVideoState(lastVideo.getVideoId(), lastVideo.getCurrentPosition(),
          lastVideo.getDuration());
      if (lastVideo.isPlaying()) {
        mManager.pausePlayback();
      }
    }

    // Switch video
    lastVideo = video;
    mLastVideoPosition = videoPosition;

    mManager.setPlayer(lastVideo);
    mManager.restoreVideoState(lastVideo.getVideoId());
    mManager.startPlayback();
  }
}
