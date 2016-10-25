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

package im.ene.toro.exoplayer.internal;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.drm.DrmSessionManager;

/**
 * Created by eneim on 6/2/16.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class EnhancedMediaCodecAudioTrackRenderer extends MediaCodecAudioTrackRenderer {

  private int audioSessionId = 0;

  // FIXME Comment out. Un-comment if necessary
  //public EnhancedMediaCodecAudioTrackRenderer(SampleSource source,
  //    MediaCodecSelector mediaCodecSelector) {
  //  super(source, mediaCodecSelector);
  //}
  //
  //public EnhancedMediaCodecAudioTrackRenderer(SampleSource source, MediaCodecSelector mediaCodecSelector,
  //    DrmSessionManager drmSessionManager, boolean playClearSamplesWithoutKeys) {
  //  super(source, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys);
  //}
  //
  //public EnhancedMediaCodecAudioTrackRenderer(SampleSource source, MediaCodecSelector mediaCodecSelector,
  //    Handler eventHandler, EventListener eventListener) {
  //  super(source, mediaCodecSelector, eventHandler, eventListener);
  //}
  //
  //public EnhancedMediaCodecAudioTrackRenderer(SampleSource source, MediaCodecSelector mediaCodecSelector,
  //    DrmSessionManager drmSessionManager, boolean playClearSamplesWithoutKeys,
  //    Handler eventHandler, EventListener eventListener) {
  //  super(source, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, eventHandler,
  //      eventListener);
  //}

  public EnhancedMediaCodecAudioTrackRenderer(SampleSource source, MediaCodecSelector mediaCodecSelector,
      DrmSessionManager drmSessionManager, boolean playClearSamplesWithoutKeys,
      Handler eventHandler, EventListener eventListener, AudioCapabilities audioCapabilities,
      int streamType) {
    super(source, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, eventHandler,
        eventListener, audioCapabilities, streamType);
  }

  public EnhancedMediaCodecAudioTrackRenderer(SampleSource[] sources,
      MediaCodecSelector mediaCodecSelector, DrmSessionManager drmSessionManager,
      boolean playClearSamplesWithoutKeys, Handler eventHandler, EventListener eventListener,
      AudioCapabilities audioCapabilities, int streamType) {
    super(sources, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, eventHandler,
        eventListener, audioCapabilities, streamType);
  }

  @Override protected void onAudioSessionId(int audioSessionId) {
    this.audioSessionId = audioSessionId;
  }

  /* package */ final int getAudioSessionId() {
    return this.audioSessionId;
  }
}
