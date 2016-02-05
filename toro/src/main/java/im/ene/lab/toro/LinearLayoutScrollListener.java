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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 1/31/16.
 *
 * @hide
 */
public final class LinearLayoutScrollListener extends ToroScrollListener {

  private int mLastVideoPosition;

  public LinearLayoutScrollListener(@NonNull VideoPlayerManager manager) {
    super(manager);
    mLastVideoPosition = -1;
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
          // check that view position
          if (video.wantsToPlay() && video.isAbleToPlay() &&
              Toro.getStrategy().allowsToPlay(video, recyclerView)) {
            if (!candidates.contains(video)) {
              candidates.add(video);
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
}
