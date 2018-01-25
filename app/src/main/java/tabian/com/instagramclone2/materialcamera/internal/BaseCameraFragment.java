package tabian.com.instagramclone2.materialcamera.internal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;


import tabian.com.instagramclone2.R;
import tabian.com.instagramclone2.materialcamera.MaterialCamera;
import tabian.com.instagramclone2.materialcamera.util.CameraUtil;
import tabian.com.instagramclone2.materialcamera.util.Degrees;

import static android.app.Activity.RESULT_CANCELED;
import static tabian.com.instagramclone2.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_BACK;
import static tabian.com.instagramclone2.materialcamera.internal.BaseCaptureActivity.FLASH_MODE_ALWAYS_ON;
import static tabian.com.instagramclone2.materialcamera.internal.BaseCaptureActivity.FLASH_MODE_AUTO;
import static tabian.com.instagramclone2.materialcamera.internal.BaseCaptureActivity.FLASH_MODE_OFF;


/** @author Aidan Follestad (afollestad) */
abstract class BaseCameraFragment extends Fragment
    implements CameraUriInterface, View.OnClickListener {

  private static final String TAG = "BaseCameraFragment";
  private static final int RESULT_START_CAMERA = 4567;
  private static final int RESULT_START_VIDEO = 4589;

  protected ImageButton mButtonVideo;
  protected ImageButton mButtonStillshot;
  protected ImageButton mButtonFacing;
  protected ImageButton mButtonFlash;
  protected ImageButton mButtonFlashVideo;
  protected TextView mRecordDuration;
//  protected TextView mDelayStartCountdown;
  protected Switch mVideoSwitch;

  private boolean mIsRecording;
  protected String mOutputUri;
  protected BaseCaptureInterface mInterface;
  protected Handler mPositionHandler;
  protected Handler mVideoPlayingHandler;
  protected Runnable mVideoPlayingRunnable;
  protected MediaRecorder mMediaRecorder;
  private int mIconTextColor;
  private int mIconTextColorDark;
  private int mRecordButtonColor;

  protected static void LOG(Object context, String message) {
    Log.d(
        context instanceof Class<?>
            ? ((Class<?>) context).getSimpleName()
            : context.getClass().getSimpleName(),
        message);
  }

  private final Runnable mPositionUpdater =
      new Runnable() {
        @Override
        public void run() {
          if (mInterface == null || mRecordDuration == null) return;
          final long mRecordStart = mInterface.getRecordingStart();
          final long mRecordEnd = mInterface.getRecordingEnd();
          if (mRecordStart == -1 && mRecordEnd == -1) return;
          final long now = System.currentTimeMillis();
          if (mRecordEnd != -1) {
            if (now >= mRecordEnd) {
              stopRecordingVideo(true);
            } else {
              final long diff = mRecordEnd - now;
              mRecordDuration.setText(String.format("-%s", CameraUtil.getDurationString(diff)));
            }
          } else {
            mRecordDuration.setText(CameraUtil.getDurationString(now - mRecordStart));
          }
          if (mPositionHandler != null) mPositionHandler.postDelayed(this, 1000);
        }
      };

  @Override
  public final View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.mcam_fragment_videocapture, container, false);
  }


