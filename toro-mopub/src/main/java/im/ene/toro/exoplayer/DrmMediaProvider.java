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
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import im.ene.toro.media.DrmMedia;

/**
 * @author eneim (7/8/17).
 *
 *         A definition of a Media source that supplies a DRM media. Application that requires DRM
 *         supports should have a {@link MediaSourceCreator} implements this interface to get
 *         support from {@link ExoPlayer} as well as {@link ExoPlayerHelper}.
 *
 *         See {@link ExoPlayerHelper#prepare(MediaSourceCreator, BandwidthMeter)}.
 *         See {@link MediaSourceCreator}.
 * @deprecated since 3.4.0
 */
@Deprecated public interface DrmMediaProvider {

  @NonNull DrmMedia getDrmMedia();
}
