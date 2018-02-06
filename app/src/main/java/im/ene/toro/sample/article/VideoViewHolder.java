/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.article;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ExoPlayerViewHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.widget.Container;

/**
 * @author eneim (2018/01/08).
 */

class VideoViewHolder extends BaseViewHolder implements ToroPlayer {

  static final Uri video = Uri.parse("file:///android_asset/design/intro_material_io.mkv");
  final SimpleExoPlayerView playerView;

  VideoViewHolder(LayoutInflater inflater, ViewGroup parent) {
    super(inflater.inflate(R.layout.article_part_video, parent, false));
    this.playerView = (SimpleExoPlayerView) itemView;
  }

  @Override void bind(Object object) {
    super.bind(object);
  }

  /// ToroPlayer

  private ExoPlayerViewHelper helper;

  @NonNull @Override public View getPlayerView() {
    return playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
  }

  @Override
  public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
    if (helper == null) {
      helper = new ExoPlayerViewHelper(container, this, video.buildUpon().build());
    }
    helper.initialize(playbackInfo);
  }

  @Override public void play() {
    if (helper != null) helper.play();
  }

  @Override public void pause() {
    if (helper != null) helper.pause();
  }

  @Override public boolean isPlaying() {
    return helper != null && helper.isPlaying();
  }

  @Override public void release() {
    if (helper != null) helper.release();
  }

  @Override public boolean wantsToPlay() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.65;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }

  @Override public void onSettled(Container container) {
    // Do nothing
  }
}
