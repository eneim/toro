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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.exoplayer2.ExoPlayerHelper;
import im.ene.toro.exoplayer2.ExoPlayerView;
import im.ene.toro.extended.ExtPlayerViewHolder;
import im.ene.toro.sample.R;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem;
import im.ene.toro.sample.util.DemoUtil;

/**
 * Created by eneim on 10/13/16.
 *
 * First item of the list, has an extra space with the height of 60dp on top.
 */
public class FirstItemViewHolder extends ExtPlayerViewHolder {

  static final int LAYOUT_RES = R.layout.vh_playlist_item_first;

  public FirstItemViewHolder(View itemView) {
    super(itemView);
  }

  @Override protected ExoPlayerView findVideoView(View itemView) {
    return (ExoPlayerView) itemView.findViewById(R.id.video);
  }

  private MediaSource mediaSource;
  private TimelineItem.VideoItem videoItem;

  @Override protected void onBind(RecyclerView.Adapter adapter, @Nullable Object object) {
    if (!(object instanceof TimelineItem.VideoItem)) {
      throw new IllegalArgumentException("Illegal object: " + object);
    }
    this.videoItem = (TimelineItem.VideoItem) object;
    // prepare mediaSource
    this.mediaSource = ExoPlayerHelper.buildMediaSource(itemView.getContext(), //
        Uri.parse(this.videoItem.getVideoUrl()), new DefaultDataSourceFactory(itemView.getContext(),
            Util.getUserAgent(itemView.getContext(), "Toro-Sample")), itemView.getHandler(), null);
  }

  @NonNull @Override protected MediaSource getMediaSource() {
    return this.mediaSource;
  }

  @Nullable @Override public String getMediaId() {
    return DemoUtil.genVideoId(this.videoItem.getVideoUrl(), getAdapterPosition());
  }

  @Override public Target getNextTarget() {
    return Target.NEXT_PLAYER;
  }
}
