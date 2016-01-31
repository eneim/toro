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

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by eneim on 1/31/16.
 *
 * @hide
 */
public final class RecyclerViewLinearScrollListener extends RecyclerViewScrollListener {

  private int mLastVideoPosition = -1;
  private ToroPlayer mLastVideo = null;
  private LinearLayoutManager mLayoutManager = null;

  private Rect mParentRect = new Rect();
  private Rect mChildRect = new Rect();

  public RecyclerViewLinearScrollListener(@NonNull ToroManager manager) {
    super(manager);
  }

  @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
      return;
    }

    // Check current playing position
    if (mLastVideo != null) {
      RecyclerView.ViewHolder viewHolder =
          recyclerView.findViewHolderForLayoutPosition(mLastVideoPosition);
      // Re-calculate the rectangles
      if (viewHolder != null) {
        recyclerView.getLocalVisibleRect(mParentRect);
        viewHolder.itemView.getLocalVisibleRect(mChildRect);
        // TODO Playing policy?
        if (mLastVideo.wantsToPlay(mParentRect, mChildRect)) {  // Current view keep playing
          return;
        }
      }
    }

    // We require current layout manager to be a LinearLayoutManager.
    if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
      return;
    }

    mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    int firstPosition = mLayoutManager.findFirstVisibleItemPosition();
    int lastPosition = mLayoutManager.findLastVisibleItemPosition();
    int videoPosition = -1;
    if ((firstPosition != RecyclerView.NO_POSITION || lastPosition != RecyclerView.NO_POSITION)
        && firstPosition <= lastPosition) { // for sure
      ToroPlayer video = null;
      for (int i = firstPosition; i <= lastPosition; i++) {
        // detected a view holder for video player
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
        if (viewHolder != null && viewHolder instanceof ToroPlayer) {
          video = (ToroPlayer) viewHolder;
          // Re-calculate the rectangles
          recyclerView.getLocalVisibleRect(mParentRect);
          viewHolder.itemView.getLocalVisibleRect(mChildRect);
          // check that view position
          if (video.wantsToPlay(mParentRect, mChildRect)) {
            videoPosition = i;
            break;
          } else {
            mManager.saveVideoState(video.getVideoId(),
                video.getCurrentPosition(), video.getDuration());
            mManager.pauseVideo(video);
            video = null;
          }
        }
      }

      if (videoPosition == mLastVideoPosition) {  // Nothing changes, keep going
        if (mLastVideo != null && !mLastVideo.isPlaying()) {
          mManager.startVideo(mLastVideo);
        }
        return;
      }

      if (video != null) {
        if (mLastVideo != null && mLastVideo.isPlaying()) {
          mManager.saveVideoState(mLastVideo.getVideoId(),
              mLastVideo.getCurrentPosition(), mLastVideo.getDuration());
          mManager.pauseVideo(mLastVideo);
        }
        // Switch video
        mLastVideo = video;
        mLastVideoPosition = videoPosition;

        mManager.setPlayer(mLastVideo);
        mManager.restoreVideoState(mLastVideo, mLastVideo.getVideoId());
        mManager.startVideo(mLastVideo);
      }
    }
  }
}
