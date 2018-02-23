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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.ramotion.cardslider.CardSliderLayoutManager;
import com.ramotion.cardslider.CardSnapHelper;
import im.ene.toro.PlayerSelector;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.sample.SinglePlayerActivity;
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

  static final int RQ_PLAYBACK_INFO = 100;

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
    return inflater.inflate(R.layout.fragment_custom_layout, container, false);
  }

  @BindView(R.id.place_holder) ViewPager viewPager;
  @BindView(R.id.player_container) Container container;
  CardSliderLayoutManager layoutManager;
  CustomLayoutAdapter adapter;
  LicensePagerAdapter pagerAdapter;

  PlayerSelector selector = PlayerSelector.DEFAULT;  // backup current selector.

  private final CardSnapHelper snapHelper = new CardSnapHelper();
  private ViewPager.OnPageChangeListener pageChangeListener;
  private RecyclerView.OnScrollListener onScrollListener;

  @Override public void onViewCreated(View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    layoutManager = new CardSliderLayoutManager(
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
          handler.obtainMessage(MSG_CONTAINER_SCROLL_IDLE, pos, -1).sendToTarget();
        }
      }
    };
    container.addOnScrollListener(onScrollListener);

    MediaList mediaList = new MediaList();
    adapter = new CustomLayoutAdapter(mediaList, new CustomLayoutAdapter.ItemClickListener() {
      @Override void onItemClick(View view, int position, Content.Media media, PlaybackInfo info) {
        String content = getString(contents[position % contents.length]);
        Point viewSize = new Point(view.getWidth(), view.getHeight());
        Point videoSize = new Point(view.getWidth(), view.getHeight());
        if (view instanceof PlayerView && ((PlayerView) view).getPlayer() != null) {
          Player player = ((PlayerView) view).getPlayer();
          Format videoFormat =  //
              player instanceof SimpleExoPlayer ? ((SimpleExoPlayer) player).getVideoFormat()
                  : null;
          if (videoFormat != null
              && videoFormat.width != Format.NO_VALUE
              && videoFormat.height != Format.NO_VALUE) {
            videoSize.set(videoFormat.width, videoFormat.height);
          }
        }

        Intent intent = SinglePlayerActivity.createIntent(getContext(), position, media.mediaUri, //
            content, info, viewSize, videoSize, false);
        //noinspection ConstantConditions
        ActivityOptionsCompat options = ActivityOptionsCompat.
            makeSceneTransitionAnimation(getActivity(), view, ViewCompat.getTransitionName(view));
        startActivityForResult(intent, RQ_PLAYBACK_INFO, options.toBundle());
      }
    });
    container.setAdapter(adapter);
    container.setCacheManager(adapter);

    pagerAdapter = new LicensePagerAdapter(adapter);
    viewPager.setAdapter(pagerAdapter);

    pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {

      @Override public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
          handler.obtainMessage(MSG_PAGER_PAGE_CHANGED, //
              viewPager.getCurrentItem(), -1).sendToTarget();
        }
      }
    };
    viewPager.addOnPageChangeListener(pageChangeListener);
    selector = container.getPlayerSelector();
  }

  @Override public void onViewStateRestored(@Nullable Bundle bundle) {
    super.onViewStateRestored(bundle);
    if (this.callback != null) {
      this.callback.onContainerAvailable(container);
    }
  }

  @Override public void onDestroyView() {
    viewPager.removeOnPageChangeListener(pageChangeListener);
    pageChangeListener = null;
    snapHelper.attachToRecyclerView(null);
    container.removeOnScrollListener(onScrollListener);
    onScrollListener = null;
    layoutManager = null;
    adapter = null;
    super.onDestroyView();
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK && requestCode == RQ_PLAYBACK_INFO && data != null) {
      PlaybackInfo info = data.getParcelableExtra(RESULT_EXTRA_PLAYBACK_INFO);
      int order = data.getIntExtra(RESULT_EXTRA_PLAYER_ORDER, -1);
      if (order >= 0 && container != null) {
        container.setPlayerSelector(PlayerSelector.NONE);
        container.savePlaybackInfo(order, info);
        container.setPlayerSelector(selector);
      }
    }
  }

  public interface Callback {

    void onContainerAvailable(@NonNull Container container);
  }

  static final int MSG_PAGER_PAGE_CHANGED = 1;
  static final int MSG_CONTAINER_SCROLL_IDLE = 2;

  final Handler handler = new Handler(new Handler.Callback() {
    int currentPage = -1;

    @Override public boolean handleMessage(Message msg) {
      int page = msg.arg1;
      Log.w(TAG, "handleMessage() called with: msg = [" + msg + "]");
      Log.i(TAG, "handleMessage: " + currentPage + " | " + page);
      if (page == currentPage) return false;
      currentPage = page;
      int what = msg.what;
      switch (what) {
        case MSG_PAGER_PAGE_CHANGED:  // viewPager finish page change --> update Container
          container.smoothScrollToPosition(currentPage);
          return true;
        case MSG_CONTAINER_SCROLL_IDLE: // container stop scrolling --> update viewpager
          viewPager.setCurrentItem(currentPage, true);
          return true;
        default:
          return false;
      }
    }
  });

  static class LicensePagerAdapter extends PagerAdapter {

    private final CustomLayoutAdapter adapter;

    LicensePagerAdapter(CustomLayoutAdapter adapter) {
      this.adapter = adapter;
    }

    @Override public int getCount() {
      return adapter.getItemCount();
    }

    @Override public boolean isViewFromObject(View view, Object object) {
      return object == view;
    }

    @Override public Object instantiateItem(ViewGroup container, int position) {
      View view = LayoutInflater.from(container.getContext())
          .inflate(R.layout.widget_custom_video_license_text, container, false);
      TextView textView = view.findViewById(R.id.text_content);
      textView.setText(
          Html.fromHtml(container.getContext().getString(contents[position % contents.length])));
      container.addView(view);
      return view;
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
      if (object instanceof View) {
        container.removeView((View) object);
      }
    }
  }
}
