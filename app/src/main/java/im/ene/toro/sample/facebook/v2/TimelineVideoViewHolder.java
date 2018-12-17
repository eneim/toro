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

package im.ene.toro.sample.facebook.v2;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.widget.Container;
import im.ene.toro.media.Media;
import im.ene.toro.media.MediaItem;
import im.ene.toro.exoplayer.MediaHub;

@SuppressWarnings("WeakerAccess")
class TimelineVideoViewHolder extends TimelineBaseViewHolder implements ToroPlayer {

  final PlayerView playerView;
  final AspectRatioFrameLayout contentView;
  final ImageView thumbnail;

  TimelineVideoViewHolder(@NonNull View itemView) {
    super(itemView);
    playerView = itemView.findViewById(R.id.player_view);
    contentView = itemView.findViewById(R.id.player_container);
    thumbnail = itemView.findViewById(R.id.thumbnail);
  }

  @Override void bind(Object data, int position) {
    super.bind(data, position);
    VideoItem videoItem = (VideoItem) data;
    contentView.setAspectRatio(videoItem.getVideoWidth() / (videoItem.getVideoHeight() * 1.f));
    media = new MediaItem(videoItem.getVideoUri(), videoItem.getCustomType());
    thumbnail.setImageResource(R.drawable.blue_gradient_background);
    thumbnail.setVisibility(View.VISIBLE);
  }

  /// ToroPlayer implementation

  ToroPlayerHelper helper;
  ToroPlayer.EventListener listener;
  Media media;

  @NonNull @Override public View getPlayerView() {
    return this.playerView;
  }

  @Override public boolean wantsToPlay() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.75;
  }

  @Override public int getPlayerOrder() {
    return this.getAdapterPosition();
  }

  @Override
  public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
    if (media == null) return;
    if (helper == null) {
      helper = MediaHub.get(itemView.getContext()).requestHelper(this, media);
      listener = new EventListener() {
        @Override public void onFirstFrameRendered() {
          thumbnail.setVisibility(View.GONE);
        }

        @Override public void onBuffering() {

        }

        @Override public void onPlaying() {

        }

        @Override public void onPaused() {

        }

        @Override public void onCompleted() {
          thumbnail.setVisibility(View.VISIBLE);
        }
      };
      helper.addPlayerEventListener(listener);
    }
    helper.initialize(container, playbackInfo);
  }

  @Override public void release() {
    thumbnail.setVisibility(View.VISIBLE);
    if (helper != null) {
      helper.release();
      helper.removePlayerEventListener(listener);
      listener = null;
      helper = null;
    }
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    return helper != null ? helper.getLatestPlaybackInfo() : PlaybackInfo.SCRAP;
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
}
