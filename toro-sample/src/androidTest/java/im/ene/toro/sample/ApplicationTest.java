package im.ene.toro.sample;

import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import im.ene.toro.Toro;
import im.ene.toro.sample.feature.basic1.Basic1Activity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class) //
public class ApplicationTest {

  Application application;

  @Rule public ActivityTestRule<Basic1Activity> basic1ActivityActivityTestRule =
      new ActivityTestRule<>(Basic1Activity.class);

  @Before public void setUp() {
    application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
    Toro.init(application);
  }

  @Test public void useAppContext() throws Exception {
    // Context of the app under test.
    Context appContext = InstrumentationRegistry.getTargetContext();
    assertEquals(BuildConfig.APPLICATION_ID, appContext.getPackageName());
  }

  @Test public void testBasicSample1() throws Exception {
    basic1ActivityActivityTestRule.getActivity();
  }
}