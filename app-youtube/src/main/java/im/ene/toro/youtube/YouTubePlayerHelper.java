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

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewParent;
import android.widget.Toast;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author eneim (8/1/17).
 */

@SuppressWarnings("WeakerAccess") //
final class YouTubePlayerHelper extends ToroPlayerHelper implements Handler.Callback {

  static final int MSG_INIT = 1000;
  static final int MSG_PLAY = 1001;
  static final int MSG_PAUSE = 1002;
  static final int MSG_DELAY = 50;

  // Actions must be done in order/queue, so we use a handler to make that happens.
  final String videoId;

  final PlaybackInfo playbackInfo = new PlaybackInfo();
  final VolumeInfo volumeInfo = new VolumeInfo(false, 1f);
  final AtomicBoolean playWhenReady = new AtomicBoolean(false);

  Callback callback;
  Handler handler;
  YouTubePlayer youTubePlayer;
  ToroYouTubePlayerFragment ytFragment;
  ToroPlayer.ErrorListeners errorListeners = new ToroPlayer.ErrorListeners();

  YouTubePlayerHelper(@NonNull ToroPlayer player, String videoId) {
    super(player);
    //noinspection ConstantConditions
    if (player.getPlayerView() == null) {
      throw new IllegalArgumentException("Player's view must not be null.");
    }

    this.videoId = videoId;
  }

  @Override protected void initialize(@NonNull PlaybackInfo playbackInfo) {
    this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
    this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
    playWhenReady.set(false);
    if (handler == null) handler = new Handler(this);
    handler.sendEmptyMessageDelayed(MSG_INIT, MSG_DELAY);
  }

  @Override public void play() {
    playWhenReady.set(true);
    if (handler != null) handler.sendEmptyMessageDelayed(MSG_PLAY, MSG_DELAY);
  }

  @Override public void pause() {
    playWhenReady.set(false);
    if (handler != null) {
      handler.removeCallbacksAndMessages(null);
      handler.sendEmptyMessage(MSG_PAUSE);
    }
  }

  @Override public void setVolume(float volume) {
    throw new UnsupportedOperationException("YouTubeApi doesn't allow to do this.");
  }

  @Override public float getVolume() {
    return volumeInfo.getVolume();
  }