protected void setImageRes(ImageView iv, @DrawableRes int res) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && iv.getBackground() instanceof RippleDrawable) {
      if(iv == mButtonVideo){
        RippleDrawable rd = (RippleDrawable) iv.getBackground();
        rd.setColor(ColorStateList.valueOf(CameraUtil.adjustAlpha(mRecordButtonColor, 0.3f)));
      }
      else if(iv == mButtonStillshot){
        RippleDrawable rd = (RippleDrawable) iv.getBackground();
        rd.setColor(ColorStateList.valueOf(CameraUtil.adjustAlpha(mIconTextColorDark, 0.3f)));
      }
      else{
        RippleDrawable rd = (RippleDrawable) iv.getBackground();
        rd.setColor(ColorStateList.valueOf(CameraUtil.adjustAlpha(mIconTextColor, 0.3f)));
      }

    }
  if(iv == mButtonVideo){
    Drawable d = AppCompatResources.getDrawable(iv.getContext(), res);
    d = DrawableCompat.wrap(d.mutate());
    DrawableCompat.setTint(d, mRecordButtonColor);
    iv.setImageDrawable(d);
  }
  else if(iv == mButtonStillshot){
    Drawable d = AppCompatResources.getDrawable(iv.getContext(), res);
    d = DrawableCompat.wrap(d.mutate());
    DrawableCompat.setTint(d, mIconTextColorDark);
    iv.setImageDrawable(d);
  }
  else{
    Drawable d = AppCompatResources.getDrawable(iv.getContext(), res);
    d = DrawableCompat.wrap(d.mutate());
    DrawableCompat.setTint(d, mIconTextColor);
    iv.setImageDrawable(d);
  }
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

//    mDelayStartCountdown = (TextView) view.findViewById(R.id.delayStartCountdown);
//    mButtonVideo = (ImageButton) view.findViewById(R.id.video);
    mButtonVideo = (ImageButton) view.findViewById(R.id.videoButton);
    mButtonStillshot = (ImageButton) view.findViewById(R.id.stillshot);
    mRecordDuration = (TextView) view.findViewById(R.id.recordDuration);
    mButtonFacing = (ImageButton) view.findViewById(R.id.facing);
    mVideoSwitch = (Switch) view.findViewById(R.id.video_switch);
    mButtonFlash = (ImageButton) view.findViewById(R.id.flashStillshot);
    mButtonFlashVideo = (ImageButton) view.findViewById(R.id.flashVideo);
    setupFlashMode();

    mButtonVideo.setOnClickListener(this);
    mButtonStillshot.setOnClickListener(this);
    mButtonFacing.setOnClickListener(this);
    mButtonFlash.setOnClickListener(this);
    mButtonFlashVideo.setOnClickListener(this);
    mVideoSwitch.setOnClickListener(this);

    int primaryColor = getArguments().getInt(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.PRIMARY_COLOR);
    if (CameraUtil.isColorDark(primaryColor)) {
      mIconTextColor = ContextCompat.getColor(getActivity(), R.color.mcam_color_light);
      mIconTextColorDark = ContextCompat.getColor(getActivity(), R.color.mcam_color_dark);
      primaryColor = CameraUtil.darkenColor(primaryColor);
    } else {
      mIconTextColor = ContextCompat.getColor(getActivity(), R.color.mcam_color_dark);
      mIconTextColorDark = ContextCompat.getColor(getActivity(), R.color.mcam_color_dark);
    }
    mRecordButtonColor = ContextCompat.getColor(getActivity(), R.color.colordarkRed);
    view.findViewById(R.id.controlsFrame);
//    .setBackgroundColor(primaryColor);
    mRecordDuration.setTextColor(mIconTextColor);

    if (mMediaRecorder != null && mIsRecording) {
      setImageRes(mButtonVideo, mInterface.iconStop());
    } else {
      setImageRes(mButtonVideo, mInterface.iconRecord());
      mInterface.setDidRecord(false);
    }

    if (savedInstanceState != null) mOutputUri = savedInstanceState.getString("output_uri");

    if (mInterface.useStillshot()) {
      mButtonVideo.setVisibility(View.GONE);
      mRecordDuration.setVisibility(View.GONE);
      mButtonStillshot.setVisibility(View.VISIBLE);
      setImageRes(mButtonStillshot, mInterface.iconStillshot());
      mButtonFlash.setVisibility(View.VISIBLE);
      mButtonFlashVideo.setVisibility(View.GONE);
    }
    else{
      mButtonFlash.setVisibility(View.GONE);
      mButtonFlashVideo.setVisibility(View.VISIBLE);
    }

