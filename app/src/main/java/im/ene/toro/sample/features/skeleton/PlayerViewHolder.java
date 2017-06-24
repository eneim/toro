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

package im.ene.toro.sample.features.skeleton;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.helper.SimpleExoPlayerViewHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.widget.Container;

/**
 * @author eneim (6/24/17).
 */

public class PlayerViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {

  static final int LAYOUT_RES = R.layout.vh_skeleton_exoplayer;

  SimpleExoPlayerView playerView;
  SimpleExoPlayerViewHelper playerViewHelper;
  Uri mediaUri;

  public PlayerViewHolder(View itemView) {
    super(itemView);
    playerView = itemView.findViewById(R.id.player);
  }

  // Called by Adapter to pass a valid media uri here.
  public void bind(Uri uri) {
    this.mediaUri = uri;
  }

  @NonNull @Override public View getPlayerView() {
    return this.playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    PlaybackInfo info = new PlaybackInfo();
    return playerViewHelper != null ? playerViewHelper.updatePlaybackInfo() : info;
  }

  @Override
  public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
    if (playerViewHelper == null) {
      playerViewHelper = new SimpleExoPlayerViewHelper(container, this, mediaUri);
    }
    playerViewHelper.initialize(playbackInfo);
  }

  @Override public void play() {
    playerViewHelper.play();
  }

  @Override public void pause() {
    playerViewHelper.pause();
  }

  @Override public boolean isPlaying() {
    return playerViewHelper != null && playerViewHelper.isPlaying();
  }

  @Override public void release() {
    if (playerViewHelper != null) {
      try {
        playerViewHelper.cancel();
      } catch (Exception e) {
        e.printStackTrace();
      }
      playerViewHelper = null;
    }
  }

  @Override public boolean wantsToPlay() {
    ViewParent parent = itemView.getParent();
    float offset = 0;
    if (parent != null && parent instanceof View) {
      offset = ToroUtil.visibleAreaOffset(playerView, (View) parent);
    }
    return offset >= 0.85;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }
}
