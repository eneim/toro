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
import com.squareup.leakcanary.LeakCanary;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import im.ene.toro.exoplayer.PlayerHub;
import im.ene.toro.exoplayer.ToroExo;

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
  private PlayerHub playerHub;

  @Override public void onCreate() {
    super.onCreate();
    singleton = this;
    // adopt from ExoPlayer demo.
    if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
      CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
    }

    playerHub = ToroExo.with(this).getHub();
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);
  }

  public static ToroDemo getApp() {
    return singleton;
  }

  public static PlayerHub getPlayerHub() {
    return singleton.playerHub;
  }
}
