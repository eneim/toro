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

package im.ene.toro.sample.feature.facebook.playlist;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import im.ene.toro.exoplayer2.ExoVideoView;
import im.ene.toro.exoplayer2.ExoVideoViewHolder;
import im.ene.toro.sample.R;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem;
import im.ene.toro.sample.util.Util;

/**
 * Created by eneim on 10/13/16.
 *
 * First item of the list, has an extra space with the height of 60dp on top.
 */
public class FirstItemViewHolder extends ExoVideoViewHolder {

  static final int LAYOUT_RES = R.layout.vh_playlist_item_first;

  public FirstItemViewHolder(View itemView) {
    super(itemView);
  }

  @Override protected ExoVideoView findVideoView(View itemView) {
    return (ExoVideoView) itemView.findViewById(R.id.video);
  }

  private TimelineItem.VideoItem videoItem;

  @Override public void bind(RecyclerView.Adapter adapter, @Nullable Object object) {
    if (!(object instanceof TimelineItem.VideoItem)) {
      throw new IllegalArgumentException("Illegal object: " + object);
    }
    this.videoItem = (TimelineItem.VideoItem) object;
    this.videoView.setMedia(Uri.parse(videoItem.getVideoUrl()));
  }

  @Nullable @Override public String getMediaId() {
    return Util.genVideoId(this.videoItem.getVideoUrl(), getAdapterPosition());
  }
}
