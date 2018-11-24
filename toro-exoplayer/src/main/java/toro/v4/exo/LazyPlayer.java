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

package toro.v4.exo;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.PlaybackParams;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import com.google.android.exoplayer2.BasePlayer;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.PlayerMessage;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AuxEffectInfo;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.video.VideoFrameMetadataListener;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.google.android.exoplayer2.video.spherical.CameraMotionListener;
import im.ene.toro.exoplayer.ToroExoPlayer;

/** Same interface as {@link SimpleExoPlayer}, just lazily create stuff. */
final class LazyPlayer extends BasePlayer
    implements ExoPlayer, Player.AudioComponent, Player.VideoComponent, Player.TextComponent {

  private ToroExoPlayer delegate;

  @NonNull private final Context context;
  @NonNull private final RenderersFactory renderersFactory;
  @NonNull private final TrackSelector trackSelector;
  @NonNull private final LoadControl loadControl;
  @NonNull private final BandwidthMeter bandwidthMeter;
  @NonNull private final Looper looper;
  @Nullable private final DrmSessionManager<FrameworkMediaCrypto> drmSessionManager;

  LazyPlayer(@NonNull Context context, @NonNull RenderersFactory renderersFactory,
      @NonNull TrackSelector trackSelector, @NonNull LoadControl loadControl,
      @NonNull BandwidthMeter bandwidthMeter, @Nullable
      DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, @NonNull Looper looper) {
    this.context = context.getApplicationContext();
    this.renderersFactory = renderersFactory;
    this.trackSelector = trackSelector;
    this.loadControl = loadControl;
    this.bandwidthMeter = bandwidthMeter;
    this.drmSessionManager = drmSessionManager;
    this.looper = looper;
  }

  private ToroExoPlayer getDelegate() {
    if (delegate == null) {
      delegate = new ToroExoPlayer(
          this.context,
          this.renderersFactory,
          this.trackSelector,
          this.loadControl,
          this.bandwidthMeter,
          this.drmSessionManager,
          this.looper
      );
    }
    return delegate;
  }

  @Nullable @Override public AudioComponent getAudioComponent() {
    return getDelegate().getAudioComponent();
  }

  @Nullable @Override public VideoComponent getVideoComponent() {
    return getDelegate().getVideoComponent();
  }

  @Nullable @Override public TextComponent getTextComponent() {
    return getDelegate().getTextComponent();
  }

  @Override public void setVideoScalingMode(int videoScalingMode) {
    getDelegate().setVideoScalingMode(videoScalingMode);
  }

  @Override public int getVideoScalingMode() {
    return getDelegate().getVideoScalingMode();
  }

  @Override public void clearVideoSurface() {
    getDelegate().clearVideoSurface();
  }

  @Override public void clearVideoSurface(Surface surface) {
    getDelegate().clearVideoSurface(surface);
  }

  @Override public void setVideoSurface(@Nullable Surface surface) {
    getDelegate().setVideoSurface(surface);
  }

  @Override public void setVideoSurfaceHolder(SurfaceHolder surfaceHolder) {
    getDelegate().setVideoSurfaceHolder(surfaceHolder);
  }

  @Override public void clearVideoSurfaceHolder(SurfaceHolder surfaceHolder) {
    getDelegate().clearVideoSurfaceHolder(surfaceHolder);
  }

  @Override public void setVideoSurfaceView(SurfaceView surfaceView) {
    getDelegate().setVideoSurfaceView(surfaceView);
  }

  @Override public void clearVideoSurfaceView(SurfaceView surfaceView) {
    getDelegate().clearVideoSurfaceView(surfaceView);
  }

  @Override public void setVideoTextureView(TextureView textureView) {
    getDelegate().setVideoTextureView(textureView);
  }

  @Override public void clearVideoTextureView(TextureView textureView) {
    getDelegate().clearVideoTextureView(textureView);
  }

  @Override public void addAudioListener(AudioListener listener) {
    getDelegate().addAudioListener(listener);
  }

  @Override public void removeAudioListener(AudioListener listener) {
    getDelegate().removeAudioListener(listener);
  }

  @Override public void setAudioAttributes(AudioAttributes audioAttributes) {
    getDelegate().setAudioAttributes(audioAttributes);
  }

  @Override
  public void setAudioAttributes(AudioAttributes audioAttributes, boolean handleAudioFocus) {
    getDelegate().setAudioAttributes(audioAttributes, handleAudioFocus);
  }

  @Override public AudioAttributes getAudioAttributes() {
    return getDelegate().getAudioAttributes();
  }

  @Override public int getAudioSessionId() {
    return getDelegate().getAudioSessionId();
  }

  @Override public void setAuxEffectInfo(AuxEffectInfo auxEffectInfo) {
    getDelegate().setAuxEffectInfo(auxEffectInfo);
  }

  @Override public void clearAuxEffectInfo() {
    getDelegate().clearAuxEffectInfo();
  }

  @Override public void setVolume(float audioVolume) {
    getDelegate().setVolume(audioVolume);
  }

  @Override public float getVolume() {
    return getDelegate().getVolume();
  }

  @Deprecated public void setAudioStreamType(int streamType) {
    getDelegate().setAudioStreamType(streamType);
  }

  @Deprecated public int getAudioStreamType() {
    return getDelegate().getAudioStreamType();
  }

  public AnalyticsCollector getAnalyticsCollector() {
    return getDelegate().getAnalyticsCollector();
  }

  public void addAnalyticsListener(AnalyticsListener listener) {
    getDelegate().addAnalyticsListener(listener);
  }

  public void removeAnalyticsListener(
      AnalyticsListener listener) {
    getDelegate().removeAnalyticsListener(listener);
  }

  @TargetApi(23) @Deprecated public void setPlaybackParams(@Nullable PlaybackParams params) {
    getDelegate().setPlaybackParams(params);
  }

  public Format getVideoFormat() {
    return getDelegate().getVideoFormat();
  }

  public Format getAudioFormat() {
    return getDelegate().getAudioFormat();
  }

  public DecoderCounters getVideoDecoderCounters() {
    return getDelegate().getVideoDecoderCounters();
  }

  public DecoderCounters getAudioDecoderCounters() {
    return getDelegate().getAudioDecoderCounters();
  }

  @Override public void addVideoListener(VideoListener listener) {
    getDelegate().addVideoListener(listener);
  }

  @Override public void removeVideoListener(VideoListener listener) {
    getDelegate().removeVideoListener(listener);
  }

  @Override public void setVideoFrameMetadataListener(
      VideoFrameMetadataListener listener) {
    getDelegate().setVideoFrameMetadataListener(listener);
  }

  @Override public void clearVideoFrameMetadataListener(
      VideoFrameMetadataListener listener) {
    getDelegate().clearVideoFrameMetadataListener(listener);
  }

  @Override public void setCameraMotionListener(
      CameraMotionListener listener) {
    getDelegate().setCameraMotionListener(listener);
  }

  @Override public void clearCameraMotionListener(
      CameraMotionListener listener) {
    getDelegate().clearCameraMotionListener(listener);
  }

  @Deprecated public void setVideoListener(SimpleExoPlayer.VideoListener listener) {
    getDelegate().setVideoListener(listener);
  }

  @Deprecated public void clearVideoListener(SimpleExoPlayer.VideoListener listener) {
    getDelegate().clearVideoListener(listener);
  }

  @Override public void addTextOutput(TextOutput listener) {
    getDelegate().addTextOutput(listener);
  }

  @Override public void removeTextOutput(TextOutput listener) {
    getDelegate().removeTextOutput(listener);
  }

  @Deprecated public void setTextOutput(TextOutput output) {
    getDelegate().setTextOutput(output);
  }

  @Deprecated public void clearTextOutput(TextOutput output) {
    getDelegate().clearTextOutput(output);
  }

  public void addMetadataOutput(MetadataOutput listener) {
    getDelegate().addMetadataOutput(listener);
  }

  public void removeMetadataOutput(MetadataOutput listener) {
    getDelegate().removeMetadataOutput(listener);
  }

  @Deprecated public void setMetadataOutput(MetadataOutput output) {
    getDelegate().setMetadataOutput(output);
  }

  @Deprecated public void clearMetadataOutput(MetadataOutput output) {
    getDelegate().clearMetadataOutput(output);
  }

  @Deprecated public void setVideoDebugListener(
      VideoRendererEventListener listener) {
    getDelegate().setVideoDebugListener(listener);
  }

  @Deprecated public void addVideoDebugListener(
      VideoRendererEventListener listener) {
    getDelegate().addVideoDebugListener(listener);
  }

  @Deprecated public void removeVideoDebugListener(
      VideoRendererEventListener listener) {
    getDelegate().removeVideoDebugListener(listener);
  }

  @Deprecated public void setAudioDebugListener(
      AudioRendererEventListener listener) {
    getDelegate().setAudioDebugListener(listener);
  }

  @Deprecated public void addAudioDebugListener(
      AudioRendererEventListener listener) {
    getDelegate().addAudioDebugListener(listener);
  }

  @Deprecated public void removeAudioDebugListener(
      AudioRendererEventListener listener) {
    getDelegate().removeAudioDebugListener(listener);
  }

  @Override public Looper getPlaybackLooper() {
    return getDelegate().getPlaybackLooper();
  }

  @Override public Looper getApplicationLooper() {
    return getDelegate().getApplicationLooper();
  }

  @Override public void addListener(Player.EventListener listener) {
    getDelegate().addListener(listener);
  }

  @Override public void removeListener(Player.EventListener listener) {
    getDelegate().removeListener(listener);
  }

  @Override public int getPlaybackState() {
    return getDelegate().getPlaybackState();
  }

  @Override @Nullable public ExoPlaybackException getPlaybackError() {
    return getDelegate().getPlaybackError();
  }

  @Override public void retry() {
    getDelegate().retry();
  }

  @Override public void prepare(MediaSource mediaSource) {
    getDelegate().prepare(mediaSource);
  }

  @Override
  public void prepare(MediaSource mediaSource, boolean resetPosition, boolean resetState) {
    getDelegate().prepare(mediaSource, resetPosition, resetState);
  }

  @Override public void setPlayWhenReady(boolean playWhenReady) {
    getDelegate().setPlayWhenReady(playWhenReady);
  }

  @Override public boolean getPlayWhenReady() {
    return getDelegate().getPlayWhenReady();
  }

  @Override public int getRepeatMode() {
    return getDelegate().getRepeatMode();
  }

  @Override public void setRepeatMode(int repeatMode) {
    getDelegate().setRepeatMode(repeatMode);
  }

  @Override public void setShuffleModeEnabled(boolean shuffleModeEnabled) {
    getDelegate().setShuffleModeEnabled(shuffleModeEnabled);
  }

  @Override public boolean getShuffleModeEnabled() {
    return getDelegate().getShuffleModeEnabled();
  }

  @Override public boolean isLoading() {
    return getDelegate().isLoading();
  }

  @Override public void seekTo(int windowIndex, long positionMs) {
    getDelegate().seekTo(windowIndex, positionMs);
  }

  @Override public void setPlaybackParameters(
      @Nullable PlaybackParameters playbackParameters) {
    getDelegate().setPlaybackParameters(playbackParameters);
  }

  @Override public PlaybackParameters getPlaybackParameters() {
    return getDelegate().getPlaybackParameters();
  }

  @Override public void setSeekParameters(@Nullable SeekParameters seekParameters) {
    getDelegate().setSeekParameters(seekParameters);
  }

  @Override public SeekParameters getSeekParameters() {
    return getDelegate().getSeekParameters();
  }

  @Override public void stop(boolean reset) {
    getDelegate().stop(reset);
  }

  @Override public void release() {
    getDelegate().release();
  }

  @Override @Deprecated public void sendMessages(ExoPlayerMessage... messages) {
    getDelegate().sendMessages(messages);
  }

  @Override public PlayerMessage createMessage(PlayerMessage.Target target) {
    return getDelegate().createMessage(target);
  }

  @Override @Deprecated public void blockingSendMessages(
      ExoPlayerMessage... messages) {
    getDelegate().blockingSendMessages(messages);
  }

  @Override public int getRendererCount() {
    return getDelegate().getRendererCount();
  }

  @Override public int getRendererType(int index) {
    return getDelegate().getRendererType(index);
  }

  @Override public TrackGroupArray getCurrentTrackGroups() {
    return getDelegate().getCurrentTrackGroups();
  }

  @Override public TrackSelectionArray getCurrentTrackSelections() {
    return getDelegate().getCurrentTrackSelections();
  }

  @Override public Timeline getCurrentTimeline() {
    return getDelegate().getCurrentTimeline();
  }

  @Override @Nullable public Object getCurrentManifest() {
    return getDelegate().getCurrentManifest();
  }

  @Override public int getCurrentPeriodIndex() {
    return getDelegate().getCurrentPeriodIndex();
  }

  @Override public int getCurrentWindowIndex() {
    return getDelegate().getCurrentWindowIndex();
  }

  @Override public long getDuration() {
    return getDelegate().getDuration();
  }

  @Override public long getCurrentPosition() {
    return getDelegate().getCurrentPosition();
  }

  @Override public long getBufferedPosition() {
    return getDelegate().getBufferedPosition();
  }

  @Override public long getTotalBufferedDuration() {
    return getDelegate().getTotalBufferedDuration();
  }

  @Override public boolean isPlayingAd() {
    return getDelegate().isPlayingAd();
  }

  @Override public int getCurrentAdGroupIndex() {
    return getDelegate().getCurrentAdGroupIndex();
  }

  @Override public int getCurrentAdIndexInAdGroup() {
    return getDelegate().getCurrentAdIndexInAdGroup();
  }

  @Override public long getContentPosition() {
    return getDelegate().getContentPosition();
  }

  @Override public long getContentBufferedPosition() {
    return getDelegate().getContentBufferedPosition();
  }
}
