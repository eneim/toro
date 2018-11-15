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

package toro.v4.exo;

import android.support.annotation.NonNull;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import toro.v4.exo.factory.BandwidthMeterFactory;

/**
 * @author eneim (2018/11/01).
 */
public class DefaultBandwidthMeterFactory implements BandwidthMeterFactory {

  @NonNull @Override public BandwidthMeter createBandwidthMeter() {
    return new DefaultBandwidthMeter();
  }
}
