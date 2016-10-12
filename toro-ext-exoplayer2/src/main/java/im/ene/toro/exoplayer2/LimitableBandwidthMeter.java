/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.exoplayer2;

import android.os.Handler;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.TransferListener;

/**
 * Created by eneim on 10/12/16.
 */

public class LimitableBandwidthMeter implements BandwidthMeter, TransferListener<Object> {

  private final DefaultBandwidthMeter delegate;

  private long limitBitrate = NO_ESTIMATE;

  public LimitableBandwidthMeter() {
    this(null, null);
  }

  public LimitableBandwidthMeter(Handler eventHandler, EventListener eventListener) {
    this(eventHandler, eventListener, DefaultBandwidthMeter.DEFAULT_MAX_WEIGHT);
  }

  public LimitableBandwidthMeter(Handler eventHandler, EventListener eventListener, int maxWeight) {
    this(eventHandler, eventListener, maxWeight, NO_ESTIMATE);
  }

  public LimitableBandwidthMeter(Handler eventHandler, EventListener eventListener, int maxWeight,
      long limitBitrate) {
    delegate = new DefaultBandwidthMeter(eventHandler, eventListener, maxWeight);
    this.limitBitrate = limitBitrate;
  }

  public void setLimitBitrate(long limitBitrate) {
    this.limitBitrate = limitBitrate;
  }

  @Override public synchronized long getBitrateEstimate() {
    return limitBitrate != NO_ESTIMATE ? Math.min(limitBitrate, delegate.getBitrateEstimate())  //
        : delegate.getBitrateEstimate();
  }

  @Override public synchronized void onTransferStart(Object source, DataSpec dataSpec) {
    delegate.onTransferStart(source, dataSpec);
  }

  @Override public synchronized void onBytesTransferred(Object source, int bytesTransferred) {
    delegate.onBytesTransferred(source, bytesTransferred);
  }

  @Override public void onTransferEnd(Object source) {
    delegate.onTransferEnd(source);
  }
}
