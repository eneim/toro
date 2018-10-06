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

package toro.v4;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.exoplayer.ToroExo;
import im.ene.toro.exoplayer.ToroExoPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import java.util.concurrent.CopyOnWriteArraySet;

import static im.ene.toro.ToroUtil.checkNotNull;
import static im.ene.toro.exoplayer.ToroExo.with;
import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;

/**
 * @author eneim (2018/10/04).
 *
 * A {@link Playable} to work with {@link MediaHub}.
 */
public class PlayableImpl implements Playable {

  private final MediaHub mediaHub;
  private final Media media;

  private final PlaybackInfo playbackInfo = new PlaybackInfo(); // never expose to outside.

  private final EventListeners listeners = new EventListeners();  // original listener.
  // Use a CopyOnWriteArraySet to prevent duplicated setup and modify while iterating.
  private CopyOnWriteArraySet<ToroPlayer.OnVolumeChangeListener> volumeChangeListeners;
  private ToroPlayer.ErrorListeners errorListeners;

  protected SimpleExoPlayer player; // on-demand, cached
  protected PlayerView playerView; // on-demand, not always required.
  private MediaSource mediaSource;  // on-demand, since we do not reuse MediaSource now.

  private boolean sourcePrepared = false;
  private boolean listenerApplied = false;

  PlayableImpl(MediaHub mediaHub, Media media) {
    this.mediaHub = mediaHub;
    this.media = media;
  }

  @CallSuper @Override public void prepare(boolean prepareSource) {
    if (prepareSource) {
      ensureMediaSource();
      ensurePlayerView();
    }
  }

  @CallSuper @Override public void setPlayerView(@Nullable PlayerView playerView) {
    if (this.playerView == playerView) return;
    if (playerView == null) {
      this.playerView.setPlayer(null);
    } else {
      if (this.player != null) {
        PlayerView.switchTargetView(this.player, this.playerView, playerView);
      }
    }

    this.playerView = playerView;
  }

  @Override public final PlayerView getPlayerView() {
    return this.playerView;
  }

  @CallSuper @Override public void play() {
    ensureMediaSource();
    ensurePlayerView();
    checkNotNull(player, "Playable#play(): Player is null!");
    player.setPlayWhenReady(true);
  }

  @CallSuper @Override public void pause() {
    // Player is not required to be non-null here.
    if (player != null) player.setPlayWhenReady(false);
  }

  @CallSuper @Override public void reset() {
    this.playbackInfo.reset();
    if (player != null) {
      // reset volume to default
      ToroExo.setVolumeInfo(this.player, new VolumeInfo(false, 1.f));
      player.stop(true);
    }
    // TODO [20180214] double check this when ExoPlayer 2.7.0 is released.
    // TODO [20180326] reusable MediaSource will be added after ExoPlayer 2.7.1.
    // TODO [20180702] back to this after updating ExoPlayer to 2.8.x
    this.mediaSource = null; // so it will be re-prepared when play() is called.
    this.sourcePrepared = false;
  }

