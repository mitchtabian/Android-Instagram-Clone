package tabian.com.instagramclone2.materialcamera.internal;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tabian.com.instagramclone2.R;
import tabian.com.instagramclone2.Utils.FilePaths;
import tabian.com.instagramclone2.Utils.ImageManager;
import tabian.com.instagramclone2.Utils.RotateBitmap;
import tabian.com.instagramclone2.materialcamera.ICallback;
import tabian.com.instagramclone2.materialcamera.MaterialCamera;
import tabian.com.instagramclone2.materialcamera.TimeLimitReachedException;
import tabian.com.instagramclone2.materialcamera.util.CameraUtil;
import tabian.com.instagramclone2.videocompressor.file.FileUtils;
import tabian.com.instagramclone2.videocompressor.video.MediaController;


/** @author Aidan Follestad (afollestad) */
public abstract class BaseCaptureActivity extends AppCompatActivity
    implements BaseCaptureInterface {

  private static final String TAG = "BaseCaptureActivity";
  private static final int RESULT_ADD_NEW_STORY = 7891;
  private static final int PROGRESS_BAR_ID = 5544;

  private int mCameraPosition = CAMERA_POSITION_UNKNOWN;
  private int mFlashMode = FLASH_MODE_OFF;
  private boolean mRequestingPermission;
  private long mRecordingStart = -1;
  private long mRecordingEnd = -1;
  private long mLengthLimit = -1;
  private Object mFrontCameraId;
  private Object mBackCameraId;
  private boolean mDidRecord = false;
  private List<Integer> mFlashModes;
  private ProgressBar mProgressBar;
  private AlertDialog mAlertDialog;
  private String mUri = null;
  private String mUploadUri = null;
  private File tempFile;
  private Boolean mDeleteCompressedMedia = false;

  public static final int PERMISSION_RC = 69;

  @IntDef({CAMERA_POSITION_UNKNOWN, CAMERA_POSITION_BACK, CAMERA_POSITION_FRONT})
  @Retention(RetentionPolicy.SOURCE)
  public @interface CameraPosition {}

  public static final int CAMERA_POSITION_UNKNOWN = 0;
  public static final int CAMERA_POSITION_FRONT = 1;
  public static final int CAMERA_POSITION_BACK = 2;

  @IntDef({FLASH_MODE_OFF, FLASH_MODE_ALWAYS_ON, FLASH_MODE_AUTO})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FlashMode {}

  public static final int FLASH_MODE_OFF = 0;
  public static final int FLASH_MODE_ALWAYS_ON = 1;
  public static final int FLASH_MODE_AUTO = 2;

  @Override
  protected final void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("camera_position", mCameraPosition);
    outState.putBoolean("requesting_permission", mRequestingPermission);
    outState.putLong("recording_start", mRecordingStart);
    outState.putLong("recording_end", mRecordingEnd);
    outState.putLong(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.LENGTH_LIMIT, mLengthLimit);
    if (mFrontCameraId instanceof String) {
      outState.putString("front_camera_id_str", (String) mFrontCameraId);
      outState.putString("back_camera_id_str", (String) mBackCameraId);
    } else {
      if (mFrontCameraId != null) outState.putInt("front_camera_id_int", (Integer) mFrontCameraId);
      if (mBackCameraId != null) outState.putInt("back_camera_id_int", (Integer) mBackCameraId);
    }
    outState.putInt("flash_mode", mFlashMode);
  }

  @Override
  protected final void onCreate(Bundle savedInstanceState) {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    super.onCreate(savedInstanceState);

    if (!CameraUtil.hasCamera(this)) {
      new MaterialDialog.Builder(this)
          .title(R.string.mcam_error)
          .content(R.string.mcam_video_capture_unsupported)
          .positiveText(android.R.string.ok)
          .dismissListener(
              new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                  finish();
                }
              })
          .show();
      return;
    }
    setContentView(R.layout.mcam_activity_videocapture);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      final int primaryColor = getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.PRIMARY_COLOR, 0);
      final boolean isPrimaryDark = CameraUtil.isColorDark(primaryColor);
      final Window window = getWindow();
      window.setStatusBarColor(CameraUtil.darkenColor(primaryColor));
      window.setNavigationBarColor(isPrimaryDark ? primaryColor : Color.BLUE);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        final View view = window.getDecorView();
        int flags = view.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        view.setSystemUiVisibility(flags);
      }
    }

    if (null == savedInstanceState) {
      checkPermissions();
      mLengthLimit = getIntent().getLongExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.LENGTH_LIMIT, -1);
    } else {
      mCameraPosition = savedInstanceState.getInt("camera_position", -1);
      mRequestingPermission = savedInstanceState.getBoolean("requesting_permission", false);
      mRecordingStart = savedInstanceState.getLong("recording_start", -1);
      mRecordingEnd = savedInstanceState.getLong("recording_end", -1);
      mLengthLimit = savedInstanceState.getLong(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.LENGTH_LIMIT, -1);
      if (savedInstanceState.containsKey("front_camera_id_str")) {
        mFrontCameraId = savedInstanceState.getString("front_camera_id_str");
        mBackCameraId = savedInstanceState.getString("back_camera_id_str");
      } else {
        mFrontCameraId = savedInstanceState.getInt("front_camera_id_int");
        mBackCameraId = savedInstanceState.getInt("back_camera_id_int");
      }
      mFlashMode = savedInstanceState.getInt("flash_mode");
    }

    getWindow()
        .addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  private void checkPermissions() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      showInitialRecorder();
      return;
    }
    final boolean cameraGranted =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED;
    final boolean audioGranted =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED;

    final boolean audioNeeded = !useStillshot() && !audioDisabled();

    String[] perms = null;
    if (cameraGranted) {
      if (audioNeeded && !audioGranted) {
        perms = new String[] {Manifest.permission.RECORD_AUDIO};
      }
    } else {
      if (audioNeeded && !audioGranted) {
        perms = new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
      } else {
        perms = new String[] {Manifest.permission.CAMERA};
      }
    }

    if (perms != null) {
      ActivityCompat.requestPermissions(this, perms, PERMISSION_RC);
      mRequestingPermission = true;
    } else {
      showInitialRecorder();
    }
  }

  @Override
  protected final void onPause() {
    Log.d(TAG, "onPause: called.");
    super.onPause();
    if (!isFinishing() && !isChangingConfigurations() && !mRequestingPermission) finish();
  }

  @Override
  public final void onBackPressed() {
    Fragment frag = getFragmentManager().findFragmentById(R.id.container);
    if (frag != null) {
      if (frag instanceof PlaybackVideoFragment && allowRetry()) {
        onRetry(((tabian.com.instagramclone2.materialcamera.internal.CameraUriInterface) frag).getOutputUri());
        return;
      } else if (frag instanceof tabian.com.instagramclone2.materialcamera.internal.BaseCameraFragment) {
        ((tabian.com.instagramclone2.materialcamera.internal.BaseCameraFragment) frag).cleanup();
      } else if (frag instanceof BaseGalleryFragment && allowRetry()) {
        onRetry(((tabian.com.instagramclone2.materialcamera.internal.CameraUriInterface) frag).getOutputUri());
        return;
      }
    }
//    deleteTempFile();
    finish();
  }

  @NonNull
  public abstract Fragment getFragment();

  public final Fragment createFragment() {
    Fragment frag = getFragment();
    frag.setArguments(getIntent().getExtras());
    return frag;
  }

  @Override
  public void setRecordingStart(long start) {
    mRecordingStart = start;
    if (start > -1 && hasLengthLimit()) setRecordingEnd(mRecordingStart + getLengthLimit());
    else setRecordingEnd(-1);
  }

  @Override
  public long getRecordingStart() {
    return mRecordingStart;
  }

  @Override
  public void setRecordingEnd(long end) {
    mRecordingEnd = end;
  }

  @Override
  public long getRecordingEnd() {
    return mRecordingEnd;
  }

  @Override
  public long getLengthLimit() {
    return mLengthLimit;
  }

  @Override
  public boolean hasLengthLimit() {
    return getLengthLimit() > -1;
  }

  @Override
  public boolean countdownImmediately() {
    return getIntent().getBooleanExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.COUNTDOWN_IMMEDIATELY, false);
  }

  @Override
  public void setCameraPosition(int position) {
    mCameraPosition = position;
  }

  @Override
  public void toggleCameraPosition() {
    if (getCurrentCameraPosition() == CAMERA_POSITION_FRONT) {
      // Front, go to back if possible
      if (getBackCamera() != null) setCameraPosition(CAMERA_POSITION_BACK);
    } else {
      // Back, go to front if possible
      if (getFrontCamera() != null) setCameraPosition(CAMERA_POSITION_FRONT);
    }
  }

  @Override
  public int getCurrentCameraPosition() {
    return mCameraPosition;
  }

  @Override
  public Object getCurrentCameraId() {
    if (getCurrentCameraPosition() == CAMERA_POSITION_FRONT) return getFrontCamera();
    else return getBackCamera();
  }

  @Override
  public void setFrontCamera(Object id) {
    mFrontCameraId = id;
  }

  @Override
  public Object getFrontCamera() {
    return mFrontCameraId;
  }

  @Override
  public void setBackCamera(Object id) {
    mBackCameraId = id;
  }

  @Override
  public Object getBackCamera() {
    return mBackCameraId;
  }

  private void showInitialRecorder() {
    getFragmentManager().beginTransaction().replace(R.id.container, createFragment()).commit();
  }

  @Override
  public final void onRetry(@Nullable String outputUri) {
    if (outputUri != null) deleteOutputFile(outputUri);
    if (!shouldAutoSubmit() || restartTimerOnRetry()) setRecordingStart(-1);
    if (getIntent().getBooleanExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.RETRY_EXITS, false)) {
      setResult(
          RESULT_OK,
          new Intent().putExtra(MaterialCamera.STATUS_EXTRA, MaterialCamera.STATUS_RETRY));
      finish();
      return;
    }
    getFragmentManager().beginTransaction().replace(R.id.container, createFragment()).commit();
  }

  @Override
  public final void onShowPreview(@Nullable final String outputUri, boolean countdownIsAtZero) {
    if ((shouldAutoSubmit() && (countdownIsAtZero || !allowRetry() || !hasLengthLimit()))
        || outputUri == null) {
      if (outputUri == null) {
        setResult(
            RESULT_CANCELED,
            new Intent().putExtra(MaterialCamera.ERROR_EXTRA, new TimeLimitReachedException()));
        finish();
        return;
      }
      useMedia(outputUri);
    } else {
      if (!hasLengthLimit() || !continueTimerInPlayback()) {
        // No countdown or countdown should not continue through playback, reset timer to 0
        setRecordingStart(-1);
      }
      Fragment frag =
          PlaybackVideoFragment.newInstance(
              outputUri, allowRetry(), getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.PRIMARY_COLOR, 0));
      getFragmentManager().beginTransaction().replace(R.id.container, frag).commit();
    }
  }

  @Override
  public void onShowStillshot(String outputUri) {
    if (shouldAutoSubmit()) {
      useMedia(outputUri);
    } else {
      Fragment frag =
          StillshotPreviewFragment.newInstance(
              outputUri, allowRetry(), getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.PRIMARY_COLOR, 0));
      getFragmentManager().beginTransaction().replace(R.id.container, frag).commit();
    }
  }

  @Override
  public final boolean allowRetry() {
    return getIntent().getBooleanExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ALLOW_RETRY, true);
  }

  @Override
  public final boolean shouldAutoSubmit() {
    return getIntent().getBooleanExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.AUTO_SUBMIT, false);
  }

  private void deleteOutputFile(@Nullable String uri) {
    if (uri != null)
      //noinspection ResultOfMethodCallIgnored
      new File(Uri.parse(uri).getPath()).delete();
  }

  @Override
  protected final void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PERMISSION_RC) showInitialRecorder();
  }

  @Override
  public final void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    mRequestingPermission = false;
    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
      new MaterialDialog.Builder(this)
          .title(R.string.mcam_permissions_needed)
          .content(R.string.mcam_video_perm_warning)
          .positiveText(android.R.string.ok)
          .dismissListener(
              new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                  finish();
                }
              })
          .show();
    } else {
      showInitialRecorder();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
//    deleteTempFile();
  }

  @Override
  public final void useMedia(String uri) {
    if (uri != null) {
      Log.d(TAG, "useMedia: upload uri: " + uri);
      mUri = uri;
//      setResult(
//          Activity.RESULT_OK,
//          getIntent()
//              .putExtra(MaterialCamera.STATUS_EXTRA, MaterialCamera.STATUS_RECORDED)
//              .setDataAndType(Uri.parse(uri), useStillshot() ? "image/jpeg" : "video/mp4"));
      saveMediaToMemory(uri);
    }
//    finish();
  }


  

  @Override
  public void addToStory(String uri) {
    Log.d(TAG, "addToStory: adding file to story.");
    initProgressBar();
    if(isMediaVideo(uri)){
      if(mUploadUri == null){
        Log.d(TAG, "addToStory: Video was not saved. Beginning compression.");
        mDeleteCompressedMedia = true;
        saveTempAndCompress(uri);
      }
      else{
        Log.d(TAG, "addToStory: video has been saved. Now uploading.");
        Log.d(TAG, "addToStory: upload uri: " + mUploadUri);
        finishActivityAndUpload();
      }
    }
    else{
      if(mUploadUri == null){
        Log.d(TAG, "addToStory: Image was not saved. Now uploading");
        mDeleteCompressedMedia = true;
        mUploadUri = uri;
        finishActivityAndUpload();
      }
      else{
        Log.d(TAG, "addToStory: Image has been saved. Now uploading.");
        Log.d(TAG, "addToStory: upload uri: " + mUploadUri);
        finishActivityAndUpload();
      }
    }
    
  }

  private void finishActivityAndUpload(){
    Log.d(TAG, "finishActivityAndUpload: called.");
    if(mDeleteCompressedMedia){
      setResult(
              RESULT_ADD_NEW_STORY,
              getIntent()
                      .putExtra(MaterialCamera.DELETE_UPLOAD_FILE_EXTRA, true)
                      .putExtra(MaterialCamera.STATUS_EXTRA, MaterialCamera.STATUS_RECORDED)
                      .setDataAndType(Uri.parse(mUploadUri), useStillshot() ? "image/jpeg" : "video/mp4"));
    }
    else{
      setResult(
              RESULT_ADD_NEW_STORY,
              getIntent()
                      .putExtra(MaterialCamera.DELETE_UPLOAD_FILE_EXTRA, false)
                      .putExtra(MaterialCamera.STATUS_EXTRA, MaterialCamera.STATUS_RECORDED)
                      .setDataAndType(Uri.parse(mUploadUri), useStillshot() ? "image/jpeg" : "video/mp4"));
    }

    finish();
  }

  private void initProgressBar(){
//    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//            RelativeLayout.LayoutParams.MATCH_PARENT,
//            RelativeLayout.LayoutParams.MATCH_PARENT
//    );
//    RelativeLayout relativeLayout = new RelativeLayout(this);
//    relativeLayout.bringToFront();
//    relativeLayout.setLayoutParams(layoutParams);
////
//    FrameLayout frameLayout = ((Activity)this).findViewById(R.id.container);
//    frameLayout.addView(relativeLayout);
//
//    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//            250,
//            RelativeLayout.LayoutParams.WRAP_CONTENT
//    );
//    params.addRule(RelativeLayout.CENTER_IN_PARENT);
//    mProgressBar = new ProgressBar(this);
//    mProgressBar.setId(PROGRESS_BAR_ID);
//    mProgressBar.setLayoutParams(params);
//    mProgressBar.setVisibility(View.VISIBLE);
//    mProgressBar.bringToFront();
//    relativeLayout.addView(mProgressBar);
//    Drawable progressDrawable = mProgressBar.getIndeterminateDrawable().mutate();
//    progressDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
//    mProgressBar.setProgressDrawable(progressDrawable);

    // retrieve display dimensions
//    Rect displayRectangle = new Rect();
//    Window window = this.getWindow();
//    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

    LayoutInflater li = LayoutInflater.from(this);
    View layout = li.inflate(R.layout.layout_processing_dialog, null);
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            150,
            FrameLayout.LayoutParams.WRAP_CONTENT
    );
    layout.setLayoutParams(params);
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setView(layout);
    mAlertDialog = alertDialogBuilder.create();
    mAlertDialog.setCancelable(false);
    mAlertDialog.show();
  }


  private void saveTempAndCompress(String uri){
    //save temporary file for compression
    String fileName = uri.substring(uri.indexOf("Stories/") + 8);
    tempFile = FileUtils.saveTempFile(fileName, this, Uri.parse(uri));

    //delete the original
    deleteOutputFile(uri);

    //compress temp file and save new compressed version in "/Stories/"
    new VideoCompressor().execute();
  }


  private void saveMediaToMemory(String uri){
    Log.d(TAG, "saveMediaToMemory: saving media to memory.");
    Log.d(TAG, "saveMediaToMemory: uri: " + uri);

    initProgressBar();

    if(isMediaVideo(uri)){

      saveTempAndCompress(uri);
    }
    else{
      Bitmap bm = null;
      RotateBitmap rotateBitmap = new RotateBitmap();
      try{
        bm = rotateBitmap.HandleSamplingAndRotationBitmap(this, Uri.parse(uri));
      }catch (IOException e){
        e.printStackTrace();
      }

      //delete the old file
      deleteOutputFile(uri);

      saveBitmapToDisk(bm);
    }

  }


  class VideoCompressor extends AsyncTask<Void, Void, String> {
//    class VideoCompressor extends AsyncTask<Void, Void, Boolean> {

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      showProgressBar();
      Log.d(TAG,"Start video compression");
    }

    @Override
    protected String doInBackground(Void... voids) {
//  protected boolean doInBackground(Void... voids) {

      return MediaController.getInstance().convertVideo(tempFile.getPath());
    }

    @Override
    protected void onPostExecute(String filePath) {
//      protected void onPostExecute(Boolean compressed) {
      super.onPostExecute(filePath);
//      super.onPostExecute(compressed);
      hideProgressBar();
      if(!filePath.equals("")){
//        if(compressed){
        mUploadUri = filePath;
        Log.d(TAG,"Compression successfully!");
        if(mDeleteCompressedMedia){
          finishActivityAndUpload();
        }
      }
    }
  }

  private void deleteTempFile(){
    if(tempFile != null && tempFile.exists()){
      tempFile.delete();
    }
  }


  private void saveBitmapToDisk(final Bitmap bm){
    final ICallback callback = new ICallback() {
      @Override
      public void done(Exception e) {
        if (e == null) {
          hideProgressBar();
          Log.d(TAG, "saveBitmapToDisk: saved file to disk.");
          Toast.makeText(BaseCaptureActivity.this, "saved", Toast.LENGTH_SHORT).show();
        } else {
          e.printStackTrace();
          hideProgressBar();
          Toast.makeText(BaseCaptureActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
        }
      }
    };
    Log.d(TAG, "saveBitmapToDisk: saving to disc.");
    final Handler handler = new Handler();
    new Thread() {
      @Override
      public void run() {
        try {
          FileOutputStream out = null;
          FileInputStream fis = null;
          try {
            FilePaths filePaths = new FilePaths();
            String timeStamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
//            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(filePaths.STORIES + "/IMG_" + timeStamp + ".jpg");
            out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, ImageManager.IMAGE_SAVE_QUALITY, out);

            File imagefile = new File(file.getPath());
            try {
              fis = new FileInputStream(imagefile);
            } catch (FileNotFoundException e) {
              e.printStackTrace();
            }
            mUploadUri = file.getPath();

            Log.d(TAG, "saveBitmapToDisk: new uri: " + mUploadUri);
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            try {
              if (out != null) {
                out.close();
              }
              if(fis != null){
                fis.close();
              }

            } catch (IOException e) {
              e.printStackTrace();
            }
          }

          handler.post(
                  new Runnable() {
                    @Override
                    public void run() {
                      callback.done(null);
                    }
                  });
        } catch (final Exception e) {
          handler.post(
                  new Runnable() {
                    @Override
                    public void run() {
                      callback.done(e);
                    }
                  });
        }
      }
    }.start();

  }

  private boolean isMediaVideo(String uri){
    if(uri.contains(".mp4") || uri.contains(".wmv") || uri.contains(".flv") || uri.contains(".avi")){
      return true;
    }
    return false;
  }

  private void showProgressBar(){
//    if(mProgressBar != null){
//      mProgressBar.setVisibility(View.VISIBLE);
//    }
    if(mAlertDialog != null){
      mAlertDialog.show();
    }
  }

  private void hideProgressBar(){
//    if(mProgressBar != null){
//      mProgressBar.setVisibility(View.INVISIBLE);
//    }
    if(mAlertDialog != null){
      mAlertDialog.dismiss();
    }
  }

  @Override
  public void setDidRecord(boolean didRecord) {
    mDidRecord = didRecord;
  }

  @Override
  public boolean didRecord() {
    return mDidRecord;
  }

  @Override
  public int getFlashMode() {
    return mFlashMode;
  }

  @Override
  public int getFlashModeVideo() {
    return mFlashMode;
  }

  @Override
  public void toggleFlashMode() {
    if (mFlashModes != null) {
      mFlashMode = mFlashModes.get((mFlashModes.indexOf(mFlashMode) + 1) % mFlashModes.size());
    }
  }



  @Override
  public void toggleFlashModeVideo() {
    Log.d(TAG, "toggleFlashModeVideo: toggling video flash mode.");
    if (mFlashModes != null) {
      Log.d(TAG, "toggleFlashModeVideo: flash mode is not null");
      if(mFlashMode == FLASH_MODE_ALWAYS_ON){
        mFlashMode = FLASH_MODE_OFF;
      }
      else{
        mFlashMode = FLASH_MODE_ALWAYS_ON;
      }
    }
  }

  @Override
  public boolean restartTimerOnRetry() {
    return getIntent().getBooleanExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.RESTART_TIMER_ON_RETRY, false);
  }

  @Override
  public boolean continueTimerInPlayback() {
    return getIntent().getBooleanExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.CONTINUE_TIMER_IN_PLAYBACK, false);
  }

  @Override
  public int videoEncodingBitRate(int defaultVal) {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.VIDEO_BIT_RATE, defaultVal);
  }

  @Override
  public int audioEncodingBitRate(int defaultVal) {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.AUDIO_ENCODING_BIT_RATE, defaultVal);
  }

  @Override
  public int videoFrameRate(int defaultVal) {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.VIDEO_FRAME_RATE, defaultVal);
  }

  @Override
  public float videoPreferredAspect() {
    return getIntent().getFloatExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.VIDEO_PREFERRED_ASPECT, 4f / 3f);
  }

  @Override
  public int videoPreferredHeight() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.VIDEO_PREFERRED_HEIGHT, 720);
  }

  @Override
  public long maxAllowedFileSize() {
    return getIntent().getLongExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.MAX_ALLOWED_FILE_SIZE, -1);
  }

  @Override
  public int qualityProfile() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.QUALITY_PROFILE, CamcorderProfile.QUALITY_HIGH);
  }

  @DrawableRes
  @Override
  public int iconPause() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_PAUSE, R.drawable.evp_action_pause);
  }

  @DrawableRes
  @Override
  public int iconPlay() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_PLAY, R.drawable.evp_action_play);
  }

  @DrawableRes
  @Override
  public int iconRestart() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_RESTART, R.drawable.evp_action_restart);
  }

  @DrawableRes
  @Override
  public int iconRearCamera() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_REAR_CAMERA, R.drawable.mcam_camera_rear);
  }

  @DrawableRes
  @Override
  public int iconFrontCamera() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_FRONT_CAMERA, R.drawable.mcam_camera_front);
  }

  @DrawableRes
  @Override
  public int iconStop() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_STOP, R.drawable.mcam_action_stop);
  }

  @DrawableRes
  @Override
  public int iconRecord() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_RECORD, R.drawable.mcam_action_capture);
  }

  @StringRes
  @Override
  public int labelRetry() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.LABEL_RETRY, R.string.mcam_retry);
  }

  @Deprecated
  @StringRes
  @Override
  public int labelUseVideo() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.LABEL_CONFIRM, R.string.mcam_use_video);
  }

  @StringRes
  @Override
  public int labelConfirm() {
    return getIntent()
        .getIntExtra(
            tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.LABEL_CONFIRM,
            useStillshot() ? R.string.mcam_use_stillshot : R.string.mcam_use_video);
  }

  @DrawableRes
  @Override
  public int iconStillshot() {
    return getIntent()
        .getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_STILL_SHOT, R.drawable.mcam_action_stillshot);
  }

