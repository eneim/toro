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

package im.ene.lab.toro.sample.legacy.simplelist;

import android.support.annotation.Nullable;
import android.view.View;
import im.ene.lab.toro.ToroVideoViewHolder;
import im.ene.lab.toro.sample.legacy.R;
import im.ene.lab.toro.sample.legacy.data.VideoItem;
import im.ene.lab.toro.widget.ToroVideoView;

/**
 * Created by eneim on 6/8/16.
 */
public class SimpleVideoViewHolder extends ToroVideoViewHolder {

  private VideoItem item;

  static final int LAYOUT_RES = R.layout.vh_simple_video;

  public SimpleVideoViewHolder(View itemView) {
    super(itemView);
  }

  @Override protected ToroVideoView findVideoView(View itemView) {
    return (ToroVideoView) itemView.findViewById(R.id.video_view);
  }

  @Nullable @Override public String getVideoId() {
    return item.video + "@" + getAdapterPosition();
  }

  @Override public void bind(@Nullable Object object) {
    if (!(object instanceof VideoItem)) {
      throw new IllegalArgumentException("Require a VideoItem");
    }

    item = (VideoItem) object;
    mVideoView.setVideoPath(item.video);
  }

}
