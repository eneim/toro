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

  private final VideoPlayerManager playerManager;
  private final List<ToroPlayer> candidates;

  ToroScrollListener(@NonNull VideoPlayerManager manager) {
    this.playerManager = manager;
    this.candidates = new ArrayList<>();
  }

  @NonNull final VideoPlayerManager getManager() {
    return playerManager;
  }

  @Override public void onScrollStateChanged(RecyclerView parent, int newState) {
    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
      return;
    }

    // clear current playback candidates
    candidates.clear();
    // Check current playing position
    final ToroPlayer currentVideo = playerManager.getPlayer();
    if (currentVideo != null && currentVideo.getPlayOrder() != RecyclerView.NO_POSITION) {
      if (currentVideo.wantsToPlay() && Toro.getStrategy().allowsToPlay(currentVideo, parent)) {
        candidates.add(currentVideo);
      }
    }

    ToroPlayer candidate;
    int firstPosition = RecyclerView.NO_POSITION;
    int lastPosition = RecyclerView.NO_POSITION;

    // Find visible positions bound
    if (parent.getLayoutManager() instanceof LinearLayoutManager) {
      LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
      firstPosition = layoutManager.findFirstVisibleItemPosition();
      lastPosition = layoutManager.findLastVisibleItemPosition();
    } else if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager) {
      StaggeredGridLayoutManager layoutManager =
          (StaggeredGridLayoutManager) parent.getLayoutManager();

      // StaggeredGridLayoutManager can have many rows ...
      int[] firstVisibleItemPositions = layoutManager.findFirstVisibleItemPositions(null);
      int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null);

      List<Integer> firstVisiblePositions = Util.asList(firstVisibleItemPositions);
      List<Integer> lastVisiblePositions = Util.asList(lastVisibleItemPositions);

      firstPosition = Collections.min(firstVisiblePositions);
      lastPosition = Collections.max(lastVisiblePositions);
    } else if (parent.getLayoutManager() instanceof ToroLayoutManager) {
      ToroLayoutManager layoutManager = (ToroLayoutManager) parent.getLayoutManager();
      firstPosition = layoutManager.getFirstVisibleItemPosition();
      lastPosition = layoutManager.getLastVisibleItemPosition();
    }

    if (firstPosition <= lastPosition &&  // don't screw up the 'for' loop
        (firstPosition != RecyclerView.NO_POSITION || lastPosition != RecyclerView.NO_POSITION)) {
      for (int i = firstPosition; i <= lastPosition; i++) {
        // Detected a view holder for video player
        RecyclerView.ViewHolder viewHolder = parent.findViewHolderForAdapterPosition(i);
        if (viewHolder != null && viewHolder instanceof ToroPlayer) {
          candidate = (ToroPlayer) viewHolder;
          // check that view position
          if (candidate.wantsToPlay() && Toro.getStrategy().allowsToPlay(candidate, parent)) {
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

    if (electedPlayer == currentVideo) {
      // No thing changes, no new President.
      if (currentVideo != null && !currentVideo.isPlaying()) {
        playerManager.restoreVideoState(currentVideo.getVideoId());
        playerManager.startPlayback();
        currentVideo.onPlaybackStarted();
      }
      return;
    }

    // Current player is not elected anymore, pause it.
    if (currentVideo != null) {
      playerManager.saveVideoState(currentVideo.getVideoId(), currentVideo.getCurrentPosition(),
          currentVideo.getDuration());
      playerManager.pausePlayback();
      currentVideo.onPlaybackPaused();
    }

    if (electedPlayer == null) {
      // There is no new President, we are screw up, get out of here.
      return;
    }

    // Well...!
    playerManager.setPlayer(electedPlayer);
    playerManager.restoreVideoState(electedPlayer.getVideoId());
    playerManager.startPlayback();
    electedPlayer.onPlaybackStarted();
  }
}