//  @Override
//  public void setUseStillshot(boolean bool) {
//    getIntent().putExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.STILL_SHOT, bool);
//  }

  @Override
  public boolean useStillshot() {
    return getIntent().getBooleanExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.STILL_SHOT, false);
  }

  @DrawableRes
  @Override
  public int iconFlashAuto() {
    return getIntent()
        .getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_FLASH_AUTO, R.drawable.mcam_action_flash_auto);
  }

  @DrawableRes
  @Override
  public int iconFlashOn() {
    return getIntent().getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_FLASH_ON, R.drawable.mcam_action_flash);
  }

  @DrawableRes
  @Override
  public int iconFlashOff() {
    return getIntent()
        .getIntExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ICON_FLASH_OFF, R.drawable.mcam_action_flash_off);
  }

  @Override
  public void setFlashModes(List<Integer> modes) {
    mFlashModes = modes;
  }

  @Override
  public boolean shouldHideFlash() {
    return !useStillshot() || mFlashModes == null;
  }

  @Override
  public long autoRecordDelay() {
    return getIntent().getLongExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.AUTO_RECORD, -1);
  }

  @Override
  public boolean audioDisabled() {
    return getIntent().getBooleanExtra(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.AUDIO_DISABLED, false);
  }

  @Override
  public boolean shouldHideCameraFacing() {
    return !getIntent().getBooleanExtra(CameraIntentKey.ALLOW_CHANGE_CAMERA, false);
  }
}
