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

package im.ene.lab.toro.sample.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.internal.ForegroundLinearLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import im.ene.lab.toro.sample.R;

/**
 * Created by eneim on 3/11/16.
 */
public class SampleItemButton extends ForegroundLinearLayout {

  public SampleItemButton(Context context) {
    this(context, null);
  }

  public SampleItemButton(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  @Bind(R.id.title) TextView mTitle;
  @Bind(R.id.subtitle) TextView mSubtitle;
  @Bind(R.id.sample_gif) ImageView mImage;

  public SampleItemButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    LayoutInflater.from(context).inflate(R.layout.sample_item_button, this, true);
    ButterKnife.bind(this, this);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SampleItemButton, defStyle, 0);
    String title = a.getString(R.styleable.SampleItemButton_btn_title);
    String subTitle = a.getString(R.styleable.SampleItemButton_btn_subtitle);

    final int imageResource = a.getResourceId(R.styleable.SampleItemButton_btn_image, 0);

    a.recycle();

    if (title == null || TextUtils.isEmpty(title)) {
      throw new IllegalArgumentException("Title must not be empty");
    }

    mTitle.setText(title);

    if (subTitle == null || TextUtils.isEmpty(subTitle)) {
      mSubtitle.setVisibility(GONE);
    } else {
      mSubtitle.setVisibility(VISIBLE);
      mSubtitle.setText(subTitle);
    }

    if (imageResource != 0) {
      mImage.setVisibility(VISIBLE);
      GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(mImage);
      Glide.with(context).load(imageResource).crossFade().into(imageViewTarget);
    } else {
      mImage.setVisibility(GONE);
    }
  }
}
