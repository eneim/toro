/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package im.ene.toro.exoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.widget.Toast;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsMediaSource.MediaSourceFactory;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroPlayer.ErrorListeners;
import im.ene.toro.ToroPlayer.VolumeChangeListeners;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import im.ene.toro.media.Media;

import static com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS;
import static im.ene.toro.ToroUtil.checkNotNull;
import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;

/**
 * @author eneim (2018/10/12).
 */
@SuppressWarnings("WeakerAccess") //
public class DefaultPlayable implements Playable {

  private static final String TAG = "ToroLib:X:Playable";

  static final int MODE_MASK =
      Player.REPEAT_MODE_OFF | Player.REPEAT_MODE_ONE | Player.REPEAT_MODE_ALL;

  @NonNull final Context context;
  @NonNull final Media media;
  @NonNull final MediaSourceFactory mediaSourceFactory;
  @NonNull final ExoPlayerManager playerManager;

  @NonNull final PlaybackInfo playbackInfo = new PlaybackInfo();
  @NonNull final EventListeners listeners = new EventListeners();  // original listener.
  @NonNull final VolumeChangeListeners volumeChangeListeners = new VolumeChangeListeners();

  private PlayerEventListener listener;
  private ErrorMessageProvider<ExoPlaybackException> errorMessageProvider;
  protected ErrorListeners errorListeners;

  protected ExoPlayer player;
  protected MediaSource mediaSource;
  protected PlayerView playerView;
  protected PlaybackParameters playbackParameters;

  // Adapt from ExoPlayer demo.
  TrackGroupArray lastSeenTrackGroupArray;
  boolean inErrorState = false;
  private boolean sourcePrepared = false;
  private boolean listenerApplied = false;
  private int repeatMode = ToroPlayer.RepeatMode.REPEAT_MODE_OFF;

  public DefaultPlayable( //
      @NonNull Context context, //
      @NonNull Media media, //
      @NonNull MediaSourceFactoryProvider mediaSourceFactoryProvider, //
      @NonNull ExoPlayerManager playerManager //
  ) {
    this.context = context.getApplicationContext();
    this.media = media;
    this.playerManager = playerManager;
    this.mediaSourceFactory = mediaSourceFactoryProvider.provideMediaSourceFactory(media);
  }

  @Override public void prepare(boolean prepareSource) {
    if (listener == null) {
      listener = new PlayerEventListener();
      this.addEventListener(listener);
    }
    if (player == null) {
      sourcePrepared = false;
      player = playerManager.acquireExoPlayer(this.media);
      listenerApplied = false;
    }
    if (prepareSource) {
      prepareMediaSource();
      ensurePlayerView();
    }
    this.lastSeenTrackGroupArray = null;
    this.inErrorState = false;
  }

  @CallSuper @Override public void setPlayerView(@Nullable PlayerView playerView) {
    if (this.playerView == playerView) return;
    this.lastSeenTrackGroupArray = null;
    this.inErrorState = false;
    if (playerView == null) {
      this.playerView.setPlayer(null);
      this.playerView.setErrorMessageProvider(null);
    } else {
      if (this.player != null) {
        PlayerView.switchTargetView(this.player, this.playerView, playerView);
      }
    }

    this.playerView = playerView;
    if (this.playerView != null) {
      if (errorMessageProvider == null) errorMessageProvider = new PlayerErrorMessageProvider();
      this.playerView.setErrorMessageProvider(errorMessageProvider);
    }
  }

  @Override public final PlayerView getPlayerView() {
    return this.playerView;
  }

  @Override public void play() {
    prepareMediaSource();
    checkNotNull(player, "Playable#play(): Player is null!");
    ensurePlayerView();
    player.setPlayWhenReady(true);
  }

  @Override public void pause() {
    // Player is not required to be non-null here.
    if (player != null) player.setPlayWhenReady(false);
  }

  @Override public void reset() {
    this.playbackInfo.reset();
    if (player != null) {
      // Reset volume to default
      MediaHub.setVolumeInfo(this.player, new VolumeInfo(false, 1.f));
      player.stop(true);
    }
    this.mediaSource = null; // So it will be re-prepared when play() is called.
    this.sourcePrepared = false;
    this.lastSeenTrackGroupArray = null;
    this.inErrorState = false;
  }

  @Override public void release() {
    if (listener != null) {
      this.removeEventListener(listener);
      listener = null;
    }
    this.setPlayerView(null);
    if (this.player != null) {
      // Reset player volume to default
      MediaHub.setVolumeInfo(player, new VolumeInfo(false, 1.f));
      if (player instanceof VolumeInfoController) {
        ((VolumeInfoController) player).clearOnVolumeChangeListener();
      }
      if (listenerApplied) {
        MediaHub.removeEventListener(player, listeners);
        listenerApplied = false;
      }
      player.stop(true);
      // This call will also call this.player.stop(true) to reset internal resource, still reusable.
      playerManager.releasePlayer(media, player);
    }
    this.player = null;
    this.mediaSource = null;
    this.sourcePrepared = false;
    this.lastSeenTrackGroupArray = null;
    this.inErrorState = false;
  }

