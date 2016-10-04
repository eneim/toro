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

package im.ene.toro.mediaplayer;

import android.media.MediaPlayer;

/**
 * Created by eneim on 10/3/16.
 */

public class MediaPlayerException extends Exception {

  private final int errorCode;

  public MediaPlayerException(int errorCode) {
    super(parseErrorMessage(errorCode));
    this.errorCode = errorCode;
  }

  private static String parseErrorMessage(int code) {
    switch (code) {
      case MediaPlayer.MEDIA_ERROR_IO:
        return "File or network related operation errors.";
      case MediaPlayer.MEDIA_ERROR_MALFORMED:
        return "Bitstream is not conforming to the related coding standard or file spec.";
      case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
        return "Bitstream is conforming to the related coding standard or file spec, but the media framework does not support the feature.";
      case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
        return "Some operation takes too long to complete, usually more than 3-5 seconds.";
      default:
        return "Unspecified Error";
    }
  }

  public final int getErrorCode() {
    return errorCode;
  }
}
