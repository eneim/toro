/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro;

/**
 * @author eneim.
 * @hide
 * @since 5/14/17.
 */

public final class Log {

  private Log() {
    throw new RuntimeException("Meh!");
  }

  private static final boolean D = BuildConfig.DEBUG;

  public static int v(String tag, String msg) {
    if (!D) {
      return -1;
    }

    return android.util.Log.v(tag, msg);
  }

  public static int v(String tag, String msg, Throwable tr) {
    if (!D) {
      return -1;
    }

    return android.util.Log.v(tag, msg, tr);
  }

  public static int d(String tag, String msg) {
    if (!D) {
      return -1;
    }

    return android.util.Log.d(tag, msg);
  }

  public static int d(String tag, String msg, Throwable tr) {
    if (!D) {
      return -1;
    }

    return android.util.Log.d(tag, msg, tr);
  }

  public static int i(String tag, String msg) {
    if (!D) {
      return -1;
    }

    return android.util.Log.i(tag, msg);
  }

  public static int i(String tag, String msg, Throwable tr) {
    if (!D) {
      return -1;
    }

    return android.util.Log.i(tag, msg, tr);
  }

  public static int w(String tag, String msg) {
    if (!D) {
      return -1;
    }

    return android.util.Log.w(tag, msg);
  }

  public static int w(String tag, String msg, Throwable tr) {
    if (!D) {
      return -1;
    }

    return android.util.Log.w(tag, msg, tr);
  }

  public static int w(String tag, Throwable tr) {
    if (!D) {
      return -1;
    }

    return android.util.Log.w(tag, tr);
  }

  public static int e(String tag, String msg) {
    if (!D) {
      return -1;
    }

    return android.util.Log.e(tag, msg);
  }

  public static int e(String tag, String msg, Throwable tr) {
    if (!D) {
      return -1;
    }

    return android.util.Log.e(tag, msg, tr);
  }
}
