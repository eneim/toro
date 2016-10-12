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

package im.ene.toro.sample.develop.facebook;

import android.support.annotation.DrawableRes;
import im.ene.toro.sample.R;

/**
 * Created by eneim on 10/11/16.
 */

public class FbUser {

  private final int profileImageUrl;
  private final String userName;
  private final String userDescription;
  private final String userUrl;

  public FbUser() {
    profileImageUrl = R.drawable.ic_profile;
    userName = "Toro Creator";
    userDescription = "Nam Nguyen, nam@ene.im";
    userUrl = "https://github.com/eneim";
  }

  @DrawableRes public int getProfileImageUrl() {
    return profileImageUrl;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserDescription() {
    return userDescription;
  }

  public String getUserUrl() {
    return userUrl;
  }
}
