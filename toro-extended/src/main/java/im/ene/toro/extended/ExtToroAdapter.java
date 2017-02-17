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
import im.ene.toro.BaseAdapter;
import im.ene.toro.ToroAdapter;
import im.ene.toro.ToroPlayer;

/**
 * Created by eneim on 10/7/16.
 */

public abstract class ExtToroAdapter<VH extends ToroAdapter.ViewHolder> extends BaseAdapter<VH> {

  public static final int INVALID_VIDEO_POSITION = -1;

  private RecyclerView parent;

  public ExtToroAdapter() {

  }

  @Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    this.parent = recyclerView;
  }

  @Override public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    // TODO Do some tearing down jobs before setting the parent View to null
    this.parent = null;
  }

  @Nullable private RecyclerView.ViewHolder findViewHolderForPosition(int position) {
    return parent == null ? null : parent.findViewHolderForAdapterPosition(position);
  }

  private void scrollToPosition(int position) {
    if (parent != null) {
      parent.smoothScrollToPosition(position);
    }
  }

  private int findNextVideoPosition() {
    int currentVideoPosition = getPlayer() == null ?  //
        INVALID_VIDEO_POSITION : getPlayer().getPlayOrder();

    do {
      currentVideoPosition++;
    } while (currentVideoPosition < getItemCount() && //
        !(findViewHolderForPosition(currentVideoPosition) instanceof ToroPlayer));

    return currentVideoPosition < getItemCount() ? currentVideoPosition : INVALID_VIDEO_POSITION;
  }

  // Comment out, Un-comment if need
  //final ToroPlayer findNextPlayer() {
  //  int nextVideoPosition = findNextVideoPosition();
  //  return nextVideoPosition == INVALID_VIDEO_POSITION ? null
  //      : (ToroPlayer) findViewHolderForPosition(nextVideoPosition);
  //}

  final void scrollToNextVideo() {
    int nextVideoPosition = findNextVideoPosition();
    if (nextVideoPosition != INVALID_VIDEO_POSITION) {
      scrollToPosition(nextVideoPosition);
    }
  }

  @Override public void remove() throws Exception {
    super.remove();
    this.parent = null;
  }
}
