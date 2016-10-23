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

package im.ene.toro.sample.feature.facebook.playlist;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.MediaPlayerManager;
import im.ene.toro.ToroAdapter;
import im.ene.toro.ToroPlayer;
import im.ene.toro.MediaPlayerManagerImpl;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 10/13/16.
 */

public class MoreVideosAdapter extends ToroAdapter<ToroAdapter.ViewHolder>
    implements MediaPlayerManager {

  static final int VIDEO_TYPE_FIRST = 0;
  static final int VIDEO_TYPE_NEXT = 1;

  private final List<TimelineItem.VideoItem> items;
  private final MediaPlayerManager delegate;

  public MoreVideosAdapter(@NonNull TimelineItem.VideoItem firstItem) {
    this.items = new ArrayList<>();
    this.items.add(firstItem);
    this.delegate = new MediaPlayerManagerImpl();
  }

  @Nullable @Override protected TimelineItem.VideoItem getItem(int position) {
    return items.get(position);
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View view;
    final ViewHolder viewHolder;
    if (viewType == VIDEO_TYPE_FIRST) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(FirstItemViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new FirstItemViewHolder(view);
    } else {
      view = LayoutInflater.from(parent.getContext())
          .inflate(NextItemViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new NextItemViewHolder(view);
    }

    // TODO Setup click listener
    return viewHolder;
  }

  @Override public int getItemViewType(int position) {
    return position == 0 ? VIDEO_TYPE_FIRST : VIDEO_TYPE_NEXT;
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public void addAll(List<TimelineItem.VideoItem> newItems) {
    synchronized (this) {
      int oldLen = getItemCount();
      this.items.addAll(newItems);
      notifyItemRangeInserted(oldLen, newItems.size());
    }
  }

  // MediaPlayerManager implementation

  @Nullable @Override public ToroPlayer getPlayer() {
    return delegate.getPlayer();
  }

  @Override public void setPlayer(ToroPlayer player) {
    delegate.setPlayer(player);
  }

  @Override public void onRegistered() {
    delegate.onRegistered();
  }

  @Override public void onUnregistered() {
    delegate.onUnregistered();
  }

  @Override public void startPlayback() {
    delegate.startPlayback();
  }

  @Override public void pausePlayback() {
    delegate.pausePlayback();
  }

  @Override public void stopPlayback() {
    delegate.stopPlayback();
  }

  @Override public void saveVideoState(String videoId, @Nullable Long position, long duration) {
    delegate.saveVideoState(videoId, position, duration);
  }

  @Override public void restoreVideoState(String videoId) {
    delegate.restoreVideoState(videoId);
  }

  @Nullable @Override public Long getSavedPosition(String videoId) {
    return delegate.getSavedPosition(videoId);
  }
}
