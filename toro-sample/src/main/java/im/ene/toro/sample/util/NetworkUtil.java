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

package im.ene.toro.sample.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Check device's network connectivity and speed
 *
 * @author emil http://stackoverflow.com/users/220710/emil
 */
public class NetworkUtil {

  /**
   * Get the network info
   */
  private static NetworkInfo getNetworkInfo(Context context) {
    return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)) //
        .getActiveNetworkInfo();
  }

  /**
   * Check if there is any connectivity
   */
  public static boolean isConnected(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return (info != null && info.isConnected());
  }

  /**
   * Check if there is any connectivity to a Wifi network
   */
  public static boolean isConnectedToWifi(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
  }

  /**
   * Check if there is any connectivity to a mobile network
   */
  public static boolean isConnectedToCellular(Context context) {
    NetworkInfo info = NetworkUtil.getNetworkInfo(context);
    return (info != null && info.isConnected()  //
        && info.getType() == ConnectivityManager.TYPE_MOBILE);
  }

  /**
   * Check if there is fast connectivity
   */
  public static boolean isConnectionFast(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return (info != null && info.isConnected() && isConnectionFast(info.getType(),
        info.getSubtype()));
  }

  /**
   * Check if the connection is fast
   *
   * Requires Permission:
   * {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
   */
  private static boolean isConnectionFast(int type, int subType) {
    if (type == ConnectivityManager.TYPE_WIFI) {
      return true;
    } else if (type == ConnectivityManager.TYPE_MOBILE) {
      switch (subType) {
        case TelephonyManager.NETWORK_TYPE_1xRTT:
          return false; // ~ 50-100 kbps
        case TelephonyManager.NETWORK_TYPE_CDMA:
          return false; // ~ 14-64 kbps
        case TelephonyManager.NETWORK_TYPE_EDGE:
          return false; // ~ 50-100 kbps
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
          return true; // ~ 400-1000 kbps
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
          return true; // ~ 600-1400 kbps
        case TelephonyManager.NETWORK_TYPE_GPRS:
          return false; // ~ 100 kbps
        case TelephonyManager.NETWORK_TYPE_HSDPA:
          return true; // ~ 2-14 Mbps
        case TelephonyManager.NETWORK_TYPE_HSPA:
          return true; // ~ 700-1700 kbps
        case TelephonyManager.NETWORK_TYPE_HSUPA:
          return true; // ~ 1-23 Mbps
        case TelephonyManager.NETWORK_TYPE_UMTS:
          return true; // ~ 400-7000 kbps
      /*
       * Above API level 7, make sure to set android:targetSdkVersion
			 * to appropriate level to use these
			 */
        case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
          return true; // ~ 1-2 Mbps
        case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
          return true; // ~ 5 Mbps
        case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
          return true; // ~ 10-20 Mbps
        case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
          return false; // ~25 kbps
        case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
          return true; // ~ 10+ Mbps
        // Unknown
        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
        default:
          return false;
      }
    } else {
      return false;
    }
  }
}
