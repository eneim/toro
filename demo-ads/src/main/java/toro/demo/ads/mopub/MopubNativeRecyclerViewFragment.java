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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mopub.common.MoPub;
import com.mopub.common.util.DeviceUtils;
import com.mopub.nativeads.MediaViewBinder;
import com.mopub.nativeads.MoPubNativeAdPositioning;
import com.mopub.nativeads.MoPubRecyclerAdapter;
import com.mopub.nativeads.MoPubVideoNativeAdRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import toro.demo.ads.R;
import toro.demo.ads.common.BaseFragment;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.mopub.common.Constants.UNUSED_REQUEST_CODE;

/**
 * @author eneim (2018/08/21).
 */
public class MopubNativeRecyclerViewFragment extends BaseFragment {

  private static final List<String> REQUIRED_DANGEROUS_PERMISSIONS = new ArrayList<>();

  static {
    REQUIRED_DANGEROUS_PERMISSIONS.add(ACCESS_COARSE_LOCATION);
    REQUIRED_DANGEROUS_PERMISSIONS.add(WRITE_EXTERNAL_STORAGE);
  }

  public static MopubNativeRecyclerViewFragment newInstance() {
    Bundle args = new Bundle();
    MopubNativeRecyclerViewFragment fragment = new MopubNativeRecyclerViewFragment();
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
    RecyclerView recyclerView = view.findViewById(R.id.native_recycler_view);
    final RecyclerView.Adapter originalAdapter = new DemoRecyclerAdapter();

    recyclerAdapter = new MoPubRecyclerAdapter(requireActivity(), originalAdapter,
        new MoPubNativeAdPositioning.MoPubServerPositioning());

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
    recyclerView.setAdapter(recyclerAdapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    recyclerAdapter.loadAds(adConfig.getAdUnitId());
  }

  @Override public void onDestroyView() {
    // You must call this or the ad adapter may cause a memory leak.
    recyclerAdapter.destroy();
    super.onDestroyView();
  }

  static class DemoRecyclerAdapter extends RecyclerView.Adapter<DemoViewHolder> {
    private static final int ITEM_COUNT = 150;

    @NonNull @Override
    public DemoViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
      final View itemView = LayoutInflater.from(parent.getContext())
          .inflate(android.R.layout.simple_list_item_1, parent, false);
      return new DemoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final DemoViewHolder holder, final int position) {
      holder.textView.setText(String.format(Locale.US, "Content Item #%d", position));
    }

    @Override public long getItemId(final int position) {
      return (long) position;
    }

    @Override public int getItemCount() {
      return ITEM_COUNT;
    }
  }

  static class DemoViewHolder extends RecyclerView.ViewHolder {
    final TextView textView;

    DemoViewHolder(final View itemView) {
      super(itemView);
      textView = itemView.findViewById(android.R.id.text1);
    }
  }
}
