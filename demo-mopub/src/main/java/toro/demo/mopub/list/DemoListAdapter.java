/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package toro.demo.mopub.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import toro.demo.mopub.R;

/**
 * @author eneim (2018/03/13).
 */

public class DemoListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

  static final int TYPE_TEXT = 10;
  static final int TYPE_VIDEO = 30;

  private LayoutInflater inflater;

  @NonNull @Override
  public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (inflater == null || inflater.getContext() != parent.getContext()) {
      inflater = LayoutInflater.from(parent.getContext());
    }

    return viewType == TYPE_VIDEO ? //
        new VideoViewHolder(parent, inflater, R.layout.view_holder_video)
        : new TextViewHolder(parent, inflater, R.layout.view_holder_text);
  }

  @Override public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    holder.bind("This is a demo!");
  }

  @Override public int getItemCount() {
    return Integer.MAX_VALUE;
  }

  @Override public int getItemViewType(int position) {
    return position % 3 == 0 ? TYPE_TEXT : TYPE_VIDEO;
  }
}
