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

package im.ene.toro.sample.features.facebook.playlist;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.extra.ExoPlayerHelper;
import im.ene.toro.extra.SimpleExoPlayerViewHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.sample.ToroDemo;
import im.ene.toro.sample.common.DemoUtil;
import im.ene.toro.sample.data.MediaUrl;
import im.ene.toro.sample.features.facebook.data.FbVideo;
import im.ene.toro.widget.Container;
import java.util.List;

/**
 * @author eneim | 6/18/17.
 */

@SuppressWarnings("WeakerAccess") //
public class MoreVideoItemViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {

  static final int LAYOUT_RES = R.layout.vh_fbcard_base_dark;

  @Nullable SimpleExoPlayerViewHelper helper;
  @Nullable private Uri mediaUri;

  @BindView(R.id.fb_user_icon) ImageView userIcon;
  @BindView(R.id.fb_user_name) TextView userName;
  @BindView(R.id.fb_user_profile) TextView userProfile;
  @BindView(R.id.fb_item_middle) FrameLayout container;
  @BindView(R.id.fb_video_player) SimpleExoPlayerView playerView;
  @BindView(R.id.player_state) TextView state;

  private ExoPlayerHelper.EventListener listener = new ExoPlayerHelper.EventListener() {
    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      super.onPlayerStateChanged(playWhenReady, playbackState);
      state.setText("STATE: " + playbackState + "・PWR: " + playWhenReady);
    }
  };

  MoreVideoItemViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
    playerView.setVisibility(View.VISIBLE);
  }

  void bind(MoreVideosAdapter adapter, FbVideo item, List<Object> payloads) {
    if (item != null) {
      userName.setText(item.author.userName);
      Glide.with(ToroDemo.getApp()).load(item.author.userIcon).into(userIcon);
      MediaUrl url = item.getMediaUrl();
      mediaUri = url.getUri();
      userProfile.setText(DemoUtil.getRelativeTimeString(item.timeStamp) + "・" + url.name());
    }
  }

  @NonNull @Override public View getPlayerView() {
    return this.playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    return helper != null ? helper.updatePlaybackInfo() : new PlaybackInfo();
  }

  @Override
  public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
    if (helper == null) {
      helper = new SimpleExoPlayerViewHelper(container, this, mediaUri);
      helper.setEventListener(listener);
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
    if (helper != null) {
      helper.setEventListener(null);
      try {
        helper.cancel();
      } catch (Exception e) {
        e.printStackTrace();
      }
      helper = null;
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
