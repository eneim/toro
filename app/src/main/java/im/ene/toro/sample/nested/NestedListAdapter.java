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

package im.ene.toro.sample.nested;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import im.ene.toro.CacheManager;

/**
 * @author eneim (7/1/17).
 */

public class NestedListAdapter extends RecyclerView.Adapter<BaseViewHolder>
    implements CacheManager {

  private static final int MEDIA_LIST_POSITION = 3;

  private LayoutInflater inflater;
  private final MediaList mediaList = new MediaList();

  @NonNull @Override public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (inflater == null || inflater.getContext() != parent.getContext()) {
      inflater = LayoutInflater.from(parent.getContext());
    }

    final View view;
    final BaseViewHolder viewHolder;
    if (viewType == 1000) {
      view = inflater.inflate(MediaListViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new MediaListViewHolder(view);
    } else {
      view = inflater.inflate(TextViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new TextViewHolder(view);
    }

    return viewHolder;
  }

  @Override public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    holder.bind(position, position == MEDIA_LIST_POSITION ? mediaList : getClass().getSimpleName());
  }

  @Override public int getItemViewType(int position) {
    return position == MEDIA_LIST_POSITION ? 1000 : 1;  // 1000 --> MediaList
  }

  @Override public int getItemCount() {
    return Integer.MAX_VALUE;
  }

  @Override public void onViewDetachedFromWindow(@NonNull BaseViewHolder holder) {
    super.onViewDetachedFromWindow(holder);
    if (holder instanceof MediaListViewHolder) {
      ((MediaListViewHolder) holder).onDetached();
    }
  }

  @Override public void onViewAttachedToWindow(@NonNull BaseViewHolder holder) {
    super.onViewAttachedToWindow(holder);
    if (holder instanceof MediaListViewHolder) {
      ((MediaListViewHolder) holder).onAttached();
    }
  }

  //// State manager

  @NonNull @Override public Object getKeyForOrder(int order) {
    return order;
  }

  @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
    return key instanceof Integer ? (Integer) key : null;
  }
}
