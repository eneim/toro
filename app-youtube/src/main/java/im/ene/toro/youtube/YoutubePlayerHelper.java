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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

import static im.ene.toro.youtube.BuildConfig.API_KEY;

/**
 * @author eneim (8/1/17).
 */

@SuppressWarnings("WeakerAccess") //
class YoutubePlayerHelper extends ToroPlayerHelper implements Handler.Callback {

  final FragmentManager fragmentManager;
  final PlaybackInfo playbackInfo = new PlaybackInfo();
  final String videoId;

  int playerViewId; // also the Id for playerFragment's container
  YouTubePlayer youTubePlayer;
  YouTubePlayerSupportFragment playerFragment;

  final Handler handler = new Handler(this);
  final int MSG_INIT = 1000;
  final int MSG_PLAY = 1001;
  final int MSG_PAUSE = 1002;
  final int MSG_DELAY = 300;

  YoutubePlayerHelper(@NonNull Container container, @NonNull ToroPlayer player,
      FragmentManager fragmentManager, String videoId) {
    super(container, player);
    //noinspection ConstantConditions
    if (player.getPlayerView() == null) {
      throw new IllegalArgumentException("Player's view must not be null.");
    }
    this.fragmentManager = fragmentManager;
    this.videoId = videoId;
  }

  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    if (playbackInfo != null) {
      this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
      this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
    }

    if (container.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
      handler.sendEmptyMessageDelayed(MSG_INIT, MSG_DELAY);
      nextMsg = -1;
    } else {
      handler.removeCallbacksAndMessages(null);
      nextMsg = MSG_INIT;
    }
  }

  @Override public void play() {
    if (container.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
      handler.sendEmptyMessageDelayed(MSG_PLAY, MSG_DELAY);
      nextMsg = -1;
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
      fragmentManager.beginTransaction().remove(playerFragment).commitNowAllowingStateLoss();
      playerFragment = null;
    }

    super.release();
  }

  @Override public boolean handleMessage(Message message) {
    switch (message.what) {
      case MSG_INIT:
        // 1. Remove current fragment if there is one
        this.playerViewId = player.getPlayerView().getId();
        if (playerViewId != View.NO_ID) {
          playerFragment = (YouTubePlayerSupportFragment) fragmentManager.findFragmentById(
              playerViewId);
        }
        if (playerFragment != null) {
          fragmentManager.beginTransaction().remove(playerFragment).commitNowAllowingStateLoss();
          playerFragment = null;
        }
        // 2. Generate new unique Id for the fragment's container and add new fragment.
        playerViewId = ViewUtil.generateViewId();
        player.getPlayerView().setId(playerViewId);
        playerFragment = YouTubePlayerSupportFragment.newInstance();
        fragmentManager.beginTransaction().replace(playerViewId, playerFragment).commitNowAllowingStateLoss();
        break;
      case MSG_PLAY:
        if (playerFragment == null || !playerFragment.isVisible()) break;
        playerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
          @Override
          public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean b) {
            YoutubePlayerHelper.this.youTubePlayer = player;
            player.setShowFullscreenButton(false);  // may lead to weird behavior
            player.loadVideo(videoId,
                (int) YoutubePlayerHelper.this.playbackInfo.getResumePosition());
          }

          @Override public void onInitializationFailure(Provider provider,
              YouTubeInitializationResult result) {
            throw new RuntimeException("Failed with result: " + result.name());
          }
        });
        break;
      case MSG_PAUSE:
        updateResumePosition();
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

  int nextMsg = -1; // message 'what' to be executed after Container stops scrolling

  @Override public void onContainerScrollStateChange(int newState) {
    super.onContainerScrollStateChange(newState);
    if (newState != RecyclerView.SCROLL_STATE_IDLE) return;
    if (nextMsg != -1) {
      handler.sendEmptyMessageDelayed(nextMsg, MSG_DELAY);
      nextMsg = -1;
    }
  }
}