  @Override public void setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
    throw new UnsupportedOperationException("YouTubeApi doesn't allow to do this.");
  }

  @NonNull @Override public VolumeInfo getVolumeInfo() {
    return volumeInfo;
  }

  @Override
  public void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
    throw new UnsupportedOperationException("YouTube helper doesn't allow to do this.");
  }

  @Override public void removeOnVolumeChangeListener(ToroPlayer.OnVolumeChangeListener listener) {
    throw new UnsupportedOperationException("YouTube helper doesn't allow to do this.");
  }

  @Override public boolean isPlaying() {
    return youTubePlayer != null;
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    updateResumePosition();
    return new PlaybackInfo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  void updateResumePosition() {
    if (youTubePlayer != null) {
      playbackInfo.setResumePosition(youTubePlayer.getCurrentTimeMillis());
    }
  }

  @Override public void release() {
    if (handler != null) {
      handler.removeCallbacksAndMessages(null);
      handler = null;
    }
    playWhenReady.set(false);
    // If the player is paused by Toro, youtubePlayer will be released before this call,
    // But in case the Fragment is released by System, "pause()" is not called yet, we use
    // Fragment Lifecycle callback to ask the Adapter to do the cleanup and it will call this method.
    if (youTubePlayer != null) {
      youTubePlayer.release();
      youTubePlayer = null;
      if (callback != null) callback.onPlayerDestroyed(this);
    }
    super.release();
  }

  @Override public boolean handleMessage(Message message) {
    switch (message.what) {
      case MSG_INIT:
        // Prepare the Fragment and put it in place.
        // Since this is Fragment transaction, it will be handled by the Manager.
        break;
      case MSG_PLAY:
        if (ytFragment == null || !ytFragment.isVisible()) break; // Not visible, do nothing.
        final YouTubePlayerHelper helper = YouTubePlayerHelper.this; // Make a local copy.
        ytFragment.initialize(BuildConfig.API_KEY, new YouTubePlayer.OnInitializedListener() {
          @Override
          public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean b) {
            helper.youTubePlayer = player;
            if (helper.callback != null) helper.callback.onPlayerCreated(helper, player);
            player.setPlayerStateChangeListener(new StateChangeListenerImpl());
            player.setPlaybackEventListener(new PlaybackEventListenerImpl());
            player.setShowFullscreenButton(false);  // fullscreen requires more work ...
            if (shouldPlay()) { // Make sure YouTubePlayerView is playable at this moment.
              player.loadVideo(videoId, (int) helper.playbackInfo.getResumePosition());
            }
          }

          @Override public void onInitializationFailure(Provider provider,
              YouTubeInitializationResult result) {
            Exception error = new RuntimeException("YouTube init error: " + result.name());
            errorListeners.onError(error);
          }
        });
        break;
      case MSG_PAUSE:
        updateResumePosition();
        // Because YoutubePlayerSDK allows up to one player instance to be used at once.
        // We need to release current player so that other widget can be playable.
        if (youTubePlayer != null) {
          youTubePlayer.release();
          youTubePlayer = null;
          if (callback != null) callback.onPlayerDestroyed(this);
        }
        break;
      default:
        break;
    }
    return true;
  }

  class StateChangeListenerImpl implements PlayerStateChangeListener {

    @Override public void onLoading() {

    }

    @Override public void onLoaded(String videoId) {

    }

    @Override public void onAdStarted() {

    }

    @Override public void onVideoStarted() {

    }

    @Override public void onVideoEnded() {

    }

    @Override public void onError(YouTubePlayer.ErrorReason reason) {
      // if (BuildConfig.DEBUG) throw new RuntimeException("YouTubePlayer Error: " + reason);
      errorListeners.onError(new RuntimeException(reason.toString()));
      if (ytFragment != null && ytFragment.isAdded()) {
        Toast.makeText(ytFragment.requireContext(), "Error: " + reason, Toast.LENGTH_SHORT).show();
      }
    }
  }

  class PlaybackEventListenerImpl implements YouTubePlayer.PlaybackEventListener {

    @Override public void onPlaying() {
      onPlayerStateUpdated(playWhenReady.get(), ToroPlayer.State.STATE_READY);
    }

    @Override public void onPaused() {
      onPlayerStateUpdated(playWhenReady.get(), ToroPlayer.State.STATE_READY);
    }

    @Override public void onStopped() {
      onPlayerStateUpdated(playWhenReady.get(), ToroPlayer.State.STATE_END);
    }

    @Override public void onBuffering(boolean b) {
      onPlayerStateUpdated(playWhenReady.get(), ToroPlayer.State.STATE_BUFFERING);
    }

    @Override public void onSeekTo(int i) {

    }
  }

  @Override public void addErrorListener(@NonNull ToroPlayer.OnErrorListener errorListener) {
    errorListeners.add(ToroUtil.checkNotNull(errorListener));
  }

  @Override public void removeErrorListener(ToroPlayer.OnErrorListener errorListener) {
    errorListeners.remove(errorListener);
  }

  // Ensure that we are in the good situation.
  boolean shouldPlay() {
    if (ytFragment == null || !ytFragment.isVisible()) return false;
    View ytView = ytFragment.getView();
    return ytView != null && visibleAreaOffset(ytView) >= 0.999;  // fully visible.
  }

  @Override public String toString() {
    return "Toro:Yt:Helper{" + "videoId='" + videoId + '\'' + ", player=" + player + '}';
  }

  ToroPlayer getPlayer() {
    return player;
  }

  /** Mimic {@link ToroUtil#visibleAreaOffset(ToroPlayer, ViewParent)} */
  private static float visibleAreaOffset(@NonNull View playerView) {
    Rect drawRect = new Rect();
    playerView.getDrawingRect(drawRect);
    int drawArea = drawRect.width() * drawRect.height();

    Rect playerRect = new Rect();
    boolean visible = playerView.getGlobalVisibleRect(playerRect, new Point());

    float offset = 0.f;
    if (visible && drawArea > 0) {
      int visibleArea = playerRect.height() * playerRect.width();
      offset = visibleArea / (float) drawArea;
    }
    return offset;
  }

  interface Callback {

    void onPlayerCreated(@NonNull YouTubePlayerHelper helper, @NonNull YouTubePlayer player);

    void onPlayerDestroyed(@NonNull YouTubePlayerHelper helper);

    @SuppressWarnings("unused") void onFullscreen(@NonNull YouTubePlayerHelper helper,
        @Nullable YouTubePlayer player, boolean fullscreen);
  }
}
