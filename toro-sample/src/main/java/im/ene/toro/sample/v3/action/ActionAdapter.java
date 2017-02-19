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

package im.ene.toro.sample.v3.action;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.ToroAdapter;

/**
 * Created by eneim on 2/18/17.
 */

public class ActionAdapter extends ToroAdapter<ActionViewHolder> {

  ActionClickListener actionClickListener;

  public void setActionClickListener(ActionClickListener actionClickListener) {
    this.actionClickListener = actionClickListener;
  }

  @Nullable @Override protected Object getItem(int position) {
    return Action.values()[position];
  }

  @Override public ActionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(ActionViewHolder.LAYOUT_RES, parent, false);
    final ActionViewHolder viewHolder = new ActionViewHolder(view);
    viewHolder.actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        int pos = viewHolder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && actionClickListener != null) {
          actionClickListener.onActionClick(v, Action.values()[pos]);
        }
      }
    });
    return viewHolder;
  }

  @Override public int getItemCount() {
    return Action.values().length;
  }

  public interface ActionClickListener {

    void onActionClick(View view, Action action);
  }
}
