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

package im.ene.toro.sample.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import java.util.List;

/**
 * @author eneim | 6/6/17.
 */

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

  static BaseViewHolder createViewHolder(ViewGroup parent, int type) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    final View view;
    final BaseViewHolder viewHolder;
    switch (type) {
      case ContentAdapter.TYPE_MEDIA:
        view = inflater.inflate(SimpleExoPlayerViewHolder.LAYOUT_RES, parent, false);
        viewHolder = new SimpleExoPlayerViewHolder(view);
        break;
      case ContentAdapter.TYPE_TEXT:
        view = inflater.inflate(TextViewHolder.LAYOUT_RES, parent, false);
        viewHolder = new TextViewHolder(view);
        break;
      default:
        throw new IllegalArgumentException("Un-supported view type: " + type);
    }

    return viewHolder;
  }

  BaseViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  public abstract void bind(@NonNull Adapter adapter, @Nullable Object item,
      @Nullable List<Object> payloads);
}
