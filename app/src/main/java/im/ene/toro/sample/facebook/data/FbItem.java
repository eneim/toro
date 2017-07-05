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

public class FbItem implements Parcelable {

  public final FbUser author;
  public final long index;
  public final long timeStamp;

  public FbItem(FbUser author, long index, long timeStamp) {
    this.author = author;
    this.index = index;
    this.timeStamp = timeStamp;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public long getIndex() {
    return index;
  }

  @Override public String toString() {
    return "FbItem{" + "author=" + author + ", index=" + index + ", timeStamp=" + timeStamp + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(this.author, flags);
    dest.writeLong(this.index);
    dest.writeLong(this.timeStamp);
  }

  protected FbItem(Parcel in) {
    this.author = in.readParcelable(FbUser.class.getClassLoader());
    this.index = in.readLong();
    this.timeStamp = in.readLong();
  }

  public static final Creator<FbItem> CREATOR = new ClassLoaderCreator<FbItem>() {
    @Override public FbItem createFromParcel(Parcel source) {
      return new FbItem(source);
    }

    @Override public FbItem createFromParcel(Parcel source, ClassLoader loader) {
      return new FbItem(source);
    }

    @Override public FbItem[] newArray(int size) {
      return new FbItem[size];
    }
  };
}
