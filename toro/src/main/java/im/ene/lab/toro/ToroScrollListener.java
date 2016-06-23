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
    final ToroPlayer currentPlayer = playerManager.getPlayer();
    if (currentPlayer != null && currentPlayer.getPlayOrder() != RecyclerView.NO_POSITION) {
      if (currentPlayer.wantsToPlay() && Toro.getStrategy().allowsToPlay(currentPlayer, parent)) {
        candidates.add(currentPlayer);
      }
    }

    ToroPlayer candidate;
    int firstPosition = RecyclerView.NO_POSITION;
    int lastPosition = RecyclerView.NO_POSITION;

    // Find visible positions range
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

      List<Integer> firstVisiblePositions = ToroUtil.asList(firstVisibleItemPositions);
      List<Integer> lastVisiblePositions = ToroUtil.asList(lastVisibleItemPositions);

      firstPosition = Collections.min(firstVisiblePositions);
      lastPosition = Collections.max(lastVisiblePositions);
    } else if (parent.getLayoutManager() instanceof ToroLayoutManager) {
      ToroLayoutManager layoutManager = (ToroLayoutManager) parent.getLayoutManager();
      firstPosition = layoutManager.getFirstVisibleItemPosition();
      lastPosition = layoutManager.getLastVisibleItemPosition();
    }

    if (firstPosition <= lastPosition /* don't screw up the 'for' loop */ &&  //
        (firstPosition != RecyclerView.NO_POSITION || lastPosition != RecyclerView.NO_POSITION)) {
      for (int i = firstPosition; i <= lastPosition; i++) {
        // Detected a view holder for video player
        RecyclerView.ViewHolder viewHolder = parent.findViewHolderForAdapterPosition(i);
        if (viewHolder != null && viewHolder instanceof ToroPlayer) {
          candidate = (ToroPlayer) viewHolder;
          // check candidate's view position
          if (candidate.wantsToPlay() && Toro.getStrategy().allowsToPlay(candidate, parent)) {
            // Have a new candidate who can play
            if (!candidates.contains(candidate)) {
              candidates.add(candidate);
            }
          }
        }
      }
    }

    // Ask strategy to elect one
    final ToroPlayer electedPlayer = Toro.getStrategy().findBestPlayer(candidates);

    if (electedPlayer == currentPlayer) {
      // No thing changes, no new President.
      if (currentPlayer != null && !currentPlayer.isPlaying()) {
        playerManager.restoreVideoState(currentPlayer.getVideoId());
        playerManager.startPlayback();
        currentPlayer.onPlaybackStarted();
      }
      return;
    }

    // Current player is not elected anymore, pause it.
    if (currentPlayer != null) {
      playerManager.saveVideoState(currentPlayer.getVideoId(), currentPlayer.getCurrentPosition(),
          currentPlayer.getDuration());
      playerManager.pausePlayback();
      currentPlayer.onPlaybackPaused();
    }

    if (electedPlayer == null) {
      // Old president resigned, there is no new one, we are screwed up, get out of here.
      return;
    }

    // Well... let's the BlackHouse starts new cycle with the new President!
    playerManager.setPlayer(electedPlayer);
    playerManager.restoreVideoState(electedPlayer.getVideoId());
    playerManager.startPlayback();
    electedPlayer.onPlaybackStarted();
  }
}
