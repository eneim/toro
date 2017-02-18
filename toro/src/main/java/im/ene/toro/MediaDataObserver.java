/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by eneim on 2/18/17.
 */

final class MediaDataObserver extends RecyclerView.AdapterDataObserver implements Removable {

  private static final String TAG = Toro.TAG + "@Observer";
  final RecyclerView.Adapter adapter;

  MediaDataObserver(@NonNull RecyclerView.Adapter adapter) {
    this.adapter = adapter;
  }

  @Override public void onChanged() {
    super.onChanged();
    Log.d(TAG, "onChanged() called");
  }

  @Override public void onItemRangeChanged(int positionStart, int itemCount) {
    super.onItemRangeChanged(positionStart, itemCount);
    Log.d(TAG, "onItemRangeChanged() called with: positionStart = ["
        + positionStart
        + "], itemCount = ["
        + itemCount
        + "]");
  }

  @Override public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
    super.onItemRangeChanged(positionStart, itemCount, payload);
    Log.d(TAG, "onItemRangeChanged() called with: positionStart = ["
        + positionStart
        + "], itemCount = ["
        + itemCount
        + "], payload = ["
        + payload
        + "]");
  }

  @Override public void onItemRangeInserted(int positionStart, int itemCount) {
    super.onItemRangeInserted(positionStart, itemCount);
    Log.d(TAG, "onItemRangeInserted() called with: positionStart = ["
        + positionStart
        + "], itemCount = ["
        + itemCount
        + "]");
  }

  @Override public void onItemRangeRemoved(int positionStart, int itemCount) {
    super.onItemRangeRemoved(positionStart, itemCount);
    Log.d(TAG, "onItemRangeRemoved() called with: positionStart = ["
        + positionStart
        + "], itemCount = ["
        + itemCount
        + "]");
  }

  @Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
    Log.d(TAG, "onItemRangeMoved() called with: fromPosition = ["
        + fromPosition
        + "], toPosition = ["
        + toPosition
        + "], itemCount = ["
        + itemCount
        + "]");
  }

  @Override public void remove() throws Exception {
    if (adapter.hasObservers()) {
      adapter.unregisterAdapterDataObserver(this);
    }
  }
}
