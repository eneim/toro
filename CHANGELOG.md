# Release note

### 2.1.0 (2016/10/26)

**Months of works, Toro has been re-designed from ground up**

- **New** package structure:
  - Before: ```im.ene.lab.toro.*``` --> ***After***: ```im.ene.toro.*```.

- **New** dependency repositories and ids:
  - Before:
    - Repository: ```maven { url "https://jitpack.io" }```
    
    - Dependency: ```"com.github.eneim:Toro:${toro_latest_version}"```
  
  - After:
    - Repository: ```jcenter() # or do nothing since it is Android Studio's default.```
    
    - Dependency: ```"im.ene.toro2:toro:${toro_latest_version}"```
 
- **New** library structures:
  - Before: Toro is All In One solution, robust, compact but somehow inflexible and imposible to extend.
  
  - After: Toro is now lightweight, extensible:
    - Toro core library provides all the concepts and base implementation.

    - The extensions those are based on Media playback libraries will do the real Application level work (imlementing the VideoHolder, Video Player Widget, Player helpers, ...).

- **New** extensions based on ExoPlayer 2, Toro is now catching up with latest developer's interest.
 
 - ExoPlayer v2 is supported as an Toro's Extension. All the basic work is done.
 
 - Fallback to legacy
  
   - ExoPlayer v1 and Official Android MediaPlayer API is supported as 2 other Extensions.

- **New** extension: 'Toro Extended'
 - People asked many times for the looping ability of Video Widget, I put it to even more flexible concept : ExtToroPlayer interface will ask for the next target to grab. Here you can define that target as current Player itself (i.e LOOP), or next player to play (Visible Video in the same Window will be detected and scrolled to, trigger the next playback). More 'traversable' behaviours will come in the future. 
 
- **New** rich features sample App
 - IMO, sample App is as important as the library. Toro comes with a rich feature App, wrap the usecases from simple to advance, including Custom LayoutManager samples as well as a well-taylored sample which reproduce the behaviour of **Facebook timeline**.
 
- **New**: Custom LayoutManager
 - Toro 2 keeps trying best to support RecyclerView. Now with an extra interface for LayoutManager, custom LayoutManager can be integrated easily by implementing ```ToroLayoutManager``` (Toro 1 supports only Linear/Staggered Layout Managers).

- Nougat - ready
 - Carefully deal with Activity's lifecycle, support the change on Android 7.
 - Resizeable Activity is all set.
 
- Many more features to explore, but still easy to use.

- Q&A, Help:
  
  - Backward Compatible to v1? - ***No***
  
  - Migration Helper? - ***No Official Guide***, it is too much. But since ***(1)*** v2 has completely new package structure, you can include both v1 and v2 into your project, and change the current implementation partly. ***(2)*** Question related to Migration can be post in issue with the tag "Migration v1 - v2", I will be there for help.

  - Why v2.1.0, where is v2.0.0? - ***Sorry***, due to some internal changes at the time of releasing, and some mental issue (mostly my last decision of the package name and bintray repo relocation), I decided to move directly to 2.1.0.
  
### 2.0.0 (2016/10/16: Unreleased)

**Due to some big internal changes as well as my mental changes, I decided to skip version 2.0 and move directly to 2.1.0**

### 2.0.0-RC4 (2016.06.23)

**Toro has been re-designed from ground up**

