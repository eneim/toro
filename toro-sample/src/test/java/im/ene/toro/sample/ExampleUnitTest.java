package im.ene.toro.sample;

import android.app.Application;
import android.view.View;
import com.google.android.exoplayer2.ExoPlayer;
import im.ene.toro.Toro;
import im.ene.toro.exoplayer2.ExoPlayerViewHelper;
import im.ene.toro.exoplayer2.ExoVideoViewHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

  @Mock Application application;

  @Mock ExoVideoViewHolder mockViewHolder;
  @Mock View itemView;

  private ExoPlayerViewHelper helper;

  @Before public void setUp() {
    // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
    // inject the mocks in the test the initMocks method needs to be called.
    MockitoAnnotations.initMocks(this);
    Toro.init(application);
    helper = new ExoPlayerViewHelper(mockViewHolder, itemView);
  }

  @Test public void testPlayerHelperStateBufferTrue() throws Exception {
    Assert.assertNotNull(helper);

    helper.onPlayerStateChanged(true, ExoPlayer.STATE_BUFFERING);
    verify(mockViewHolder, never()).onVideoPrepared();
  }

  @Test public void testPlayerHelperStateBufferFalse() throws Exception {
    Assert.assertNotNull(helper);

    helper.onPlayerStateChanged(false, ExoPlayer.STATE_BUFFERING);
    verify(mockViewHolder).onVideoPrepared();
  }
}