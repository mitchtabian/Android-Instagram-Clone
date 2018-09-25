package tabian.com.instagramclone2.opengl;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;

import tabian.com.instagramclone2.R;
import tabian.com.instagramclone2.Utils.FilePaths;
import tabian.com.instagramclone2.materialcamera.MaterialCamera;


/**
 * Created by User on 1/8/2018.
 */

public class NewStoryActivity extends Activity {

    public static final String TAG = "NewStoryActivity";

    private final static int CAMERA_RQ = 6969;
    private final static int DEFAULT_BITRATE = 1024000;
    private static final int RESULT_START_CAMERA = 4567;
    private static final int RESULT_START_VIDEO = 4589;
    private static final int RESULT_ADD_NEW_STORY = 7891;
    private MaterialCamera mMaterialCamera;


    private int mStartType = RESULT_START_VIDEO;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_story);

        init();
    }

    private void init(){
        Log.d(TAG, "init: initializing material camera.");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

//        File saveFolder = new File(Environment.getExternalStorageDirectory(), "Stories/" + getTimestamp());
        FilePaths filePaths = new FilePaths();
        File saveFolder = new File(filePaths.STORIES);
        try{
            if (!saveFolder.mkdirs());
        }catch (RuntimeException e){
            e.printStackTrace();
        }

        mMaterialCamera = new MaterialCamera(this);                               // Constructor takes an Activity

        if(mStartType == RESULT_START_VIDEO) {
            Log.d(TAG, "init: starting camera with VIDEO enabled.");
            mMaterialCamera
                    .allowRetry(true)                                  // Whether or not 'Retry' is visible during playback
                    .autoSubmit(false)                                 // Whether or not user is allowed to playback videos after recording. This can affect other things, discussed in the next section.
                    .saveDir(saveFolder)                               // The folder recorded videos are saved to
//                .primaryColorAttr(R.attr.colorPrimary)             // The theme color used for the camera, defaults to colorPrimary of Activity in the constructor
                    .showPortraitWarning(false)                         // Whether or not a warning is displayed if the user presses record in portrait orientation
                    .defaultToFrontFacing(false)                       // Whether or not the camera will initially show the front facing camera
//                .allowChangeCamera(true)                           // Allows the user to change cameras.
                    .retryExits(false)                                 // If true, the 'Retry' button in the playback screen will exit the camera instead of going back to the recorder
                    .restartTimerOnRetry(false)                        // If true, the countdown timer is reset to 0 when the user taps 'Retry' in playback
                    .continueTimerInPlayback(false)                    // If true, the countdown timer will continue to go down during playback, rather than pausing.
                    //Play with the encoding bitrate to change quality and size.
                    .videoEncodingBitRate(DEFAULT_BITRATE * 5)                     // Sets a custom bit rate for video recording.
                    .audioEncodingBitRate(50000)                       // Sets a custom bit rate for audio recording.
                    .videoFrameRate(30)                                // Sets a custom frame rate (FPS) for video recording.
//                    .qualityProfile(MaterialCamera.QUALITY_1080P)       // Sets a quality profile, manually setting bit rates or frame rates with other settings will overwrite individual quality profile settings
                    .videoPreferredHeight(720)                         // Sets a preferred height for the recorded video output.
                    .videoPreferredAspect(16f / 9f)                     // Sets a preferred aspect ratio for the recorded video output.
                    .maxAllowedFileSize(1024 * 1024 * 40)               // Sets a max file size of 4MB, recording will stop if file reaches this limit. Keep in mind, the FAT file system has a file size limit of 4GB.
                    .iconRecord(R.drawable.mcam_action_capture)        // Sets a custom icon for the button used to start recording
                    .iconStop(R.drawable.mcam_action_stop)             // Sets a custom icon for the button used to stop recording
                    .iconFrontCamera(R.drawable.mcam_camera_front)     // Sets a custom icon for the button used to switch to the front camera
                    .iconRearCamera(R.drawable.mcam_camera_rear)       // Sets a custom icon for the button used to switch to the rear camera
                    .iconPlay(R.drawable.evp_action_play)              // Sets a custom icon used to start playback
                    .iconPause(R.drawable.evp_action_pause)            // Sets a custom icon used to pause playback
                    .iconRestart(R.drawable.evp_action_restart)        // Sets a custom icon used to restart playback
                    .labelRetry(R.string.mcam_retry)                   // Sets a custom button label for the button used to retry recording, when available
                    .labelConfirm(R.string.mcam_use_video)             // Sets a custom button label for the button used to confirm/submit a recording
//                .autoRecordWithDelaySec(5)                         // The video camera will start recording automatically after a 5 second countdown. This disables switching between the front and back camera initially.
//                .autoRecordWithDelayMs(5000)                       // Same as the above, expressed with milliseconds instead of seconds.
                    .audioDisabled(false)                              // Set to true to record video without any audio.
                    .countdownSeconds(15f)
                    .start(CAMERA_RQ);
        }
        else{
            Log.d(TAG, "init: starting camera with STILLSHOT enabled.");
            mMaterialCamera
                    .allowRetry(true)                                  // Whether or not 'Retry' is visible during playback
                    .autoSubmit(false)                                 // Whether or not user is allowed to playback videos after recording. This can affect other things, discussed in the next section.
                    .saveDir(saveFolder)                               // The folder recorded videos are saved to
//                .primaryColorAttr(R.attr.colorPrimary)             // The theme color used for the camera, defaults to colorPrimary of Activity in the constructor
                    .showPortraitWarning(false)                         // Whether or not a warning is displayed if the user presses record in portrait orientation
                    .defaultToFrontFacing(false)                       // Whether or not the camera will initially show the front facing camera
//                .allowChangeCamera(true)                           // Allows the user to change cameras.
                    .retryExits(false)                                 // If true, the 'Retry' button in the playback screen will exit the camera instead of going back to the recorder
                    .restartTimerOnRetry(false)                        // If true, the countdown timer is reset to 0 when the user taps 'Retry' in playback
                    .continueTimerInPlayback(false)                    // If true, the countdown timer will continue to go down during playback, rather than pausing.
                    .videoEncodingBitRate(DEFAULT_BITRATE * 5)                     // Sets a custom bit rate for video recording.
                    .audioEncodingBitRate(50000)                       // Sets a custom bit rate for audio recording.
                    .videoFrameRate(30)                                // Sets a custom frame rate (FPS) for video recording.
//                    .qualityProfile(MaterialCamera.QUALITY_1080P)       // Sets a quality profile, manually setting bit rates or frame rates with other settings will overwrite individual quality profile settings
                    .videoPreferredHeight(720)                         // Sets a preferred height for the recorded video output.
                    .videoPreferredAspect(16f / 9f)                     // Sets a preferred aspect ratio for the recorded video output.
                    .maxAllowedFileSize(1024 * 1024 * 10)               // Sets a max file size of 4MB, recording will stop if file reaches this limit. Keep in mind, the FAT file system has a file size limit of 4GB.
                    .iconRecord(R.drawable.mcam_action_capture)        // Sets a custom icon for the button used to start recording
                    .iconStop(R.drawable.mcam_action_stop)             // Sets a custom icon for the button used to stop recording
                    .iconFrontCamera(R.drawable.mcam_camera_front)     // Sets a custom icon for the button used to switch to the front camera
                    .iconRearCamera(R.drawable.mcam_camera_rear)       // Sets a custom icon for the button used to switch to the rear camera
                    .iconPlay(R.drawable.evp_action_play)              // Sets a custom icon used to start playback
                    .iconPause(R.drawable.evp_action_pause)            // Sets a custom icon used to pause playback
                    .iconRestart(R.drawable.evp_action_restart)        // Sets a custom icon used to restart playback
                    .labelRetry(R.string.mcam_retry)                   // Sets a custom button label for the button used to retry recording, when available
                    .labelConfirm(R.string.mcam_use_video)             // Sets a custom button label for the button used to confirm/submit a recording
//                .autoRecordWithDelaySec(5)                         // The video camera will start recording automatically after a 5 second countdown. This disables switching between the front and back camera initially.
//                .autoRecordWithDelayMs(5000)                       // Same as the above, expressed with milliseconds instead of seconds.
                    .audioDisabled(false)                              // Set to true to record video without any audio.
                    .countdownSeconds(15f)
                    .stillShot()
                    .start(CAMERA_RQ);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: incoming result.");
        // Received recording or error from MaterialCamera
        if (requestCode == CAMERA_RQ) {

            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: result is OK.");
//                Toast.makeText(this, "Saved to: " + data.getDataString(), Toast.LENGTH_LONG).show();
            }
            else if(resultCode == RESULT_START_CAMERA){
                Log.d(TAG, "onActivityResult: got activity result. Opening Camera.");
                mStartType = RESULT_START_CAMERA;
                init();
            }
            else if(resultCode == RESULT_START_VIDEO){
                Log.d(TAG, "onActivityResult: got activity result. Opening video.");
                mStartType = RESULT_START_VIDEO;
                init();
            }
            else if(resultCode == RESULT_ADD_NEW_STORY){
                Log.d(TAG, "onActivityResult: preparing to add new story.");
                Log.d(TAG, "onActivityResult: upload uri: " + data.getData());
                if(data.hasExtra(MaterialCamera.DELETE_UPLOAD_FILE_EXTRA)){
                    setResult(
                            RESULT_ADD_NEW_STORY,
                            getIntent()
                                    .putExtra(MaterialCamera.DELETE_UPLOAD_FILE_EXTRA, true)
                                    .putExtra(MaterialCamera.STATUS_EXTRA, MaterialCamera.STATUS_RECORDED)
                                    .setDataAndType(data.getData(), data.getType()));
                    finish();
                }
                else{
                    setResult(
                            RESULT_ADD_NEW_STORY,
                            getIntent()
                                    .putExtra(MaterialCamera.DELETE_UPLOAD_FILE_EXTRA, false)
                                    .putExtra(MaterialCamera.STATUS_EXTRA, MaterialCamera.STATUS_RECORDED)
                                    .setDataAndType(data.getData(), data.getType()));
                    finish();
                }


            }
            else if(data != null) {
                Log.d(TAG, "onActivityResult: something went wrong.");
                Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }




}



















