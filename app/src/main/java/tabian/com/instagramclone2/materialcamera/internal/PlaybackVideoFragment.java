package tabian.com.instagramclone2.materialcamera.internal;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import tabian.com.instagramclone2.R;
import tabian.com.instagramclone2.easyvideoplayer.EasyVideoCallback;
import tabian.com.instagramclone2.easyvideoplayer.EasyVideoPlayer;
import tabian.com.instagramclone2.materialcamera.util.CameraUtil;


/** @author Aidan Follestad (afollestad) */
public class PlaybackVideoFragment extends Fragment
    implements CameraUriInterface, EasyVideoCallback {

  private static final String TAG = "PlaybackVideoFragment";

  private EasyVideoPlayer mPlayer;
  private String mOutputUri;
  private BaseCaptureInterface mInterface;

  private Handler mCountdownHandler;
  private final Runnable mCountdownRunnable =
      new Runnable() {
        @Override
        public void run() {
          if (mPlayer != null) {
            long diff = mInterface.getRecordingEnd() - System.currentTimeMillis();
            if (diff <= 0) {
              useVideo();
              return;
            }
            mPlayer.setBottomLabelText(String.format("-%s", CameraUtil.getDurationString(diff)));
            if (mCountdownHandler != null) mCountdownHandler.postDelayed(mCountdownRunnable, 200);
          }
        }
      };

  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mInterface = (BaseCaptureInterface) activity;
  }

  public static PlaybackVideoFragment newInstance(
      String outputUri, boolean allowRetry, int primaryColor) {
    PlaybackVideoFragment fragment = new PlaybackVideoFragment();
    fragment.setRetainInstance(true);
    Bundle args = new Bundle();
    args.putString("output_uri", outputUri);
    args.putBoolean(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ALLOW_RETRY, allowRetry);
    args.putInt(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.PRIMARY_COLOR, primaryColor);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (getActivity() != null)
      getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mPlayer != null) {
      mPlayer.release();
      mPlayer.reset();
      mPlayer = null;
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.mcam_fragment_videoplayback, container, false);

  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mPlayer = (EasyVideoPlayer) view.findViewById(R.id.playbackView);
    mPlayer.setCallback(this);

    mPlayer.setSubmitTextRes(mInterface.labelConfirm());
    mPlayer.setRetryTextRes(mInterface.labelRetry());
    mPlayer.setPlayDrawableRes(mInterface.iconPlay());
    mPlayer.setPauseDrawableRes(mInterface.iconPause());

    if (getArguments().getBoolean(tabian.com.instagramclone2.materialcamera.internal.CameraIntentKey.ALLOW_RETRY, true))
      mPlayer.setLeftAction(EasyVideoPlayer.LEFT_ACTION_RETRY);
    mPlayer.setRightAction(EasyVideoPlayer.RIGHT_ACTION_SUBMIT);

    mPlayer.setThemeColor(getArguments().getInt(CameraIntentKey.PRIMARY_COLOR));
    mOutputUri = getArguments().getString("output_uri");

    if (mInterface.hasLengthLimit()
        && mInterface.shouldAutoSubmit()
        && mInterface.continueTimerInPlayback()) {
      final long diff = mInterface.getRecordingEnd() - System.currentTimeMillis();
      mPlayer.setBottomLabelText(String.format("-%s", CameraUtil.getDurationString(diff)));
      startCountdownTimer();
    }

    mPlayer.setSource(Uri.parse(mOutputUri));
  }

  private void startCountdownTimer() {
    if (mCountdownHandler == null) mCountdownHandler = new Handler();
    else mCountdownHandler.removeCallbacks(mCountdownRunnable);
    mCountdownHandler.post(mCountdownRunnable);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (mCountdownHandler != null) {
      mCountdownHandler.removeCallbacks(mCountdownRunnable);
      mCountdownHandler = null;
    }
    if (mPlayer != null) {
      mPlayer.release();
      mPlayer = null;
    }
  }

  private void useVideo() {
    if (mPlayer != null) {
      mPlayer.release();
      mPlayer = null;
    }
    if (mInterface != null) mInterface.useMedia(mOutputUri);
  }

  @Override
  public String getOutputUri() {
    return getArguments().getString("output_uri");
  }

  @Override
  public void onStarted(EasyVideoPlayer player) {}

  @Override
  public void onPaused(EasyVideoPlayer player) {}

  @Override
  public void onPreparing(EasyVideoPlayer player) {}

  @Override
  public void onPrepared(EasyVideoPlayer player) {}

  @Override
  public void onBuffering(int percent) {}

  @Override
  public void onError(EasyVideoPlayer player, Exception e) {
    new MaterialDialog.Builder(getActivity())
        .title(R.string.mcam_error)
        .content(e.getMessage())
        .positiveText(android.R.string.ok)
        .show();
  }



  @Override
  public void onCompletion(EasyVideoPlayer player) {}

  @Override
  public void onRetry(EasyVideoPlayer player, Uri source) {
    if (mInterface != null) mInterface.onRetry(mOutputUri);
  }

  @Override
  public void onSubmit(EasyVideoPlayer player, Uri source) {
    useVideo();
  }

  @Override
  public void onClickVideoFrame(EasyVideoPlayer player) {
    Log.d(TAG, "onClickVideoFrame: clicked video frame");
  }

  @Override
  public void addToStory(EasyVideoPlayer player, Uri source) {
    Log.d(TAG, "addToStory: adding new video story.");
    mInterface.addToStory(mOutputUri);
  }

  @Override
  public void saveStory(EasyVideoPlayer player, Uri source) {
    Log.d(TAG, "saveStory: saving new video story.");
    mInterface.useMedia(mOutputUri);
  }
}














