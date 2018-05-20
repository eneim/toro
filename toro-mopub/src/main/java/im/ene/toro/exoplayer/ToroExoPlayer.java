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

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.VolumeInfo;
import java.util.HashSet;
import java.util.Set;

/**
 * A custom {@link SimpleExoPlayer} that also notify the change of Volume.
 *
 * @author eneim (2018/03/27).
 */
public class ToroExoPlayer extends SimpleExoPlayer {

  @SuppressWarnings("WeakerAccess")
  protected ToroExoPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector,
      LoadControl loadControl) {
    super(renderersFactory, trackSelector, loadControl);
  }

  private Set<ToroPlayer.OnVolumeChangeListener> listeners;

  public final void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
    if (this.listeners == null) this.listeners = new HashSet<>();
    this.listeners.add(ToroUtil.checkNotNull(listener));
  }

  public final void removeOnVolumeChangeListener(ToroPlayer.OnVolumeChangeListener listener) {
    if (this.listeners != null) this.listeners.remove(listener);
  }

  public final void clearOnVolumeChangeListener() {
    if (this.listeners != null) this.listeners.clear();
  }

  @CallSuper
  @Override public void setVolume(float audioVolume) {
    this.setVolumeInfo(new VolumeInfo(audioVolume == 0, audioVolume));
  }

  private final VolumeInfo volumeInfo = new VolumeInfo(false, 1f);

  @SuppressWarnings("UnusedReturnValue")  //
  @CallSuper
  public boolean setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
    boolean changed = !this.volumeInfo.equals(volumeInfo);
    if (changed) {
      this.volumeInfo.setTo(volumeInfo.isMute(), volumeInfo.getVolume());
      super.setVolume(volumeInfo.isMute() ? 0 : volumeInfo.getVolume());
      if (listeners != null) {
        for (ToroPlayer.OnVolumeChangeListener listener : this.listeners) {
          listener.onVolumeChanged(volumeInfo);
        }
      }
    }

    return changed;
  }

  @SuppressWarnings("unused") @NonNull public VolumeInfo getVolumeInfo() {
    return volumeInfo;
  }
}
