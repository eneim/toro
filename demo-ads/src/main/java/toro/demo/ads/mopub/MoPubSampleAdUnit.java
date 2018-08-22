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

package toro.demo.ads.mopub;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import java.util.Comparator;
import java.util.Locale;

/**
 * @author eneim (2018/08/21).
 */
@SuppressWarnings("WeakerAccess") //
class MoPubSampleAdUnit implements Comparable<MoPubSampleAdUnit> {

  static final String AD_UNIT_ID = "adUnitId";
  static final String DESCRIPTION = "description";
  static final String AD_TYPE = "adType";
  static final String IS_USER_DEFINED = "isCustom";
  static final String ID = "id";

  // Note that entries are also sorted in this order
  enum AdType {
    RECYCLER_VIEW("Native RecyclerView", MopubNativeRecyclerViewFragment.class);

    String getName() {
      return name;
    }

    final String name;
    private final Class<? extends Fragment> fragmentClass;

    AdType(final String name, final Class<? extends Fragment> fragmentClass) {
      this.name = name;
      this.fragmentClass = fragmentClass;
    }

    Class<? extends Fragment> getFragmentClass() {
      return fragmentClass;
    }

    static AdType fromFragmentClassName(final String fragmentClassName) {
      for (final AdType adType : values()) {
        if (adType.fragmentClass.getName().equals(fragmentClassName)) {
          return adType;
        }
      }

      return null;
    }

    @Nullable static AdType fromDeeplinkString(@Nullable final String adType) {
      if (adType == null) {
        return null;
      }
      switch (adType.toLowerCase(Locale.US)) {
        case "nativetableplacer":
          return RECYCLER_VIEW;
        default:
          return null;
      }
    }
  }

  static final Comparator<MoPubSampleAdUnit> COMPARATOR = MoPubSampleAdUnit::compareTo;

  @SuppressWarnings("UnusedReturnValue") static class Builder {
    final String mAdUnitId;
    final AdType mAdType;

    String mDescription;
    boolean mIsUserDefined;
    long mId;

    Builder(final String adUnitId, final AdType adType) {
      mAdUnitId = adUnitId;
      mAdType = adType;
      mId = -1;
    }

    Builder description(final String description) {
      mDescription = description;
      return this;
    }

    Builder isUserDefined(boolean userDefined) {
      mIsUserDefined = userDefined;
      return this;
    }

    Builder id(final long id) {
      mId = id;
      return this;
    }

    MoPubSampleAdUnit build() {
      return new MoPubSampleAdUnit(this);
    }
  }

  private final String mAdUnitId;
  private final AdType mAdType;
  private final String mDescription;
  private final boolean mIsUserDefined;
  private final long mId;

  MoPubSampleAdUnit(final Builder builder) {
    mAdUnitId = builder.mAdUnitId;
    mAdType = builder.mAdType;
    mDescription = builder.mDescription;
    mIsUserDefined = builder.mIsUserDefined;
    mId = builder.mId;
  }

  Class<? extends Fragment> getFragmentClass() {
    return mAdType.getFragmentClass();
  }

  String getAdUnitId() {
    return mAdUnitId;
  }

  String getDescription() {
    return mDescription;
  }

  String getFragmentClassName() {
    return mAdType.getFragmentClass().getName();
  }

  String getHeaderName() {
    return mAdType.name;
  }

  long getId() {
    return mId;
  }

  boolean isUserDefined() {
    return mIsUserDefined;
  }

  Bundle toBundle() {
    final Bundle bundle = new Bundle();
    bundle.putLong(ID, mId);
    bundle.putString(AD_UNIT_ID, mAdUnitId);
    bundle.putString(DESCRIPTION, mDescription);
    bundle.putSerializable(AD_TYPE, mAdType);
    bundle.putBoolean(IS_USER_DEFINED, mIsUserDefined);

    return bundle;
  }

  @SuppressWarnings("ConstantConditions") static MoPubSampleAdUnit fromNull() {
    Long id = -1L;
    // String adUnitId = "11a17b188668469fb0412708c3d16813";
    String adUnitId = "02a2d288d2674ad09f3241d46a44356e";
    AdType adType = AdType.RECYCLER_VIEW;
    String description = adType.name;
    boolean isUserDefined = false;
    final Builder builder = new MoPubSampleAdUnit.Builder(adUnitId, adType);
    builder.description(description);
    builder.id(id);
    builder.isUserDefined(isUserDefined);
    return builder.build();
  }

  @SuppressWarnings("SameParameterValue")
  static MoPubSampleAdUnit fromBundle(@Nullable Bundle bundle) {
    if (bundle == null) return fromNull();
    final Long id = bundle.getLong(ID, -1L);
    final String adUnitId = bundle.getString(AD_UNIT_ID);
    final AdType adType = (AdType) bundle.getSerializable(AD_TYPE);
    final String description = bundle.getString(DESCRIPTION);
    final boolean isUserDefined = bundle.getBoolean(IS_USER_DEFINED, false);
    final Builder builder = new MoPubSampleAdUnit.Builder(adUnitId, adType);
    builder.description(description);
    builder.id(id);
    builder.isUserDefined(isUserDefined);

    return builder.build();
  }

  @Override public int compareTo(@NonNull MoPubSampleAdUnit that) {
    if (mAdType != that.mAdType) {
      return mAdType.ordinal() - that.mAdType.ordinal();
    }

    return mDescription.compareTo(that.mDescription);
  }

  @Override public int hashCode() {
    int result = 11;
    result = 31 * result + mAdType.ordinal();
    result = 31 * result + (mIsUserDefined ? 1 : 0);
    result = 31 * result + mDescription.hashCode();
    result = 31 * result + mAdUnitId.hashCode();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (o == null) {
      return false;
    }

    if (this == o) {
      return true;
    }

    if (!(o instanceof MoPubSampleAdUnit)) {
      return false;
    }

    final MoPubSampleAdUnit that = (MoPubSampleAdUnit) o;

    return that.mAdType.equals(this.mAdType)
        && that.mIsUserDefined == this.mIsUserDefined
        && that.mDescription.equals(this.mDescription)
        && that.mAdUnitId.equals(this.mAdUnitId);
  }
}