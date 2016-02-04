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

package im.ene.lab.toro.sample.viewholder;

import android.support.annotation.IntDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.ToroAdapter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by eneim on 1/30/16.
 */
public abstract class BaseViewHolder extends ToroAdapter.ViewHolder {

  public static final int VIEW_TYPE_NO_VIDEO = 1;

  public static final int VIEW_TYPE_VIDEO = 1 << 1;

  @IntDef({
      VIEW_TYPE_NO_VIDEO, VIEW_TYPE_VIDEO
  }) @Retention(RetentionPolicy.SOURCE) public @interface Type {
  }

  public BaseViewHolder(View itemView) {
    super(itemView);
  }

  public static ToroAdapter.ViewHolder createViewHolder(ViewGroup parent, @Type int viewType) {
    final ToroAdapter.ViewHolder viewHolder;
    final View view;
    if (viewType == VIEW_TYPE_VIDEO) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(SampleToroVideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new SampleToroVideoViewHolder(view);
    } else {
      view = LayoutInflater.from(parent.getContext())
          .inflate(NormalViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new NormalViewHolder(view);
    }

    return viewHolder;
  }

}
