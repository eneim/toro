Changelog
===========

3.4.0-alpha1 (2018/02/XX)
--------------

This pre-release bring overall improvement to ExoPlayer extension. The interfaces are kept unchanged (or a little), but the underneath implementation is re-written. Detail is as below:

- New interface ``ExoCreator`` that helps to create a new ``SimpleExoPlayer`` instance as well as to build a ``MediaSource`` from existing Uri. Implementing of this interface should always return a creation result, not from cache. Toro take care of the caching mechanism for ``SimpleExoPlayer`` instances and will call this interface if need. ``DefaultExoCreator`` is the default implementation for this interface.

- ``Playable`` interface defines playback behavior. Implementation of this interface must guarantee to survive config change. ``ExoCreator`` will also create a Playable, using default implementation (See ``ExoCreator#createPlayable(Uri)``).
 
- Some old event listeners (interfaces) are replaced with ``Playable$EventListener``. Client need to update them.


3.3.0 (2018/01/01)
--------------

***This release contains breaking changes, minor migration may be required.***

#### API Updates

- BREAKING CHANGE: Remove ExoPlayer API from Core. This is to remove the dependant of Toro from ExoPlayer. Some libraries may use older version of ExoPlayer. Current implementation of ExoPlayer is released as separated extension for Toro, User should add the following entry to gradle:

```groovy
implementation "im.ene.toro3:toro-ext-exoplayer:${toroVersion}" // same version as Toro
```

- BREAKING CHANGE: ExoPlayer extension is released as separated library (see above). This extension will **always** use latest version of ExoPlayer. For this time's release, it is **2.6.0**. User can refer to its implementation to support different version which doesn't binary compatible (for example r2.4.4).

#### Internal Updates

- Various implementation improvement.

#### Demo App Updates

- YouTube demo application is improved, full-screen player is added. Forkers of this library are required to provide their own application Id for YouTube demo app (see gradle.properties-sample) for the key. 

3.2.0 (2017/11/23)
--------------

***This release will make scrolling with many Videos smoother. Sorry for making you wait!***

#### API Updates

- BREAKING CHANGE: ``ToroPlayer#onContainerScrollStateChange(Container, int)`` has been removed, replaced by ``ToroPlayer#onSettled(Container)``.
- BREAKING CHANGE: ``ToroPlayer#onCompleted(Container, ToroPlayer)`` has been removed, replaced by ``ToroPlayer#onCompleted()``.
- BREAKING CHANGE: ``ToroLayoutManager`` has been removed. Internal implement now doesn't depend on first and last ViewHolder indicator.

#### Internal Updates

- ``ToroUtil#visibleAreaOffset()`` implementation now doesn't require ``ViewParent`` to be a ``Container`` anymore.
- ``Common#allowsToPlay()`` implementation improved.
- ``Container`` and ``PlayerManager`` internal implementation has been significantly improved.

#### Demo App Updates

- Minor coding improvement for Youtube demo.

3.1.1 (2017/10/06)
--------------

#### Internal Updates

- Inline documentation is improved. (I was bad at documenting things, sorry).
- ```ExoPlayer``` version is updated to 2.5.3.

#### Demo App Updates

- Add demo with single player activity, where User can click to a Video to start a dedicated Player for it. Read source code for more information. 

3.1.0 (2017/09/05)
--------------

#### API Updates

- Inline documentation is improved.
- ```ExoPlayer``` version is updated to 2.5.1 (**This version is not binary compatible with 2.4.x**).

#### Bug fixes

- Fix a bug where Container keeps playing while Device Screen is turned off.

3.0.0 (2017/08/02)
--------------

#### API Updates

- Add ```ToroPlayer#onContainerScrollStateChange``` to allow ToroPlayer to hook into Container scroll state change event.
- Add ```ToroPlayerHelper#onContainerScrollStateChange``` which does nothing, so that sub class can used to provide helpful support for ToroPlayer.
- ```ExoPlayer``` version is updated to 2.4.4

#### Demo app update

- Add **ViewPagers in ViewPager** demo, which demonstrates how to handle more complicated use case where user have ViewPager inside Fragment which in turns is inside another ViewPager. (The problem is when a ViewPager is shown to the user).

- Add ```app-youtube``` module to show the use of toro with Youtube video, using Android Youtube Player API. To be able to build this module, user must provide a *youtube api key* to ```gradle.properties``` (see ```gradle.properties-sample``` for the key, replace the dummy key to the actual key, rename this file to ```gradle.properties``` as usual and build).

#### Other update

- **toro** now requires Android Studio 3.0 to develop.

3.0.0 Beta 1 (2017/07/07)
-------------

> 七夕だ、*make a wish*

#### API Updates

- ``VideoView`` support has been removed. An example of using it is added in Sample app.

- ``ToroUtil#visibleAreaOffset(View, View)`` has become ``ToroUtil#visibleAreaOffset(ToroPlayer, ViewParent)``. This allows a more accurate calculation and less error prone usage.

- ``PlayerStateManager`` is deprecated and **removed**. The reason is its implementation is not obvious to user as well as the common way to use this is to have a ``HashMap`` of ``PlaybackInfo``. So I include the implementation into ``Container`` itself.

- ``CacheManager`` has been added, replace the ``PlayerStateManager``. This interface only asks for the Key of a ToroPlayer's cache, which will be easier to implement and understand. This interface also comes with default implementation, but it is highly recommended that users have their own implementation using the real dataset.

- ``PlayerSelector#select(View container, List<ToroPlayer> items)`` has become ``PlayerSelector#select(Container container,
 List<ToroPlayer> items)`` to ensure the type-safety of implementations.
 
