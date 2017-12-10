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

package im.ene.toro.youtube;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import im.ene.toro.CacheManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eneim (2017/11/23).
 */

class YouTubePlaylistAdapter extends RecyclerView.Adapter<PlaylistItemViewHolder>
    implements CacheManager {

  private final YouTubePlayerManager manager;
  private VideoListResponse data = new VideoListResponse();

  YouTubePlaylistAdapter(YouTubePlayerManager playerManager) {
    super();
    this.manager = playerManager;
  }

  public void setData(VideoListResponse data) {
    this.data = data;
    notifyDataSetChanged();
  }

  @Override public PlaylistItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(PlaylistItemViewHolder.LAYOUT_RES, parent, false);
    return new PlaylistItemViewHolder(manager, view);
  }

  @Override public void onBindViewHolder(PlaylistItemViewHolder holder, int position) {
    holder.bind(getItem(position));
  }

  private Video getItem(int position) {
    return getItems().get(position);
  }

  private List<Video> getItems() {
    List<Video> items = data.getItems();
    if (items == null) items = new ArrayList<>();
    return items;
  }

  @Override public int getItemCount() {
    return getItems().size();
  }

  /// CacheManager implementation

  @Nullable @Override public Object getKeyForOrder(int order) {
    return order < 0 ? null : getItem(order);
  }

  @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
    return key instanceof Video ? getItems().indexOf(key) : null;
  }
}

