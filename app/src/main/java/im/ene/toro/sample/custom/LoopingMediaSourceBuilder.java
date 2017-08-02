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

package im.ene.toro.sample.custom;

import android.content.Context;
import android.net.Uri;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import im.ene.toro.exoplayer.MediaSourceBuilder;

/**
 * @author eneim (7/8/17).
 *
 *         {@link MediaSourceBuilder} that will builds a {@link LoopingMediaSource}.
 */

public class LoopingMediaSourceBuilder extends MediaSourceBuilder {

  LoopingMediaSourceBuilder(Context context, Uri mediaUri) {
    super(context, mediaUri);
  }

  @Override public MediaSource build(BandwidthMeter bandwidthMeter) {
    MediaSource source = super.build(bandwidthMeter);
    return new LoopingMediaSource(source);
  }
}
