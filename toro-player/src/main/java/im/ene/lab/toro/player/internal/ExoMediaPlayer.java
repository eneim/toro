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
package im.ene.lab.toro.player.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec.CryptoException;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.FloatRange;
import android.view.Surface;
import com.google.android.exoplayer.CodecCounters;
import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.SingleSampleSource;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer.MetadataRenderer;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.TextRenderer;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.util.DebugTextViewHelper;
import im.ene.lab.toro.media.OnInfoListener;
import im.ene.lab.toro.media.OnPlayerStateChangeListener;
import im.ene.lab.toro.media.OnVideoSizeChangedListener;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.media.Cineer;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A wrapper around {@link ExoPlayer} that provides a higher level interface. It can be prepared
 * with one of a number of {@link RendererBuilder} classes to suit different use cases (e.g. DASH,
 * SmoothStreaming and so on).
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ExoMediaPlayer
    implements Cineer, ExoPlayer.Listener, ChunkSampleSource.EventListener,
    HlsSampleSource.EventListener, ExtractorSampleSource.EventListener,
    SingleSampleSource.EventListener, DefaultBandwidthMeter.EventListener,
    MediaCodecVideoTrackRenderer.EventListener, MediaCodecAudioTrackRenderer.EventListener,
    StreamingDrmSessionManager.EventListener, DashChunkSource.EventListener, TextRenderer,
    MetadataRenderer<List<Id3Frame>>, DebugTextViewHelper.Provider,
    AudioCapabilitiesReceiver.Listener {

  @Override public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {

  }

  /**
   * Builds renderers for the player.
   */
  public interface RendererBuilder {
    /**
     * Builds renderers for playback.
     *
     * @param player The player for which renderers are being built. {@link
     * ExoMediaPlayer#onRenderers}
     * should be invoked once the renderers have been built. If building fails,
     * {@link ExoMediaPlayer#onRenderersError} should be invoked.
     */
    void buildRenderers(ExoMediaPlayer player);

    /**
     * Cancels the current build operation, if there is one. Else does nothing.
     * <p>
     * A canceled build operation must not invoke {@link ExoMediaPlayer#onRenderers} or
     * {@link ExoMediaPlayer#onRenderersError} on the player, which may have been released.
     */
    void cancel();
  }

  /**
   * A listener for core events.
   */
  public interface Listener {

    void onStateChanged(boolean playWhenReady, int playbackState);

    void onError(Exception e);

    void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio);
  }

  /**
   * A listener for internal errors.
   * <p>
   * These errors are not visible to the user, and hence this listener is provided for
   * informational purposes only. Note however that an internal error may cause a fatal
   * error if the player fails to recover. If this happens, {@link Listener#onError(Exception)}
   * will be invoked.
   */
  public interface InternalErrorListener {

    void onRendererInitializationError(Exception e);

    void onAudioTrackInitializationError(AudioTrack.InitializationException e);

    void onAudioTrackWriteError(AudioTrack.WriteException e);

    void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs);

    void onDecoderInitializationError(DecoderInitializationException e);

    void onCryptoError(CryptoException e);

    void onLoadError(int sourceId, IOException e);

    void onDrmSessionManagerError(Exception e);
  }

  /**
   * A listener for debugging information.
   */
  public interface InfoListener {

    void onVideoFormatEnabled(Format format, int trigger, long mediaTimeMs);

    void onAudioFormatEnabled(Format format, int trigger, long mediaTimeMs);

    void onDroppedFrames(int count, long elapsed);

    void onBandwidthSample(int elapsedMs, long bytes, long bitrateEstimate);

    void onLoadStarted(int sourceId, long length, int type, int trigger, Format format,
        long mediaStartTimeMs, long mediaEndTimeMs);

    void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format,
        long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs);

    void onDecoderInitialized(String decoderName, long elapsedRealtimeMs,
        long initializationDurationMs);

    void onAvailableRangeChanged(int sourceId, TimeRange availableRange);
  }

  /**
   * A listener for receiving notifications of timed text.
   */
  public interface CaptionListener {

    void onCues(List<Cue> cues);
  }

  /**
   * A listener for receiving ID3 metadata parsed from the media stream.
   */
  public interface Id3MetadataListener {

    void onId3Metadata(List<Id3Frame> id3Frames);
  }

  // Constants pulled into this class for convenience.
  private static final int EXO_STATE_IDLE = ExoPlayer.STATE_IDLE;
  private static final int EXO_STATE_PREPARING = ExoPlayer.STATE_PREPARING;
  private static final int EXO_STATE_BUFFERING = ExoPlayer.STATE_BUFFERING;
  private static final int EXO_STATE_READY = ExoPlayer.STATE_READY;
  private static final int EXO_STATE_ENDED = ExoPlayer.STATE_ENDED;

  public static final int TRACK_DISABLED = ExoPlayer.TRACK_DISABLED;
  public static final int TRACK_DEFAULT = ExoPlayer.TRACK_DEFAULT;

  public static final int RENDERER_COUNT = 4;
  public static final int TYPE_VIDEO = 0;
  public static final int TYPE_AUDIO = 1;
  public static final int TYPE_TEXT = 2;
  public static final int TYPE_METADATA = 3;

  private static final int RENDERER_BUILDING_STATE_IDLE = 1;
  private static final int RENDERER_BUILDING_STATE_BUILDING = 2;
  private static final int RENDERER_BUILDING_STATE_BUILT = 3;

  private final RendererBuilder rendererBuilder;
  /* package */final ExoPlayer player;

  private final Handler mainHandler;
  private final CopyOnWriteArrayList<Listener> listeners;

  private int rendererBuildingState;
  private int lastReportedPlaybackState;
  private boolean lastReportedPlayWhenReady;

  private Surface surface;
  private TrackRenderer videoRenderer;
  private TrackRenderer audioRenderer;
  private CodecCounters codecCounters;
  private Format videoFormat;
  private int videoTrackToRestore;

  private BandwidthMeter bandwidthMeter;
  private boolean backgrounded;

  /**
   * The names of the available tracks, indexed by ExoplayerWrapper INDEX_* constants.
   * May be null if the track names are unknown. An individual element may be null if the track
   * names are unknown for the corresponding type.
   */
  private String[][] trackNames;

  private CaptionListener captionListener;
  private Id3MetadataListener id3MetadataListener;
  private InternalErrorListener internalErrorListener;
  private InfoListener infoListener;

  private OnPlayerStateChangeListener onPlayerStateChangeListener;
  // private OnPreparedListener onPreparedListener;
  // private OnCompletionListener onCompletionListener;
  private OnVideoSizeChangedListener onVideoSizeChangedListener;
  // private OnBufferingUpdateListener onBufferingUpdateListener;

  public ExoMediaPlayer(RendererBuilder rendererBuilder) {
    player = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
    player.addListener(this);
    this.rendererBuilder = rendererBuilder;
    mainHandler = new Handler();
    listeners = new CopyOnWriteArrayList<>();
    lastReportedPlaybackState = EXO_STATE_IDLE;
    rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    // Disable text initially.
    player.setSelectedTrack(TYPE_TEXT, TRACK_DISABLED);
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  public void setInternalErrorListener(InternalErrorListener listener) {
    internalErrorListener = listener;
  }

  public void setInfoListener(InfoListener listener) {
    infoListener = listener;
  }

  public void setCaptionListener(CaptionListener listener) {
    captionListener = listener;
  }

  public void setMetadataListener(Id3MetadataListener listener) {
    id3MetadataListener = listener;
  }

  public void setSurface(Surface surface) {
    this.surface = surface;
    pushSurface(false);
  }

  @Override public void prepareAsync() throws IllegalStateException {
    prepare();
    setPlayWhenReady(false);
  }

  public Surface getSurface() {
    return surface;
  }

  public void blockingClearSurface() {
    surface = null;
    pushSurface(true);
  }

  /**
   * Returns the name of the track at the given index.
   * @param type The index indicating the type of video (ex {@link #TYPE_VIDEO})
   */
  public String[] getTracks(int type) {
    return trackNames == null ? null : trackNames[type];
  }

  public int getTrackCount(int type) {
    return player.getTrackCount(type);
  }

  public MediaFormat getTrackFormat(int type, int index) {
    return player.getTrackFormat(type, index);
  }

  public int getSelectedTrack(int type) {
    return player.getSelectedTrack(type);
  }

  public void setSelectedTrack(int type, int index) {
    player.setSelectedTrack(type, index);
    if (type == TYPE_TEXT && index < 0 && captionListener != null) {
      captionListener.onCues(Collections.<Cue>emptyList());
    }
  }

  public boolean getBackgrounded() {
    return backgrounded;
  }

  public void setBackgrounded(boolean backgrounded) {
    if (this.backgrounded == backgrounded) {
      return;
    }

    this.backgrounded = backgrounded;
    if (backgrounded) {
      videoTrackToRestore = getSelectedTrack(TYPE_VIDEO);
      setSelectedTrack(TYPE_VIDEO, TRACK_DISABLED);
      blockingClearSurface();
    } else {
      setSelectedTrack(TYPE_VIDEO, videoTrackToRestore);
    }
  }

  public void prepare() {
    if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILT) {
      player.stop();
    }
    rendererBuilder.cancel();
    videoFormat = null;
    videoRenderer = null;
    audioRenderer = null;
    rendererBuildingState = RENDERER_BUILDING_STATE_BUILDING;
    maybeReportPlayerState();
    rendererBuilder.buildRenderers(this);
  }

  /**
   * Invoked with the results from a {@link RendererBuilder}.
   *
   * @param renderers Renderers indexed by {@link ExoMediaPlayer} TYPE_* constants. An individual
   * element may be null if there do not exist tracks of the corresponding type.
   * @param bandwidthMeter Provides an estimate of the currently available bandwidth. May be null.
   */
  /* package */ void onRenderers(TrackRenderer[] renderers, BandwidthMeter bandwidthMeter) {
    // Normalize the results.
    if (trackNames == null) {
      trackNames = new String[RENDERER_COUNT][];
    }

    for (int i = 0; i < RENDERER_COUNT; i++) {
      if (renderers[i] == null) {
        // Convert a null renderer to a dummy renderer.
        renderers[i] = new DummyTrackRenderer();
      }
    }
    // Complete preparation.
    this.videoRenderer = renderers[TYPE_VIDEO];
    this.audioRenderer = renderers[TYPE_AUDIO];
    if (!(this.audioRenderer instanceof EnhancedMediaCodecAudioTrackRenderer)) {
      throw new RuntimeException("Audio Renderer must be a TrMediaCodecAudioTrackRenderer");
    }
    this.codecCounters = videoRenderer instanceof MediaCodecTrackRenderer
        ? ((MediaCodecTrackRenderer) videoRenderer).codecCounters
        : ((EnhancedMediaCodecAudioTrackRenderer) renderers[TYPE_AUDIO]).codecCounters;
    this.bandwidthMeter = bandwidthMeter;
    pushSurface(false);
    player.prepare(renderers);
    rendererBuildingState = RENDERER_BUILDING_STATE_BUILT;
  }

  /**
   * Invoked if a {@link RendererBuilder} encounters an error.
   *
   * @param e Describes the error.
   */
  /* package */ void onRenderersError(Exception e) {
    if (internalErrorListener != null) {
      internalErrorListener.onRendererInitializationError(e);
    }
    for (Listener listener : listeners) {
      listener.onError(e);
    }
    if (this.onPlayerStateChangeListener != null) {
      this.onPlayerStateChangeListener.onPlayerError(this, new PlaybackException(e, 0, 0));
    }
    rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    maybeReportPlayerState();
  }

  public void setPlayWhenReady(boolean playWhenReady) {
    player.setPlayWhenReady(playWhenReady);
  }

  public void seekTo(long positionMs) {
    player.seekTo(positionMs);
  }

  @Override public boolean isPlaying() {
    return player.getPlayWhenReady();
  }

  private int videoWidth;
  private int videoHeight;

  @Override public int getVideoWidth() {
    return videoWidth;
  }

  @Override public int getVideoHeight() {
    return videoHeight;
  }

  @Override public void setPlayerStateChangeListener(OnPlayerStateChangeListener listener) {
    this.onPlayerStateChangeListener = listener;
  }

  @Override public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
    this.onVideoSizeChangedListener = listener;
  }

  @Override public void start() throws IllegalStateException {
    setPlayWhenReady(true);
  }

  @Override public void pause() {
    setPlayWhenReady(false);
  }

  public void stop() {
    player.stop();
  }

  public void release() {
    rendererBuilder.cancel();
    rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    surface = null;
    player.release();
  }

  @Override public void reset() {
    release();
  }

  public int getPlaybackState() {
    if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILDING) {
      return EXO_STATE_PREPARING;
    }
    int playerState = player.getPlaybackState();
    if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILT
        && playerState == EXO_STATE_IDLE) {
      // This is an edge case where the renderers are built, but are still being passed to the
      // player's playback thread.
      return EXO_STATE_PREPARING;
    }
    return playerState;
  }

  @Override public Format getFormat() {
    return videoFormat;
  }

  @Override public BandwidthMeter getBandwidthMeter() {
    return bandwidthMeter;
  }

  @Override public CodecCounters getCodecCounters() {
    return codecCounters;
  }

  @Override public long getCurrentPosition() {
    return player.getCurrentPosition();
  }

  @Override public long getDuration() {
    return player.getDuration();
  }

  @Override public int getBufferedPercentage() {
    return player.getBufferedPercentage();
  }

  public boolean getPlayWhenReady() {
    return player.getPlayWhenReady();
  }

  /* package */ Looper getPlaybackLooper() {
    return player.getPlaybackLooper();
  }

  /* package */ Handler getMainHandler() {
    return mainHandler;
  }

  @Override public void onPlayerStateChanged(boolean playWhenReady, int state) {
    maybeReportPlayerState();
  }

  @Override public void onPlayerError(ExoPlaybackException exception) {
    rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    for (Listener listener : listeners) {
      listener.onError(exception);
    }

    if (this.onPlayerStateChangeListener != null) {
      this.onPlayerStateChangeListener.onPlayerError(this, new PlaybackException(exception, 0, 0));
    }
  }

  @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
      float pixelWidthHeightRatio) {
    this.videoWidth = width;
    this.videoHeight = height;

    for (Listener listener : listeners) {
      listener.onVideoSizeChanged(width, height, unAppliedRotationDegrees, pixelWidthHeightRatio);
    }

    if (this.onVideoSizeChangedListener != null) {
      this.onVideoSizeChangedListener.onVideoSizeChanged(this, width, height);
    }
  }

  @Override public void onDroppedFrames(int count, long elapsed) {
    if (infoListener != null) {
      infoListener.onDroppedFrames(count, elapsed);
    }
  }

  @Override public void onBandwidthSample(int elapsedMs, long bytes, long bitrateEstimate) {
    if (infoListener != null) {
      infoListener.onBandwidthSample(elapsedMs, bytes, bitrateEstimate);
    }
  }

  @Override public void onDownstreamFormatChanged(int sourceId, Format format, int trigger,
      long mediaTimeMs) {
    if (infoListener == null) {
      return;
    }
    if (sourceId == TYPE_VIDEO) {
      videoFormat = format;
      infoListener.onVideoFormatEnabled(format, trigger, mediaTimeMs);
    } else if (sourceId == TYPE_AUDIO) {
      infoListener.onAudioFormatEnabled(format, trigger, mediaTimeMs);
    }
  }

  @Override public void onDrmKeysLoaded() {
    // Do nothing.
  }

  @Override public void onDrmSessionManagerError(Exception e) {
    if (internalErrorListener != null) {
      internalErrorListener.onDrmSessionManagerError(e);
    }
  }

  @Override public void onDecoderInitializationError(DecoderInitializationException e) {
    if (internalErrorListener != null) {
      internalErrorListener.onDecoderInitializationError(e);
    }
  }

  @Override public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
    if (internalErrorListener != null) {
      internalErrorListener.onAudioTrackInitializationError(e);
    }
  }

  @Override public void onAudioTrackWriteError(AudioTrack.WriteException e) {
    if (internalErrorListener != null) {
      internalErrorListener.onAudioTrackWriteError(e);
    }
  }

  @Override
  public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
    if (internalErrorListener != null) {
      internalErrorListener.onAudioTrackUnderrun(bufferSize, bufferSizeMs, elapsedSinceLastFeedMs);
    }
  }

  @Override public void onCryptoError(CryptoException e) {
    if (internalErrorListener != null) {
      internalErrorListener.onCryptoError(e);
    }
  }

  @Override public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs,
      long initializationDurationMs) {
    if (infoListener != null) {
      infoListener.onDecoderInitialized(decoderName, elapsedRealtimeMs, initializationDurationMs);
    }
  }

  @Override public void onLoadError(int sourceId, IOException e) {
    if (internalErrorListener != null) {
      internalErrorListener.onLoadError(sourceId, e);
    }
  }

  @Override public void onCues(List<Cue> cues) {
    if (captionListener != null && getSelectedTrack(TYPE_TEXT) != TRACK_DISABLED) {
      captionListener.onCues(cues);
    }
  }

  @Override public void onMetadata(List<Id3Frame> id3Frames) {
    if (id3MetadataListener != null && getSelectedTrack(TYPE_METADATA) != TRACK_DISABLED) {
      id3MetadataListener.onId3Metadata(id3Frames);
    }
  }

  @Override public void onAvailableRangeChanged(int sourceId, TimeRange availableRange) {
    if (infoListener != null) {
      infoListener.onAvailableRangeChanged(sourceId, availableRange);
    }
  }

  @Override public void onPlayWhenReadyCommitted() {
    // Do nothing.
  }

  @Override public void onDrawnToSurface(Surface surface) {
    // Do nothing.
  }

  @Override
  public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format,
      long mediaStartTimeMs, long mediaEndTimeMs) {
    if (infoListener != null) {
      infoListener.onLoadStarted(sourceId, length, type, trigger, format, mediaStartTimeMs,
          mediaEndTimeMs);
    }
  }

  @Override
  public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format,
      long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {
    if (infoListener != null) {
      infoListener.onLoadCompleted(sourceId, bytesLoaded, type, trigger, format, mediaStartTimeMs,
          mediaEndTimeMs, elapsedRealtimeMs, loadDurationMs);
    }
  }

  @Override public void onLoadCanceled(int sourceId, long bytesLoaded) {
    // Do nothing.
  }

  @Override
  public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {
    // Do nothing.
  }

  private boolean mediaPrepared = false;

  private void maybeReportPlayerState() {
    boolean playWhenReady = player.getPlayWhenReady();
    int playbackState = getPlaybackState();
    if (lastReportedPlayWhenReady != playWhenReady || lastReportedPlaybackState != playbackState) {
      for (Listener listener : listeners) {
        listener.onStateChanged(playWhenReady, playbackState);
      }
      lastReportedPlayWhenReady = playWhenReady;
      lastReportedPlaybackState = playbackState;

      // Other listener
      switch (getPlaybackState()) {
        case EXO_STATE_BUFFERING:
          if (!mediaPrepared) {
            if (this.onPlayerStateChangeListener != null) {
              this.onPlayerStateChangeListener.onPlayerStateChanged(this, lastReportedPlayWhenReady,
                  PLAYER_PREPARED);
            }
          }

          mediaPrepared = true;
          if (this.onPlayerStateChangeListener != null) {
            this.onPlayerStateChangeListener.onPlayerStateChanged(this, lastReportedPlayWhenReady,
                PLAYER_BUFFERING);
          }
          break;
        case EXO_STATE_READY:
          if (this.onPlayerStateChangeListener != null) {
            this.onPlayerStateChangeListener.onPlayerStateChanged(this, lastReportedPlayWhenReady,
                PLAYER_READY);
          }
          break;
        case EXO_STATE_ENDED:
          if (this.onPlayerStateChangeListener != null) {
            this.onPlayerStateChangeListener.onPlayerStateChanged(this, lastReportedPlayWhenReady,
                PLAYER_ENDED);
          }
          this.videoWidth = 0;
          this.videoHeight = 0;
          mediaPrepared = false;
          break;
        case EXO_STATE_PREPARING:
          if (this.onPlayerStateChangeListener != null) {
            this.onPlayerStateChangeListener.onPlayerStateChanged(this, lastReportedPlayWhenReady,
                PLAYER_PREPARING);
          }
          this.videoWidth = 0;
          this.videoHeight = 0;
          mediaPrepared = false;
          break;
        case EXO_STATE_IDLE:
        default:
          if (this.onPlayerStateChangeListener != null) {
            this.onPlayerStateChangeListener.onPlayerStateChanged(this, lastReportedPlayWhenReady,
                PLAYER_IDLE);
          }
          this.videoWidth = 0;
          this.videoHeight = 0;
          mediaPrepared = false;
          break;
      }
    }
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, volume);
  }

  private void pushSurface(boolean blockForSurfacePush) {
    if (videoRenderer == null) {
      return;
    }

    if (blockForSurfacePush) {
      player.blockingSendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE,
          surface);
    } else {
      player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    }
  }

  // private int audioSessionId;

  @Override public void setAudioSessionId(int audioSessionId) {
    // Have no effect
  }

  @Override public int getAudioSessionId() {
    return audioRenderer != null
        ? ((EnhancedMediaCodecAudioTrackRenderer) audioRenderer).getAudioSessionId() : 0;
  }

  @Override public void setOnInfoListener(OnInfoListener listener) {
    // TODO FIXME
  }

  @Override public void setDataSource(Context context, Uri uri, Map<String, String> headers)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    // left blank
  }

  @Override public void setAudioStreamType(int audioStreamType) {
    // left blank
  }

  @Override public void setScreenOnWhilePlaying(boolean screenOnWhilePlaying) {
    // left blank
  }

  ///* package */ void processText(String text) {
  //  if (textListener == null || getSelectedTrack(TYPE_TEXT) == TRACK_DISABLED) {
  //    return;
  //  }
  //  textListener.onText(text);
  //}
}
