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
import android.support.v4.app.FragmentManager;
import android.view.View;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

/**
 * @author eneim (8/1/17).
 */

@SuppressWarnings("WeakerAccess") //
final class YouTubePlayerHelper extends ToroPlayerHelper implements Handler.Callback {

  private static final String TAG = "Toro:Yt:Helper";

  static final int MSG_INIT = 1000;
  static final int MSG_PLAY = 1001;
  static final int MSG_PAUSE = 1002;
  static final int MSG_DELAY = 50;
  static final int MSG_NONE = -1;

  final Handler handler = new Handler(this);
  final Context context;
  final String videoId;
  final FragmentManager manager;
  final PlaybackInfo playbackInfo = new PlaybackInfo();
  @IntRange(from = 1) final int playerViewId; // also the Id for playerFragment's container

  YouTubePlayer youTubePlayer;
  YouTubePlayerSupportFragment ytFragment;

  YouTubePlayerHelper(@NonNull Container container, @NonNull ToroPlayer player,
      FragmentManager fragmentManager, String videoId) {
    super(container, player);
    //noinspection ConstantConditions
    if (player.getPlayerView() == null) {
      throw new IllegalArgumentException("Player's view must not be null.");
    }

    this.context = container.getContext();
    this.playerViewId = player.getPlayerView().getId();
    this.manager = fragmentManager;
    this.videoId = videoId;
  }

  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    if (playbackInfo != null) {
      this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
      this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
    }

    handler.sendEmptyMessageDelayed(MSG_INIT, MSG_DELAY);
    nextMsg = MSG_NONE;
  }

  @Override public void play() {
    handler.sendEmptyMessageDelayed(MSG_PLAY, MSG_DELAY);
    nextMsg = MSG_NONE;
  }

  @Override public void pause() {
    handler.removeCallbacksAndMessages(null);
    handler.sendEmptyMessage(MSG_PAUSE);
  }

  @Override public boolean isPlaying() {
    return youTubePlayer != null;
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    updateResumePosition();
    return new PlaybackInfo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
  }

  private void updateResumePosition() {
    if (youTubePlayer != null) {
      playbackInfo.setResumePosition(youTubePlayer.getCurrentTimeMillis());
    }
  }

  @Override public void release() {
    handler.removeCallbacksAndMessages(null);
    releasePlayer();
    if (ytFragment != null) {
      if (ytFragment.isVisible()) manager.beginTransaction().remove(ytFragment).commitNow();
      ytFragment = null;
    }
    super.release();
  }

  void releasePlayer() {
    if (youTubePlayer != null) {
      youTubePlayer.release();
      youTubePlayer = null;
    }
  }

  @Override public boolean handleMessage(Message message) {
    switch (message.what) {
      case MSG_INIT:
        // 1. Remove current fragment if there is one
        ytFragment = (YouTubePlayerSupportFragment) manager.findFragmentById(playerViewId);
        if (ytFragment != null) {
          manager.beginTransaction().remove(ytFragment).commitNow();
        }
        // 2. Generate new unique Id for the fragment's container and add new fragment.
        ytFragment = YouTubePlayerSupportFragment.newInstance();
        manager.beginTransaction().replace(playerViewId, ytFragment).commitNow();
        break;
      case MSG_PLAY:
        if (ytFragment == null || !ytFragment.isVisible()) break;
        final YouTubePlayerHelper helper = YouTubePlayerHelper.this; // make a local access
        ytFragment.initialize(BuildConfig.API_KEY, new YouTubePlayer.OnInitializedListener() {
          @Override
          public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean b) {
            helper.youTubePlayer = player;
            player.setPlayerStateChangeListener(new StateChangeImpl());
            player.setShowFullscreenButton(true);  // fullscreen requires more work ...
            player.setFullscreen(false);
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
        }
        break;
      default:
        break;
    }
    return true;
  }

  int nextMsg = MSG_NONE; // message (the 'what') to be executed after Container stops scrolling

  class StateChangeImpl implements PlayerStateChangeListener {

    @Override public void onLoading() {

    }

    @Override public void onLoaded(String s) {

    }

    @Override public void onAdStarted() {

    }

    @Override public void onVideoStarted() {

    }

    @Override public void onVideoEnded() {

    }

    @Override public void onError(YouTubePlayer.ErrorReason errorReason) {
      // Force a crash to log to Fabric.
      throw new RuntimeException("YouTubePlayer Error: " + errorReason);
    }
  }

  boolean shouldPlay() {
    if (ytFragment == null || !ytFragment.isVisible()) return false;
    View ytView = ytFragment.getView();
    return ytView != null && visibleAreaOffset(ytView) >= 0.999;
  }

  static float visibleAreaOffset(@NonNull View playerView) {
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
}