//    if (mInterface.autoRecordDelay() < 1000) {
//      mDelayStartCountdown.setVisibility(View.GONE);
//    } else {
//      mDelayStartCountdown.setText(Long.toString(mInterface.autoRecordDelay() / 1000));
//    }
    setupFlashModeVideo();

    if(mInterface.useStillshot()){
      mVideoSwitch.setChecked(true);
    }

    if (mInterface.shouldHideCameraFacing() || CameraUtil.isChromium()) {
      mButtonFacing.setVisibility(View.GONE);
      if(!mInterface.useStillshot()){
        mButtonFlashVideo.setVisibility(View.GONE);
      }
    } else {
      setImageRes(
              mButtonFacing,
              mInterface.getCurrentCameraPosition() == CAMERA_POSITION_BACK
                      ? mInterface.iconFrontCamera()
                      : mInterface.iconRearCamera());
    }
  }

  protected void onFlashModesLoaded() {
    if (getCurrentCameraPosition() != BaseCaptureActivity.CAMERA_POSITION_FRONT) {
      invalidateFlash(false);
    }
  }

  private boolean mDidAutoRecord = false;
  private Handler mDelayHandler;
  private int mDelayCurrentSecond = -1;

  protected void onCameraOpened() {
    if (mDidAutoRecord
        || mInterface == null
        || mInterface.useStillshot()
        || mInterface.autoRecordDelay() < 0
        || getActivity() == null) {
//      mDelayStartCountdown.setVisibility(View.GONE);
      mDelayHandler = null;
      return;
    }
    mDidAutoRecord = true;
    mButtonFacing.setVisibility(View.GONE);
    mButtonFlashVideo.setVisibility(View.GONE);

    if (mInterface.autoRecordDelay() == 0) {
//      mDelayStartCountdown.setVisibility(View.GONE);
      mIsRecording = startRecordingVideo();
      mDelayHandler = null;
      return;
    }

    mDelayHandler = new Handler();
    mButtonVideo.setEnabled(false);

    if (mInterface.autoRecordDelay() < 1000) {
      // Less than a second delay
//      mDelayStartCountdown.setVisibility(View.GONE);
      mDelayHandler.postDelayed(
          new Runnable() {
            @Override
            public void run() {
              if (!isAdded() || getActivity() == null || mIsRecording) return;
              mButtonVideo.setEnabled(true);
              mIsRecording = startRecordingVideo();
              mDelayHandler = null;
            }
          },
          mInterface.autoRecordDelay());
      return;
    }

//    mDelayStartCountdown.setVisibility(View.VISIBLE);
    mDelayCurrentSecond = (int) mInterface.autoRecordDelay() / 1000;
    mDelayHandler.postDelayed(
        new Runnable() {
          @SuppressLint("SetTextI18n")
          @Override
          public void run() {
            if (!isAdded() || getActivity() == null || mIsRecording) return;
            mDelayCurrentSecond -= 1;
//            mDelayStartCountdown.setText(Integer.toString(mDelayCurrentSecond));

            if (mDelayCurrentSecond == 0) {
//              mDelayStartCountdown.setVisibility(View.GONE);
              mButtonVideo.setEnabled(true);
              mIsRecording = startRecordingVideo();
              mDelayHandler = null;
              return;
            }

            mDelayHandler.postDelayed(this, 1000);
          }
        },
        1000);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mButtonVideo = null;
    mButtonStillshot = null;
    mButtonFacing = null;
    mButtonFlash = null;
    mButtonFlashVideo = null;
    mRecordDuration = null;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (mInterface != null && mInterface.hasLengthLimit()) {
      if (mInterface.countdownImmediately() || mInterface.getRecordingStart() > -1) {
        if (mInterface.getRecordingStart() == -1)
          mInterface.setRecordingStart(System.currentTimeMillis());
        startCounter();
      } else {
        mRecordDuration.setText(
            String.format("-%s", CameraUtil.getDurationString(mInterface.getLengthLimit())));
      }
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public final void onAttach(Activity activity) {
    super.onAttach(activity);
    mInterface = (BaseCaptureInterface) activity;
  }

  @NonNull
  protected final File getOutputMediaFile() {
    return CameraUtil.makeTempFile(
        getActivity(), getArguments().getString(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.SAVE_DIR), "VID_", ".mp4");
  }

  @NonNull
  protected final File getOutputPictureFile() {
    return CameraUtil.makeTempFile(
        getActivity(), getArguments().getString(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.SAVE_DIR), "IMG_", ".jpg");
  }

  public abstract void openCamera();


  public abstract void closeCamera();

  public void cleanup() {
    closeCamera();
    releaseRecorder();
    stopCounter();
  }

  public abstract void takeStillshot();

  public abstract void onPreferencesUpdated();

  @Override
  public void onPause() {
    super.onPause();
    cleanup();
  }

  @Override
  public final void onDetach() {
    super.onDetach();
    mInterface = null;
  }

  public final void startCounter() {
    if (mPositionHandler == null) mPositionHandler = new Handler();
    else mPositionHandler.removeCallbacks(mPositionUpdater);
    mPositionHandler.post(mPositionUpdater);
  }

  @BaseCaptureActivity.CameraPosition
  public final int getCurrentCameraPosition() {
    if (mInterface == null) return BaseCaptureActivity.CAMERA_POSITION_UNKNOWN;
    return mInterface.getCurrentCameraPosition();
  }

  public final int getCurrentCameraId() {
    if (mInterface.getCurrentCameraPosition() == CAMERA_POSITION_BACK)
      return (Integer) mInterface.getBackCamera();
    else return (Integer) mInterface.getFrontCamera();
  }

  public final void stopCounter() {
    if (mPositionHandler != null) {
      mPositionHandler.removeCallbacks(mPositionUpdater);
      mPositionHandler = null;
    }
  }

  public final void releaseRecorder() {
    if (mMediaRecorder != null) {
      if (mIsRecording) {
        try {
          mMediaRecorder.stop();
        } catch (Throwable t) {
          //noinspection ResultOfMethodCallIgnored
          new File(mOutputUri).delete();
          t.printStackTrace();
        }
        mIsRecording = false;
        Log.d(TAG, "releaseRecorder: releasing media player.");
      }
      mMediaRecorder.reset();
      mMediaRecorder.release();
      mMediaRecorder = null;
    }
  }

  public boolean startRecordingVideo() {
    if (mInterface != null && mInterface.hasLengthLimit() && !mInterface.countdownImmediately()) {
      // Countdown wasn't started in onResume, start it now
      if (mInterface.getRecordingStart() == -1)
        mInterface.setRecordingStart(System.currentTimeMillis());
      startCounter();
      Log.d(TAG, "startRecordingVideo: starting recording session.");

    }

    final int orientation = Degrees.getActivityOrientation(getActivity());
    //noinspection ResourceType
    Log.d(TAG, "startRecordingVideo: setting orientation: " + orientation);
    getActivity().setRequestedOrientation(orientation);
    mInterface.setDidRecord(true);

//    if(mVideoPlayingHandler != null){
//      mVideoPlayingHandler.removeCallbacks(mVideoPlayingRunnable);
//    }
//    mVideoPlayingHandler = new Handler();
//    mVideoPlayingRunnable = new Runnable() {
//      @Override
//      public void run() {
//        mVideoPlayingHandler.postDelayed(mVideoPlayingRunnable, 100);
//        if(mIsRecording){
//          Log.d(TAG, "recording: is recording: " + mIsRecording);
//        }
//      }
//    };
//    mVideoPlayingRunnable.run();

    return true;
  }

  public void stopRecordingVideo(boolean reachedZero) {
    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    Log.d(TAG, "stopRecordingVideo: ending recording session.");

  }


  @Override
  public final void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("output_uri", mOutputUri);
  }

  @Override
  public final String getOutputUri() {
    return mOutputUri;
  }

  protected final void throwError(Exception e) {
    Activity act = getActivity();
    if (act != null) {
      act.setResult(RESULT_CANCELED, new Intent().putExtra(MaterialCamera.ERROR_EXTRA, e));
      act.finish();
    }
  }

  @Override
  public void onClick(View view) {
    final int id = view.getId();
    if (id == R.id.facing) {
      mInterface.toggleCameraPosition();
      setImageRes(
          mButtonFacing,
          mInterface.getCurrentCameraPosition() == CAMERA_POSITION_BACK
              ? mInterface.iconFrontCamera()
              : mInterface.iconRearCamera());
      closeCamera();
      openCamera();
      setupFlashMode();
      setupFlashModeVideo();
    }
//    else if (id == R.id.video) {
      else if (id == R.id.videoButton) {
      if (mIsRecording) {
        stopRecordingVideo(false);
        mIsRecording = false;
      } else {
        if (getArguments().getBoolean(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.SHOW_PORTRAIT_WARNING, true)
            && Degrees.isPortrait(getActivity())) {
          new MaterialDialog.Builder(getActivity())
              .title(R.string.mcam_portrait)
              .content(R.string.mcam_portrait_warning)
              .positiveText(R.string.mcam_yes)
              .negativeText(android.R.string.cancel)
              .onPositive(
                  new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                        @NonNull MaterialDialog materialDialog,
                        @NonNull DialogAction dialogAction) {
                      mIsRecording = startRecordingVideo();
                    }
                  })
              .show();
        } else {
          mIsRecording = startRecordingVideo();
        }
      }
      setupFlashModeVideo();
    } else if (id == R.id.stillshot) {
      takeStillshot();
    } else if (id == R.id.flashStillshot) {
      invalidateFlash(true);
    }
    else if (id == R.id.flashVideo) {
      Log.d(TAG, "onClick: toggling video flash.");
      toggleFlashVideo(true);
      closeCamera();
      openCamera();
      setupFlashMode();
      setupFlashModeVideo();
    }
    else if(id == R.id.video_switch){
      Log.d(TAG, "onClick: toggling camera mode.");
      if(mVideoSwitch.isChecked()){ //camera mode is on so switch to video
        Log.d(TAG, "onClick: video mode is on so switch to camera.");
        getActivity().setResult(RESULT_START_CAMERA);
        getActivity().finish();
      }
      else{
        Log.d(TAG, "onClick: camera mode is on so switch to video");
        getActivity().setResult(RESULT_START_VIDEO);
        getActivity().finish();
      }
    }
  }

  private void invalidateFlash(boolean toggle) {
    if (toggle) mInterface.toggleFlashMode();
    setupFlashMode();
    onPreferencesUpdated();
  }

  private void toggleFlashVideo(boolean toggle) {
    if (toggle) mInterface.toggleFlashModeVideo();
    setupFlashModeVideo();
    onPreferencesUpdated();
  }

  private void setupFlashMode() {
    if (mInterface.shouldHideFlash()) {
      mButtonFlash.setVisibility(View.GONE);
      return;
    }
    else {
      mButtonFlash.setVisibility(View.VISIBLE);
    }

    final int res;
    switch (mInterface.getFlashMode()) {
      case FLASH_MODE_AUTO:
        res = mInterface.iconFlashAuto();
        break;
      case FLASH_MODE_ALWAYS_ON:
        res = mInterface.iconFlashOn();
        break;
      case FLASH_MODE_OFF:
      default:
        res = mInterface.iconFlashOff();
    }

    setImageRes(mButtonFlash, res);

  }

  private void setupFlashModeVideo() {
    Log.d(TAG, "setupFlashModeVideo: setting up flash mode for video.");

    if(mIsRecording){
      mButtonFlashVideo.setVisibility(View.GONE);
    }
    else{
      mButtonFlashVideo.setVisibility(View.VISIBLE);
    }


    final int res;
    switch (mInterface.getFlashModeVideo()) {
      case FLASH_MODE_ALWAYS_ON:
        res = mInterface.iconFlashOn();
        break;
      case FLASH_MODE_OFF:
      default:
        res = mInterface.iconFlashOff();
    }

    setImageRes(mButtonFlashVideo, res);
  }
}
