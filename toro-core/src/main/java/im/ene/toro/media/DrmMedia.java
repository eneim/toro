/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.media;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import im.ene.toro.exoplayer.DrmMediaProvider;
import im.ene.toro.exoplayer.ExoPlayerHelper;

/**
 * @author eneim | 6/5/17.
 *
 *         A definition of DRM media type. Media that supports DRM should implement this to get
 *         support from {@link ExoPlayer}.
 *
 *         See {@link DrmMediaProvider}.
 *         See {@link ExoPlayerHelper#prepare(MediaSource, BandwidthMeter, DrmSessionManager)}.
 */

public interface DrmMedia {

  String getType();

  String getLicenseUrl();

  String[] getKeyRequestPropertiesArray();
}
