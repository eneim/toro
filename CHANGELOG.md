Changelog
===========

3.5.0 (2018/05/18)
------------------

**TL,DR:** From **Toro 3.5.0**, client can actively prepare a ``PlaybackInfo`` for ``ToroPlayer`` by its order, using the newly added ``Container$Initializer`` interface.

Custom initial PlaybackInfo sample:

```java
container.setPlayerInitializer(order -> {
  VolumeInfo volumeInfo = new VolumeInfo(true, 0.75f); // mute by default, but will be 0.75 when un-mute
  return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
});
```

**Toro** is also updated with improved PlaybackInfo in-memory caching mechanism, giving client better performance and correctness.

**DETAILS**

- Toro is now developed using Android Studio 3.2
- ExoPlayer extension now depends on ExoPlayer 2.7.3. (2.8.0 is a breaking change and will be support in next major release).

- ToroPlayer:
  - ``ToroPlayer#initialize(Container, PlaybackInfo)`` now has non-null PlaybackInfo (was null-able).
  - ``ToroPlayerHelper#initialize(Container, PlaybackInfo)`` now has non-null PlaybackInfo (was null-able).
  - ``ToroPlayerHelper#initialize(PlaybackInfo)`` now has non-null PlaybackInfo (was null-able).

- PlaybackInfo:
  - **NEW**: ``PlaybackInfo`` has been updated with ``VolumeInfo`` as a field.
  - **NEW**: Add ``PlaybackInfo#SCRAP`` object, which is a default, trivial static instance of PlaybackInfo. This instance should be used only to mark a player as un-initialized.

- Container:
  - **NEW**: Add ``Container#getLatestPlaybackInfos()`` as a utility method so that client can get current playback info of all possible players at any time.
  - **NEW**: Add ``Container$Initializer`` interface. This interface, when set, will request client to initialize the ``PlaybackInfo`` for a ``ToroPlayer`` by its order, via ``Initializer#initPlaybackInfo(int)``. Default implementation return a trivial ``PlaybackInfo``.

- ExoPlayer extension:
  - ``Playable$EventListeners`` now extends a ``HashSet`` instead of ``ArrayList``, to guarantee the uniqueness of listener to be added by its hash value.

- Add ``RemoveIn`` annotation so that user of **Toro** would know when a deprecated item will be removed.
- Some internal methods are changed to final, other improvements and also deprecated class(es) are removed.

3.4.2 (2018/04/11)
------------------

> Toro 3.4.2 and above will be developed by Android Studio 3.1.0+, and maybe kotlin as well.

- **Development**
  - Migrate the repo to Android Studio 3.1.1.
  - Remove ``gradle.properties-sample`` and some internal change so contributor can start forking and contributing more easily.
  - **Toro** is now distributed to SNAPSHOT channel as well for early builds. This enables user to try latest updates with ease. Detail can be found on README.

- **toro-core**
  - Add a mechanism to support the case ``Container`` is used in ``CoordinatorLayout`` with other Views, using ``Behavior``. Detail can be found on README.
  - Add ``ToroUtil#wrapParamBehavior()`` to help shorten the setup of new ``Behavior`` above.
  - Add ``VolumeInfo`` for a tailored volume setup. It holds the 'mute' status as well as the actual volume value when the playback is unmuted.
  - Add ``ToroPlayer$OnVolumeChangeListener`` that listens to the change of internal ``VolumeInfo``. Instance of this interface is setup by ``ToroPlayerHelper`` and its variants.
  - ``Container`` will no longer start a delayed playback if the scroll is not idled.

- **toro-exoplayer**
  - ``Config`` now accepts an array of ``DrmSessionManager``s instead of a single ``DrmSessionManager``. This is an experiment.
  - Add ``Playable#setParameters()`` and ``Playable#getParameters`` to match ExoPlayer behaviour.
  - Add ``ToroExoPlayer`` which extends ``SimpleExoPlayer`` and provides the ability to work with ``VolumeInfo``.
  - ``Playable`` and default implementations are updated to work with ``VolumeInfo``.

- **toro-mopub**
  - Add custom UI components: ``PlayerView`` and ``ToroControlView`` that combines ExoPlayer r2.4.4 implementation with some fixes from ExoPlayer 2.7.0.
  - Add ``Playable#setParameters()`` and ``Playable#getParameters`` to match ExoPlayer behaviour.
  - Add ``ToroExoPlayer`` which extends ``SimpleExoPlayer`` and provides the ability to work with ``VolumeInfo``.
  - ``Playable`` and default implementations are updated to work with ``VolumeInfo``.
  - Demo app is updated using new UI components as well as ``VolumeInfo``.

