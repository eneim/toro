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

package im.ene.toro.sample.v3;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.BaseAdapter;
import im.ene.toro.PlaybackState;
import im.ene.toro.sample.data.SimpleVideoObject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by eneim on 2/9/17.
 */

public class MediaListAdapter extends BaseAdapter<MediaItemViewHolder> {

  // cache data, mimic a data source
  private final Map<Integer, SimpleVideoObject> items = new LinkedHashMap<>();

  @SuppressWarnings("WeakerAccess") ItemClickHandler itemClickHandler;

  public void setItemClickHandler(ItemClickHandler itemClickHandler) {
    this.itemClickHandler = itemClickHandler;
  }

  @Nullable @Override protected Object getItem(int position) {
    SimpleVideoObject item = items.get(position);
    if (item == null) {
      item = new SimpleVideoObject("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
      items.put(position, item);
    }

    return item;
  }

  @Override public MediaItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(MediaItemViewHolder.LAYOUT_RES, parent, false);
    final MediaItemViewHolder viewHolder = new MediaItemViewHolder(view);
    viewHolder.setOnItemClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        // open full screen player?
        int pos = viewHolder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && itemClickHandler != null) {
          itemClickHandler.onItemClick(MediaListAdapter.this, viewHolder, v, pos, getItemId(pos));
        }
      }
    });
    return viewHolder;
  }

  @Override public int getItemCount() {
    return Integer.MAX_VALUE;
  }

  static abstract class ItemClickHandler implements OnItemClickListener {

    public abstract void openVideoPlayer(View view, SimpleVideoObject source, PlaybackState state);

    @Override
    public void onItemClick(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder,
        View view, int position, long itemId) {
      if (viewHolder instanceof MediaItemViewHolder) {
        MediaItemViewHolder vh = (MediaItemViewHolder) viewHolder;
        PlaybackState state =
            new PlaybackState(vh.getMediaId(), vh.getDuration(), vh.getCurrentPosition());
        openVideoPlayer(view, (SimpleVideoObject) ((MediaListAdapter) adapter).getItem(position),
            state);
      }
    }
  }
}
