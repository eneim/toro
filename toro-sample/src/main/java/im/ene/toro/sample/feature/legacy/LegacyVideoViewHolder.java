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

package im.ene.toro.sample.feature.legacy;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.VideoView;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.SimpleVideoObject;
import im.ene.toro.mediaplayer.BaseLegacyVideoViewHolder;

/**
 * Created by eneim on 9/29/16.
 */

public class LegacyVideoViewHolder extends BaseLegacyVideoViewHolder {

  static final int LAYOUT_RES = R.layout.vh_toro_video_legacy;

  private SimpleVideoObject video;

  public LegacyVideoViewHolder(View itemView) {
    super(itemView);
  }

  @Override protected VideoView findVideoView(View itemView) {
    return (VideoView) itemView.findViewById(R.id.video);
  }

  @Nullable @Override public String getMediaId() {
    return this.video != null ? this.video.video + "@" + getAdapterPosition() : null;
  }

  @Override public void bind(RecyclerView.Adapter adapter, Object item) {
    if (item instanceof SimpleVideoObject) {
      this.video = (SimpleVideoObject) item;
    } else {
      throw new IllegalArgumentException("Item must be a SimpleVideoObject");
    }

    helper.preparePlayer(false);
    videoView.setVideoPath(this.video.video);
  }
}
