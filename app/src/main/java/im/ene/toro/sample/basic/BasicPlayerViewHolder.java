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
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.helper.SimpleExoPlayerViewHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.widget.Container;

/**
 * @author eneim (7/1/17).
 */

@SuppressWarnings("WeakerAccess") //
public class BasicPlayerViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {

  private static final String TAG = "Toro:Holder:ExoPlayer";

  static final int LAYOUT_RES = R.layout.view_holder_exoplayer_basic;

  SimpleExoPlayerViewHelper helper;
  Uri mediaUri;

  @BindView(R.id.player) SimpleExoPlayerView playerView;

  public BasicPlayerViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  @NonNull @Override public View getPlayerView() {
    return playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
  }

  @Override
  public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
    if (helper == null) {
      helper = new SimpleExoPlayerViewHelper(container, this, mediaUri);
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
    Log.d(TAG, "release() called: " + getAdapterPosition());
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

  @Override public String toString() {
    return "ExoPlayer{" + hashCode() + " " + getAdapterPosition() + "}";
  }

  void bind(VideoData videoData) {
    this.mediaUri = videoData.getMediaUri();
  }
}
