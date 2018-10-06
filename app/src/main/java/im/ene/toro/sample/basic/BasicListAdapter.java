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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.widget.PressablePlayerSelector;
import toro.v4.Media;
import toro.v4.MediaHub;
import toro.v4.PreLoader;

/**
 * @author eneim (7/1/17).
 */

class BasicListAdapter extends RecyclerView.Adapter<BasicPlayerViewHolder> implements PreLoader {

  private static final String TAG = "Toro:Demo:Basic";

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") //
  private MediaList mediaList = new MediaList();

  @Nullable private final PressablePlayerSelector selector;
  private final MediaHub mediaHub;

  BasicListAdapter(@Nullable PressablePlayerSelector selector) {
    this.selector = selector;
    this.mediaHub = null;
  }

  BasicListAdapter(@Nullable PressablePlayerSelector selector, MediaHub mediaHub) {
    this.selector = selector;
    this.mediaHub = mediaHub;
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

  // PreLoader implementation

  @Override public void prepareAround(int beforeOrder, int afterOrder, int limit) {
    Log.w(TAG, "prepareAround() called with: beforeOrder = ["
        + beforeOrder
        + "], afterOrder = ["
        + afterOrder
        + "], limit = ["
        + limit
        + "]");
    //if (mediaHub == null) return;
    //Media media = new Media(this.mediaList.get(afterOrder).mediaUri, null);
    //if (!mediaHub.didQueue(media)) {
    //  mediaHub.queue(media);
    //}
  }
}
