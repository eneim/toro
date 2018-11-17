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

package im.ene.toro.sample.basic;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ExoPlayerDispatcher;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.widget.Container;
import im.ene.toro.widget.PressablePlayerSelector;
import toro.v4.Media;
import toro.v4.MediaItem;
import toro.v4.exo.MediaHub;

/**
 * @author eneim (7/1/17).
 */

@SuppressWarnings({ "WeakerAccess", "unused" }) //
class BasicPlayerViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {

  private static final String TAG = "Toro:Basic:Holder";

  static final int LAYOUT_RES = R.layout.view_holder_exoplayer_basic;

  ToroPlayerHelper helper;
  Uri mediaUri;
  Media media;

  @BindView(R.id.thumbnail) ImageView thumbnail;
  @BindView(R.id.player) PlayerView playerView;

  public BasicPlayerViewHolder(View itemView, PressablePlayerSelector selector) {
    super(itemView);
    ButterKnife.bind(this, itemView);
    if (selector != null) playerView.setControlDispatcher(new ExoPlayerDispatcher(selector, this));
    playerView.removeView(thumbnail);
    playerView.getOverlayFrameLayout().addView(thumbnail);
  }

  @NonNull @Override public View getPlayerView() {
    return playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
  }

  @Override
  public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
    if (helper == null) {
      // helper = new ExoPlayerViewHelper(this, mediaUri);
      helper = MediaHub.get(itemView.getContext()).requestHelper(this, media);
      helper.addPlayerEventListener(new EventListener() {
        @Override public void onFirstFrameRendered() {
          thumbnail.setVisibility(View.GONE);
        }

        @Override public void onBuffering() {

        }

        @Override public void onPlaying() {
          thumbnail.setVisibility(View.GONE);
        }

        @Override public void onPaused() {
          thumbnail.setVisibility(View.VISIBLE);
        }

        @Override public void onCompleted() {
          thumbnail.setVisibility(View.VISIBLE);
        }
      });
    }
    thumbnail.setVisibility(View.VISIBLE);
    helper.initialize(container, playbackInfo);
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
    thumbnail.setVisibility(View.VISIBLE);
    if (helper != null) {
      helper.release();
      helper = null;
    }
  }

  @Override public boolean wantsToPlay() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.85;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }

  @NonNull @Override public String toString() {
    return "ExoPlayer{" + hashCode() + " " + getAdapterPosition() + "}";
  }

  void bind(Content.Media media) {
    this.mediaUri = media.mediaUri;
    this.media = new MediaItem(this.mediaUri, null);
    Glide.with(itemView)
        .load("https://cdn.pixabay.com/photo/2018/02/06/22/43/painting-3135875_960_720.jpg")
        .into(thumbnail);
  }
}
