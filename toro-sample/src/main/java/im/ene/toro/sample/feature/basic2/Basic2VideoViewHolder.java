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

package im.ene.toro.sample.feature.basic2;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.VideoView;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.SimpleVideoObject;
import im.ene.toro.sample.feature.legacy.LegacyActivity;
import im.ene.toro.exoplayer2.ExoVideoView;
import im.ene.toro.exoplayer2.ExoVideoViewHolder;

/**
 * Created by eneim on 6/29/16.
 *
 * This sample use {@link ExoVideoView} API to play medias. So by default, Video ViewHolder
 * requires an implemented Component of that interface. For samples those use legacy API such as
 * {@link VideoView} or {@link MediaPlayer}, please take a look at {@link LegacyActivity}
 * implementations.
 */
public class Basic2VideoViewHolder extends ExoVideoViewHolder {

  public static final int LAYOUT_RES = R.layout.vh_toro_video_basic;

  private SimpleVideoObject video;

  public Basic2VideoViewHolder(View itemView) {
    super(itemView);
  }

  @Override public void bind(RecyclerView.Adapter adapter, Object item) {
    if (!(item instanceof SimpleVideoObject)) {
      throw new IllegalArgumentException("Invalid Object: " + item);
    }

    this.video = (SimpleVideoObject) item;
    this.videoView.setMedia(Uri.parse(this.video.video));
  }

  // MEMO: Unique or null
  @Nullable @Override public String getMediaId() {
    return this.video != null ? this.video.video + "@" + getAdapterPosition() : null;
  }

  @Override protected ExoVideoView findVideoView(View itemView) {
    return (ExoVideoView) itemView.findViewById(R.id.video);
  }
}
