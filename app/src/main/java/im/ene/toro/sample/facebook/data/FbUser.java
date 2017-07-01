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

package im.ene.toro.sample.facebook.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author eneim | 6/18/17.
 */

public class FbUser implements Parcelable {

  public final String userName;
  public final String userProfile;
  public final String userIcon;

  public FbUser(String userName, String userProfile, String userIcon) {
    this.userName = userName;
    this.userProfile = userProfile;
    this.userIcon = userIcon;
  }

  private static FbUser DEFAULT =
      new FbUser("eneim", "Toro Creator, a good guy", "file:///android_asset/profile.jpg");

  public static FbUser getUser() {
    return DEFAULT;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.userName);
    dest.writeString(this.userProfile);
    dest.writeString(this.userIcon);
  }

  protected FbUser(Parcel in) {
    this.userName = in.readString();
    this.userProfile = in.readString();
    this.userIcon = in.readString();
  }

  public static final Creator<FbUser> CREATOR = new Creator<FbUser>() {
    @Override public FbUser createFromParcel(Parcel source) {
      return new FbUser(source);
    }

    @Override public FbUser[] newArray(int size) {
      return new FbUser[size];
    }
  };
}
