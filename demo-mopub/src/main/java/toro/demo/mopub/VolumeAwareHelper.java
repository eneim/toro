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

package toro.demo.mopub;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroPlayer.OnVolumeChangeListener;
import im.ene.toro.exoplayer.PlayerViewHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;

/**
 * @author eneim (2018/03/26).
 */
public class VolumeAwareHelper extends PlayerViewHelper implements OnVolumeChangeListener {

  private static final String TAG = "Toro:Volume";

  private final VolumeAwarePlaybackInfo playbackInfo = new VolumeAwarePlaybackInfo();

  VolumeAwareHelper(@NonNull ToroPlayer player, @NonNull Uri uri) {
    super(player, uri);
  }

  @Override protected void initialize(@Nullable PlaybackInfo playbackInfo) {
    if (playbackInfo != null) {
      this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
      this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
      if (playbackInfo instanceof VolumeAwarePlaybackInfo) {
        VolumeInfo temp = ((VolumeAwarePlaybackInfo) playbackInfo).getVolumeInfo();
        this.playbackInfo.getVolumeInfo().setTo(temp.isMute(), temp.getVolume());
      }
    }

    super.initialize(playbackInfo);
    super.addOnVolumeChangeListener(this);
  }

  @Override public void release() {
    this.removeOnVolumeChangeListener(this);
    super.release();
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    PlaybackInfo superInfo = super.getLatestPlaybackInfo();
    this.playbackInfo.setResumePosition(superInfo.getResumePosition());
    this.playbackInfo.setResumeWindow(superInfo.getResumeWindow());
    return this.playbackInfo;
  }

  @Override public void onVolumeChanged(@NonNull VolumeInfo volumeInfo) {
    Log.d(TAG, "onVolumeChanged() called with: volumeInfo = [" + volumeInfo + "]");
    playbackInfo.getVolumeInfo().setTo(volumeInfo.isMute(), volumeInfo.getVolume());
  }
}
