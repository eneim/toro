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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import im.ene.toro.media.PlaybackInfo;

/**
 * @author eneim (2018/01/04).
 */

public interface ExoPlayerManager {

  class Factory {
    public static ExoPlayerManager with(Context context) {
      return ExoPlayerManagerImpl.with(context);
    }
  }

  @NonNull Playback prepare(@NonNull Playback.Resource resource,
      @Nullable PlaybackInfo playbackInfo, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
      @ExtensionRendererMode int extensionMode, @Nullable EventListener listener);
}
