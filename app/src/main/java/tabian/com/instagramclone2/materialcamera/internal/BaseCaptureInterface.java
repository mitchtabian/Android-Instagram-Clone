package tabian.com.instagramclone2.materialcamera.internal;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

import tabian.com.instagramclone2.materialcamera.internal.*;

/** @author Aidan Follestad (afollestad) */
public interface BaseCaptureInterface {

  void onRetry(@Nullable String outputUri);

  void onShowPreview(@Nullable String outputUri, boolean countdownIsAtZero);

  void onShowStillshot(String outputUri);

  void setRecordingStart(long start);

  void setRecordingEnd(long end);

  long getRecordingStart();

  long getRecordingEnd();

  boolean hasLengthLimit();

  boolean countdownImmediately();

  long getLengthLimit();

  void setCameraPosition(int position);

  void toggleCameraPosition();

  Object getCurrentCameraId();

  @tabian.com.instagramclone2.materialcamera.internal.BaseCaptureActivity.CameraPosition
  int getCurrentCameraPosition();

  void setFrontCamera(Object id);

  void setBackCamera(Object id);

  Object getFrontCamera();

  Object getBackCamera();

  void useMedia(String uri);

  void addToStory(String uri);

  boolean shouldAutoSubmit();

  boolean allowRetry();

  void setDidRecord(boolean didRecord);

  boolean didRecord();

  boolean restartTimerOnRetry();

  boolean continueTimerInPlayback();

  int videoEncodingBitRate(int defaultVal);

  int audioEncodingBitRate(int defaultVal);

  int videoFrameRate(int defaultVal);

  int videoPreferredHeight();

  float videoPreferredAspect();

  long maxAllowedFileSize();

  int qualityProfile();

  @DrawableRes
  int iconRecord();

  @DrawableRes
  int iconStop();

  @DrawableRes
  int iconFrontCamera();

  @DrawableRes
  int iconRearCamera();

  @DrawableRes
  int iconPlay();

  @DrawableRes
  int iconPause();

  @DrawableRes
  int iconRestart();

  @StringRes
  int labelRetry();

  @Deprecated
  @StringRes
  int labelUseVideo();

  @StringRes
  int labelConfirm();

  @DrawableRes
  int iconStillshot();

  /** @return true if we only want to take photographs instead of video capture */
  boolean useStillshot();

//  void setUseStillshot(boolean bool);

  void toggleFlashMode();

  void toggleFlashModeVideo();


  @tabian.com.instagramclone2.materialcamera.internal.BaseCaptureActivity.FlashMode
  int getFlashMode();

  @tabian.com.instagramclone2.materialcamera.internal.BaseCaptureActivity.FlashMode
  int getFlashModeVideo();

  @DrawableRes
  int iconFlashAuto();

  @DrawableRes
  int iconFlashOn();

  @DrawableRes
  int iconFlashOff();

  void setFlashModes(List<Integer> modes);

  boolean shouldHideFlash();

  long autoRecordDelay();

  boolean audioDisabled();

  boolean shouldHideCameraFacing();
}
