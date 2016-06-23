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

package im.ene.lab.toro.sample.legacy.betterlist;

import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import im.ene.lab.toro.ToroVideoViewHolder;
import im.ene.lab.toro.sample.legacy.R;
import im.ene.lab.toro.sample.legacy.Util;
import im.ene.lab.toro.sample.legacy.data.VideoItem;
import im.ene.lab.toro.widget.ToroVideoView;

/**
 * Created by eneim on 6/8/16.
 */
public class FinedGrainVideoViewHolder extends ToroVideoViewHolder {

  static final int LAYOUT_REST = R.layout.vh_fined_grain_video;

  private VideoItem item;
  private TextView infoText;
  private ImageView thumbnailView;

  public FinedGrainVideoViewHolder(View itemView) {
    super(itemView);
    infoText = (TextView) itemView.findViewById(R.id.info_text);
    thumbnailView = (ImageView) itemView.findViewById(R.id.thumbnail_view);
  }

  @Override protected ToroVideoView findVideoView(View itemView) {
    return (ToroVideoView) itemView.findViewById(R.id.video_view);
  }

  @Nullable @Override public String getVideoId() {
    return item != null ? item.video + "@" + getAdapterPosition() : null;
  }

  @Override public void bind(@Nullable Object object) {
    if (!(object instanceof VideoItem)) {
      throw new IllegalArgumentException("Require a VideoItem");
    }

    item = (VideoItem) object;
    mVideoView.setVideoPath(item.video);
  }

  @Override public void onViewHolderBound() {
    super.onViewHolderBound();
    infoText.setText("BOUND");
    Glide.with(itemView.getContext()).load(R.drawable.code_for_fun_web).into(thumbnailView);
    infoText.setText("PREPARING");
  }

  // Playback cycle

  @Override public void onVideoPrepared(MediaPlayer mp) {
    super.onVideoPrepared(mp);
    infoText.setText("PREPARED");
  }

  @Override public boolean onPlaybackError(MediaPlayer mp, int what, int extra) {
    infoText.setText("ERROR");
    thumbnailView.setVisibility(View.VISIBLE);
    return super.onPlaybackError(mp, what, extra);
  }

  @Override public void onPlaybackStarted() {
    super.onPlaybackStarted();
    // DO nothing here.
  }

  @Override public void onPlaybackProgress(int position, int duration) {
    if (position > 0) {
      // Hide thumbnail only when we are really playing
      thumbnailView.setVisibility(View.GONE);
    }
    infoText.setText("PLAYING: " + Util.timeStamp(position, duration));
  }

  @Override public void onPlaybackPaused() {
    infoText.setText("PAUSED");
    thumbnailView.setVisibility(View.VISIBLE);
  }

  @Override public void onPlaybackStopped() {
    super.onPlaybackStopped();
    infoText.setText("STOPPED");
    thumbnailView.setVisibility(View.VISIBLE);
  }
}
