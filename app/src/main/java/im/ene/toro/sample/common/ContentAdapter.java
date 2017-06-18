/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.common;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.google.android.exoplayer2.C;
import im.ene.toro.sample.data.Entity;
import im.ene.toro.sample.data.MediaItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author eneim | 6/7/17.
 */

public class ContentAdapter extends RecyclerView.Adapter<BaseViewHolder> {

  static final int TYPE_TEXT = 1 << 1;   // non-trivial type, just for fun

  static final int TYPE_MEDIA = 1 << 2;  // non-trivial type, just for fun

  static final int TYPE_MEDIA_MP4 = 1 << 3; // for VideoView sample.

  protected final List<Entity> entities = new ArrayList<>();

  @Override public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return BaseViewHolder.createViewHolder(parent, viewType);
  }

  @Override public void onBindViewHolder(BaseViewHolder holder, int position) {
    holder.bind(this, entities.get(position), null);
  }

  @Override
  public void onBindViewHolder(BaseViewHolder holder, int position, List<Object> payloads) {
    holder.bind(this, entities.get(position), payloads);
  }

  @Override public int getItemViewType(int position) {
    Entity item = this.entities.get(position);
    return item instanceof MediaItem ? (((MediaItem) item).inferContentType() == C.TYPE_OTHER
        ? TYPE_MEDIA_MP4 : TYPE_MEDIA) : TYPE_TEXT;
  }

  @Override public int getItemCount() {
    return entities.size();
  }

  public void addMany(boolean reset, @NonNull List<Entity> items) {
    if (reset) {
      notifyItemRangeRemoved(0, this.entities.size());
      this.entities.clear();  // clear after the notify
    }

    int oldCount = getItemCount();
    if (this.entities.addAll(items)) {
      notifyItemRangeInserted(oldCount, items.size());
    }
  }

  protected final Entity getItem(int position) {
    return this.entities.get(position);
  }

  /// Item Move

  public boolean onItemMoved(int fromPos, int toPos) {
    if (fromPos == toPos) return false;
    if (fromPos < toPos) {
      for (int i = fromPos; i < toPos; i++) {
        Collections.swap(this.entities, i, i + 1);
      }
    } else {
      for (int i = fromPos; i > toPos; i--) {
        Collections.swap(this.entities, i, i - 1);
      }
    }
    notifyItemMoved(fromPos, toPos);
    return true;
  }
}