- ``Cancellable`` interface has been removed.

- ``ToroPlayerHelper#cancel()`` is now ``ToroPlayerHelper#release()`` as ``Cancellable`` is removed.

- [Bug Fix] ``ToroPlayerHelper`` now has default ``ToroPlayer.EventListener`` to correctly handle the playback's complete event.

- ``ExoPlayerHelper`` and other classes those supports ``ExoPlayer`` library are now re-located into ``exoplayer`` package.

- ``MediaSourceBuilder`` class is added, provides the easy way to build up ``ExoPlayer``'s ``MediaSource``. Along with this, ``ExoPlayerHelper#prepare()`` methods will now accept ``MediaSourceBuilder`` and optional ``BandwidthMeter`` instead of current ``Uri`` or ``MediaSource``. This change will increases the flexibility for the users of this library.

- Add ``DrmMediaProvider`` interface by which ``ExoPlayerHelper`` can know if application is using a Drm media or not.

- [Bug Fix] ``ExoPlayerHelper`` will now correctly update resume position: it will not use Player's data if the Player is in IDLE state.

- All method that add listener/callback will require a non-null parameter.

- [Bug Fix] ``SimpleExoPlayerViewHelper`` will now initialize at most once.

- ``Container``'s internal implementation updates:
  - No longer need null check for ``PlayerManager`` instance.
  - ``Container#getActivePlayers()`` is removed, there is ``Container#filterBy(Filter)`` which is more powerful. ``Filter`` is a new interface that will check a ``ToroPlayer`` for some condition (Same as Java 8``Predicate`` interface).
  - It will throw an NPE at runtime if ``ToroPlayer#getPlayerView()`` returns null.
  - [Bug Fix] ``setAdapter`` and ``swapAdapter`` are now correctly using result from super class for the old Adapter before setting up the new one.
  - ``Container#savePlaybackInfo(int, PlaybackInfo), Container#getPlaybackInfo(int), Container#getSavedPlayerOrders()`` are newly added to replace the old ``PlayerStateManager``. Those methods above are public.
  - ``CacheManager`` setter/getter are added.

- (Internal API) ``Common#allowsToPlay(View videoView, Container parent)`` is now less complicated and (hopefully) more comprehensive and correct.

#### Sample app updates

- Sample app is totally revised, in which each feature is implemented in its own package, without sharing any component with other, even the layout file. This allow users to be able to use it as-it, as well as confidently modify the code without harming other feature's implementation.

