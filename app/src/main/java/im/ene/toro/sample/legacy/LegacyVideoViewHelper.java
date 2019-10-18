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

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import com.google.android.exoplayer2.C;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroPlayer.State;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import java.util.Set;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;
import static im.ene.toro.ToroUtil.checkNotNull;

/**
 * @author eneim | 6/11/17.
 *
 * Helper class for {@link ToroVideoView}. This class makes the playback using {@link
 * ToroVideoView} easier by wrapping all necessary components and functionality.
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
  ToroPlayer.ErrorListeners errorListeners = new ToroPlayer.ErrorListeners();

  @State int playerState = State.STATE_IDLE;
  boolean playWhenReady = false;  // mimic the ExoPlayer
  // TODO merge this into playbackInfo field.
  final VolumeInfo volumeInfo = new VolumeInfo(false, 1f);

  public LegacyVideoViewHelper(ToroPlayer player, @NonNull Uri mediaUri) {
    super(player);
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

  @SuppressLint("ObsoleteSdkInt")
  @Override protected void initialize(@NonNull final PlaybackInfo playbackInfo) {
    this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());

    final LegacyVideoViewHelper helper = LegacyVideoViewHelper.this;
    // On Complete event, we reset the player, re-prepare the VideoView so that it can be re-used.
    this.playerView.setOnCompletionListener(mp -> {
      playerState = State.STATE_END;
      onPlayerStateUpdated(playWhenReady, playerState);
      if (helper.onCompletionListener != null) {
        helper.onCompletionListener.onCompletion(mp);
      }
      // Reset the player so it can be reused.
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
      if (helper.onPreparedListener != null) {
        helper.onPreparedListener.onPrepared(mp);
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
          case MEDIA_INFO_VIDEO_RENDERING_START:
            // Call immediately.
            helper.internalListener.onFirstFrameRendered();
            for (ToroPlayer.EventListener listener : helper.getEventListeners()) {
              listener.onFirstFrameRendered();
            }
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
      errorListeners.onError(new RuntimeException("Error: " + what + ", " + extra));
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

  @Override public void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo) {
    this.playbackInfo.setVolumeInfo(playbackInfo.getVolumeInfo());
    this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
    this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());

    if (this.playbackInfo.getResumePosition() >= 0) {
      this.playerView.seekTo((int) this.playbackInfo.getResumePosition());
    }
    this.setVolumeInfo(this.playbackInfo.getVolumeInfo());
  }

  @Override public void setVolume(float volume) {
    this.setVolumeInfo(new VolumeInfo(volume == 0, volume));
  }

  @Override public float getVolume() {
    return volumeInfo.getVolume();
  }

  @Override public void setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
    if (mediaPlayer == null) return;
    boolean changed = !this.volumeInfo.equals(checkNotNull(volumeInfo));
    if (changed) {
      float volume = volumeInfo.isMute() ? 0 : volumeInfo.getVolume();
      mediaPlayer.setVolume(volume, volume);
      this.volumeInfo.setTo(volumeInfo.isMute(), volumeInfo.getVolume());
      if (volumeChangeListeners != null) {
        for (ToroPlayer.OnVolumeChangeListener listener : volumeChangeListeners) {
          listener.onVolumeChanged(volumeInfo);
        }
      }
    }
  }

  @NonNull @Override public VolumeInfo getVolumeInfo() {
    return this.volumeInfo;
  }

  // Use a Set to prevent duplicated setup.
  protected Set<ToroPlayer.OnVolumeChangeListener> volumeChangeListeners;

  void updateResumePosition() {
    try {
      if (mediaPlayer != null) playbackInfo.setResumePosition(mediaPlayer.getCurrentPosition());
    } catch (IllegalStateException er) {
      er.printStackTrace();
    }
  }

  @SuppressLint("ObsoleteSdkInt")
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
