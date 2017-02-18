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

package im.ene.toro.sample.v3;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.exoplayer2.ExoPlayerHelper;
import im.ene.toro.exoplayer2.ExoPlayerView;
import im.ene.toro.exoplayer2.ExoPlayerViewHolder;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.OrderedVideoObject;

/**
 * Created by eneim on 2/9/17.
 */

public class MediaItemViewHolder extends ExoPlayerViewHolder {

  public static final int LAYOUT_RES = R.layout.vh_toro_video_basic_4;

  private OrderedVideoObject videoItem;
  private MediaSource mediaSource;

  private TextView number;

  public MediaItemViewHolder(View itemView) {
    super(itemView);
    number = (TextView) itemView.findViewById(R.id.text_number);
  }

  @Override public void setOnItemClickListener(View.OnClickListener listener) {
    super.setOnItemClickListener(listener);
    this.playerView.setOnClickListener(listener);
  }

  @Override protected ExoPlayerView findVideoView(View itemView) {
    return (ExoPlayerView) itemView.findViewById(R.id.video);
  }

  @Override protected MediaSource getMediaSource() {
    return mediaSource;
  }

  @Override protected void onBind(RecyclerView.Adapter adapter, @Nullable Object item) {
    if (!(item instanceof OrderedVideoObject)) {
      throw new IllegalArgumentException("Invalid Object: " + item);
    }

    this.videoItem = (OrderedVideoObject) item;
    this.number.setText("" + videoItem.position);
    // prepare mediaSource
    this.mediaSource = ExoPlayerHelper.buildMediaSource(itemView.getContext(), //
        Uri.parse(this.videoItem.video), new DefaultDataSourceFactory(itemView.getContext(),
            Util.getUserAgent(itemView.getContext(), "Toro-Sample")), itemView.getHandler(), null);
  }

  @Nullable @Override public String getMediaId() {
    return this.videoItem != null ? this.videoItem.video + "@" + getAdapterPosition() : null;
  }
}
