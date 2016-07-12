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

package im.ene.lab.toro.sample.presentation.youtube;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

/**
 * Created by eneim on 7/6/16.
 */
public class YouTubeDataHelper {

  static final JsonFactory JSON_FACTORY = new GsonFactory();

  static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

  static final String LIST_GOOGLE_IO_ANDROID = "PLWz5rJ2EKKc8jQTUYvIfqA9lMvSGQWtte";

  static final String CHANNEL_ANDROID_DEV = "UCVHFbqXqoYvEWM1Ddxl0QDg";
}