  @CallSuper @NonNull @Override public PlaybackInfo getPlaybackInfo() {
    updatePlaybackInfo();
    return new PlaybackInfo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition(),
        playbackInfo.getVolumeInfo());
  }

  @CallSuper @Override public void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo) {
    this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
    this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
    this.playbackInfo.setVolumeInfo(playbackInfo.getVolumeInfo());

    if (player != null) {
      MediaHub.setVolumeInfo(player, this.playbackInfo.getVolumeInfo());
      boolean haveResumePosition = this.playbackInfo.getResumeWindow() != INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(this.playbackInfo.getResumeWindow(), this.playbackInfo.getResumePosition());
      }
    }
  }

  @Override public final void addEventListener(@NonNull EventListener listener) {
    this.listeners.add(checkNotNull(listener));
  }

  @Override public final void removeEventListener(EventListener listener) {
    this.listeners.remove(listener);
  }

  @Override public final void setVolume(float volume) {
    throw new UnsupportedOperationException("Deprecated");
  }

  @Override public final float getVolume() {
    throw new UnsupportedOperationException("Deprecated");
  }

  @Override public boolean setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
    return player != null && MediaHub.setVolumeInfo(this.player, volumeInfo);
  }

  @NonNull @Override public VolumeInfo getVolumeInfo() {
    return player != null ? MediaHub.getVolumeInfo(player) : playbackInfo.getVolumeInfo();
  }

  @Override public void setParameters(@Nullable PlaybackParameters parameters) {
    this.playbackParameters = parameters;
    if (player != null) player.setPlaybackParameters(this.playbackParameters);
  }

  @Nullable @Override public PlaybackParameters getParameters() {
    return this.playbackParameters;
  }

  @SuppressLint("WrongConstant") @Override public void setRepeatMode(int repeatMode) {
    this.repeatMode = repeatMode;
    if (this.player != null) this.player.setRepeatMode(this.repeatMode & MODE_MASK);
  }

  @Override public int getRepeatMode() {
    return this.repeatMode;
  }

  @Override
  public void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
    volumeChangeListeners.add(checkNotNull(listener));
  }

  @Override
  public void removeOnVolumeChangeListener(@Nullable ToroPlayer.OnVolumeChangeListener listener) {
    volumeChangeListeners.remove(listener);
  }

  @Override public boolean isPlaying() {
    return player != null && player.getPlayWhenReady();
  }

  @Override public void addErrorListener(@NonNull ToroPlayer.OnErrorListener listener) {
    if (errorListeners == null) errorListeners = new ErrorListeners();
    errorListeners.add(checkNotNull(listener));
  }

  @Override public void removeErrorListener(@Nullable ToroPlayer.OnErrorListener listener) {
    if (errorListeners != null) errorListeners.remove(listener);
  }

  final void updatePlaybackInfo() {
    if (player == null || player.getPlaybackState() == Player.STATE_IDLE) return;
    playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
    playbackInfo.setResumePosition(player.isCurrentWindowSeekable() ? //
        Math.max(0, player.getCurrentPosition()) : TIME_UNSET);
    playbackInfo.setVolumeInfo(MediaHub.getVolumeInfo(player));
  }

  private void ensurePlayerView() {
    if (playerView != null && playerView.getPlayer() != player) playerView.setPlayer(player);
  }

  private void prepareMediaSource() {
    // Note: we allow subclass to create MediaSource on demand. So it can be not-null here.
    // The flag sourcePrepared can also be set to false somewhere else.
    if (mediaSource == null) {
      sourcePrepared = false;
      mediaSource = mediaSourceFactory.createMediaSource(this.media.getUri());
    }

    if (!sourcePrepared) {
      ensurePlayer();
      player.prepare(mediaSource, playbackInfo.getResumeWindow() == C.INDEX_UNSET, false);
      sourcePrepared = true;
    }
  }

  @SuppressLint("WrongConstant")
  private void ensurePlayer() {
    if (player == null) {
      sourcePrepared = false;
      player = playerManager.acquireExoPlayer(this.media);
      listenerApplied = false;
    }

    if (!listenerApplied) {
      if (player instanceof VolumeInfoController) {
        ((VolumeInfoController) player).addOnVolumeChangeListener(volumeChangeListeners);
      }
      MediaHub.addEventListener(player, listeners);
      listenerApplied = true;
    }

    this.player.setPlaybackParameters(this.playbackParameters);
    boolean haveResumePosition = playbackInfo.getResumeWindow() != C.INDEX_UNSET;
    if (haveResumePosition) {
      player.seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
    }
    MediaHub.setVolumeInfo(player, this.playbackInfo.getVolumeInfo());
    this.player.setRepeatMode(this.repeatMode & MODE_MASK);
  }

  @SuppressWarnings({ "WeakerAccess", "unused" }) //
  protected final void onErrorMessage(@NonNull String message) {
    // Sub class can have custom reaction about the error here, including not to show this toast
    // (by not calling super.onErrorMessage(message)).
    if (this.errorListeners != null && this.errorListeners.size() > 0) {
      this.errorListeners.onError(new RuntimeException(message));
    } else if (playerView != null) {
      Toast.makeText(playerView.getContext(), message, Toast.LENGTH_SHORT).show();
    }
  }

  /// Some code from demo

  class PlayerEventListener extends DefaultEventListener {

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      super.onTracksChanged(trackGroups, trackSelections);
      if (trackGroups == lastSeenTrackGroupArray) return;
      lastSeenTrackGroupArray = trackGroups;
      TrackSelector trackSelector = playerManager instanceof DefaultExoPlayerManager
          ? ((DefaultExoPlayerManager) playerManager).getTrackSelector() : null;
      if (!(trackSelector instanceof MappingTrackSelector)) return;
      MappingTrackSelector selector = (MappingTrackSelector) trackSelector;
      MappingTrackSelector.MappedTrackInfo trackInfo = selector.getCurrentMappedTrackInfo();
      if (trackInfo != null) {
        if (trackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO) == RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
          onErrorMessage(context.getString(R.string.error_unsupported_video));
        }

        if (trackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO) == RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
          onErrorMessage(context.getString(R.string.error_unsupported_audio));
        }
      }
    }

    @Override public void onPlayerError(ExoPlaybackException error) {
      /// Adapt from ExoPlayer Demo
      if (playerView == null) {
        String errorString = null;
        if (error.type == ExoPlaybackException.TYPE_RENDERER) {
          Exception cause = error.getRendererException();
          if (cause instanceof DecoderInitializationException) {
            // Special case for decoder initialization failures.
            DecoderInitializationException exception = (DecoderInitializationException) cause;
            if (exception.decoderName == null) {
              if (exception.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                errorString = context.getString(R.string.error_querying_decoders);
              } else if (exception.secureDecoderRequired) {
                errorString =
                    context.getString(R.string.error_no_secure_decoder, exception.mimeType);
              } else {
                errorString = context.getString(R.string.error_no_decoder, exception.mimeType);
              }
            } else {
              errorString =
                  context.getString(R.string.error_instantiating_decoder, exception.decoderName);
            }
          }
        }

        if (errorString != null) onErrorMessage(errorString);
      }

      inErrorState = true;
      if (isBehindLiveWindow(error)) {
        reset();
      } else {
        updatePlaybackInfo();
      }

      super.onPlayerError(error);
    }

    @Override public void onPositionDiscontinuity(int reason) {
      if (inErrorState) {
        // Adapt from ExoPlayer demo.
        // "This will only occur if the user has performed a seek whilst in the error state. Update
        // the resume position so that if the user then retries, playback will resume from the
        // position to which they seek." - ExoPlayer
        updatePlaybackInfo();
      }

      super.onPositionDiscontinuity(reason);
    }
  }

  static boolean isBehindLiveWindow(ExoPlaybackException error) {
    if (error.type != ExoPlaybackException.TYPE_SOURCE) return false;
    Throwable cause = error.getSourceException();
    while (cause != null) {
      if (cause instanceof BehindLiveWindowException) return true;
      cause = cause.getCause();
    }
    return false;
  }

  class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {

    @Override public Pair<Integer, String> getErrorMessage(ExoPlaybackException e) {
      String errorString = context.getString(R.string.error_generic);
      if (e.type == ExoPlaybackException.TYPE_RENDERER) {
        Exception cause = e.getRendererException();
        if (cause instanceof DecoderInitializationException) {
          // Special case for decoder initialization failures.
          DecoderInitializationException exception = (DecoderInitializationException) cause;
          if (exception.decoderName == null) {
            if (exception.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
              errorString = context.getString(R.string.error_querying_decoders);
            } else if (exception.secureDecoderRequired) {
              errorString = context.getString(R.string.error_no_secure_decoder, exception.mimeType);
            } else {
              errorString = context.getString(R.string.error_no_decoder, exception.mimeType);
            }
          } else {
            errorString =
                context.getString(R.string.error_instantiating_decoder, exception.decoderName);
          }
        }
      }

      return Pair.create(0, errorString);
    }
  }
}
