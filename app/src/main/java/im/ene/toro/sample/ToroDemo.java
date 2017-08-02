/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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
import android.content.res.Configuration;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.leakcanary.LeakCanary;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * @author eneim | 6/5/17.
 */

public class ToroDemo extends Application {

  private static final CookieManager DEFAULT_COOKIE_MANAGER;

  static {
    DEFAULT_COOKIE_MANAGER = new CookieManager();
    DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  private static ToroDemo singleton;

  @Override public void onCreate() {
    super.onCreate();
    singleton = this;
    AndroidThreeTen.init(this);
    prettyTime = new PrettyTime();

    // adopt from ExoPlayer demo.
    if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
      CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
    }

    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);
  }

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // locale changes and so on.
    prettyTime = new PrettyTime();
  }

  public static ToroDemo getApp() {
    return singleton;
  }

  private PrettyTime prettyTime;

  public PrettyTime getPrettyTime() {
    return prettyTime;
  }
}
