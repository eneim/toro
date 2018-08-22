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

package toro.demo.ads.mopub;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ExoPlayerViewHelper;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toro.demo.ads.R;
import toro.demo.ads.common.BaseViewHolder;

/**
 * @author eneim (2018/08/21).
 */
@SuppressWarnings("WeakerAccess") //
class VideoViewHolder extends BaseViewHolder implements ToroPlayer, ToroPlayer.EventListener {

  final PlayerView playerView;
  final ImageView posterView;
  ToroPlayerHelper helper;
  Uri mediaUri;

  VideoViewHolder(@NotNull View itemView) {
    super(itemView);
    playerView = itemView.findViewById(R.id.playerView);
    posterView = itemView.findViewById(R.id.posterView);
  }

  @NonNull @Override public View getPlayerView() {
    return this.playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
  }

  @Override
  public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
    if (helper == null) {
      if (mediaUri != null) helper = new ExoPlayerViewHelper(this, mediaUri);
    }

    if (helper != null) {
      helper.addPlayerEventListener(this);
      helper.initialize(container, playbackInfo);
    }
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
    if (helper != null) {
      helper.removePlayerEventListener(this);
      helper.release();
      helper = null;
    }
  }

  @Override public boolean wantsToPlay() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.75;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }

  @Override public void onBuffering() {
    posterView.setVisibility(View.GONE);
  }

  @Override public void onPlaying() {
    posterView.setVisibility(View.GONE);
  }

  @Override public void onPaused() {
    posterView.setVisibility(View.VISIBLE);
  }

  @Override public void onCompleted() {
    posterView.setVisibility(View.VISIBLE);
  }

  @Override public void onBind(@Nullable Object payload) {
    super.onBind(payload);
    posterView.setVisibility(View.VISIBLE);
    if (payload instanceof Uri) {
      this.mediaUri = (Uri) payload;
    } else {
      this.mediaUri = Uri.parse("https://video-dev.github.io/streams/x36xhzz/x36xhzz.m3u8");
    }

    Glide.with(itemView)
        .load("https://archive.org/download/Big_Buck_Bunny-13302/Big_Buck_Bunny-13302.jpg")
        .thumbnail(0.15f)
        .into(posterView);
  }
}
