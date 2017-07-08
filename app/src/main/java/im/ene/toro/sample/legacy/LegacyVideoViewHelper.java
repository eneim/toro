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

package im.ene.toro.sample.legacy;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.C;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroPlayer.State;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;

/**
 * @author eneim | 6/11/17.
 *
 *         Helper class for {@link ToroVideoView}. This class makes the playback using {@link
 *         ToroVideoView} easier by wrapping all necessary components and functionality.
 */

@SuppressWarnings({ "WeakerAccess", "ConstantConditions", "unused" }) //
public class LegacyVideoViewHelper extends ToroPlayerHelper {

  private static final String TAG = "Toro:Helper:Legacy";

  final PlaybackInfo playbackInfo = new PlaybackInfo();
  @NonNull final ToroVideoView playerView;
  @NonNull final Uri mediaUri;

  MediaPlayer mediaPlayer;  // obtain from onPrepared, free at release.
  MediaPlayer.OnCompletionListener onCompletionListener;
  MediaPlayer.OnPreparedListener onPreparedListener;

  @State int playerState = State.STATE_IDLE;
  boolean playWhenReady = false;  // mimic the ExoPlayer

  public LegacyVideoViewHelper(Container container, ToroPlayer player, @NonNull Uri mediaUri) {
    super(container, player);
    if (!(player.getPlayerView() instanceof ToroVideoView)) {
      throw new IllegalArgumentException("Only support ToroVideoView.");
    }
    if (mediaUri == null) {
      throw new IllegalArgumentException("Media Uri must not be null.");
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

  @Override public void initialize(@Nullable final PlaybackInfo playbackInfo) {
    if (playbackInfo != null) {
      this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
    }

    // On Complete event, we reset the player, re-prepare the VideoView so that it can be re-used.
    this.playerView.setOnCompletionListener(mp -> {
      playerState = State.STATE_END;
      onPlayerStateUpdated(playWhenReady, playerState);
      if (LegacyVideoViewHelper.this.onCompletionListener != null) {
        LegacyVideoViewHelper.this.onCompletionListener.onCompletion(mp);
      }
      // Reset the player so it can be immediately reused.
      if (mediaPlayer != null) {
        mediaPlayer.reset();
      }
      playWhenReady = false;  // !!Keeping playWhenReady as true will make a loop playback.
      playerView.setVideoURI(mediaUri);
    });

    this.playerView.setOnPreparedListener(mp -> {
      playerState = State.STATE_READY;
      onPlayerStateUpdated(playWhenReady, playerState);

      mediaPlayer = mp;
      if (LegacyVideoViewHelper.this.onPreparedListener != null) {
        LegacyVideoViewHelper.this.onPreparedListener.onPrepared(mp);
      }
      if (playWhenReady) play();
    });

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      this.playerView.setOnInfoListener((mp, what, extra) -> {
        boolean handled;
        switch (what) {
          case MEDIA_INFO_BUFFERING_START:
            playerState = State.STATE_BUFFERING;
            onPlayerStateUpdated(playWhenReady, playerState);
            handled = true;
            break;
          case MEDIA_INFO_BUFFERING_END:
            playerState = State.STATE_READY;
            onPlayerStateUpdated(playWhenReady, playerState);
            handled = true;
            break;
          default:
            handled = false;
            break;
        }
        return handled;
      });
    }

    this.playerView.setOnErrorListener((mp, what, extra) -> {
      return true;  // prevent the system error dialog.
    });

    this.playerView.setPlayerEventListener(new ToroVideoView.PlayerEventListener() {
      @Override public void onPlay() {
        playWhenReady = true;
        onPlayerStateUpdated(playWhenReady, playerState);
      }

      @Override public void onPause() {
        playWhenReady = false;
        onPlayerStateUpdated(playWhenReady, playerState);
      }
    });

    this.playerView.setVideoURI(mediaUri);
    if (this.playbackInfo.getResumePosition() >= 0) {
      this.playerView.seekTo((int) this.playbackInfo.getResumePosition());
    }
  }

  @Override public void play() {
    this.playerView.start();
  }

  @Override public void pause() {
    updateResumePosition();
    this.playerView.pause();
  }

  @Override public boolean isPlaying() {
    return playWhenReady || this.playerView.isPlaying(); // is actually playing or is buffering
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    updateResumePosition();
    return new PlaybackInfo(C.INDEX_UNSET, playbackInfo.getResumePosition());
  }

  void updateResumePosition() {
    try {
      if (mediaPlayer != null) playbackInfo.setResumePosition(mediaPlayer.getCurrentPosition());
    } catch (IllegalStateException er) {
      er.printStackTrace();
    }
  }

  @Override public void release() {
    this.playerView.setOnCompletionListener(null);
    this.playerView.setOnPreparedListener(null);
    this.playerView.setPlayerEventListener(null);
    this.playerView.setOnErrorListener(null);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      this.playerView.setOnInfoListener(null);
    }
    this.mediaPlayer = null;
    this.playerState = State.STATE_IDLE;
    this.playWhenReady = false;
    super.release();
  }
}
