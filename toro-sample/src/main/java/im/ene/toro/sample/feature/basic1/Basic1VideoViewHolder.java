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

package im.ene.toro.sample.feature.basic1;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import im.ene.toro.exoplayer2.ExoVideoView;
import im.ene.toro.exoplayer2.ExoVideoViewHolder;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.SimpleVideoObject;

/**
 * Created by eneim on 6/29/16.
 *
 * This sample use {@link ExoVideoView} API to play medias.
 */
public class Basic1VideoViewHolder extends ExoVideoViewHolder {

  public static final int LAYOUT_RES = R.layout.vh_toro_video_basic;

  private SimpleVideoObject videoItem;

  public Basic1VideoViewHolder(View itemView) {
    super(itemView);
  }

  @Override public void bind(RecyclerView.Adapter adapter, Object item) {
    if (!(item instanceof SimpleVideoObject)) {
      throw new IllegalArgumentException("Invalid Object: " + item);
    }

    this.videoItem = (SimpleVideoObject) item;
    this.videoView.setMedia(Uri.parse(this.videoItem.video));
  }

  @Override protected ExoVideoView findVideoView(View itemView) {
    return (ExoVideoView) itemView.findViewById(R.id.video);
  }

  @Nullable @Override public String getMediaId() {
    return this.videoItem != null ? this.videoItem.video + "@" + getAdapterPosition() : null;
  }
}
