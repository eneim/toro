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

package im.ene.lab.toro.sample.presentation.youtube;

import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import im.ene.lab.toro.ext.YouTubeVideosAdapter;

/**
 * Created by eneim on 7/6/16.
 */
public class MyYouTubeVideosAdapter extends YouTubeVideosAdapter<MyYouTubeItemViewHolder> {

  private PlaylistItemListResponse response;

  public MyYouTubeVideosAdapter(FragmentManager fragmentManager,
      PlaylistItemListResponse response) {
    super(fragmentManager);
    this.response = response;
  }

  @Override
  public MyYouTubeItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(MyYouTubeItemViewHolder.LAYOUT_RES, parent, false);
    return new MyYouTubeItemViewHolder(view, this);
  }

  @Override public int getItemCount() {
    return response != null && response.getItems() != null ? response.getItems().size() : 0;
  }

  public YouTubeVideoItem getItem(int position) {
    return getItemCount() > position ? new YouTubeVideoItem(response.getItems().get(position)) : null;
  }
}
