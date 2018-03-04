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

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;

import static im.ene.toro.ToroUtil.checkNotNull;

/**
 * @author eneim (8/1/17).
 */

@SuppressWarnings("WeakerAccess") //
final class YouTubePlayerHelper extends ToroPlayerHelper implements Handler.Callback {

  private static final String TAG = "YouT:Helper";

  static final int MSG_INIT = 1000;
  static final int MSG_PLAY = 1001;
  static final int MSG_PAUSE = 1002;
  static final int MSG_DELAY = 50;

  // Actions must be done in order/queue, so we use a handle to make that happens.
  final Handler handler = new Handler(this);
  final Context context;
  final String videoId;
  final Callback callback;

  final PlaybackInfo playbackInfo = new PlaybackInfo();
  @IntRange(from = 1) final int playerViewId; // also the Id for playerFragment's container

  YouTubePlayer youTubePlayer;
  ToroYouTubePlayerFragment ytFragment;

  YouTubePlayerHelper(@NonNull Callback callback, @NonNull ToroPlayer player, String videoId) {
    super(player);
    //noinspection ConstantConditions
    if (player.getPlayerView() == null) {
      throw new IllegalArgumentException("Player's view must not be null.");
    }

    this.context = checkNotNull(player.getPlayerView().getContext());

    this.callback = callback;
    this.playerViewId = player.getPlayerView().getId();
    this.videoId = videoId;
  }

  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    if (playbackInfo != null) {
      this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
      this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
    }

    handler.sendEmptyMessageDelayed(MSG_INIT, MSG_DELAY);
  }

  @Override public void play() {
    handler.sendEmptyMessageDelayed(MSG_PLAY, MSG_DELAY);
  }

  @Override public void pause() {
    handler.removeCallbacksAndMessages(null);
    handler.sendEmptyMessage(MSG_PAUSE);
  }

  @Override public void setVolume(float volume) {
    throw new UnsupportedOperationException("YouTubeApi doesn't allow to do this.");
  }

  @Override public float getVolume() {
    return 1;
  }

  @Override public boolean isPlaying() {
    return youTubePlayer != null;
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    updateResumePosition();
    return new PlaybackInfo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
  }

  void updateResumePosition() {
    if (youTubePlayer != null) {
      playbackInfo.setResumePosition(youTubePlayer.getCurrentTimeMillis());
    }
  }

  String getVideoId() {
    return videoId;
  }

  ToroPlayer getToroPlayer() {
    return this.player;
  }

  @Override public void release() {
    Log.d(TAG, "release() called, " + player);
    handler.removeCallbacksAndMessages(null);
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
    Log.i(TAG, "handleMessage: " + message.what + ", " + player.getPlayerOrder());
    switch (message.what) {
      case MSG_INIT:
        // Since this is Fragment transaction, it will be handled by the Adapter.
        break;
      case MSG_PLAY:
        if (ytFragment == null || !ytFragment.isVisible()) break;
        final YouTubePlayerHelper helper = YouTubePlayerHelper.this; // make a local access
        ytFragment.initialize(BuildConfig.API_KEY, new YouTubePlayer.OnInitializedListener() {
          @Override
          public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean b) {
            helper.youTubePlayer = player;
            if (helper.callback != null) helper.callback.onPlayerCreated(helper, player);
            player.setPlayerStateChangeListener(new StateChangeImpl());
            player.setShowFullscreenButton(true);  // fullscreen requires more work ...
            player.setOnFullscreenListener(new FullScreenListenerImpl());
            if (shouldPlay()) { // make sure YouTubePlayerView is fully visible.
              player.loadVideo(videoId, (int) helper.playbackInfo.getResumePosition());
            }
          }

          @Override public void onInitializationFailure(Provider provider,
              YouTubeInitializationResult result) {
            throw new RuntimeException("YouTube init error: " + result.name());
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

  class StateChangeImpl implements PlayerStateChangeListener {

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

    @Override public void onError(YouTubePlayer.ErrorReason errorReason) {
      // Force a crash to log to Fabric.
      // throw new RuntimeException("YouTubePlayer Error: " + errorReason);
    }
  }

  class FullScreenListenerImpl implements YouTubePlayer.OnFullscreenListener {

    @Override public void onFullscreen(boolean fullscreen) {
      Log.d(TAG, "onFullscreen() called with: fullscreen = [" + fullscreen + "], " + player);
      if (callback != null) {
        callback.onFullscreen(YouTubePlayerHelper.this, YouTubePlayerHelper.this.youTubePlayer,
            fullscreen);
      }
    }
  }

  boolean shouldPlay() {
    if (ytFragment == null || !ytFragment.isVisible()) return false;
    View ytView = ytFragment.getView();
    return ytView != null && visibleAreaOffset(ytView) >= 0.999;
  }

  @Override public String toString() {
    return "Toro:Yt:Helper{" + "videoId='" + videoId + '\'' + ", player=" + player + '}';
  }

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

    void onPlayerCreated(YouTubePlayerHelper helper, YouTubePlayer player);

    void onPlayerDestroyed(YouTubePlayerHelper helper);

    void onFullscreen(YouTubePlayerHelper helper, YouTubePlayer player, boolean fullscreen);
  }
}
