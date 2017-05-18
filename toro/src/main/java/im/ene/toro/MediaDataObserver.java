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

/**
 * Created by eneim on 2/18/17.
 *
 * @since 3.0.0, on-going work
 */
final class MediaDataObserver extends RecyclerView.AdapterDataObserver implements Removable {

  @SuppressWarnings("unused") private static final String TAG = Toro.TAG + "@Observer";
  private PlayerManager manager;

  MediaDataObserver(@NonNull PlayerManager manager) {
    this.manager = manager;
  }

  @Override public void onChanged() {
    super.onChanged();
    // placeholder: v3
    // TODO implement me
  }

  @Override public void onItemRangeChanged(int positionStart, int itemCount) {
    super.onItemRangeChanged(positionStart, itemCount);
    // placeholder: v3
    // TODO implement me
  }

  @Override public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
    super.onItemRangeChanged(positionStart, itemCount, payload);
    // placeholder: v3
    // TODO implement me
  }

  @Override public void onItemRangeInserted(int positionStart, int itemCount) {
    super.onItemRangeInserted(positionStart, itemCount);
    // placeholder: v3
    // TODO implement me
  }

  @Override public void onItemRangeRemoved(int positionStart, int itemCount) {
    super.onItemRangeRemoved(positionStart, itemCount);
    // placeholder: v3
    // TODO implement me
  }

  @Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
    // placeholder: v3
    // TODO implement me
  }

  @Override public void remove() throws Exception {
    if (manager != null && ((RecyclerView.Adapter) manager).hasObservers()) {
      ((RecyclerView.Adapter) manager).unregisterAdapterDataObserver(this);
      manager = null;
    }
  }
}
