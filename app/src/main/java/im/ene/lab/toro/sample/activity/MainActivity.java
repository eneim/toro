package im.ene.lab.toro.sample.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import im.ene.lab.toro.sample.fragment.SingleVideoListFragment;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
    if (fragment == null) {
      fragment = SingleVideoListFragment.newInstance();
      getSupportFragmentManager().beginTransaction()
          .replace(android.R.id.content, fragment)
          .commit();
    }
  }
}
