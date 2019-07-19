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

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import im.ene.toro.youtube.common.AspectRatioFrameLayout;
import im.ene.toro.youtube.common.ViewUtil;

import static im.ene.toro.ToroUtil.visibleAreaOffset;

/**
 * @author eneim (8/1/17).
 */

@SuppressWarnings({ "WeakerAccess", "unused" }) //
final class YouTubeVideoViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {

  private static final String TAG = "YouT:ViewHolder";

  static final int LAYOUT_RES = R.layout.view_holder_youtube_player_full;

  private final RequestOptions options =
      new RequestOptions().fitCenter().placeholder(R.drawable.ic_launcher_background);
  private final YouTubePlayerManager helperManager;

  String videoId;
  YouTubePlayerHelper helper;

  final TextView videoName;
  final TextView videoCaption;
  final ImageView thumbnailView;
  final FrameLayout playerView;
  final AspectRatioFrameLayout playerViewContainer;

  YouTubeVideoViewHolder(YouTubePlayerManager helperManager, View itemView) {
    super(itemView);
    this.helperManager = helperManager;
    playerViewContainer = itemView.findViewById(R.id.player_container);
    videoName = itemView.findViewById(R.id.video_id);
    videoCaption = itemView.findViewById(R.id.video_description);
    thumbnailView = itemView.findViewById(R.id.thumbnail);

    playerView = itemView.findViewById(R.id.player_view);
    int viewId = ViewUtil.generateViewId();
    playerView.setId(viewId);
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
      helper = helperManager.obtainHelper(this, this.videoId);
    }

    helper.initialize(container, playbackInfo);
    thumbnailView.setVisibility(View.VISIBLE);
  }

  @Override public void play() {
    thumbnailView.setVisibility(View.GONE);
    if (helper != null) helper.play();
  }

  @Override public void pause() {
    if (helper != null) helper.pause();
    thumbnailView.setVisibility(View.VISIBLE);
  }

  @Override public boolean isPlaying() {
    return helper != null && helper.isPlaying();
  }

  @Override public void release() {
    thumbnailView.setVisibility(View.VISIBLE);
    helperManager.releaseHelper(this);
    this.helper = null;
  }

  @Override public boolean wantsToPlay() {
    // YouTube Player API requires the player view to be fully visible.
    return visibleAreaOffset(this, itemView.getParent()) >= 0.999;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }

  void bind(@NonNull Video item) {
    this.videoId = item.getId();
    this.videoName.setText(item.getSnippet().getTitle());
    this.videoCaption.setText(item.getSnippet().getDescription());

    Thumbnail thumbnail = item.getSnippet().getThumbnails().getHigh();
    if (thumbnail != null && thumbnail.getHeight() > 0) {
      playerViewContainer.setAspectRatio(thumbnail.getWidth() / (float) thumbnail.getHeight());
      Glide.with(itemView).load(thumbnail.getUrl()).apply(options).into(thumbnailView);
    }
  }

  @Override public String toString() {
    return "Player{" + "videoId='" + videoId + '\'' + ", order=" + getPlayerOrder() + '}';
  }
}
