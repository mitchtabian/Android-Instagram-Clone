package tabian.com.instagramclone2.materialcamera.internal;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import tabian.com.instagramclone2.R;


public class StillshotPreviewFragment extends tabian.com.instagramclone2.materialcamera.internal.BaseGalleryFragment {

  private static final String TAG = "StillshotPreviewFragmen";

  private ImageView mImageView;

  /**
   * Reference to the bitmap, in case 'onConfigurationChange' event comes, so we do not recreate the
   * bitmap
   */
  private static Bitmap mBitmap;

  public static StillshotPreviewFragment newInstance(
      String outputUri, boolean allowRetry, int primaryColor) {
    final StillshotPreviewFragment fragment = new StillshotPreviewFragment();
    fragment.setRetainInstance(true);
    Bundle args = new Bundle();
    args.putString("output_uri", outputUri);
    args.putBoolean(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ALLOW_RETRY, allowRetry);
    args.putInt(CameraIntentKey.PRIMARY_COLOR, primaryColor);
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.mcam_fragment_stillshot, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mImageView = (ImageView) view.findViewById(R.id.stillshot_imageview);

//    if(mInterface.useStillshot()){
//      mConfirm.setText(getString(R.string.mcam_use_stillshot));
//    }
//    else{
//      mConfirm.setText(getString(R.string.mcam_use_video));
//    }
//    mConfirm.setText(mInterface.labelConfirm());
//    mRetry.setText(mInterface.labelRetry());

    mRetry.setOnClickListener(this);
    mSaveStory.setOnClickListener(this);
    mAddToStory.setOnClickListener(this);

    mImageView
        .getViewTreeObserver()
        .addOnPreDrawListener(
            new ViewTreeObserver.OnPreDrawListener() {
              @Override
              public boolean onPreDraw() {
                setImageBitmap();
                mImageView.getViewTreeObserver().removeOnPreDrawListener(this);

                return true;
              }
            });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (mBitmap != null && !mBitmap.isRecycled()) {
      try {
        mBitmap.recycle();
        mBitmap = null;
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  /** Sets bitmap to ImageView widget */
  private void setImageBitmap() {
    final int width = mImageView.getMeasuredWidth();
    final int height = mImageView.getMeasuredHeight();

//    // TODO IMPROVE MEMORY USAGE HERE, ESPECIALLY ON LOW-END DEVICES.
//    if (mBitmap == null)
//      Log.d(TAG, "setImageBitmap: image uri: " + mOutputUri);
//      mBitmap = ImageUtil.getRotatedBitmap(Uri.parse(mOutputUri).getPath(), width, height);
//
//    if (mBitmap == null)
//      showDialog(
//          getString(R.string.mcam_image_preview_error_title),
//          getString(R.string.mcam_image_preview_error_message));
//    else {
//      mImageView.setImageBitmap(mBitmap);
//      getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//              WindowManager.LayoutParams.FLAG_FULLSCREEN);
//      DisplayMetrics displaymetrics = new DisplayMetrics();
//      getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//      mImageView.setMinimumWidth(displaymetrics.widthPixels);
//      mImageView.setMinimumHeight(displaymetrics.heightPixels);
//      Glide.with(getActivity())
//              .asBitmap()
//              .load(mOutputUri)
//              .into(mImageView);

//    }

    Log.d(TAG, "setImageBitmap: output uri: " + mOutputUri);
    Glide.with(getActivity())
            .asBitmap()
            .load(mOutputUri)
            .into(mImageView);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.retry) mInterface.onRetry(mOutputUri);
    else if (v.getId() == R.id.save_story) mInterface.useMedia(mOutputUri);
    else if(v.getId() == R.id.add_to_story) mInterface.addToStory(mOutputUri);
  }


  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy: called.");
    super.onDestroy();
    if(mOutputUri != null){
      Log.d(TAG, "onDestroy: cleaning up files.");
      deleteOutputFile(mOutputUri);
      mOutputUri = null;
    }
  }


  private void deleteOutputFile(@Nullable String uri) {
    if (uri != null)
      //noinspection ResultOfMethodCallIgnored
      new File(Uri.parse(uri).getPath()).delete();
  }
}
















