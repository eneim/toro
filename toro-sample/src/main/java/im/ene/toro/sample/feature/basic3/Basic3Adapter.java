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

package im.ene.toro.sample.feature.basic3;

import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.Toro;
import im.ene.toro.ToroAdapter;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.SimpleObject;
import im.ene.toro.sample.data.SimpleVideoObject;

/**
 * Created by eneim on 6/29/16.
 */
public class Basic3Adapter extends ToroAdapter<ToroAdapter.ViewHolder> {

  private static final String TAG = "ExoPlayer2Adapter";

  public Basic3Adapter() {
    super();
    setHasStableIds(true);  // MUST have this.
  }

  @Override public ToroAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
    final View view;
    final ToroAdapter.ViewHolder viewHolder;
    if (viewType == Basic3ViewHolder.TYPE_VIDEO) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(Basic3VideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new Basic3VideoViewHolder(view);
    } else {
      view = LayoutInflater.from(parent.getContext())
          .inflate(Basic3NormalViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new Basic3NormalViewHolder(view);
    }

    if (viewHolder instanceof Basic3VideoViewHolder) {
      viewHolder.setOnItemClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
          // Do this for for videoView only.
          if (view == ((Basic3VideoViewHolder) viewHolder).getPlayerView()) {
            // 1. Temporary disable the playback.
            Toro.rest(true);
            new AlertDialog.Builder(parent.getContext()).setTitle(R.string.app_name)
                .setMessage(R.string.sample)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                  @Override public void onDismiss(DialogInterface dialogInterface) {
                    // 2. Resume the playback.
                    Toro.rest(false);
                  }
                })
                .create()
                .show();
            Snackbar.make(parent, "Clicked to VIDEO", Snackbar.LENGTH_LONG).show();
          } else if (view == ((Basic3VideoViewHolder) viewHolder).dummyView) {
            Snackbar.make(parent, "Clicked to TEXT", Snackbar.LENGTH_LONG).show();
          }
        }
      });

      viewHolder.setOnItemLongClickListener(new View.OnLongClickListener() {
        @Override public boolean onLongClick(View view) {
          Snackbar.make(parent, "Long pressed to VIDEO", Snackbar.LENGTH_LONG).show();
          return false; // Return false to trigger Toro's behaviour.
        }
      });
    }

    return viewHolder;
  }

  @Override protected Object getItem(int position) {
    if (position % 3 == 0) {
      return new SimpleVideoObject("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
    } else {
      return new SimpleObject();
    }
  }

  @Override public int getItemViewType(int position) {
    return position % 3 == 0 ? Basic3ViewHolder.TYPE_VIDEO : Basic3ViewHolder.TYPE_NORMAL;
  }

  @Override public int getItemCount() {
    return 512;
  }

  // Toro requires this method to return item's unique Id.
  @Override public long getItemId(int position) {
    return position;
  }
}