- **Others**: minor performance improvement update.

- Detail implementation and suggested usage can be found on demo applications.

3.4.1 (2018/03/06)
--------------

- Fix a critical bug in 3.4.0, which cause the NPE when initializing the playables.
- **toro-exoplayer**: Some methods of ``ExoCreator`` and ``MediaSourceBuilder`` now requires the Uri extension as option, to deal with the case that actual Url doesn't end with file extension. Client should pass expected media type so that ExoPlayer can build suitable MediaSource instances. As a result, ``ExoPlayerViewHelper`` adds addition constructor to support optional media type extension. 

<h3>[Important notice, 2018/03/05] Due to some internal bugs, 3.4.0 has been pulled down from Jcenter. Please wait for 3.4.1 with the bug fixes. At the mean time, please stay at 3.3.0 or 3.4.0-A4. Sorry for the inconvenience.</h3>

3.4.0 (2018/03/04)
--------------

> 3.4.0 is the biggest release since 3.0.0. In short, many issues are fixed, many internal improvement and many new ways to start your new playback. This changes also focus on ExoPlayer so that using ExoPlayer with Toro is easier than ever before.
 
- **toro-core**
  - Add ``PlayerDispatcher``: this interface intercepts the call to ``ToroPlayer#play()``, and tells the library 'how long should this call be delayed'. It is useful when client need some delay before the playback starts. ``PlayerDispatcher`` works with ``PlayerManager`` and can be setup using ``Container#setPlayerDispatcher(PlayerDispatcher)``. Defaul ``PlayerDispatcher`` dispatch the call immediately.
  - Improve ``PlayerSelector``: the ``ToroPlayer`` list in parameter of ``PlayerSelector#select()`` is always sorted, while the order is unclear to the caller. This release adds the annotation **Sorted** telling the caller if a list is sorted by which order (either ``ASCENDING`` or ``DESCENDING``).
  - ``ToroPlayer``: ``ToroPlayer#onSettled(Container)`` is deprecated and will be removed in next major release (3.5.0).
  - ``ToroPlayerHelper``: instance of ``Container`` is no longer required when constructing an instance of this class. This brings flexibility and usability to the subclass of this helper. Also, it removes the concern about sharing instance of this helper across different Containers. Along with this update, ``initialize(PlaybackInfo)`` is deprecated and replaced by ``initialize(Container container, PlaybackInfo)``. Sub class still need to override ``initialize(PlaybackInfo)`` for backward compatibility, but it is recommended to use/call the new one in practice.
  - ``ToroPlayerHelper``: default behavior when the ``ToroPlayer`` state changes to play or pause is changed. When ``ToroPlayer``'s state is playing, the player View will keep the screen on, and when the state is pause, player View will not keep the screen on.
  - This release adds ``setVolume(float)`` and ``getVolume`` to the helper as well. Sub classes must provide a volume control for this change.
  - ``DrmMedia`` adds method ``multiSession()`` that returns a boolean telling if the Drm playback should supports multi sessions or not. This adapts the change from ExoPlayer, but not depend on it.

