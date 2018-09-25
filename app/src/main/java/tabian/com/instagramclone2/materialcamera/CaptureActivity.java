package tabian.com.instagramclone2.materialcamera;

import android.app.Fragment;
import android.support.annotation.NonNull;

import tabian.com.instagramclone2.materialcamera.internal.BaseCaptureActivity;
import tabian.com.instagramclone2.materialcamera.internal.CameraFragment;

public class CaptureActivity extends BaseCaptureActivity {

  @Override
  @NonNull
  public Fragment getFragment() {
    return CameraFragment.newInstance();
  }
}
