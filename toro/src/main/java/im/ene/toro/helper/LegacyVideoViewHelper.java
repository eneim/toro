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

package im.ene.toro.helper;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.exoplayer2.C;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import im.ene.toro.widget.ToroVideoView;

/**
 * @author eneim | 6/11/17.
 */

public class LegacyVideoViewHelper extends PlayerViewHelper {

  @NonNull private final ToroVideoView playerView;
  private final Uri mediaUri;
  @SuppressWarnings("WeakerAccess") MediaPlayer mediaPlayer;  // just in case.

  @SuppressWarnings("WeakerAccess") MediaPlayer.OnCompletionListener onCompletionListener;
  @SuppressWarnings("WeakerAccess") MediaPlayer.OnPreparedListener onPreparedListener;

  public LegacyVideoViewHelper(Container container, ToroPlayer player, Uri mediaUri) {
    super(container, player);
    if (!(player.getPlayerView() instanceof ToroVideoView)) {
      throw new IllegalArgumentException("Only VideoView is supported.");
    }
    this.playerView = (ToroVideoView) player.getPlayerView();
    this.mediaUri = mediaUri;
  }

  public void setOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
    this.onCompletionListener = onCompletionListener;
  }

  public void setOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
    this.onPreparedListener = onPreparedListener;
  }

  @Override public void initialize(@NonNull final PlaybackInfo playbackInfo) throws Exception {
    this.playerView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mp) {
        LegacyVideoViewHelper.super.onPlayerStateUpdated(mp != null && mp.isPlaying(), 4);
        if (LegacyVideoViewHelper.this.onCompletionListener != null) {
          LegacyVideoViewHelper.this.onCompletionListener.onCompletion(mp);
        }
      }
    });
    this.playerView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared() called with: mp = [" + mp + "]");
        mediaPlayer = mp;
        if (LegacyVideoViewHelper.this.onPreparedListener != null) {
          LegacyVideoViewHelper.this.onPreparedListener.onPrepared(mp);
        }
      }
    });

    this.playerView.setPlaybackEventListener(new ToroVideoView.PlaybackEventListener() {
      @Override public void onPlay() {
        LegacyVideoViewHelper.super.onPlayerStateUpdated(
            mediaPlayer != null && mediaPlayer.isPlaying(), 3);
      }

      @Override public void onPause() {
        LegacyVideoViewHelper.super.onPlayerStateUpdated(false, 3);
      }
    });

    this.playerView.setVideoURI(mediaUri);
    if (playbackInfo.getResumePosition() >= 0) {
      this.playerView.seekTo((int) playbackInfo.getResumePosition());
    }
  }

  @Override public void play() {
    this.playerView.start();
  }

  @Override public void pause() {
    this.playerView.pause();
  }

  @Override public boolean isPlaying() {
    return this.playerView.isPlaying();
  }

  @Override public PlaybackInfo getPlaybackInfo() {
    int position = 0;
    try {
      position = mediaPlayer != null ? mediaPlayer.getCurrentPosition() : C.POSITION_UNSET;
    } catch (IllegalStateException er) {
      er.printStackTrace();
    }

    return new PlaybackInfo(C.INDEX_UNSET, position);
  }

  @Override public void cancel() throws Exception {
    this.playerView.setOnCompletionListener(null);
    this.playerView.setOnPreparedListener(null);
    this.playerView.setPlaybackEventListener(null);
    this.mediaPlayer = null;
    super.cancel();
  }
}
