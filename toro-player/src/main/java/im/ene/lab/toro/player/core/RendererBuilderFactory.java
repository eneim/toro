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

package im.ene.lab.toro.player.core;

import android.content.Context;
import com.google.android.exoplayer.util.Util;
import im.ene.lab.toro.player.DrmMediaSource;
import im.ene.lab.toro.player.MediaSource;
import im.ene.lab.toro.player.internal.DashRendererBuilder;
import im.ene.lab.toro.player.internal.ExoMediaPlayer;
import im.ene.lab.toro.player.internal.ExtractorRendererBuilder;
import im.ene.lab.toro.player.internal.HlsRendererBuilder;
import im.ene.lab.toro.player.internal.WidevineTestMediaDrmCallback;

/**
 * Generate a renderer builder appropriate for rendering a video.
 */
public class RendererBuilderFactory {

  /**
   * Create a renderer builder which can build the given video.
   *
   * @param context The context (ex {@link android.app.Activity} in whicb the video has been
   * created.
   * @param video The video which will be played.
   */
  public static ExoMediaPlayer.RendererBuilder createRendererBuilder(Context context,
      MediaSource video) {
    switch (video.videoType) {
      case HLS:
        return new HlsRendererBuilder(context, Util.getUserAgent(context, "Toro"),
            video.mediaUri.toString());
      case DASH:
        return new DashRendererBuilder(context, Util.getUserAgent(context, "Toro"),
            video.mediaUri.toString(),
            new WidevineTestMediaDrmCallback(((DrmMediaSource) video).contentId,
                ((DrmMediaSource) video).provider));
      case MP4:
        return new ExtractorRendererBuilder(context, Util.getUserAgent(context, "Toro"),
            video.mediaUri);
      case OTHER:
        return new ExtractorRendererBuilder(context, Util.getUserAgent(context, "Toro"),
            video.mediaUri);
      default:
        return null;
    }
  }
}
