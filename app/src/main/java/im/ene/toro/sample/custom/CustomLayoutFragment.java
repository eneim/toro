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

package im.ene.toro.sample.custom;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;
import butterknife.BindView;
import com.ramotion.cardslider.CardSnapHelper;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.widget.Container;

/**
 * @author eneim (7/1/17).
 *
 *         A list of content that contains a {@link Container} as one of its child. We gonna use a
 *         {@link PagerSnapHelper} to mimic a Pager-inside-RecyclerView. Other contents will be
 *         normal text to preseve the performance and also to not make user confused.
 */

@SuppressWarnings("unused") public class CustomLayoutFragment extends BaseFragment {

  public static CustomLayoutFragment newInstance() {
    Bundle args = new Bundle();
    CustomLayoutFragment fragment = new CustomLayoutFragment();
    fragment.setArguments(args);
    return fragment;
  }

  static final int[] contents =
      { R.string.license_tos, R.string.license_bbb, R.string.license_cosmos };

  private Callback callback;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof Callback) {
      this.callback = (Callback) context;
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    this.callback = null;
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_custom_layouts, container, false);
  }

  @BindView(R.id.text_content) TextView textView;
  @BindView(R.id.player_container) Container container;
  CustomCardSliderLayoutManager layoutManager;
  CustomLayoutAdapter adapter;

  private final CardSnapHelper snapHelper = new CardSnapHelper();
  private RecyclerView.OnScrollListener onScrollListener;

  @Override public void onViewCreated(View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    layoutManager = new CustomCardSliderLayoutManager(
        getResources().getDimensionPixelOffset(R.dimen.custom_card_left),
        getResources().getDimensionPixelSize(R.dimen.custom_item_width),
        getResources().getDimensionPixelOffset(R.dimen.custom_card_gap));
    container.setLayoutManager(layoutManager);
    layoutManager.setItemPrefetchEnabled(true);
    snapHelper.attachToRecyclerView(container);

    onScrollListener = new RecyclerView.OnScrollListener() {
      @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
          int pos = layoutManager.getActiveCardPosition();
          if (pos >= 0 && textView != null) {
            textView.setText(Html.fromHtml(getString(contents[pos % contents.length])));
          }
        }
      }
    };

    container.addOnScrollListener(onScrollListener);

    MediaList mediaList = new MediaList();
    adapter = new CustomLayoutAdapter(mediaList);
    container.setAdapter(adapter);
    container.setCacheManager(adapter);

    // first layout
    container.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        int pos = layoutManager.getActiveCardPosition();
        if (pos >= 0 && textView != null) {
          textView.setText(Html.fromHtml(getString(contents[pos % contents.length])));
        }
      }
    });
  }

  @Override public void onViewStateRestored(@Nullable Bundle bundle) {
    super.onViewStateRestored(bundle);
    if (this.callback != null) {
      this.callback.onContainerAvailable(container);
    }
  }

  @Override public void onDestroyView() {
    snapHelper.attachToRecyclerView(null);
    container.removeOnScrollListener(onScrollListener);
    layoutManager = null;
    adapter = null;
    super.onDestroyView();
  }

  public interface Callback {

    void onContainerAvailable(@NonNull Container container);
  }
}
