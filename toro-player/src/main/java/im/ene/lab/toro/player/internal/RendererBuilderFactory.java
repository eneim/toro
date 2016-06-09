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

package im.ene.lab.toro.player.internal;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.google.android.exoplayer.util.Util;

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
  public static ExoMediaPlayer.RendererBuilder createRendererBuilder(Context context, Uri uri) {
    final String userAgent = Util.getUserAgent(context, "Toro");
    int contentType = inferContentType(uri, "");
    switch (contentType) {
      case Util.TYPE_SS:
        return new SmoothStreamingRendererBuilder(context, userAgent, uri.toString(),
            new SmoothStreamingTestMediaDrmCallback());
      case Util.TYPE_HLS:
        return new HlsRendererBuilder(context, userAgent, uri.toString());
      case Util.TYPE_DASH:
        return new DashRendererBuilder(context, userAgent, uri.toString(),
            new WidevineTestMediaDrmCallback(null, null));  // FIXME invalid sourceId and provider
      case Util.TYPE_OTHER:
        return new ExtractorRendererBuilder(context, userAgent, uri);
      default:
        throw new IllegalStateException("Unsupported type: " + contentType);
    }
  }

  /**
   * Makes a best guess to infer the type from a media {@link Uri} and an optional overriding file
   * extension.
   *
   * @param uri The {@link Uri} of the media.
   * @param fileExtension An overriding file extension.
   * @return The inferred type.
   */
  private static int inferContentType(Uri uri, String fileExtension) {
    String lastPathSegment =
        !TextUtils.isEmpty(fileExtension) ? "." + fileExtension : uri.getLastPathSegment();
    return Util.inferContentType(lastPathSegment);
  }
}
