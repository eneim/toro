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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by eneim on 1/31/16.
 *
 * @hide
 */
public class ToroScrollListener extends RecyclerView.OnScrollListener {

  @NonNull protected final VideoPlayerManager mManager;

  public ToroScrollListener(@NonNull VideoPlayerManager manager) {
    this.mManager = manager;
  }

  @NonNull protected final VideoPlayerManager getManager() {
    return mManager;
  }

  private int mLastVideoPosition = -1;

  @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
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
            lastVideo.onPlaybackPaused();
          }
        }
      }
    }

    int videoPosition = -1;
    ToroPlayer video;
    int firstPosition = RecyclerView.NO_POSITION;
    int lastPosition = RecyclerView.NO_POSITION;

    // Find visible positions bound
    if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
      LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
      firstPosition = layoutManager.findFirstVisibleItemPosition();
      lastPosition = layoutManager.findLastVisibleItemPosition();
    } else if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
      StaggeredGridLayoutManager layoutManager =
          (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
      int[] firstVisibleItemPositions = layoutManager.findFirstVisibleItemPositions(null);
      int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null);

      List<Integer> firstVisiblePositions = ToroUtils.asList(firstVisibleItemPositions);
      List<Integer> lastVisiblePositions = ToroUtils.asList(lastVisibleItemPositions);
      try {
        firstPosition = Collections.min(firstVisiblePositions);
        lastPosition = Collections.max(lastVisiblePositions);
      } catch (NullPointerException er) {
        er.printStackTrace();
      }
    }

    if ((firstPosition != RecyclerView.NO_POSITION || lastPosition != RecyclerView.NO_POSITION)
        && firstPosition <= lastPosition) { // for sure
      for (int i = firstPosition; i <= lastPosition; i++) {
        // detected a view holder for video player
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
        if (viewHolder != null && viewHolder instanceof ToroPlayer) {
          video = (ToroPlayer) viewHolder;
          // check that view position
          if (video.wantsToPlay() && video.isAbleToPlay() &&
              Toro.getStrategy().allowsToPlay(video, recyclerView)) {
            if (!candidates.contains(video)) {
              candidates.add(video);
            }
          }
        }
      }
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
        lastVideo.onPlaybackStarted();
      }
      return;
    }

    // Pause last video
    if (lastVideo != null) {
      mManager.saveVideoState(lastVideo.getVideoId(), lastVideo.getCurrentPosition(),
          lastVideo.getDuration());
      if (lastVideo.isPlaying()) {
        mManager.pausePlayback();
        lastVideo.onPlaybackPaused();
      }
    }

    // Switch video
    lastVideo = video;
    mLastVideoPosition = videoPosition;

    mManager.setPlayer(lastVideo);
    mManager.restoreVideoState(lastVideo.getVideoId());
    mManager.startPlayback();
    lastVideo.onPlaybackStarted();
  }
}
