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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.cache.Cache;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

/**
 * Necessary configuration for {@link ExoCreator} to produces {@link SimpleExoPlayer} and
 * {@link MediaSource}. Instance of this class must be construct using {@link Builder}.
 *
 * @author eneim (2018/01/23).
 * @since 3.4.0
 */

public final class Config {

  // primitive flags
  @ExtensionRendererMode final int extensionMode;

  // NonNull options
  @NonNull final BaseMeter meter;
  @NonNull final LoadControl loadControl;
  @NonNull final MediaSourceBuilder mediaSourceBuilder;

  // Nullable options
  @SuppressWarnings("WeakerAccess") //
  @Nullable final DrmSessionManager drmSessionManager;
  @Nullable final Cache cache; // null by default

  Config(int extensionMode, @NonNull BaseMeter meter, @NonNull LoadControl loadControl,
      @NonNull MediaSourceBuilder mediaSourceBuilder, @Nullable DrmSessionManager drmSessionManager,
      @Nullable Cache cache) {
    this.extensionMode = extensionMode;
    this.meter = meter;
    this.loadControl = loadControl;
    this.mediaSourceBuilder = mediaSourceBuilder;
    this.drmSessionManager = drmSessionManager;
    this.cache = cache;
  }

  @SuppressWarnings("unused") public Builder newBuilder() {
    return new Builder().setCache(this.cache)
        .setDrmSessionManager(this.drmSessionManager)
        .setExtensionMode(this.extensionMode)
        .setLoadControl(this.loadControl)
        .setMediaSourceBuilder(this.mediaSourceBuilder)
        .setMeter(this.meter);
  }

  /// Builder
  @SuppressWarnings({ "unused", "WeakerAccess" }) //
  public static class Builder {
    @ExtensionRendererMode private int extensionMode = EXTENSION_RENDERER_MODE_OFF;
    private final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    @SuppressWarnings("unchecked")  //
    private BaseMeter meter = new BaseMeter(bandwidthMeter, bandwidthMeter);
    private LoadControl loadControl = new DefaultLoadControl();
    private MediaSourceBuilder mediaSourceBuilder = MediaSourceBuilder.DEFAULT;
    private DrmSessionManager drmSessionManager = null;
    private Cache cache = null;

    public Builder setExtensionMode(@ExtensionRendererMode int extensionMode) {
      this.extensionMode = extensionMode;
      return this;
    }

    public Builder setMeter(@NonNull BaseMeter meter) {
      this.meter = checkNotNull(meter, "Need non-null BaseMeter");
      return this;
    }

    public Builder setLoadControl(@NonNull LoadControl loadControl) {
      this.loadControl = checkNotNull(loadControl, "Need non-null LoadControl");
      return this;
    }

    public Builder setMediaSourceBuilder(@NonNull MediaSourceBuilder mediaSourceBuilder) {
      this.mediaSourceBuilder =
          checkNotNull(mediaSourceBuilder, "Need non-null MediaSourceBuilder");
      return this;
    }

    public Builder setDrmSessionManager(@Nullable DrmSessionManager drmSessionManager) {
      this.drmSessionManager = drmSessionManager;
      return this;
    }

    public Builder setCache(@Nullable Cache cache) {
      this.cache = cache;
      return this;
    }

    public Config build() {
      return new Config(extensionMode, meter, loadControl,  //
          mediaSourceBuilder, drmSessionManager, cache);
    }
  }
}