- **toro-exoplayer** (Toro Extension for ExoPlayer 2)
  - Has been vastly rewritten. Building new ``SimpleExoPlayer`` instance is now easier and more flexible than before. And the instance will be cached for re-use, which shows significant performance improvement. Client now has the simpler and safer way to integrate ExoPlayer into ToroPlayer, and also the ability to use ExoPlayer separately to ToroPlayer's API with the same flexibility.  
  - This release comes with some new helper classes, including ``ToroExo``, ``ExoCreator``, ``Config``, ``Playable``, ``ExoPlayable``, ``MediaSourceBuilder``.
  - ``ExoCreator``: this is the main character, playing the roles that create new ``SimpleExoPlayer`` instances, ``MediaSource`` instances and also ``Playable`` instance.
  - ``Config``: this class defines the necessary setup for a ``SimpleExoPlayer`` as well as ``MediaSource``. Its instance can be built using Builder, which gives client the ease of use and the flexibility to reuse the Config instance (By calling ``Config#newBuilder()``, client can simply clone the Config and update it with different options). Also, it is recommended to have global instance of Config for each client, and if it requires different configs, the number of config should be kept as small as possible.
  - ``Playable``: similar to ``ToroPlayer`` in that it defines core playback control such as play/pause and volume update. But this interface is built for ExoPlayer components, and deeply integrates into its API. Instance of this interface can be used either in a subclass of ``ToroPlayerHelper`` to support a ``ToroPlayer``, or individually as a single playback controller. ``Playable`` is designed to be resuable, even across configuration change, so that client can keept the playback smoothly in those changes.
  - ``ExoPlayable``: an implementation of ``Playable`` where client can extend it for customization with flexibility.
  - ``MediaSourceBuilder``: a single interface tells client `how should a MediaSource` should be created, and ask for the implementation. Providing the ``Config`` by an instance of this interface is enough for the rest of your app.
  - ``ToroExo``: the manager for this new update. It hides the complexity of implementations of ``ExoCreator`` or ``Playable``, gives client necessary but powerful entry points to request for those instances. Also, initiating this class is just as simple as calling ``ToroExo.with(context)`` you may see else where. ``ToroExo.with(Context)``, ``ToroExo#requestPlayer(ExoCreator)``, ``ToroExo#releasePlayer(ExoCreator, SimpleExoCreator)``, ``ToroExo#getCreator(Config)``, ``ToroExo#getDefaultCreator()`` are only methods you need to remember.  
  - ``ExoPlayerViewHelper`` is renamed from ``ExoPlayerHelper`` and also a rewritten version of it. It is the combination between ``ToroPlayerHelper`` and ``ExoPlayable``, brings the best of this release to user of **toro**.
  - ``SimpleExoPlayerViewHelper`` is deprecated, client should use ``ExoPlayerViewHelper`` instead. This class will be removed from next major release (3.5.0).

- **toro-mopub**
  - The changes for this extension is almost the same with the **toro-exoplayer**, while its responsibility is to keep the compatibility with ExoPlayer r2.4.4, which is non-trivial.
  - The addition interfaces are the same, but their signature are slightly different. Client is recommended to check out the source to take a look. But to use them, it is just as simple as calling the same methods from ``ToroExo`` with the same set of options as in ``toro-exoplayer``.
  - ``PlayerView`` is added. It is a clone of ``SimpleExoPlayerView`` where there are some addition improvement brought from ExoPlayer 2.7.0.
  - ``ToroControlView`` is added, with a custom UI where user can also change the Volume. This options is not available in official ExoPlayer Widgets, which is the reason why I create this View.
  - The old ``MediaSourceBuilder`` is renamed to ``MediaSourceCreator`` and also be created in favor to the new setup for ExoPlayer.
  - The new ``MediaSourceBuild`` class is added to work with new building system for ExoPlayer.
  - Helpers for ``ToroPlayer`` are now: ``PlayerViewHelper`` for new custom ``PlayerView`` and ``ExoPlayerViewHelper`` for ``SimpleExoPlayerView``. 

- Demo
  - Add **demo-exoplayer** that contains simple Activities to show how to use the new ``ToroExo`` with all other new classes, either with ``Container`` or as single Media player.
  - Add **demo-mopub** to show how to use **toro-mopub** extension effectively.

- Others
  - **toro-exoplayer** now uses ExoPlayer 2.7.0 as dependency.
  - **app** the demo app is also updated with latest ExoPlayer version and latest improvement from **toro-exoplayer**.

3.4.0-alpha3 (2018/02/24)
--------------

- **toro-core**
  - Add ``PlayerDispatcher`` to provide more flexible playback. In detail, it can delay the call to ToroPlayer#play() where the delay time is configurable by client. Default behavior is no delay.

- **toro-exoplayer**
  - Now compile with ExoPlayer version 2.7.0. There are breaking changes in that library, so clients of Toro are expected to migrate them. The migration is trivial.

- All demo apps are now compile with ExoPlayer version 2.7.0.
  

3.4.0-alpha2 (2018/02/19)
--------------

> This release improve alpha 1, regarding usability of Playable instance, minor (but important) implementation improvement that in turns improve client UX and performance, and many other tiny tuning.

- Changes for ``Playable``:
  - ``attacheView(SimpleExoPlayerView)`` and ``detachView()`` are replaced by one method ``setPlayerView(SimpleExoPlayerView)``, makes more sense.
  - ``Playable.EventListener`` now extends ``MetadataOutput`` as well. Client should update the implementation.
  - ``PlayableImpl`` has been improve so that: ``MediaSource`` will be prepared only after client calls ``play()`` for the first time. **This is to prevent Device to download the media even if the playback is not requested.** 
  
- Changes for ``ToroExo``:
  - ``ToroExo#cleanUp()`` is added so that client can aggressively/actively cleanup all currently cached SimpleExoPlayer instance. This is useful when the Application is running out of memory. Note that only instances those are staying in the Pool are released, ones are in used is not affected by this, which is expected.
  - ``ToroExo#requestPlayer(ExoCreator)`` is added, so that client can effectively request for SimpleExoPlayer instance. Instead of calling ``ExoCreator#createPlayer()`` in alpha1, which will not mind the cache, this method will only create new instance of there is no cache in the Pool.
  - ``ToroExo#releasePlayer(ExoCreator, SimpleExoPlayer)`` is added, on-par with the method above. Client can use this method to release an SimpleExoPlayer instance back to the Pool (which must be mapped to the ExoCreator).
- Other changes:
  - ``DefaultExoCreator`` is changed to public so that client can extend it for custom usage.
  - ``ExoPlayerViewHelper`` constructor signature's items order has been change.
 
- Update to ``demo-exoplayer``: PlayableDemoActivity.kt has been updated with the logic to reuse ``Playable`` instance across config change. This shows how to keep the playback smooth across config change (eg: Window size change, Orientation change, etc). This update take the use of ``onRetainCustomNonConfigurationInstance`` which may or may not good practice, so User to this library should consider this as a "Proof of Concept" kind of thing, and get the concept of how it *can work*. I actively work on this to figure out a good way for production level stability.
  - How is the behavior: the Activity can start normally in either orientation. If it starts in landscape mode or multi windows mode whose horizontal edge is longer than vertical edge, it will be a single full-screen player. If it starts in portrait mode or multi windows mode whose horizontal edge is shorter than vertical edge, it will be a list of content and the Player view stays on top of the LinearLayout (inside a NestedScrollView, etc).
  - When you change the config by either change the orientation or enter multi windows mode, the Activity do necessary cleanup, save the Playable instance via ``onRetainCustomNonConfigurationInstance`` and so on. When the Activity is recreated, retrieve the Playable and update its Player view to the new one. This way, the Audio playback is kept smoothly while Video playback is disturbed by the Config change only.
  - Note that this implementation involves 'Switching the Surface' for SimpleExoPlayer, which in turns the MediaCodec and so on. This work is known to be troublesome in low-spec/low-level Devices. It should work best on API 23+. So making it widely usable in production is a long way ahead. Just you have been warned.

3.4.0-alpha1 (2018/02/12)
--------------

This pre-release bring overall improvement to ExoPlayer extension. The interfaces are kept unchanged (or a little), but the underneath implementation is re-written. Detail is as below:

- New interface ``ExoCreator`` that helps to create a new ``SimpleExoPlayer`` instance as well as to build a ``MediaSource`` from existing Uri. Implementing of this interface should always return a creation result, not from cache. Toro take care of the caching mechanism for ``SimpleExoPlayer`` instances and will call this interface if need. ``DefaultExoCreator`` is the default implementation for this interface.

- ``Playable`` interface defines playback behavior. Implementation of this interface must guarantee to survive config change. ``ExoCreator`` will also create a Playable, using default implementation (See ``ExoCreator#createPlayable(Uri)``).

- ``Config`` class defines the configuration for a ``ExoCreator``. One config should be kept statically for one Application. One App can have one or more Configs. ``ToroExo`` holds a default config that fits most of the cases so you don't have to create anything by default. Build up a new Config by using its ``Config$Builder`` class, or by using ``Config#newBuilder()`` to create a Builder from current Config.

- ``ToroExo`` is the global manager of this update. It helps manager the cache for SimpleExoPlayer, ExoCreator, etc. Obtaining its (singleton) by calling ``ToroExo.with(context)`` and from there, Client has the access into its functionality.

- Some old event listeners (interfaces) are replaced with ``Playable$EventListener``. Client need to update them.

- ``SimpleExoPlayerViewHelper`` is kept but its internal implementation has changed using the new mechanism. This class's existence is to maintain backward compatibility of current Clients using older library. In fact, Client still need to migrate to use the new ``EventListener``, but the change should be a little. Changing to use ``ExoPlayerViewHelper`` is recommended.

- New demo app dedicated for ExoPlayer extension is added: ``demo-exoplayer``. Currently, there are 3 demos:
  - ``BasicListActivity`` shows normal usage of **Toro** with new implementation of ``ExoPlayerViewHelper``. Written in Kotlin.
  - ``CreatorDemoActivity`` shows non-Toro use case, where Client use ``ExoCreator`` API to build ``SimpleExoPlayer`` and ``MediaSource``. Written in Java.
  - ``PlayableDemoActivity`` shows non-Toro use case, where Client use ``ExoCreator`` API to build ``Playable``. Written in Kotlin.

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
