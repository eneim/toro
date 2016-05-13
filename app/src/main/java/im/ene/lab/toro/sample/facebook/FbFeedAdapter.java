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

package im.ene.lab.toro.sample.facebook;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.ToroAdapter;

/**
 * Created by eneim on 5/13/16.
 */
public class FbFeedAdapter extends ToroAdapter<ToroAdapter.ViewHolder> {

  @Nullable @Override protected Object getItem(int position) {
    return (int) (Math.random() * 512);
  }

  @Override public int getItemViewType(int position) {
    return (int) getItem(position) % 4 == 1 ? (Math.random() > 0.35
        ? FbItemViewHolder.POST_TYPE_PHOTO : FbItemViewHolder.POST_TYPE_VIDEO)
        : FbItemViewHolder.POST_TYPE_TEXT;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, @FbItemViewHolder.PostType int viewType) {
    final ViewHolder viewHolder = FbItemViewHolder.createViewHolder(parent, viewType);
    if (viewHolder instanceof FbItemViewHolder.VideoPost) {
      viewHolder.setOnItemClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          // TODO
        }
      });
    }

    return viewHolder;
  }

  @Override public int getItemCount() {
    return 512; // magic number
  }
}
