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

package im.ene.toro.sample.article;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.DemoUtil;
import im.ene.toro.widget.Container;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;
import org.jsoup.Jsoup;

public class ScrollingArticleActivity extends Activity {

  private static final String TAG = "Toro:Article";

  static final String cover = "file:///android_asset/design/design_is_never_done_cover.jpg";
  static final String BODY_SELECTOR =
      "body > div.gd-page > div.body-container > div > div.article-page--main.mdc-layout-grid.js-share-fab-parent > div.article-page--copy.mdc-layout-grid__cell.mdc-layout-grid__cell--span-10 > div";

  @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
  @BindView(R.id.cover) ImageView coverView;
  @BindView(R.id.player_container) Container container;

  ArticleAdapter adapter;
  RequestOptions options = new RequestOptions().fitCenter();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scrolling_article);
    ButterKnife.bind(this);

    Glide.with(this).load(cover).apply(options).into(coverView);
    adapter = new ArticleAdapter();
    container.setAdapter(adapter);
    container.setLayoutManager(new LinearLayoutManager(this));

    Observable.just(Jsoup.parse(DemoUtil.getFileContent(this, "design/material.html")))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(document -> toolbarLayout.setTitle(document.title()))
        .map(document -> document.body().select(BODY_SELECTOR).first().children())
        .subscribe(elements -> adapter.setElements(elements));
  }
}
