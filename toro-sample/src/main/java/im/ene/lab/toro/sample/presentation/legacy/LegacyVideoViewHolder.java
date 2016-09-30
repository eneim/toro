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

package im.ene.lab.toro.sample.presentation.legacy;

import android.net.Uri;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.VideoView;
import im.ene.lab.toro.player.ExoVideo;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.data.SimpleVideoObject;

/**
 * Created by eneim on 9/29/16.
 */

public class LegacyVideoViewHolder extends LegacyBaseVideoViewHolder {

  static final int LAYOUT_RES = R.layout.vh_toro_video_legacy;

  private SimpleVideoObject video;
  private VideoView videoView;

  public LegacyVideoViewHolder(View itemView) {
    super(itemView);
    videoView = (VideoView) itemView.findViewById(R.id.video);
    videoView.setOnPreparedListener(helper);
    videoView.setOnCompletionListener(helper);
  }

  @Override public void preparePlayer(boolean playWhenReady) {
    helper.setPlayWhenReady(playWhenReady);
    helper.preparePlayer();
  }

  @Override public void start() {
    videoView.start();
  }

  @Override public void pause() {
    videoView.pause();
  }

  @Override public void stop() {
    videoView.stopPlayback();
  }

  @Override public void releasePlayer() {
    helper.releasePlayer();
  }

  @Override public long getDuration() {
    return videoView.getDuration();
  }

  @Override public long getCurrentPosition() {
    return videoView.getCurrentPosition();
  }

  @Override public void seekTo(long pos) {
    videoView.seekTo((int) pos);
  }

  @Override public boolean isPlaying() {
    return videoView.isPlaying();
  }

  @Override public void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume) {
    this.helper.setVolume(volume);
  }

  @Nullable @Override public String getMediaId() {
    return this.video != null ? this.video.video + "@" + getAdapterPosition() : null;
  }

  @NonNull @Override public View getPlayerView() {
    return videoView;
  }

  @Override public void bind(RecyclerView.Adapter adapter, Object item) {
    if (item instanceof SimpleVideoObject) {
      this.video = (SimpleVideoObject) item;
    } else {
      throw new IllegalArgumentException("Item must be a SimpleVideoObject");
    }

    ExoVideo video = new ExoVideo(Uri.parse(this.video.video), this.video.name);
    helper.setPlayWhenReady(false);
    videoView.setVideoPath(this.video.video);
  }
}
