/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.youtube;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

/**
 * @author eneim (8/1/17).
 */

public class YoutubePlayerViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {

  static final int LAYOUT_RES = R.layout.view_holder_youtube_player;

  private YoutubePlayerHelper helper;
  private FragmentManager fragmentManager;
  private String videoId;

  @SuppressWarnings("WeakerAccess") FrameLayout playerViewContainer;
  @SuppressWarnings("WeakerAccess") TextView videoName;

  YoutubePlayerViewHolder(View itemView) {
    super(itemView);
    playerViewContainer = itemView.findViewById(R.id.player_container);
    videoName = itemView.findViewById(R.id.video_id);
  }

  @NonNull @Override public View getPlayerView() {
    return playerViewContainer.getChildAt(0);
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
  }

  @Override
  public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
    if (helper == null) {
      helper = new YoutubePlayerHelper(container, this, fragmentManager, videoId);
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
    helper = null;
  }

  @Override public boolean wantsToPlay() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.99;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }

  @Override public void onSettled(Container container) {
    if (helper != null) helper.onSettled();
  }

  void bind(FragmentManager fragmentManager, String videoId) {
    this.fragmentManager = fragmentManager;
    this.videoId = videoId;
    this.videoName.setText("Video: " + videoId);
  }
}
