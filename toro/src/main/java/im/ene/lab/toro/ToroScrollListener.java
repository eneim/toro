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
final class ToroScrollListener extends RecyclerView.OnScrollListener {

  @NonNull private final VideoPlayerManager mManager;

  ToroScrollListener(@NonNull VideoPlayerManager manager) {
    this.mManager = manager;
  }

  @NonNull final VideoPlayerManager getManager() {
    return mManager;
  }

  @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
      return;
    }

    List<ToroPlayer> candidates = new ArrayList<>();
    // Check current playing position
    final ToroPlayer currentVideo = mManager.getPlayer();
    if (currentVideo != null && currentVideo.getPlayOrder() != RecyclerView.NO_POSITION) {
      RecyclerView.ViewHolder viewHolder =
          recyclerView.findViewHolderForLayoutPosition(currentVideo.getPlayOrder());
      // Re-calculate the rectangles
      if (viewHolder != null) {
        if (currentVideo.wantsToPlay() && currentVideo.isAbleToPlay() &&
            Toro.getStrategy().allowsToPlay(currentVideo, recyclerView)) {
          candidates.add(currentVideo);
        }
      }
    }

    ToroPlayer candidate;
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

      // StaggeredGridLayoutManager can have many rows ...
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

    if (firstPosition <= lastPosition &&  // don't want to screw up the for loop
        (firstPosition != RecyclerView.NO_POSITION || lastPosition != RecyclerView.NO_POSITION)) {
      for (int i = firstPosition; i <= lastPosition; i++) {
        // detected a view holder for video player
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
        if (viewHolder != null && viewHolder instanceof ToroPlayer) {
          candidate = (ToroPlayer) viewHolder;
          // check that view position
          if (candidate.wantsToPlay() && candidate.isAbleToPlay() &&
              Toro.getStrategy().allowsToPlay(candidate, recyclerView)) {
            // Have a new candidate who wants to play
            if (!candidates.contains(candidate)) {
              candidates.add(candidate);
            }
          }
        }
      }
    }

    // Ask strategy to elect one
    final ToroPlayer electedPlayer = Toro.getStrategy().findBestPlayer(candidates);

    if (electedPlayer == null) {
      // There is no good one, bye
      return;
    }

    // From here, we have a candidate to playback
    // Actually we added current video into candidate list too
    if (currentVideo != null && electedPlayer.getPlayOrder() == currentVideo.getPlayOrder()) {
      // Nothing changes, keep going
      if (!currentVideo.isPlaying()) {
        mManager.startPlayback();
        currentVideo.onPlaybackStarted();
      }
      return;
    }

    // Current player is not elected, it must resign ...
    if (currentVideo != null) {
      if (currentVideo.isPlaying()) {
        mManager.saveVideoState(currentVideo.getVideoId(), currentVideo.getCurrentPosition(),
            currentVideo.getDuration());
        mManager.pausePlayback();
        currentVideo.onPlaybackPaused();
      }
    }

    // New president!
    mManager.setPlayer(electedPlayer);
    mManager.restoreVideoState(electedPlayer.getVideoId());
    mManager.startPlayback();
    electedPlayer.onPlaybackStarted();
  }
}
