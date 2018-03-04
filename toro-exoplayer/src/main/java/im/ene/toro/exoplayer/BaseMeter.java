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
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.TransferListener;

/**
 * Abstract the {@link DefaultBandwidthMeter}, provide a wider use.
 *
 * @author eneim (2018/01/26).
 * @since 3.4.0
 */

@SuppressWarnings("WeakerAccess") //
public final class BaseMeter<T extends BandwidthMeter, S extends TransferListener<Object>>
    implements BandwidthMeter, TransferListener {

  @NonNull protected final T bandwidthMeter;
  @NonNull protected final S transferListener;

  @SuppressWarnings("WeakerAccess") //
  public BaseMeter(@NonNull T bandwidthMeter, @NonNull S transferListener) {
    this.bandwidthMeter = bandwidthMeter;
    this.transferListener = transferListener;
  }

  @Override public long getBitrateEstimate() {
    return bandwidthMeter.getBitrateEstimate();
  }

  @Override public void onTransferStart(Object source, DataSpec dataSpec) {
    transferListener.onTransferStart(source, dataSpec);
  }

  @Override public void onBytesTransferred(Object source, int bytesTransferred) {
    transferListener.onBytesTransferred(source, bytesTransferred);
  }

  @Override public void onTransferEnd(Object source) {
    transferListener.onTransferEnd(source);
  }
}
