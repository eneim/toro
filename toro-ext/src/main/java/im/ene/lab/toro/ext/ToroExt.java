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

package im.ene.lab.toro.ext;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ext.youtube.Youtube;

/**
 * Created by eneim on 6/6/16.
 *
 * Main class for Toro's extensions.
 */
public class ToroExt {

  // We will ask for Youtube API Key by Application's meta tag
  /**
   * Initialize Youtube support for current Application. Note that a valid Youtube API Key must be
   * provide via App's manifest meta-data.
   */
  public static void initYoutube(Application app) {
    try {
      ApplicationInfo appInfo = app.getPackageManager()
          .getApplicationInfo(app.getPackageName(), PackageManager.GET_META_DATA);
      String YT_META_NAME = "Youtube.API_KEY";
      Youtube.setApiKey(appInfo.metaData.getString(YT_META_NAME));
      Toro.init(app);
    } catch (PackageManager.NameNotFoundException | NullPointerException error) {
      error.printStackTrace();
      throw new RuntimeException("Youtube API Key must be provided by Manifest's meta-data.");
    }
  }

  public static Builder with(Application app) {
    return new Builder(app);
  }

  public static final class Builder {

    private final Application app;

    Builder(Application app) {
      this.app = app;
    }

    public Builder youtube() {
      try {
        ApplicationInfo appInfo = app.getPackageManager()
            .getApplicationInfo(app.getPackageName(), PackageManager.GET_META_DATA);
        String YT_META_NAME = "Youtube.API_KEY";
        Youtube.setApiKey(appInfo.metaData.getString(YT_META_NAME));
        return this;
      } catch (PackageManager.NameNotFoundException | NullPointerException error) {
        error.printStackTrace();
        throw new RuntimeException("Youtube API Key must be provided by Manifest's meta-data.");
      }
    }

    public void init() {
      Toro.init(this.app);
    }
  }
}
