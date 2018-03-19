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

package im.ene.toro.exoplayer.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.TimeBar;
import im.ene.toro.exoplayer.VolumeInfo;
import im.ene.toro.mopub.R;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An extension of {@link PlaybackControlView} that adds Volume control buttons. It works on-par
 * with {@link PlayerView}. The reason is the original {@link SimpleExoPlayerView} doesn't accept
 * custom {@link PlaybackControlView} by default. {@link PlayerView} copies its best and add some
 * improvement on top of it. Any other behavior should be the same.
 *
 * @author eneim (2018/02/28).
 */

public class ToroControlView extends PlaybackControlView {

  static final String TAG = "ToroExo:Control";

  static Method hideAfterTimeoutMethod; // from parent ...
  static boolean hideMethodFetched;
  static Field hideActionField;
  static boolean hideActionFetched;

  final ComponentListener componentListener;
  final View volumeUpButton;
  final View volumeOffButton;
  final TimeBar volumeBar;
  final VolumeInfo volumeInfo = new VolumeInfo(false, 1);

  public ToroControlView(Context context) {
    this(context, null);
  }

  public ToroControlView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ToroControlView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    volumeOffButton = findViewById(R.id.exo_volume_off);
    volumeUpButton = findViewById(R.id.exo_volume_up);
    volumeBar = findViewById(R.id.volume_bar);

    componentListener = new ComponentListener();
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (volumeUpButton != null) volumeUpButton.setOnClickListener(componentListener);
    if (volumeOffButton != null) volumeOffButton.setOnClickListener(componentListener);
    if (volumeBar != null) volumeBar.setListener(componentListener);

    updateVolumeButtons();
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (volumeUpButton != null) volumeUpButton.setOnClickListener(null);
    if (volumeOffButton != null) volumeOffButton.setOnClickListener(null);
    if (volumeBar != null) volumeBar.setListener(null);
  }

  @SuppressLint("ClickableViewAccessibility") @Override
  public boolean onTouchEvent(MotionEvent event) {
    // After processing all children' touch event, this View will just stop it here.
    // User can click to PlayerView to show/hide this view, but since this View's height is not
    // significantly large, clicking to show/hide may disturb other actions like clicking to button,
    // seeking the bars, etc. This extension will stop the touch event here so that PlayerView has
    // nothing to do when User touch this View.
    return true;
  }

  @Override public void setPlayer(ExoPlayer player) {
    super.setPlayer(player);
    float volume = player instanceof SimpleExoPlayer ? ((SimpleExoPlayer) player).getVolume() : 1;
    updateVolumeInfo(volume);
    updateVolumeButtons();
  }

  private void updateVolumeInfo(float actualVolume) {
    boolean actuallyMute = actualVolume == 0;
    volumeInfo.setTo(actuallyMute, actualVolume);
  }

  // Called by PlayerView to update UI buttons here.
  void onVolumeInfoUpdate(VolumeInfo volumeInfo) {
    this.volumeInfo.setTo(volumeInfo.isMute(), volumeInfo.getVolume());
    updateVolumeButtons();
  }

  private class ComponentListener implements OnClickListener, TimeBar.OnScrubListener {

    @Override public void onClick(View v) {
      ExoPlayer player = getPlayer();
      if (player == null || !(player instanceof SimpleExoPlayer)) return;
      SimpleExoPlayer exoPlayer = (SimpleExoPlayer) player;
      if (v == volumeOffButton) {  // click to vol Off --> unmute
        volumeInfo.setTo(false, volumeInfo.getVolume());
        exoPlayer.setVolume(volumeInfo.getVolume());
      } else if (v == volumeUpButton) {  // click to vol Up --> mute
        volumeInfo.setTo(true, volumeInfo.getVolume());
        exoPlayer.setVolume(0);
      }

      updateVolumeButtons();
    }

    /// TimeBar.OnScrubListener

    @Override public void onScrubStart(TimeBar timeBar, long position) {
      dispatchOnScrubStart();
    }

    @Override public void onScrubMove(TimeBar timeBar, long position) {
      dispatchOnScrubMove(position);
    }

    @Override public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
      // no-ops
    }
  }

  @Override protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
    super.onVisibilityChanged(changedView, visibility);
    if (changedView != this) return;
    updateVolumeButtons();
  }

  @SuppressLint("LogNotTimber") @SuppressWarnings("ConstantConditions") //
  void updateVolumeButtons() {
    if (!isVisible() || !ViewCompat.isAttachedToWindow(this)) {
      return;
    }
    boolean requestButtonFocus = false;
    // if muted then show volumeOffButton, or else show volumeUpButton
    boolean muted = volumeInfo.isMute();
    if (volumeOffButton != null) {
      requestButtonFocus |= muted && volumeOffButton.isFocused();
      volumeOffButton.setVisibility(muted ? View.VISIBLE : View.GONE);
    }
    if (volumeUpButton != null) {
      requestButtonFocus |= !muted && volumeUpButton.isFocused();
      volumeUpButton.setVisibility(!muted ? View.VISIBLE : View.GONE);
    }

    if (volumeBar != null) {
      volumeBar.setDuration(100);
      volumeBar.setPosition(muted ? 0 : (long) (volumeInfo.getVolume() * 100));
    }

    if (requestButtonFocus) {
      requestButtonFocus();
    }

    // A hack to access PlaybackControlView's hideAfterTimeout. Don't want to re-implement it.
    // Reflection happens once for all instances, so it should not affect the performance.
    if (!hideMethodFetched) {
      long start = System.nanoTime();
      try {
        hideAfterTimeoutMethod = PlaybackControlView.class.getDeclaredMethod("hideAfterTimeout");
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
      hideAfterTimeoutMethod.setAccessible(true);
      hideMethodFetched = true;
      Log.i(TAG, "reflection time: " + (System.nanoTime() - start) + "ns");
    }

    if (hideAfterTimeoutMethod != null) {
      try {
        hideAfterTimeoutMethod.invoke(this);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  private void requestButtonFocus() {
    boolean muted = volumeInfo.isMute();
    if (!muted && volumeUpButton != null) {
      volumeUpButton.requestFocus();
    } else if (muted && volumeOffButton != null) {
      volumeOffButton.requestFocus();
    }
  }

  void dispatchOnScrubStart() {
    // Fetch the 'hideAction' Runnable from super class. We need this to synchronize the show/hide
    // behaviour when user does something.
    if (!hideActionFetched) {
      try {
        hideActionField = PlaybackControlView.class.getDeclaredField("hideAction");
        hideActionField.setAccessible(true);
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      }
      hideActionFetched = true;
    }

    if (hideActionField != null) {
      try {
        removeCallbacks((Runnable) hideActionField.get(this));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  // Scrub Move will always modify actual Volume, there is no 'mute-with-non-zero-volume' state.
  void dispatchOnScrubMove(long position) {
    if (position > 100) position = 100;
    if (position < 0) position = 0;

    float actualVolume = position / (float) 100;
    if (getPlayer() instanceof SimpleExoPlayer) {
      ((SimpleExoPlayer) getPlayer()).setVolume(actualVolume);
    }

    updateVolumeInfo(actualVolume);
    updateVolumeButtons();
  }
}