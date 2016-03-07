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

package im.ene.lab.toro.sample.adapter;

import android.content.DialogInterface;
import android.support.annotation.IntDef;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.sample.data.VideoSource;
import im.ene.lab.toro.sample.viewholder.MixedToroVideoViewHolder;
import im.ene.lab.toro.sample.viewholder.SimpleToroVideoViewHolder;
import im.ene.lab.toro.sample.viewholder.TextViewHolder;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 2/6/16.
 */
public abstract class BaseSampleAdapter extends ToroAdapter<ToroAdapter.ViewHolder> {

  public static final int VIEW_TYPE_NO_VIDEO = 1;

  public static final int VIEW_TYPE_VIDEO = 1 << 1;

  public static final int VIEW_TYPE_VIDEO_MIXED = 1 << 2;

  protected List<SimpleVideoObject> mVideos = new ArrayList<>();

  @IntDef({
      VIEW_TYPE_NO_VIDEO, VIEW_TYPE_VIDEO, VIEW_TYPE_VIDEO_MIXED
  }) @Retention(RetentionPolicy.SOURCE) public @interface Type {
  }

  public BaseSampleAdapter() {
    super();
    setHasStableIds(true);
    for (String item : VideoSource.SOURCES) {
      mVideos.add(new SimpleVideoObject(item));
    }
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final ToroAdapter.ViewHolder viewHolder;
    final View view;
    if (viewType == VIEW_TYPE_VIDEO) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(SimpleToroVideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new SimpleToroVideoViewHolder(view);
    } else if (viewType == VIEW_TYPE_VIDEO_MIXED) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(MixedToroVideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new MixedToroVideoViewHolder(view);
    } else {
      view = LayoutInflater.from(parent.getContext())
          .inflate(TextViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new TextViewHolder(view);
    }

    viewHolder.setOnItemClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Toro.rest(true);
        new AlertDialog.Builder(v.getContext()).setTitle("Sample Action")
            .setMessage("Sample Content")
            .setOnDismissListener(new DialogInterface.OnDismissListener() {
              @Override public void onDismiss(DialogInterface dialog) {
                Toro.rest(false);
              }
            })
            .create()
            .show();
      }
    });
    return viewHolder;
  }

  @Type @Override public int getItemViewType(int position) {
    Object item = getItem(position);
    return item instanceof SimpleVideoObject ? VIEW_TYPE_VIDEO : VIEW_TYPE_NO_VIDEO;
  }

  @Override public long getItemId(int position) {
    Object item = getItem(position);
    if (item != null) {
      return item.hashCode();
    } else {
      return 0;
    }
  }

  @Override public int getItemCount() {
    return 512; // Magic number :trollface:
  }
}
