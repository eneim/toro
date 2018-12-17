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
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import im.ene.toro.annotations.Beta;
import im.ene.toro.media.Media;

/**
 * @author eneim (2018/10/08).
 * @since 3.7.0.2901
 *
 * This interface is used in default implementation of {@link ExoPlayerManager} only.
 *
 * @see DefaultExoPlayerManager
 */
@Beta public interface DrmSessionManagerProvider {

  @Nullable DrmSessionManager<FrameworkMediaCrypto> provideDrmSessionManager(@NonNull Media media);
}