- **Backed by ExoPlayer, fallback to legacy API**: Toro v2 changes Media playback API to [ExoPlayer](https://github.com/google/ExoPlayer), which is more powerful and flexible API. 
Meanwhile, I keep a small effort to support legacy MediaPlayer API for older Application. 
Read ExoPlayer documentation for more information about its limitation and requirement.

- **Separated modules, more flexibility**: Toro v2 comes with 4 sub modules which can be used as independent libraries: 

```toro-media``` - base definition of higher API layers;

```toro-player``` - core implementation of Playback API. Heavily adapted from ExoPlayer's demo and well tailored for normal usage;

```toro``` - the Toro as you know, but has been simplified to contain only most important implementation;

```toro-ext``` - extension of Toro, contains higher level of APIs, more specific implementation of Toro's components, provides extra support for Youtube and other mainstream playback APIs.
 
 By separating those module, and clarify the depending relationship, I hope Toro v2 could give you more power to custom your current components to match Toro mindset. 
 You don't have to follow the old way of extending Toro's built-in widgets anymore, but they are still their for you, more friendly.
  
- **Goodbye jitpack, hello jcenter**: I was a fan of jitpack.io for quite a long time. In fact, most of my current libraries are published on jitpack. But since Toro v2 has been splitted to many sub modules, jitpack seems to not very well support.
Furthermore, I tend to update my libraries' dependencies to latest official version really quick, meanwhile jitpack server is not that adaptive. So I move Toro to jcenter which is always there with Android Studio.

To include Toro's modules into your application, new dependencies are as follow:

```groovy
// As usual
repositories {
    jcenter()
    mavenCentral()  // Toro is available on Maven too, optional beside jcenter.
}
```

<a href='https://bintray.com/eneim/maven/Toro/_latestVersion'><img src='https://api.bintray.com/packages/eneim/maven/Toro/images/download.svg'></a>

```groovy
ext {
  toro_version = '<hint: check the badge number>'  // one version for all modules
}

dependencies {
  compile "im.ene.lab:toro-media:${toro_version}"
  
  compile "im.ene.lab:toro-player:${toro_version}"
  
  compile "im.ene.lab:toro:${toro_version}"
  
  compile "im.ene.lab:toro-ext:${toro_version}"
}

```

There is new dependency relationship among Toro's sub modules which you might want to know:
```toro-player``` depends on ```toro-media```, ```toro``` depends on ```toro-media```, ```toro-ext``` depends on both ```toro``` and ```toro-player```,

Therefore, you don't always need to add all modules to your gradle's dependencies, just add what you want to use.

- **More**: about new implementation mechanism and technique, as well as practice - COMING SOON.

- **Migration from 1.x**: COMING SOON.

### 1.2.0 (2016.04.06)

Breaking changes. Clients should take care of this release.

- **[CHANGE]** Better error handling, see [this PR](https://github.com/eneim/Toro/pull/26/files). ```ToroPlayer#onPlaybackError(...)``` will now require a ***boolean response***.
- **[CHANGE]** **[NEW FEATURE]** Support ***loop playback***. Current playing **ToroPlayer** could automatically repeat **immediately** after it completes latest playback. Loop playback is ***disable*** by default. Setup by overriding ```ToroPlayer#isLoopAble()``` and return ***true***. Sample usage:

```java
public class MyLoopAblePlayerViewHolder extends ToroViewHolder {

  // Do stuff
  
  // Support loop on this ViewHolder
  @Override public boolean isLoopAble() {
    return true;
    // You may want to control it better like following implementation
    // return getAdapterPosition() % 2 == 0;
  }
}
```
- **[NEW FEATURE]** Ability to temporary pause all playing players by new **ToroStrategy**: ***REST*** (*internal API, user has no direct access to this*). This Strategy will disable all player. This is useful for the cases when Client would like to open a Dialog (Eg: A big floating playback window) on top of current window, then it wants to pause the player until that Dialog dismisses. Usage (see sample App for more information):

```java
Toro.rest(true);  // Tell Toro you want to take a rest
// Do stuff which requires the player to be silent.
Toro.rest(false); // Done stuff, resume the playback.
```
Please note that using this feature should be careful. It's really simple implementation, for really simple usecase. 2 calls should always be coupled. So please don't screw up your App.

- **[NEW FEATURE]** Custom LayoutManager: Client can now add their own LayoutManager. The requirement is that LayoutManager must implement **ToroLayoutManager** interface to help Toro knows how to travel throw the Children. *Sample is coming soon*.

- **[INTERNAL CHANGE]** Custom ViewHolder with RecyclerViewItemHelper: RecyclerViewItemHelper is now public and non-final. This allows client to create their own ViewHolder just by implementing **ToroPlayer** interface, and still get support from Toro's core. Just use/extend RecyclerViewItemHelper wisely. *Sample is coming soon*.

- Better UI for sample App.

- **Many internal improvement**.

- **MIGRATING FROM 1.1.0 TO 1.2.0**: fix/re-code all your ```ToroPlayer#onPlaybackError```. If you have any problem, don't hesitate to create new issue or even notify my via [Gitter](https://gitter.im/eneim/Toro).

### 1.1.0 (2016.02.09)

**Happy Vietnam New Lunar Year!**

This release contains some changes from internal API as well as code improvement:

- Narrow down visibility of some classes and object. Clients should test carefully for side-affect. So sorry for this inconvenient. Normally you would not be affect, unless you have some custom components.
  - VideoPlayerManagerImpl is now package private. Don't use it else where. Instead, implement to your Adapter.
  - ToroScrollListener is now package private. Don't use it else where. 

- Built-in strategies is strengthen by more accurate decision on ```#allowsToPlay()``` method.

- Some other code improvement.

### 1.0.0 (2016.02.06)

- First stable release