  @CallSuper @Override public void release() {
    this.setPlayerView(null);
    if (this.player != null) {
      // reset volume to default
      ToroExo.setVolumeInfo(this.player, new VolumeInfo(false, 1.f));
      this.player.stop(true);
      if (this.player instanceof ToroExoPlayer) {
        ((ToroExoPlayer) this.player).clearOnVolumeChangeListener();
      }
      if (listenerApplied) {
        player.removeListener(listeners);
        player.removeVideoListener(listeners);
        player.removeTextOutput(listeners);
        player.removeMetadataOutput(listeners);
        listenerApplied = false;
      }
      with(checkNotNull(mediaHub.context, "ExoCreator has no Context")) //
          .releasePlayer(mediaHub.creator, this.player);
    }
    this.player = null;
    this.mediaSource = null;
    this.sourcePrepared = false;
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
      ToroExo.setVolumeInfo(player, this.playbackInfo.getVolumeInfo());
      boolean haveResumePosition = this.playbackInfo.getResumeWindow() != INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(this.playbackInfo.getResumeWindow(), this.playbackInfo.getResumePosition());
      }
    }
  }

  @Override public final void addEventListener(@NonNull EventListener listener) {
    //noinspection ConstantConditions
    if (listener != null) this.listeners.add(listener);
  }

  @Override public final void removeEventListener(EventListener listener) {
    this.listeners.remove(listener);
  }

  @CallSuper @Override public void setVolume(float volume) {
    checkNotNull(player, "Playable#setVolume(): Player is null!");
    playbackInfo.getVolumeInfo().setTo(volume == 0, volume);
    ToroExo.setVolumeInfo(player, this.playbackInfo.getVolumeInfo());
  }

  @CallSuper @Override public float getVolume() {
    return checkNotNull(player, "Playable#getVolume(): Player is null!").getVolume();
  }

  @Override public boolean setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
    checkNotNull(player, "Playable#setVolumeInfo(): Player is null!");
    boolean changed = !this.playbackInfo.getVolumeInfo().equals(checkNotNull(volumeInfo));
    if (changed) {
      this.playbackInfo.getVolumeInfo().setTo(volumeInfo.isMute(), volumeInfo.getVolume());
      ToroExo.setVolumeInfo(player, this.playbackInfo.getVolumeInfo());
    }
    return changed;
  }

  @NonNull @Override public VolumeInfo getVolumeInfo() {
    return this.playbackInfo.getVolumeInfo();
  }

  @Override public void setParameters(@Nullable PlaybackParameters parameters) {
    checkNotNull(player, "Playable#setParameters(PlaybackParameters): Player is null") //
        .setPlaybackParameters(parameters);
  }

  @Override public PlaybackParameters getParameters() {
    return checkNotNull(player, "Playable#getParameters(): Player is null").getPlaybackParameters();
  }

  @Override
  public void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
    if (volumeChangeListeners == null) volumeChangeListeners = new CopyOnWriteArraySet<>();
    volumeChangeListeners.add(ToroUtil.checkNotNull(listener));
    if (this.player instanceof ToroExoPlayer) {
      ((ToroExoPlayer) this.player).addOnVolumeChangeListener(listener);
    }
  }

  @Override
  public void removeOnVolumeChangeListener(@Nullable ToroPlayer.OnVolumeChangeListener listener) {
    if (volumeChangeListeners != null) {
      volumeChangeListeners.remove(listener);
      if (this.player instanceof ToroExoPlayer) {
        ((ToroExoPlayer) this.player).removeOnVolumeChangeListener(listener);
      }
    }
  }

  @Override public boolean isPlaying() {
    return player != null && player.getPlayWhenReady();
  }

  @Override public void addErrorListener(@NonNull ToroPlayer.OnErrorListener listener) {
    if (this.errorListeners == null) {
      this.errorListeners = new ToroPlayer.ErrorListeners();
    }
    this.errorListeners.add(checkNotNull(listener));
  }

  @Override public void removeErrorListener(@Nullable ToroPlayer.OnErrorListener listener) {
    if (this.errorListeners != null) this.errorListeners.remove(listener);
  }

  @SuppressWarnings("WeakerAccess") final void updatePlaybackInfo() {
    if (player == null || player.getPlaybackState() == Player.STATE_IDLE) return;
    playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
    playbackInfo.setResumePosition(player.isCurrentWindowSeekable() ? //
        Math.max(0, player.getCurrentPosition()) : TIME_UNSET);
    playbackInfo.setVolumeInfo(ToroExo.getVolumeInfo(player));
  }

  private void ensurePlayerView() {
    if (playerView != null && playerView.getPlayer() != player) playerView.setPlayer(player);
  }

  // TODO [20180822] Double check this.
  private void ensureMediaSource() {
    if (mediaSource == null) {  // Only actually prepare the source when play() is called.
      sourcePrepared = false;
      mediaSource = mediaHub.buildMediaSource(this.media);
    }

    if (!sourcePrepared) {
      ensurePlayer(); // sourcePrepared is set to false only when player is null.
      player.prepare(mediaSource, playbackInfo.getResumeWindow() == C.INDEX_UNSET, false);
      sourcePrepared = true;
    }
  }

  private void ensurePlayer() {
    if (player == null) {
      sourcePrepared = false;
      player = with(checkNotNull(mediaHub.context, "ExoCreator has no Context")) //
          .requestPlayer(mediaHub.creator);
      if (player instanceof ToroExoPlayer && volumeChangeListeners != null) {
        for (ToroPlayer.OnVolumeChangeListener listener : volumeChangeListeners) {
          ((ToroExoPlayer) player).addOnVolumeChangeListener(listener);
        }
      }
      ToroExo.setVolumeInfo(player, this.playbackInfo.getVolumeInfo());
      listenerApplied = false;
    }

    if (!listenerApplied) {
      player.addListener(listeners);
      player.addVideoListener(listeners);
      player.addTextOutput(listeners);
      player.addMetadataOutput(listeners);
      listenerApplied = true;
    }

    boolean haveResumePosition = playbackInfo.getResumeWindow() != C.INDEX_UNSET;
    if (haveResumePosition) {
      player.seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
    }
  }
}
