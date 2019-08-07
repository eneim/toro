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

package im.ene.toro.sample.basic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import im.ene.toro.widget.PressablePlayerSelector;

/**
 * @author eneim (7/1/17).
 */

class BasicListAdapter extends RecyclerView.Adapter<BasicPlayerViewHolder> {

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") //
  private MediaList mediaList = new MediaList();

  @Nullable private final PressablePlayerSelector selector;

  BasicListAdapter(@Nullable PressablePlayerSelector selector) {
    this.selector = selector;
  }

  @NonNull @Override
  public BasicPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(BasicPlayerViewHolder.LAYOUT_RES, parent, false);
    BasicPlayerViewHolder viewHolder = new BasicPlayerViewHolder(view, this.selector);
    if (this.selector != null) viewHolder.itemView.setOnLongClickListener(this.selector);
    return viewHolder;
  }

  @Override public void onBindViewHolder(@NonNull BasicPlayerViewHolder holder, int position) {
    holder.bind(mediaList.get(position));
  }

  @Override public int getItemCount() {
    return mediaList.size();
  }
}
