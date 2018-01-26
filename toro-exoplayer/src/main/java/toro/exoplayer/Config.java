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

package toro.exoplayer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.cache.Cache;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;

/**
 * @author eneim (2018/01/23).
 */

final class Config {

  // primitive flags
  @ExtensionRendererMode int extensionMode = EXTENSION_RENDERER_MODE_OFF;

  private final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
  // NonNull options
  @SuppressWarnings("unchecked")  //
  @NonNull BaseMeter meter = new BaseMeter(bandwidthMeter, bandwidthMeter);

  // Nullable options
  @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
  @Nullable Cache cache = null;

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Config config = (Config) o;

    if (extensionMode != config.extensionMode) return false;
    if (!meter.equals(config.meter)) return false;
    if (drmSessionManager != null ? !drmSessionManager.equals(config.drmSessionManager)
        : config.drmSessionManager != null) {
      return false;
    }
    return cache != null ? cache.equals(config.cache) : config.cache == null;
  }

  @Override public int hashCode() {
    int result = meter.hashCode();
    result = 31 * result + (drmSessionManager != null ? drmSessionManager.hashCode() : 0);
    result = 31 * result + extensionMode;
    result = 31 * result + (cache != null ? cache.hashCode() : 0);
    return result;
  }
}
