# Release note

### 2.0.0-RC3 (2016.06.23)

**Toro has been re-designed from ground up**

- **Backed by ExoPlayer, fallback to legacy API**: Toro v2 changes Media playback API to [ExoPlayer](https://github.com/google/ExoPlayer), which is more powerful and flexible API. 
Meanwhile, I keep a small effort to support legacy MediaPlayer API for older Application. 
Read ExoPlayer documentation for more information about its limitation and requirement.

- **Separated modules, more flexibility**: Toro v2 comes with 4 sub modules which can be used as independent libraries: ```toro-media``` - base definition of higher API layers;
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

ext {
  toro-version = '2.0.0-RC3'  // one version for all modules
}

dependencies {
  compile "im.ene.lab:toro-media:${toro-version}"
  
  compile "im.ene.lab:toro-player:${toro-version}"
  
  compile "im.ene.lab:toro:${toro-version}"
  
  compile "im.ene.lab:toro-ext:${toro-version}"
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
