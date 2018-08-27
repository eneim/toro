/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package toro.demo.ads

import android.app.Application
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration


/**
 * @author eneim (2018/08/21).
 */
class AdsApp : Application() {

  override fun onCreate() {
    super.onCreate()
    if (!MoPub.isSdkInitialized()) {
      val configuration = SdkConfiguration.Builder("b195f8dd8ded45fe847ad89ed1d016da").build()
      MoPub.initializeSdk(this, configuration, null)
    }
  }
}