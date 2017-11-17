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

package im.ene.toro.sample.facebook.timeline;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ExoPlayerHelper;
import im.ene.toro.exoplayer.SimpleExoPlayerViewHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.MediaUrl;
import im.ene.toro.sample.facebook.data.FbItem;
import im.ene.toro.sample.facebook.data.FbVideo;
import im.ene.toro.widget.Container;
import java.util.List;
import java.util.Locale;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static java.lang.String.format;

/**
 * @author eneim | 6/18/17.
 */

@SuppressWarnings("WeakerAccess") //
public class TimelineVideoViewHolder extends TimelineViewHolder implements ToroPlayer {

  @Nullable SimpleExoPlayerViewHelper helper;
  @Nullable private Uri mediaUri;

  @BindView(R.id.fb_video_player) SimpleExoPlayerView playerView;
  @BindView(R.id.player_state) TextView state;

  private ExoPlayerHelper.EventListener listener = new ExoPlayerHelper.EventListener() {
    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      super.onPlayerStateChanged(playWhenReady, playbackState);
      state.setText(format(Locale.getDefault(), "STATE: %d・PWR: %s", playbackState, playWhenReady));
    }
  };

  TimelineVideoViewHolder(View itemView) {
    super(itemView);
    playerView.setVisibility(View.VISIBLE);
  }

  @Override public void setClickListener(View.OnClickListener clickListener) {
    super.setClickListener(clickListener);
    playerView.setOnClickListener(clickListener);
    userIcon.setOnClickListener(clickListener);
  }

  @Override void bind(TimelineAdapter adapter, FbItem item, List<Object> payloads) {
    super.bind(adapter, item, payloads);
    if (item != null && item instanceof FbVideo) {
      MediaUrl url = ((FbVideo) item).getMediaUrl();
      mediaUri = url.getUri();
      userProfile.setText(format("%s・%s", getRelativeTimeSpanString(item.timeStamp), url.name()));
    }
  }

  @NonNull @Override public View getPlayerView() {
    return this.playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
  }

  @Override
  public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
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
      helper.release();
      helper = null;
    }
  }

  @Override public boolean wantsToPlay() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.85;
  }

  @Override public void onSettled(Container container) {
    // Do nothing
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }
}
