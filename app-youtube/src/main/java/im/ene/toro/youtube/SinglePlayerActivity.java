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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;

public class SinglePlayerActivity extends AppCompatActivity {

  private static final String TAG = "YouT:Single";

  ToroYouTubePlayerFragment fragment;
  YouTubePlayer youTubePlayer;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_single_player);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    if (savedInstanceState == null) {
      fragment = (ToroYouTubePlayerFragment) getSupportFragmentManager().findFragmentById(
          R.id.player_fragment);

      if (fragment != null) {
        fragment.initialize(BuildConfig.API_KEY, new YouTubePlayer.OnInitializedListener() {
          @Override
          public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
              boolean restored) {
            youTubePlayer = player;
            configPlayer();
            youTubePlayer.loadVideo("6ZfuNTqbHE8");
          }

          @Override public void onInitializationFailure(YouTubePlayer.Provider provider,
              YouTubeInitializationResult youTubeInitializationResult) {

          }
        });
      }
    }
  }

  void configPlayer() {
    if (this.youTubePlayer == null) return;
    //youTubePlayer.addFullscreenControlFlag(
    //    YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE
    //        | YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI);
    youTubePlayer.setPlayerStateChangeListener(new PlayerStateImpl());
    youTubePlayer.setPlaybackEventListener(new PlaybackEventImpl());
    youTubePlayer.setShowFullscreenButton(true);
  }

  YouTubePlayer.ErrorReason errorReason;

  class PlayerStateImpl implements YouTubePlayer.PlayerStateChangeListener {

    @Override public void onLoading() {

    }

    @Override public void onLoaded(String s) {

    }

    @Override public void onAdStarted() {

    }

    @Override public void onVideoStarted() {
      Log.i(TAG, "onVideoStarted() called");
    }

    @Override public void onVideoEnded() {
      Log.i(TAG, "onVideoEnded() called");
    }

    @Override public void onError(YouTubePlayer.ErrorReason errorReason) {
      Log.e(TAG, "onError() called with: errorReason = [" + errorReason + "]");
      SinglePlayerActivity.this.errorReason = errorReason;
    }
  }

  class PlaybackEventImpl implements YouTubePlayer.PlaybackEventListener {

    @Override public void onPlaying() {
      Log.d(TAG, "onPlaying() called");
    }

    @Override public void onPaused() {
      Log.d(TAG, "onPaused() called");
    }

    @Override public void onStopped() {
      Log.d(TAG, "onStopped() called");
      if (errorReason != null) Log.i(TAG, "onStopped: " + errorReason);
    }

    @Override public void onBuffering(boolean b) {
      Log.d(TAG, "onBuffering() called with: b = [" + b + "]");
    }

    @Override public void onSeekTo(int i) {

    }
  }
}