- There are **7 + 1** demos at the moment, carefully implemented:
  
  - **Custom LayoutManager**: this is the entry point of Sample app, also shows the use of Toro using custom LayoutManager. 
  Clicking to **OPEN DEMOS** will reveal more demonstrations as below. This demo also showcases the use of ``LoopingMediaSource`` using a custom ``ToroPlayerHelper`` and ``LoopingMediaSourceBuilder``.
  
  - **Basic**: using Toro with least effort. This shows how simple it is to get start with Toro. Also this sample does not support playback info save/restore.
  
  - **Facebook Timeline**: this demo uses Toro to mimic the behaviour of Facebook's timeline, with ``click to open playlist``, ``rotate to open current Video in full-screen``. It also surpass the default behavior by correctly handling the config changes and supporting Multi-Windows mode. This demo also showcases a custom use of ``ToroPlayer.EventListener`` to allow the playlist to automatically scroll to next player after completing the playback.
  
  - **Nested Container**: this demo shows how to use a ``Container`` inside other ``Container``. The real use case of this is the suggested Video list for a specific Video (like Youtube).
  
  - **Complicated Grid (Complex Grid)**: this demo shows how to use Toro to control number of simultaneous players at one time. Again this is a power consumed feature and not recommended.
  
  - **Flexible Grid**: this is the same as Complex Grid demo above, with the addition of ``drag/drop`` feature. This represents the Toro's ability of supporting data changes.
  
  - **Legacy**: this demo shows how to use Toro with Android's default VideoView. In this package, there is a custom VideoView adding the ability to listen to ``play/pause`` event which is required in some cases.
  
  - **ViewPager**: last but not least, this demo shows how to use Toro with many ``Container``s that are hosted by a ViewPager (each page is a ``Fragment`` having a ``Container``). The ``Activity`` lauched by clicking **OPEN DEMOS** is where the ``ViewPager`` is. So instead of clicking to button in first page to open a specific demo, swiping over the page will also reveal those demos. Note that: some of the demos will not appear in ``ViewPager`` demo due to technicle issue.

3.0.0 alpha 2 (2017/06/30)
-------------

#### API Updates

- Drop support to ```VideoView``` (via ```ToroVideoView``` and ```LegacyVideoViewHelper```). User can find a demo of using VideoView in demo app, **legacy** package.

- Move ```PlayerSelector``` from ```im.ene.toro.widget``` to ```im.ene.toro```.

- Method name change: ```ToroPlayerHelper#updatePlaybackInfo``` becomes ```ToroPlayerHelper#getLatestPlaybackInfo```.

- All initialize method now requires a Nullable PlaybackInfo (was Nonnull).

#### Sample app updates

- Add demo for ```VideoView```.

#### Others

- Minor coding improvement and documentation improvement.

---

3.0.0 alpha 1 (2017/06/24)
-------------

#### Toro 3.0 is a completely rewritten version of Toro. I borrow just a few helper from 2.x.

To integrate Toro in your App, just replace the ```RecyclerView``` to ```Container``` in place of Video list.

```xml
<im.ene.toro.widget.Container
  android:id="@+id/recycler_view"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  app:max_player_number="1"
/>
```

Next: implement ```ToroPlayer``` to the ```ViewHolder``` that will be a Media player.

```java
public class SimpleExoPlayerViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {

  static final int LAYOUT_RES = R.layout.vh_exoplayer_basic;

  @Nullable SimpleExoPlayerViewHelper helper;
  @Nullable private Uri mediaUri;

  @BindView(R.id.player) SimpleExoPlayerView playerView;

  SimpleExoPlayerViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  @Override
  public void bind(@NonNull RecyclerView.Adapter adapter, Uri item, List<Object> payloads) {
    if (item != null) {
      mediaUri = item;
    }
  }

  @NonNull @Override public View getPlayerView() {
    return playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    PlaybackInfo state = new PlaybackInfo();
    if (helper != null) state = helper.getLatestPlaybackInfo();
    return state;
  }

  @Override
  public void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo) {
    if (helper == null) {
      helper = new SimpleExoPlayerViewHelper(container, this, mediaUri);
      helper.setEventListener(eventListener);
    }
    helper.initialize(playbackInfo);
  }

  @Override public void release() {
    if (helper != null) {
      try {
        helper.cancel();
      } catch (Exception e) {
        e.printStackTrace();
      }
      helper = null;
    }
  }

  @Override public void play() {
    if (helper != null) helper.play();
  }

  @Override public void pause() {
    if (helper != null) helper.pause();
  }

  @Override public boolean isPlaying() {
    return helper != null && helper.isPlaying();
  }

  @Override public boolean wantsToPlay() {
    ViewParent parent = itemView.getParent();
    float offset = 0;
    if (parent != null && parent instanceof View) {
      offset = ToroUtil.visibleAreaOffset(playerView, (View) parent);
    }
    return offset >= 0.85;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }
}
```

You are ready to have Toro 3.0 support. More advance usage are being updated.
