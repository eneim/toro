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

import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
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
final class YoutubePlayerHelper extends ToroPlayerHelper implements Handler.Callback {

  final FragmentManager manager;
  final PlaybackInfo playbackInfo = new PlaybackInfo();
  final String videoId;
  @IntRange(from = 1) final int playerViewId; // also the Id for playerFragment's container

  YouTubePlayer youTubePlayer;
  YouTubePlayerSupportFragment playerFragment;

  final Handler handler = new Handler(this);
  final int MSG_INIT = 1000;
  final int MSG_PLAY = 1001;
  final int MSG_PAUSE = 1002;
  final int MSG_DELAY = 50;
  final int MSG_NONE = -1;

  YoutubePlayerHelper(@NonNull Container container, @NonNull ToroPlayer player,
      FragmentManager fragmentManager, String videoId) {
    super(container, player);
    //noinspection ConstantConditions
    if (player.getPlayerView() == null) {
      throw new IllegalArgumentException("Player's view must not be null.");
    }

    this.playerViewId = player.getPlayerView().getId();
    this.manager = fragmentManager;
    this.videoId = videoId;
  }

  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    if (playbackInfo != null) {
      this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
      this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
    }

    if (container.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
      handler.sendEmptyMessageDelayed(MSG_INIT, MSG_DELAY);
      nextMsg = MSG_NONE;
    } else {
      handler.removeCallbacksAndMessages(null);
      nextMsg = MSG_INIT;
    }
  }

  @Override public void play() {
    if (container.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
      handler.sendEmptyMessageDelayed(MSG_PLAY, MSG_DELAY);
      nextMsg = MSG_NONE;
    } else {
      handler.removeCallbacksAndMessages(null);
      nextMsg = MSG_PLAY;
    }
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
    // TODO reproduce the case YoutubePlayer is released by System, and fix it.
    if (youTubePlayer != null) {
      playbackInfo.setResumePosition(youTubePlayer.getCurrentTimeMillis());
    }
  }

  @Override public void release() {
    handler.removeCallbacksAndMessages(null);
    if (youTubePlayer != null) {
      youTubePlayer.release();
      youTubePlayer = null;
    }

    if (playerFragment != null) {
      if (playerFragment.isAdded()) manager.beginTransaction().remove(playerFragment).commitNow();
      playerFragment = null;
    }
    super.release();
  }

  @Override public boolean handleMessage(Message message) {
    switch (message.what) {
      case MSG_INIT:
        // 1. Remove current fragment if there is one
        playerFragment = (YouTubePlayerSupportFragment) manager.findFragmentById(playerViewId);
        if (playerFragment != null) {
          manager.beginTransaction().remove(playerFragment).commitNow();
        }
        // 2. Generate new unique Id for the fragment's container and add new fragment.
        playerFragment = YouTubePlayerSupportFragment.newInstance();
        manager.beginTransaction().replace(playerViewId, playerFragment).commitNow();
        break;
      case MSG_PLAY:
        if (playerFragment == null || !playerFragment.isVisible()) break;
        final YoutubePlayerHelper helper = YoutubePlayerHelper.this; // make a local access
        playerFragment.initialize(BuildConfig.API_KEY, new YouTubePlayer.OnInitializedListener() {
          @Override
          public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean b) {
            helper.youTubePlayer = player;
            player.setShowFullscreenButton(false);  // fullscreen requires more work ...
            player.loadVideo(videoId, (int) helper.playbackInfo.getResumePosition());
          }

          @Override public void onInitializationFailure(Provider provider,
              YouTubeInitializationResult result) {
            throw new RuntimeException("Failed with result: " + result.name());
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

  @Override public void onSettled() {
    super.onSettled();
    if (nextMsg != MSG_NONE) {
      handler.sendEmptyMessageDelayed(nextMsg, MSG_DELAY);
      nextMsg = MSG_NONE;
    }
  }
}
