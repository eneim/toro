/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample.data;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Created by eneim on 2/18/17.
 */

public class OrderedVideoObject extends SimpleVideoObject {

  public final int position;

  public OrderedVideoObject(@NonNull String video, int position) {
    super(video);
    this.position = position;
  }

  @Override public String toString() {
    return "Video{" + "position=" + position + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeInt(this.position);
  }

  protected OrderedVideoObject(Parcel in) {
    super(in);
    this.position = in.readInt();
  }

  public static final Creator<OrderedVideoObject> CREATOR = new Creator<OrderedVideoObject>() {
    @Override public OrderedVideoObject createFromParcel(Parcel source) {
      return new OrderedVideoObject(source);
    }

    @Override public OrderedVideoObject[] newArray(int size) {
      return new OrderedVideoObject[size];
    }
  };
}
