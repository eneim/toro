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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.cache.Cache;
import im.ene.toro.annotations.Beta;
import java.util.Arrays;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
import static im.ene.toro.ToroUtil.checkNotNull;

/**
 * Necessary configuration for {@link ExoCreator} to produces {@link SimpleExoPlayer} and
 * {@link MediaSource}. Instance of this class must be construct using {@link Builder}.
 *
 * @author eneim (2018/01/23).
 * @since 3.4.0
 */

@SuppressWarnings("SimplifiableIfStatement")  //
public final class Config {

  // primitive flags
  @ExtensionRendererMode final int extensionMode;

  // NonNull options
  @NonNull final BaseMeter meter;
  @NonNull final LoadControl loadControl;
  @NonNull final MediaSourceBuilder mediaSourceBuilder;

  // Nullable options
  @SuppressWarnings("WeakerAccess") //
  @Nullable final DrmSessionManager[] drmSessionManagers;
  @Nullable final Cache cache; // null by default
  // If null, ExoCreator must come up with a default one.
  // This is to help customizing the Data source, for example using OkHttp extension.
  @Nullable final DataSource.Factory dataSourceFactory;

  Config(int extensionMode, @NonNull BaseMeter meter, @NonNull LoadControl loadControl,
      @Nullable DataSource.Factory dataSourceFactory, @NonNull MediaSourceBuilder mediaSourceBuilder,
      @Nullable DrmSessionManager[] drmSessionManagers, @Nullable Cache cache) {
    this.extensionMode = extensionMode;
    this.meter = meter;
    this.loadControl = loadControl;
    this.dataSourceFactory = dataSourceFactory;
    this.mediaSourceBuilder = mediaSourceBuilder;
    this.drmSessionManagers = drmSessionManagers;
    this.cache = cache;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Config config = (Config) o;

    if (extensionMode != config.extensionMode) return false;
    if (!meter.equals(config.meter)) return false;
    if (!loadControl.equals(config.loadControl)) return false;
    if (!mediaSourceBuilder.equals(config.mediaSourceBuilder)) return false;
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    if (!Arrays.equals(drmSessionManagers, config.drmSessionManagers)) return false;
    if (cache != null ? !cache.equals(config.cache) : config.cache != null) return false;
    return dataSourceFactory != null ? dataSourceFactory.equals(config.dataSourceFactory)
        : config.dataSourceFactory == null;
  }

  @Override public int hashCode() {
    int result = extensionMode;
    result = 31 * result + meter.hashCode();
    result = 31 * result + loadControl.hashCode();
    result = 31 * result + mediaSourceBuilder.hashCode();
    result = 31 * result + Arrays.hashCode(drmSessionManagers);
    result = 31 * result + (cache != null ? cache.hashCode() : 0);
    result = 31 * result + (dataSourceFactory != null ? dataSourceFactory.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("unused") public Builder newBuilder() {
    return new Builder().setCache(this.cache)
        .setDrmSessionManagers(this.drmSessionManagers)
        .setExtensionMode(this.extensionMode)
        .setLoadControl(this.loadControl)
        .setMediaSourceBuilder(this.mediaSourceBuilder)
        .setMeter(this.meter);
  }

  /// Builder
  @SuppressWarnings({ "unused", "WeakerAccess" }) //
  public static final class Builder {
    @ExtensionRendererMode private int extensionMode = EXTENSION_RENDERER_MODE_OFF;
    private final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    @SuppressWarnings("unchecked")  //
    private BaseMeter meter = new BaseMeter(bandwidthMeter, bandwidthMeter);
    private LoadControl loadControl = new DefaultLoadControl();
    private DataSource.Factory dataSourceFactory = null;
    private MediaSourceBuilder mediaSourceBuilder = MediaSourceBuilder.DEFAULT;
    private DrmSessionManager[] drmSessionManagers = null;
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

    // Option is Nullable, but if user customize this, it must be a Nonnull one.
    public Builder setDataSourceFactory(@NonNull DataSource.Factory dataSourceFactory) {
      this.dataSourceFactory = checkNotNull(dataSourceFactory);
      return this;
    }

    public Builder setMediaSourceBuilder(@NonNull MediaSourceBuilder mediaSourceBuilder) {
      this.mediaSourceBuilder =
          checkNotNull(mediaSourceBuilder, "Need non-null MediaSourceBuilder");
      return this;
    }

    @Beta
    public Builder setDrmSessionManagers(@Nullable DrmSessionManager[] drmSessionManagers) {
      this.drmSessionManagers = drmSessionManagers;
      return this;
    }

    public Builder setCache(@Nullable Cache cache) {
      this.cache = cache;
      return this;
    }

    public Config build() {
      return new Config(extensionMode, meter, loadControl, dataSourceFactory,
          mediaSourceBuilder, drmSessionManagers, cache);
    }
  }
}
