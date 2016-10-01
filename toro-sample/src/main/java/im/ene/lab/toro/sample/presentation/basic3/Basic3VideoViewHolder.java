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

package im.ene.lab.toro.sample.presentation.basic3;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;
import im.ene.toro.exoplayer.dev.ToroExoPlayer;
import im.ene.toro.exoplayer.ExoVideo;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.sample.develop.legacy.sample1.LegacySample1Activity;

/**
 * Created by eneim on 6/29/16.
 *
 * This sample use {@link ToroExoPlayer.Player} API to play medias. So by default, Video ViewHolder
 * requires an implemented Component of that interface. For samples those use legacy API such as
 * {@link VideoView} or {@link MediaPlayer}, please take a look at {@link LegacySample1Activity}
 * implementations.
 */
public class Basic3VideoViewHolder extends Basic3BaseVideoViewHolder {

  // vh_toro_video_basic_3 is the updated version for vh_toro_video_basic, which has an extra
  // TextView to show the selective click event handling.
  public static final int LAYOUT_RES = R.layout.vh_toro_video_basic_3;

  private SimpleVideoObject video;
  private final ToroExoPlayer.Player videoPlayer;
  /* package */ final View videoView; // package private so Adapter can judge the clicked View.
  final TextView dummyView;

  public Basic3VideoViewHolder(View itemView) {
    super(itemView);
    dummyView = (TextView) itemView.findViewById(R.id.text);
    videoView = itemView.findViewById(R.id.video);
    if (getPlayerView() instanceof ToroExoPlayer.Player) {
      videoPlayer = (ToroExoPlayer.Player) getPlayerView();
    } else {
      throw new IllegalArgumentException("Illegal Video player widget. Requires a ToroExoPlayer.Player");
    }
    // !IMPORTANT: Helper is helpful, don't forget it.
    videoPlayer.setOnPlayerStateChangeListener(helper);
  }

  @Override public void bind(RecyclerView.Adapter adapter, Object item) {
    if (!(item instanceof SimpleVideoObject)) {
      throw new IllegalArgumentException("Invalid Object: " + item);
    }

    this.video = (SimpleVideoObject) item;
    this.videoPlayer.setMedia(new ExoVideo(Uri.parse(this.video.video), this.video.name));
  }

  /* BEGIN: ToroPlayer callbacks (partly) */
  @Override public void preparePlayer(boolean playWhenReady) {
    this.videoPlayer.preparePlayer(playWhenReady);
  }

  @Override public void start() {
    this.videoPlayer.start();
  }

  @Override public void pause() {
    this.videoPlayer.pause();
  }

  @Override public void stop() {
    this.videoPlayer.stop();
  }

  @Override public void releasePlayer() {
    this.videoPlayer.releasePlayer();
  }

  @Override public long getDuration() {
    return this.videoPlayer.getDuration();
  }

  @Override public long getCurrentPosition() {
    return this.videoPlayer.getCurrentPosition();
  }

  @Override public void seekTo(long pos) {
    this.videoPlayer.seekTo(pos);
  }

  @Override public boolean isPlaying() {
    return this.videoPlayer.isPlaying();
  }

  @Override public void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume) {
    this.videoPlayer.setVolume(volume);
  }

  // MEMO: Unique or null
  @Nullable @Override public String getMediaId() {
    return this.video != null ? this.video.video + "@" + getAdapterPosition() : null;
  }

  @NonNull @Override public View getPlayerView() {
    return this.videoView;
  }

  @Override public boolean isLoopAble() {
    return true;
  }
  /* END: ToroPlayer callbacks (partly) */

  // Interaction setup
  @Override public void setOnItemClickListener(View.OnClickListener listener) {
    super.setOnItemClickListener(listener);
    videoView.setOnClickListener(listener);
    // HINT: Un-comment this to enable click event on this TextView.
    // dummyView.setOnClickListener(listener);
  }

  @Override public void setOnItemLongClickListener(final View.OnLongClickListener listener) {
    super.setOnItemLongClickListener(listener);
    // Additional support for long-press on Video.
    videoView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View view) {
        return listener.onLongClick(view) &&  //
            helper.onItemLongClick(Basic3VideoViewHolder.this, itemView, itemView.getParent());
      }
    });
  }
}
