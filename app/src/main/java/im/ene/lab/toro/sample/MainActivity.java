package im.ene.lab.toro.sample;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import im.ene.lab.toro.Toro;
import im.ene.lab.toro.sample.adapter.MyVideoListAdapter;

public class MainActivity extends AppCompatActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final String PREF_PERMISSION_GRANTED = "stream.sample.read_permission.granted";

  private static final int REQUEST_PERMISSION_READ = 1;

  private RecyclerView mVideoList;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Toro.attach(this);
    setContentView(R.layout.activity_main);
    mVideoList = (RecyclerView) findViewById(R.id.video_list);
    PreferenceManager.getDefaultSharedPreferences(this)
        .registerOnSharedPreferenceChangeListener(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!PreferenceManager.getDefaultSharedPreferences(this)
          .getBoolean(PREF_PERMISSION_GRANTED, false)) {
        requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"},
            REQUEST_PERMISSION_READ);
      }
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                   int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_PERMISSION_READ) {
      PreferenceManager.getDefaultSharedPreferences(this)
          .edit()
          .putBoolean(PREF_PERMISSION_GRANTED, grantResults[0] == PackageManager.PERMISSION_GRANTED)
          .apply();
    }
  }

  @Override protected void onResume() {
    super.onResume();
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    mVideoList.setLayoutManager(layoutManager);
    mVideoList.addItemDecoration(
        new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    if (PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean(PREF_PERMISSION_GRANTED, false)
        || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      MyVideoListAdapter adapter = new MyVideoListAdapter(this);
      mVideoList.setAdapter(adapter);
    }

    Toro.register(mVideoList);
  }

  @Override protected void onPause() {
    super.onPause();
    Toro.unregister(mVideoList);
  }

  @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (PREF_PERMISSION_GRANTED.equals(key)) {
      Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
      // Don't do this in real life
      onResume();
    }
  }

  @Override protected void onDestroy() {
    Toro.detach(this);
    PreferenceManager.getDefaultSharedPreferences(this)
        .unregisterOnSharedPreferenceChangeListener(this);
    super.onDestroy();
  }
}
