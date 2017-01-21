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

package im.ene.toro.sample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.squareup.leakcanary.LeakCanary;
import im.ene.toro.Toro;
import io.fabric.sdk.android.Fabric;

/**
 * Created by eneim on 2/1/16.
 */
public class ToroApp extends Application {

  private static ToroApp sApp;

  @Override public void onCreate() {
    super.onCreate();
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);

    Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
    Toro.init(this);
    sApp = this;
  }

  public static SharedPreferences pref() {
    return sApp.getSharedPreferences("toro_pref", Context.MODE_PRIVATE);
  }

  public static String packageName() {
    return sApp.getPackageName();
  }

  public static ToroApp getApp() {
    return sApp;
  }

  /* Preference Keys */
  public static final String PREF_ACCOUNT_NAME = "toro_pref_account_name";
}
