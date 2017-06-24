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
import android.os.Build;
import android.support.annotation.NonNull;
import com.google.android.exoplayer2.C;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroPlayer.State;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import im.ene.toro.widget.ToroVideoView;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;

/**
 * @author eneim | 6/11/17.
 */
@SuppressWarnings({ "WeakerAccess", "ConstantConditions" }) //
public class LegacyVideoViewHelper extends ToroPlayerHelper {

  @NonNull final ToroVideoView playerView;
  @NonNull final Uri mediaUri;

  MediaPlayer mediaPlayer;  // obtain in onPrepared, free at release.
  MediaPlayer.OnCompletionListener onCompletionListener;
  MediaPlayer.OnPreparedListener onPreparedListener;

  @State int playerState = State.STATE_IDLE;
  boolean playWhenReady = false;

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

  @Override public void initialize(@NonNull final PlaybackInfo playbackInfo) {
    this.playerView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mp) {
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
      }
    });
    this.playerView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        playerState = State.STATE_READY;
        onPlayerStateUpdated(playWhenReady, playerState);

        mediaPlayer = mp;
        if (LegacyVideoViewHelper.this.onPreparedListener != null) {
          LegacyVideoViewHelper.this.onPreparedListener.onPrepared(mp);
        }
        if (playWhenReady) play();
      }
    });
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      this.playerView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
        @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
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
        }
      });
    }
    this.playerView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
      @Override public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;  // to prevent the system error dialog.
      }
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
    if (playbackInfo.getResumePosition() >= 0) {
      this.playerView.seekTo((int) playbackInfo.getResumePosition());
    }
  }

  public void play() {
    this.playerView.start();
  }

  public void pause() {
    this.playerView.pause();
  }

  public boolean isPlaying() {
    return playWhenReady || this.playerView.isPlaying();
  }

  @Override public PlaybackInfo updatePlaybackInfo() {
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
    this.playerView.setPlayerEventListener(null);
    this.playerView.setOnErrorListener(null);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      this.playerView.setOnInfoListener(null);
    }
    this.mediaPlayer = null;
    this.playerState = State.STATE_IDLE;
    this.playWhenReady = false;
    super.cancel();
  }
}
