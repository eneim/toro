/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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

package im.ene.lab.toro.ext.youtube;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;
import android.view.ViewParent;
import com.google.android.youtube.player.YouTubePlayer;
import im.ene.lab.toro.ToroPlayerViewHelper;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.PlayerViewHelper;
import im.ene.lab.toro.media.PlaybackException;

/**
 * Created by eneim on 4/8/16.
 */
public class YoutubePlayerViewHelper extends PlayerViewHelper {

  private static volatile YoutubePlayerViewHelper INSTANCE;
  private final ToroPlayerViewHelper delegate;

  public YoutubePlayerViewHelper() {
    super();
    delegate = ToroPlayerViewHelper.getInstance();
  }

  // Prefer to use singleton of this class. This method must be call on UiThread
  @UiThread public static YoutubePlayerViewHelper getInstance() {
    if (INSTANCE == null) {
      synchronized (YoutubePlayerViewHelper.class) {
        INSTANCE = new YoutubePlayerViewHelper();
      }
    }

    return INSTANCE;
  }

  // Adapt from YoutubePlayer
  public void onPlaying() {
  }

  public void onPaused() {
  }

  public void onBuffering(boolean b) {
  }

  public void onSeekTo(int i) {
  }

  public void onLoading() {
  }

  public void onLoaded(String s) {
  }

  public void onAdStarted() {
  }

  public void onVideoStarted(ToroPlayer player) {
    player.onPlaybackStarted();
  }

  public void onVideoEnded(ToroPlayer player) {
    player.onPlaybackStopped();
  }

  public void onYoutubeError(ToroPlayer player, YouTubePlayer.ErrorReason errorReason) {
    PlaybackException error =
        errorReason != null ? new PlaybackException(errorReason.name(), 0, 0) : null;
    player.onPlaybackError(null, error);
  }

  public void onYoutubePlayerChanged(YouTubePlayer newPlayer) {
    // Do I need to do anything?
  }

  // Adapt from RecyclerViewItemHelper

  @Override public void onAttachedToParent(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {
    delegate.onAttachedToParent(player, itemView, parent);
  }

  @Override public void onDetachedFromParent(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {
    delegate.onDetachedFromParent(player, itemView, parent);
  }

  @Override public boolean onItemLongClick(@NonNull ToroPlayer player, @NonNull View itemView,
      @Nullable ViewParent parent) {
    return delegate.onItemLongClick(player, itemView, parent);
  }
}
