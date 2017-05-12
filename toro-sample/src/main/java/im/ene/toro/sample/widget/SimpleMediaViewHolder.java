/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample.widget;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.exoplayer2.ExoPlayerHelper;
import im.ene.toro.exoplayer2.ExoPlayerView;
import im.ene.toro.exoplayer2.ExoPlayerViewHolder;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.SimpleVideoObject;

/**
 * @author eneim.
 * @since 5/9/17.
 *
 * Simple Media ViewHolder, used for basic Demonstrations.
 */

public class SimpleMediaViewHolder extends ExoPlayerViewHolder {

  public static final int LAYOUT_RES = R.layout.vh_toro_media_simple;

  private SimpleVideoObject videoItem;
  private MediaSource mediaSource;

  public SimpleMediaViewHolder(View itemView) {
    super(itemView);
  }

  @NonNull @Override protected ExoPlayerView findVideoView(View itemView) {
    return (ExoPlayerView) itemView.findViewById(R.id.media);
  }

  @NonNull @Override protected MediaSource getMediaSource() {
    return mediaSource;
  }

  @Override protected void onBind(RecyclerView.Adapter adapter, @Nullable Object item) {
    if (!(item instanceof SimpleVideoObject)) {
      throw new IllegalArgumentException(
          "SimpleMediaViewHolder requires a SimpleVideoObject. Found: " + //
              (item == null ? "null" : item.getClass().getSimpleName()));
    }

    this.videoItem = (SimpleVideoObject) item;
    // prepare mediaSource
    this.mediaSource = ExoPlayerHelper.buildMediaSource(itemView.getContext(), //
        Uri.parse(this.videoItem.video), new DefaultDataSourceFactory(itemView.getContext(),
            Util.getUserAgent(itemView.getContext(), "Toro-Sample")), itemView.getHandler(), null);
  }

  @Nullable @Override public String getMediaId() {
    return this.videoItem != null ? this.videoItem.video + "@" + getAdapterPosition() : null;
  }
}
