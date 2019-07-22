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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mopub.common.MoPub;
import com.mopub.common.util.DeviceUtils;
import com.mopub.nativeads.MediaViewBinder;
import com.mopub.nativeads.MoPubNativeAdPositioning;
import com.mopub.nativeads.MoPubRecyclerAdapter;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.MoPubVideoNativeAdRenderer;
import com.mopub.nativeads.ViewBinder;
import im.ene.toro.CacheManager;
import im.ene.toro.widget.Container;
import java.util.ArrayList;
import java.util.List;
import toro.demo.ads.R;
import toro.demo.ads.common.BaseFragment;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.mopub.common.Constants.UNUSED_REQUEST_CODE;

/**
 * @author eneim (2018/08/21).
 */
public class MoPubNativeRecyclerViewFragment extends BaseFragment {

  private static final List<String> REQUIRED_DANGEROUS_PERMISSIONS = new ArrayList<>();

  static {
    REQUIRED_DANGEROUS_PERMISSIONS.add(ACCESS_COARSE_LOCATION);
    REQUIRED_DANGEROUS_PERMISSIONS.add(WRITE_EXTERNAL_STORAGE);
  }

  public static MoPubNativeRecyclerViewFragment newInstance() {
    Bundle args = new Bundle();
    MoPubNativeRecyclerViewFragment fragment = new MoPubNativeRecyclerViewFragment();
    fragment.setArguments(args);
    return fragment;
  }

  MoPubRecyclerAdapter recyclerAdapter;
  MoPubSampleAdUnit adConfig;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    List<String> permissionsToBeRequested = new ArrayList<>();
    for (String permission : REQUIRED_DANGEROUS_PERMISSIONS) {
      if (!DeviceUtils.isPermissionGranted(context, permission)) {
        permissionsToBeRequested.add(permission);
      }
    }

    // Request dangerous permissions
    if (!permissionsToBeRequested.isEmpty()) {
      ActivityCompat.requestPermissions(requireActivity(),
          permissionsToBeRequested.toArray(new String[0]), UNUSED_REQUEST_CODE);
    }

    // Set location awareness and precision globally for your app:
    MoPub.setLocationAwareness(MoPub.LocationAwareness.TRUNCATED);
    MoPub.setLocationPrecision(4);
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_recycler_view, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    adConfig = MoPubSampleAdUnit.fromBundle(null);
    Container container = view.findViewById(R.id.native_recycler_view);
    final RecyclerView.Adapter originalAdapter = new MoPubDemoAdapter();

    recyclerAdapter = new MoPubRecyclerAdapter(requireActivity(), originalAdapter,
        new MoPubNativeAdPositioning.MoPubServerPositioning());

    // Set up a renderer for a static native ad.
    MoPubStaticNativeAdRenderer staticNativeAdRenderer = new MoPubStaticNativeAdRenderer(
        new ViewBinder.Builder(R.layout.native_ad_list_item).titleId(R.id.native_title)
            .textId(R.id.native_text)
            .mainImageId(R.id.native_main_image)
            .iconImageId(R.id.native_icon_image)
            .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
            .build());

    // Set up a renderer for a video native ad.
    MoPubVideoNativeAdRenderer videoNativeAdRenderer = new MoPubVideoNativeAdRenderer(
        new MediaViewBinder.Builder(R.layout.video_ad_list_item).titleId(R.id.native_title)
            .textId(R.id.native_text)
            .mediaLayoutId(R.id.native_media_layout)
            .iconImageId(R.id.native_icon_image)
            // .callToActionId(R.id.native_cta)
            .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
            .build());

    recyclerAdapter.registerAdRenderer(videoNativeAdRenderer);
    recyclerAdapter.registerAdRenderer(staticNativeAdRenderer);

    container.setAdapter(recyclerAdapter);
    container.setLayoutManager(new LinearLayoutManager(requireContext()));
    container.setCacheManager(CacheManager.DEFAULT);
    recyclerAdapter.loadAds(adConfig.getAdUnitId());
  }

  @Override public void onDestroyView() {
    // You must call this or the ad adapter may cause a memory leak.
    recyclerAdapter.destroy();
    super.onDestroyView();
  }
}
