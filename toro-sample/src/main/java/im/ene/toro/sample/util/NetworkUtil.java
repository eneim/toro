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
 * Created by eneim on 10/12/16.
 */

public class NetworkUtil {

  public static String getNetworkClass(Context context) {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = cm.getActiveNetworkInfo();
    if (info == null || !info.isConnected()) return "-"; //not connected
    if (info.getType() == ConnectivityManager.TYPE_WIFI) return "WIFI";
    if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
      int networkType = info.getSubtype();
      switch (networkType) {
        case TelephonyManager.NETWORK_TYPE_GPRS:
        case TelephonyManager.NETWORK_TYPE_EDGE:
        case TelephonyManager.NETWORK_TYPE_CDMA:
        case TelephonyManager.NETWORK_TYPE_1xRTT:
        case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
          return "2G";
        case TelephonyManager.NETWORK_TYPE_UMTS:
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
        case TelephonyManager.NETWORK_TYPE_HSDPA:
        case TelephonyManager.NETWORK_TYPE_HSUPA:
        case TelephonyManager.NETWORK_TYPE_HSPA:
        case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
        case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
        case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
          return "3G";
        case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
          return "4G";
        default:
          return "UNKNOWN";
      }
    }
    return "UNKNOWN";
  }
}
