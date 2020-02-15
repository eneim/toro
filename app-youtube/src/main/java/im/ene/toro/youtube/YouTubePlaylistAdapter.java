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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import java.util.ArrayList;
import java.util.List;

import static im.ene.toro.ToroUtil.checkNotNull;

/**
 * @author eneim (2017/11/23).
 */

final class YouTubePlaylistAdapter extends Adapter<YouTubeVideoViewHolder> {

  private final YouTubePlayerManager manager;
  private VideoListResponse data = new VideoListResponse();
  private LayoutInflater inflater;

  YouTubePlaylistAdapter(YouTubePlayerManager playerManager) {
    super();
    this.manager = checkNotNull(playerManager);
  }

  public void setData(VideoListResponse data) {
    this.data = data;
    notifyDataSetChanged();
  }

  @NonNull @Override
  public YouTubeVideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (inflater == null || inflater.getContext() != parent.getContext()) {
      inflater = LayoutInflater.from(parent.getContext());
    }
    View view = inflater.inflate(YouTubeVideoViewHolder.LAYOUT_RES, parent, false);
    return new YouTubeVideoViewHolder(manager, view);
  }

  @Override public void onBindViewHolder(@NonNull YouTubeVideoViewHolder holder, int position) {
    holder.bind(getItem(position));
  }

  @SuppressWarnings("WeakerAccess") @NonNull Video getItem(int position) {
    return getItems().get(position);
  }

  @NonNull private List<Video> getItems() {
    List<Video> items = data.getItems();
    if (items == null) items = new ArrayList<>();
    return items;
  }

  @Override public int getItemCount() {
    return getItems().size();
  }
}

