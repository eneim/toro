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

package im.ene.lab.toro.ext;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import com.google.android.youtube.player.YouTubePlayer;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.VideoPlayerManager;
import im.ene.lab.toro.VideoPlayerManagerImpl;

/**
 * Created by eneim on 4/8/16.
 *
 * Youtube Video Manager + Video Adapter. This is the core of Youtube Support Extension.
 */
/* public */ abstract class YouTubeVideosAdapter<VH extends YouTubeItemViewHolder> extends ToroAdapter<VH>
    implements VideoPlayerManager {

  final FragmentManager fragmentManager;
  private final VideoPlayerManager delegate;
  YouTubePlayer youTubePlayer;
  /* package */ int lastPlayerPosition;

  public YouTubeVideosAdapter(FragmentManager fragmentManager) {
    super();
    this.fragmentManager = fragmentManager;
    this.delegate = new VideoPlayerManagerImpl();
    this.lastPlayerPosition = -1;
  }

  /**
   * @return latest Video Player
   */
  @Override public ToroPlayer getPlayer() {
    return delegate.getPlayer();
  }

  /**
   * Set current video player. There would be at most one Video player at a time.
   *
   * @param player the current Video Player of this manager
   */
  @Override public void setPlayer(ToroPlayer player) {
    delegate.setPlayer(player);
  }

  @Override public void onRegistered() {

  }

  @Override public void onUnregistered() {

  }

  /**
   * Start playing current video
   */
  @Override public void startPlayback() {
    delegate.startPlayback();
  }

  /**
   * Pause current video
   */
  @Override public void pausePlayback() {
    delegate.pausePlayback();
  }

  /**
   * Save current video state
   */
  @Override public void saveVideoState(String videoId, @Nullable Long position, long duration) {
    delegate.saveVideoState(videoId, position, duration);
  }

  /**
   * Restore and setup state of a Video to current video player
   */
  @Override public void restoreVideoState(String videoId) {
    delegate.restoreVideoState(videoId);
  }

  @Nullable @Override public Long getSavedPosition(String videoId) {
    return delegate.getSavedPosition(videoId);
  }

  @Override public void stopPlayback() {
    delegate.stopPlayback();
  }

  // Adapt from YouTubePlayer.PlaybackEventListener

  public void onPlaying() {
    // video starts playing
    // Do nothing on parent though
  }

  public void onPaused() {
    // Do nothing on parent though
  }

  public void onError(YouTubePlayer.ErrorReason errorReason) {
    // Do nothing on parent though
  }
}
