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

package im.ene.toro.sample.facebook.timeline;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import im.ene.toro.sample.R;
import im.ene.toro.sample.facebook.data.FbItem;
import java.util.List;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

/**
 * @author eneim | 6/18/17.
 */

public class TimelineViewHolder extends RecyclerView.ViewHolder {

  private static final int LAYOUT_RES = R.layout.vh_fbcard_base;

  static TimelineViewHolder createViewHolder(ViewGroup parent, int type) {
    View view = LayoutInflater.from(parent.getContext()).inflate(LAYOUT_RES, parent, false);
    final TimelineViewHolder viewHolder;
    switch (type) {
      case TimelineAdapter.TYPE_OTHER:
        viewHolder = new TimelineViewHolder(view);
        break;
      case TimelineAdapter.TYPE_VIDEO:
        viewHolder = new TimelineVideoViewHolder(view);
        break;
      default:
        throw new IllegalArgumentException("Non supported view type: " + type);
    }

    return viewHolder;
  }

  @BindView(R.id.fb_user_icon) ImageView userIcon;
  @BindView(R.id.fb_user_name) TextView userName;
  @BindView(R.id.fb_user_profile) TextView userProfile;
  @BindView(R.id.fb_item_middle) FrameLayout container;

  TimelineViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  public void setClickListener(View.OnClickListener clickListener) {
    this.itemView.setOnClickListener(clickListener);
  }

  void bind(TimelineAdapter adapter, FbItem item, List<Object> payloads) {
    userName.setText(item.author.userName);
    userProfile.setText(getRelativeTimeSpanString(item.timeStamp));
    Glide.with(itemView).load(item.author.userIcon).into(userIcon);
  }

  void onRecycled() {
    // do nothing
  }
}
