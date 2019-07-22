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

package im.ene.toro.sample.complex;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author eneim (7/1/17).
 */

class ComplexListAdapter extends RecyclerView.Adapter<ComplexPlayerViewHolder> {

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") //
  private MediaList mediaList = new MediaList();

  @NonNull @Override public ComplexPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(ComplexPlayerViewHolder.LAYOUT_RES, parent, false);
    return new ComplexPlayerViewHolder(view);
  }

  @Override public void onBindViewHolder(@NonNull ComplexPlayerViewHolder holder, int position) {
    holder.bind(mediaList.get(position), position);
  }

  @Override public int getItemCount() {
    return mediaList.size();
  }
}
