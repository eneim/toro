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

import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static im.ene.toro.ToroUtil.checkNotNull;
import static im.ene.toro.exoplayer.ToroExo.with;
import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;

/**
 * Base implementation for {@link Playable}
 *
 * @author eneim (2018/04/30).
 * @since 3.5.0
 */
@SuppressWarnings("WeakerAccess") //
abstract class BasePlayableImpl<VIEW> implements Playable<VIEW> {

  private final PlaybackInfo playbackInfo = new PlaybackInfo(); // never expose to outside.

  protected final EventListeners listeners = new EventListeners();  // original listener.
  // Use a Set to prevent duplicated setup.
  protected Set<ToroPlayer.OnVolumeChangeListener> volumeChangeListeners;
  protected ToroPlayer.ErrorListeners errorListeners;

  protected final Uri mediaUri; // immutable, parcelable
  protected final String fileExt;
  protected final ExoCreator creator; // required, cached

  protected SimpleExoPlayer player; // on-demand, cached
  protected MediaSource mediaSource;  // on-demand
  protected VIEW playerView; // on-demand, not always required.

  private boolean listenerApplied = false;

  BasePlayableImpl(ExoCreator creator, Uri uri, String fileExt) {
    this.creator = creator;
    this.mediaUri = uri;
    this.fileExt = fileExt;
  }

  @CallSuper @Override public void prepare(boolean prepareSource) {
    if (player == null) {
      player = with(checkNotNull(creator.getContext(), "ExoCreator has no Context")) //
          .requestPlayer(creator);
      if (player instanceof ToroExoPlayer) {
        if (volumeChangeListeners != null) {
          for (ToroPlayer.OnVolumeChangeListener listener : volumeChangeListeners) {
            ((ToroExoPlayer) player).addOnVolumeChangeListener(listener);
          }
        }
      }
    }

    if (!listenerApplied) {
      player.addListener(listeners);
      player.setVideoListener(listeners);
      player.setTextOutput(listeners);
      player.setMetadataOutput(listeners);
      listenerApplied = true;
    }

    ToroExo.setVolumeInfo(player, playbackInfo.getVolumeInfo());
    boolean haveResumePosition = playbackInfo.getResumeWindow() != C.INDEX_UNSET;
    if (haveResumePosition) {
      player.seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
    }

    if (prepareSource) {
      ensurePlayerView();
      ensureMediaSource();
    }
  }

  @Nullable @Override public VIEW getPlayerView() {
    return playerView;
  }

  @CallSuper @Override public void play() {
    checkNotNull(player, "Playable#play(): Player is null!");
    ensurePlayerView();
    ensureMediaSource();
    player.setPlayWhenReady(true);
  }

  @CallSuper @Override public void pause() {
    checkNotNull(player, "Playable#pause(): Player is null!").setPlayWhenReady(false);
  }

  @CallSuper @Override public void reset() {
    this.playbackInfo.reset();
    if (player != null) player.stop();
    this.mediaSource = null; // so it will be re-prepared when play() is called.
  }

  @CallSuper @Override public void release() {
    this.setPlayerView(null);
    if (this.player != null) {
      this.player.stop();
      if (this.player instanceof ToroExoPlayer) {
        ((ToroExoPlayer) this.player).clearOnVolumeChangeListener();
      }
      if (listenerApplied) {
        player.removeListener(listeners);
        player.setVideoListener(null);
        player.setTextOutput(null);
        player.setMetadataOutput(null);
        listenerApplied = false;
      }
      with(checkNotNull(creator.getContext(), "ExoCreator has no Context")) //
          .releasePlayer(this.creator, this.player);
    }
    this.player = null;
    this.mediaSource = null;
  }

  @CallSuper @NonNull @Override public PlaybackInfo getPlaybackInfo() {
    updatePlaybackInfo();
    return new PlaybackInfo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition(),
        playbackInfo.getVolumeInfo());
  }

  @CallSuper @Override public void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo) {
    this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
    this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
    this.setVolumeInfo(playbackInfo.getVolumeInfo());

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

  @Override public void addErrorListener(@NonNull ToroPlayer.OnErrorListener listener) {
    if (this.errorListeners == null) this.errorListeners = new ToroPlayer.ErrorListeners();
    this.errorListeners.add(listener);
  }

  @Override public void removeErrorListener(@Nullable ToroPlayer.OnErrorListener listener) {
    if (this.errorListeners != null) this.errorListeners.remove(listener);
  }

  @Override public boolean isPlaying() {
    return player != null && player.getPlayWhenReady();
  }

  final void updatePlaybackInfo() {
    if (player == null || player.getPlaybackState() == ExoPlayer.STATE_IDLE) return;
    playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
    playbackInfo.setResumePosition(player.isCurrentWindowSeekable() ? //
        Math.max(0, player.getCurrentPosition()) : TIME_UNSET);
    playbackInfo.setVolumeInfo(ToroExo.getVolumeInfo(player));
  }

  protected abstract void ensurePlayerView();

  private void ensureMediaSource() {
    if (mediaSource == null) {  // Only actually prepare the source when play() is called.
      mediaSource = creator.createMediaSource(mediaUri, fileExt);
      player.prepare(mediaSource, playbackInfo.getResumeWindow() == C.INDEX_UNSET, false);
    }
  }
}
