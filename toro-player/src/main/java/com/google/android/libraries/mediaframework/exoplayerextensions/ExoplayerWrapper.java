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

/**
 * This file has been taken from the ExoPlayer demo project with minor modifications.
 * https://github.com/google/ExoPlayer/
 */
package com.google.android.libraries.mediaframework.exoplayerextensions;

import android.media.MediaCodec.CryptoException;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import com.google.android.exoplayer.CodecCounters;
import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer.MetadataRenderer;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.TextRenderer;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;

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
public class ExoplayerWrapper implements ExoPlayer.Listener, ChunkSampleSource.EventListener,
    DefaultBandwidthMeter.EventListener, MediaCodecVideoTrackRenderer.EventListener,
    MediaCodecAudioTrackRenderer.EventListener, TextRenderer,
    StreamingDrmSessionManager.EventListener, DashChunkSource.EventListener,
        HlsSampleSource.EventListener, MetadataRenderer<List<Id3Frame>> {

  /**
   * Builds renderers for the player.
   */
  public interface RendererBuilder {
    /**
     * Constructs the necessary components for playback.
     *
     * @param player The parent player.
     */
    void buildRenderers(ExoplayerWrapper player);

    /**
     * Cancels the current build operation, if there is one. Else does nothing.
     */
    void cancel();
  }

  /**
   * A listener for basic playback events.
   */
  public interface PlaybackListener {
    void onStateChanged(boolean playWhenReady, int playbackState);
    void onError(Exception e);
    void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
        float pixelWidthHeightRatio);
  }

  /**
   * A listener for internal errors.
   * <p>
   * These errors are not visible to the user, and hence this listener is provided for
   * informational purposes only. Note however that an internal error may cause a fatal
   * error if the player fails to recover. If this happens,
   * {@link PlaybackListener#onError(Exception)} will be invoked.
   *
   * <p>
   * Implementing an {@link InternalErrorListener} is a good way to identify why ExoPlayer may be
   * behaving in an undesired way.
   */
  public interface InternalErrorListener {

    /**
     * Respond to error in renderer initialization.
     * @param e The error.
     */
    void onRendererInitializationError(Exception e);

    /**
     * Respond to error in initializing the audio track.
     * @param e The error.
     */
    void onAudioTrackInitializationError(AudioTrack.InitializationException e);

    /**
     * Respond to error when writing the audio track.
     * @param e The error.
     */
    void onAudioTrackWriteError(AudioTrack.WriteException e);

    /**
     * Respond to error when running the audio track.
     * @param bufferSize The buffer size.
     * @param bufferSizeMs The buffer size in Ms.
     * @param elapsedSinceLastFeedMs The time elapsed since last feed in Ms.
     */
    void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs);

    /**
     * Respond to error in initializing the decoder.
     * @param e The error.
     */
    void onDecoderInitializationError(DecoderInitializationException e);

    /**
     * Respond to error in setting up security of video.
     * @param e The error.
     */
    void onCryptoError(CryptoException e);

    /**
     * Respond to error that occurs at the source of the video.
     * @param sourceId The id of the source of the video.
     * @param e The error.
     */
    void onUpstreamError(int sourceId, IOException e);

    /**
     * Respond to error when consuming video data from a source.
     * @param sourceId The id of the source of the video.
     * @param e The error.
     */
    void onConsumptionError(int sourceId, IOException e);

    /**
     * Respond to error in DRM setup.
     * @param e The error.
     */
    void onDrmSessionManagerError(Exception e);

    void onLoadError(int sourceId, IOException e);
  }

  /**
   * A listener for debugging information.
   */
  public interface InfoListener {

    /**
     * Respond to a change in the format of the video.
     * @param format The new format of the video.
     * @param trigger The reason for a chunk being selected.
     * @param mediaTimeMs The start time of the media contained by the chunk, in microseconds.
     */
    void onVideoFormatEnabled(Format format, int trigger, long mediaTimeMs);

    /**
     * Respond to a change in the audio format.
     * @param format The new format of the audio.
     * @param trigger The reason for a chunk being selected.
     * @param mediaTimeMs The start time of the media contained by the chunk, in microseconds.
     */
    void onAudioFormatEnabled(Format format, int trigger, long mediaTimeMs);

    /**
     * Respond to frame drops.
     * @param count The number of dropped frames.
     * @param elapsed The number of milliseconds in which the frames were dropped.
     */
    void onDroppedFrames(int count, long elapsed);

    /**
     * Respond to a new estimate of the bandwidth.
     * @param elapsedMs The duration of the sampling period in milliseconds.
     * @param bytes The number of bytes received during the sampling period.
     * @param bandwidthEstimate The estimated bandwidth in bytes/sec, or
     *                          {@link DefaultBandwidthMeter
     *                          #NO_ESTIMATE} if no estimate is available. Note that this estimate
     *                          is typically derived from more information than {@code bytes} and
     *                          {@code elapsedMs}.
     */
    void onBandwidthSample(int elapsedMs, long bytes, long bandwidthEstimate);

    /**
     * Respond to starting a load of data.
     * @param sourceId The id of the source of the video.
     * @param length The length of the audio.
     * @param type The type of the audio
     * @param trigger The reason for a chunk being selected.
     * @param format The format of the source.
     * @param mediaStartTimeMs The time point of the media where we start loading.
     * @param mediaEndTimeMs The time point of the media where we end loading.
     */
    void onLoadStarted(int sourceId, long length, int type, int trigger, Format format,
        long mediaStartTimeMs, long mediaEndTimeMs);

    /**
     * Respond to a successful load of data.
     * @param sourceId The id of the source of the video.
     */
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
   * A listener for receiving notifications of timed text.
   */
  public interface TextListener {

    /**
     * Respond to text arriving (ex subtitles, captions).
     * @param text The received text.
     */
    public abstract void onText(String text);
  }

  /**
   * A listener for receiving ID3 metadata parsed from the media stream.
   */
  public interface Id3MetadataListener {
    void onId3Metadata(List<Id3Frame> metadata);
  }

  /**
   * Exoplayer renderers are managed in an array (the array representation is used throughout the
   * Exoplayer library).
   *
   * <p>There are RENDERER_COUNT elements in the array.
   */
  public static final int RENDERER_COUNT = 5;

  /**
   * The element at index TYPE_VIDEO is a video type renderer.
   */
  public static final int TYPE_VIDEO = 0;

  /**
   * The element at index TYPE_AUDIO is an audio type renderer.
   */
  public static final int TYPE_AUDIO = 1;

  /**
   * The element at index TYPE_TEXT is a text type renderer.
   */
  public static final int TYPE_TEXT = 2;
  public static final int TYPE_METADATA = 3;

  public static final int TRACK_DISABLED = ExoPlayer.TRACK_DISABLED;

  /**
   * For id3 metadata.
   */
  public static final int TYPE_TIMED_METADATA = 3;

  /**
   * The element at index TYPE_DEBUG is a debug type renderer.
   */
  public static final int TYPE_DEBUG = 4;

  /**
   * This variable must be an int, not part of an enum because it has significance within the
   * Exoplayer library.
   */
  private static final int RENDERER_BUILDING_STATE_IDLE = 1;

  /**
   * This variable must be an int, not part of an enum because it has significance within the
   * Exoplayer library.
   */
  private static final int RENDERER_BUILDING_STATE_BUILDING = 2;

  /**
   * This variable must be an int, not part of an enum because it has significance within the
   * Exoplayer library.
   */
  private static final int RENDERER_BUILDING_STATE_BUILT = 3;

  /**
   * This variable must be an int, not part of an enum because it has significance within the
   * Exoplayer library.
   */
  public static final int DISABLED_TRACK = -1;

  /**
   * This variable must be an int, not part of an enum because it has significance within the
   * Exoplayer library.
   */
  public static final int PRIMARY_TRACK = 0;

  /**
   * Responsible for loading the data from the source, processing it, and providing byte streams.
   * By modifying the renderer builder, we can support different video formats like DASH, MP4, and
   * SmoothStreaming.
   */
  private final RendererBuilder rendererBuilder;

  /**
   * The underlying Exoplayer instance responsible for playing the video.
   */
  private final ExoPlayer player;

  /**
   * Used to control the playback (ex play, pause, get duration, get elapsed time, seek to time).
   */
  private final ObservablePlayerControl playerControl;

  /**
   * Used by track renderers to send messages to the event listeners within this class.
   */
  private final Handler mainHandler;

  /**
   * Listeners are notified when the video size changes, when the underlying player's state changes,
   * or when an error occurs.
   */
  private final CopyOnWriteArrayList<PlaybackListener> playbackListeners;

  /**
   * States are idle, building, or built.
   */
  private int rendererBuildingState;

  /**
   * States are idle, prepared, buffering, ready, or ended. This is an integer (instead of an enum)
   * because the Exoplayer library uses integers.
   */
  private int lastReportedPlaybackState;

  /**
   * Whether the player was in a playWhenReady state the last time we checked.
   */
  private boolean lastReportedPlayWhenReady;

  /**
   * The surface on which the video is rendered.
   */
  private Surface surface;

  /**
   * Renders the video data.
   */
  private TrackRenderer videoRenderer;

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

  /**
   * A list of enabled or disabled tracks to render.
   */
  private int[] selectedTracks;

  /**
   * The state of a track at a given index (one of the TYPE_* constants).
   */
  private int[] trackStateForType;

  /**
   * Respond to text (ex subtitle or closed captioning) events.
   */
  private TextListener textListener;
  private CaptionListener captionListener;
  private Id3MetadataListener id3MetadataListener;

  /**
   * Respond to errors that occur in Exoplayer.
   */
  private InternalErrorListener internalErrorListener;

  /**
   * Respond to changes in media format changes, load events, bandwidth estimates,
   * and dropped frames.
   */
  private InfoListener infoListener;

  /**
   * @param rendererBuilder Responsible for loading the data from the source, processing it,
   *                        and providing byte streams. By modifying the renderer builder, we can
   *                        support different video formats like DASH, MP4, and SmoothStreaming.
   */
  public ExoplayerWrapper(RendererBuilder rendererBuilder) {
    this.rendererBuilder = rendererBuilder;
    player = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
    player.addListener(this);
    playerControl = new ObservablePlayerControl(player);
    mainHandler = new Handler();
    playbackListeners = new CopyOnWriteArrayList<PlaybackListener>();
    lastReportedPlaybackState = ExoPlayer.STATE_IDLE;
    rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    trackStateForType = new int[RENDERER_COUNT];
    // Disable text initially.
    trackStateForType[TYPE_TEXT] = DISABLED_TRACK;
    player.setSelectedTrack(TYPE_TEXT, TRACK_DISABLED);
  }

  /**
   * Returns the player control which can be used to play, pause, seek, get elapsed time, and get
   * elapsed duration.
   */
  public ObservablePlayerControl getPlayerControl() {
    return playerControl;
  }

  /**
   * Add a listener to respond to size change and error events.
   *
   * @param playbackListener
   */
  public void addListener(PlaybackListener playbackListener) {
    playbackListeners.add(playbackListener);
  }

  /**
   * Remove a listener from notifications about size changes and errors.
   *
   * @param playbackListener
   */
  public void removeListener(PlaybackListener playbackListener) {
    playbackListeners.remove(playbackListener);
  }

  /**
   * Set a listener to respond to errors within Exoplayer.
   * @param listener The listener which responds to the error events.
   */
  public void setInternalErrorListener(InternalErrorListener listener) {
    internalErrorListener = listener;
  }

  /**
   * Set a listener to respond to media format changes, bandwidth samples, load events, and dropped
   * frames.
   * @param listener Listens to media format changes, bandwidth samples, load events, and dropped
   *                 frames.
   */
  public void setInfoListener(InfoListener listener) {
    infoListener = listener;
  }

  public void setCaptionListener(CaptionListener listener) {
    captionListener = listener;
  }

  /**
   * Set the listener which responds to incoming text (ex subtitles or captions).
   *
   * @param listener The listener which can respond to text like subtitles and captions.
   */
  public void setTextListener(TextListener listener) {
    textListener = listener;
  }

  public void setMetadataListener(Id3MetadataListener listener) {
    id3MetadataListener = listener;
  }

  public void setSurface(Surface surface) {
    this.surface = surface;
    pushSurfaceAndVideoTrack(false);
  }

  /**
   * Returns the surface on which the video is rendered.
   */
  public Surface getSurface() {
    return surface;
  }

  /**
   * Clear the video surface.
   *
   * <p>In order to clear the surface, a message must be sent to the playback thread. To guarantee
   * that this message is delivered, Exoplayer uses a blocking operation. Therefore, this method is
   * blocking.
   */
  public void blockingClearSurface() {
    surface = null;
    pushSurfaceAndVideoTrack(true);
  }

  /**
   * Returns the name of the track at the given index.
   * @param type The index indicating the type of video (ex {@link #TYPE_VIDEO})
   */
  public String[] getTracks(int type) {
    return trackNames == null ? null : trackNames[type];
  }

  /**
   * Returns whether the track is {@link #PRIMARY_TRACK} or {@link #DISABLED_TRACK).
   * @param type The index indicating the type of video (ex {@link #TYPE_VIDEO}).
   */
  public int getStateForTrackType(int type) {
    return trackStateForType[type];
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

  /**
   * Build the renderers.
   */
  public void prepare() {
    if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILT) {
      player.stop();
    }
    rendererBuilder.cancel();
    videoFormat = null;
    videoRenderer = null;
    rendererBuildingState = RENDERER_BUILDING_STATE_BUILDING;
    maybeReportPlayerState();
    rendererBuilder.buildRenderers(this);
  }

  /**
   * Invoked with the results from a {@link RendererBuilder}.
   *
   * @param renderers Renderers indexed by {@link ExoplayerWrapper} TYPE_* constants. An
   *                  individual element may be null if there do not exist tracks of the
   *                  corresponding type.
   * @param bandwidthMeter Provides an estimate of the currently available bandwidth. May be null.
   */
  public void onRenderers(TrackRenderer[] renderers,  BandwidthMeter bandwidthMeter) {
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
    this.codecCounters = videoRenderer instanceof MediaCodecTrackRenderer
            ? ((MediaCodecTrackRenderer) videoRenderer).codecCounters
            : renderers[TYPE_AUDIO] instanceof MediaCodecTrackRenderer
            ? ((MediaCodecTrackRenderer) renderers[TYPE_AUDIO]).codecCounters : null;
    rendererBuildingState = RENDERER_BUILDING_STATE_BUILT;
    this.bandwidthMeter = bandwidthMeter;
    maybeReportPlayerState();
    pushSurfaceAndVideoTrack(false);
    player.prepare(renderers);
  }

  /**
   * Notify the listeners when an exception is thrown.
   * @param e The exception that has been thrown.
   */
  public void onRenderersError(Exception e) {
    if (internalErrorListener != null) {
      internalErrorListener.onRendererInitializationError(e);
    }
    for (PlaybackListener playbackListener : playbackListeners) {
      playbackListener.onError(e);
    }
    rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    maybeReportPlayerState();
  }

  /**
   * Set whether the player should begin as soon as it is setup.
   * @param playWhenReady If true, playback will start as soon as the player is setup. If false, it
   *                      must be started programmatically.
   */
  public void setPlayWhenReady(boolean playWhenReady) {
    player.setPlayWhenReady(playWhenReady);
  }

  /**
   * Move the seek head to the given position.
   * @param positionMs A number of milliseconds after the start of the video.
   */
  public void seekTo(int positionMs) {
    player.seekTo(positionMs);
  }

  /**
   * When you are finished using this object, make sure to call this method.
   */
  public void release() {
    rendererBuilder.cancel();
    rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    surface = null;
    player.release();
  }

  /**
   * Returns the state of the Exoplayer instance.
   */
  public int getPlaybackState() {
    if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILDING) {
      return ExoPlayer.STATE_PREPARING;
    }
    int playerState = player.getPlaybackState();
    if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILT
        && rendererBuildingState == RENDERER_BUILDING_STATE_IDLE) {
      // This is an edge case where the renderers are built, but are still being passed to the
      // player's playback thread.
      return ExoPlayer.STATE_PREPARING;
    }
    return playerState;
  }

  public Format getFormat() {
    return videoFormat;
  }

  public BandwidthMeter getBandwidthMeter() {
    return bandwidthMeter;
  }

  public CodecCounters getCodecCounters() {
    return codecCounters;
  }

  /**
   * Returns the position of the seek head in the number of
   * milliseconds after the start of the video.
   */
  public long getCurrentPosition() {
    return player.getCurrentPosition();
  }

  /**
   * Returns the duration of the video in milliseconds.
   */
  public long getDuration() {
    return player.getDuration();
  }

  /**
   * Returns the number of the milliseconds of the video that has been buffered.
   */
  public int getBufferedPercentage() {
    return player.getBufferedPercentage();
  }

  /**
   * Returns true if the video is set to start as soon as it is set up, returns false otherwise.
   */
  public boolean getPlayWhenReady() {
    return player.getPlayWhenReady();
  }

  /**
   * Return the looper of the Exoplayer instance which sits and waits for messages.
   */
  Looper getPlaybackLooper() {
    return player.getPlaybackLooper();
  }

  /**
   * Returns the handler which responds to messages.
   */
  Handler getMainHandler() {
    return mainHandler;
  }

  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int state) {
    maybeReportPlayerState();
  }

  @Override
  public void onPlayerError(ExoPlaybackException exception) {
    rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    for (PlaybackListener playbackListener : playbackListeners) {
      playbackListener.onError(exception);
    }
  }

  @Override
  public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                 float pixelWidthHeightRatio) {
    for (PlaybackListener listener : playbackListeners) {
      listener.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
    }
  }

  @Override
  public void onDroppedFrames(int count, long elapsed) {
    if (infoListener != null) {
      infoListener.onDroppedFrames(count, elapsed);
    }
  }

  @Override
  public void onBandwidthSample(int elapsedMs, long bytes, long bandwidthEstimate) {
    if (infoListener != null) {
      infoListener.onBandwidthSample(elapsedMs, bytes, bandwidthEstimate);
    }
  }

  @Override
  public void onDownstreamFormatChanged(int sourceId, Format format, int trigger,
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
                              long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs,
                              long loadDurationMs) {
    if (infoListener != null) {
      infoListener.onLoadCompleted(sourceId, bytesLoaded, type, trigger, format, mediaStartTimeMs,
              mediaEndTimeMs, elapsedRealtimeMs, loadDurationMs);
    }
  }

  @Override
  public void onLoadCanceled(int sourceId, long bytesLoaded) {
    // Do nothing.
  }

  @Override
  public void onDrmSessionManagerError(Exception e) {
    if (internalErrorListener != null) {
      internalErrorListener.onDrmSessionManagerError(e);
    }
  }

  @Override
  public void onDecoderInitializationError(DecoderInitializationException e) {
    if (internalErrorListener != null) {
      internalErrorListener.onDecoderInitializationError(e);
    }
  }

  @Override
  public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
    if (internalErrorListener != null) {
      internalErrorListener.onAudioTrackInitializationError(e);
    }
  }

  @Override
  public void onAudioTrackWriteError(AudioTrack.WriteException e) {
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

  @Override
  public void onCryptoError(CryptoException e) {
    if (internalErrorListener != null) {
      internalErrorListener.onCryptoError(e);
    }
  }

  @Override
  public void onDrmKeysLoaded() {
    // Do nothing.
  }

  /* package */ MetadataRenderer<List<Id3Frame>>
  getId3MetadataRenderer() {
    return new MetadataRenderer<List<Id3Frame>>() {
      @Override
      public void onMetadata(List<Id3Frame> metadata) {
        if (id3MetadataListener != null) {
          id3MetadataListener.onId3Metadata(metadata);
        }
      }
    };
  }

  @Override
  public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs,
                                   long initializationDurationMs) {
    if (infoListener != null) {
      infoListener.onDecoderInitialized(decoderName, elapsedRealtimeMs, initializationDurationMs);
    }
  }

  @Override
  public void onLoadError(int sourceId, IOException e) {
    if (internalErrorListener != null) {
      internalErrorListener.onLoadError(sourceId, e);
    }
  }

  @Override
  public void onCues(List<Cue> cues) {
    if (captionListener != null && getSelectedTrack(TYPE_TEXT) != TRACK_DISABLED) {
      captionListener.onCues(cues);
    }
  }

  @Override
  public void onMetadata(List<Id3Frame> metadata) {
    if (id3MetadataListener != null && getSelectedTrack(TYPE_METADATA) != TRACK_DISABLED) {
      id3MetadataListener.onId3Metadata(metadata);
    }
  }

  @Override
  public void onAvailableRangeChanged(int sourceId, TimeRange availableRange) {
    if (infoListener != null) {
      infoListener.onAvailableRangeChanged(sourceId, availableRange);
    }
  }

  @Override
  public void onPlayWhenReadyCommitted() {
    // Do nothing.
  }

  @Override
  public void onDrawnToSurface(Surface surface) {
    // Do nothing.
  }

  @Override
  public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {
    // Do nothing.
  }

  /**
   * If either playback state or the play when ready values have changed, notify all the playback
   * listeners.
   */
  private void maybeReportPlayerState() {
    boolean playWhenReady = player.getPlayWhenReady();
    int playbackState = getPlaybackState();
    if (lastReportedPlayWhenReady != playWhenReady || lastReportedPlaybackState != playbackState) {
      for (PlaybackListener playbackListener : playbackListeners) {
        playbackListener.onStateChanged(playWhenReady, playbackState);
      }
      lastReportedPlayWhenReady = playWhenReady;
      lastReportedPlaybackState = playbackState;
    }
  }

  /**
   * Updated the playback thread with the latest video renderer and surface.
   * @param blockForSurfacePush If true, then message sent to the underlying playback thread is
   *                            guaranteed to be delivered. However, this is a blocking operation
   */
  private void pushSurfaceAndVideoTrack(boolean blockForSurfacePush) {
    if (rendererBuildingState != RENDERER_BUILDING_STATE_BUILT) {
      return;
    }

    if (blockForSurfacePush) {
      player.blockingSendMessage(
          videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    } else {
      player.sendMessage(
          videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    }
  }

  /* package */ void processText(String text) {
    if (textListener == null || selectedTracks[TYPE_TEXT] == DISABLED_TRACK) {
      return;
    }
    textListener.onText(text);
  }


}