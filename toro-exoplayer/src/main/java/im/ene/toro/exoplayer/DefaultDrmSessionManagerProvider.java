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
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.widget.Toast;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.media.MediaDrm;
import java.util.HashMap;
import java.util.UUID;
import im.ene.toro.media.Media;

import static android.widget.Toast.LENGTH_SHORT;
import static com.google.android.exoplayer2.drm.UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME;
import static com.google.android.exoplayer2.util.Util.getDrmUuid;
import static im.ene.toro.ToroUtil.checkNotNull;

/**
 * @author eneim (2018/10/13).
 */
public class DefaultDrmSessionManagerProvider implements DrmSessionManagerProvider {

  private final Context context;
  private final HttpDataSource.Factory httpDataSourceFactory;

  @SuppressWarnings("WeakerAccess") public DefaultDrmSessionManagerProvider(Context context,
      HttpDataSource.Factory httpDataSourceFactory) {
    this.context = context.getApplicationContext();
    this.httpDataSourceFactory = httpDataSourceFactory;
  }

  @Nullable @Override
  public DrmSessionManager<FrameworkMediaCrypto> provideDrmSessionManager(@NonNull Media media) {
    MediaDrm drm = media.getMediaDrm();
    if (drm == null) return null;
    DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
    int errorStringId = R.string.error_drm_unknown;
    String subString = null;
    if (Util.SDK_INT < 18) {
      errorStringId = R.string.error_drm_not_supported;
    } else {
      UUID drmSchemeUuid = getDrmUuid(checkNotNull(drm).getType());
      if (drmSchemeUuid == null) {
        errorStringId = R.string.error_drm_unsupported_scheme;
      } else {
        try {
          drmSessionManager = buildDrmSessionManagerV18(drmSchemeUuid, drm.getLicenseUrl(),
              drm.getKeyRequestPropertiesArray(), drm.multiSession(), httpDataSourceFactory,
              drm.getOptionalKeyRequestParameters());
        } catch (UnsupportedDrmException e) {
          e.printStackTrace();
          errorStringId = e.reason == REASON_UNSUPPORTED_SCHEME ? //
              R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
          if (e.reason == REASON_UNSUPPORTED_SCHEME) {
            subString = drm.getType();
          }
        }
      }
    }

    if (drmSessionManager == null) {
      String error = TextUtils.isEmpty(subString) ? context.getString(errorStringId)
          : context.getString(errorStringId) + ": " + subString;
      Toast.makeText(context, error, LENGTH_SHORT).show();
    }

    return drmSessionManager;
  }

  @RequiresApi(18) //
  private static DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(
      @NonNull UUID uuid, @Nullable String licenseUrl, @Nullable String[] keyRequestProperties,
      boolean multiSession, @NonNull HttpDataSource.Factory httpDataSourceFactory,
      @Nullable HashMap<String, String> optionalKeyRequestParameters)
      throws UnsupportedDrmException {
    HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, httpDataSourceFactory);
    if (keyRequestProperties != null) {
      for (int i = 0; i < keyRequestProperties.length - 1; i += 2) {
        drmCallback.setKeyRequestProperty(keyRequestProperties[i], keyRequestProperties[i + 1]);
      }
    }
    return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback,
        optionalKeyRequestParameters, multiSession);
  }
}
