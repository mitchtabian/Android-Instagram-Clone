package tabian.com.instagramclone2.opengl;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.hdodenhof.circleimageview.CircleImageView;
import tabian.com.instagramclone2.R;


import static java.lang.Math.round;


/**
 * Created by User on 11/27/2017.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "fsnklsnflkds";

    private static final int MEDIA_TIMEOUT = 15000; //15 seconds or 15000 milliseconds
    private static final int VIDEO_REFRESH_COUNT_LIMIT = 5; // if the video fails to load 10 frames then force it
    private static final int INIT_VIDEO_PROGRESS_BAR = 33;
    private static final int UPDATE_UI_WITH_VIDEO_PROGRESS = 11;
    private static final int REMOVE_PROGRESS_BAR_CALLBACKS = 22;
    private static final int HIDE_PROGRESS_BAR = 44;
    private static final float STEP_SIZE = 2.0f;
    private static final float STARTING_STEP_SIZE = 6.0f;
    private static final int SURFACE_1 = 0;
    private static final int SURFACE_2 = 1;
    private static final int SURFACE_3 = 2;
    private static final int SURFACE_4 = 3;
    private static final int STARTING_SURFACE = 0;
    private static final int STARTING_SURFACE_PLUS_ONE = 1;
    private static final int STARTING_SURFACE_MINUS_ONE = 3;
    private static final int BACKGROUND_SURFACE = 4;
    private int mCurrentSurface = SURFACE_1;
    private static final int PLAYER_ONE = 1;
    private static final int PLAYER_ONE_SECONDARY = -1;
    private static final int PLAYER_TWO = 2;
    private static final int PLAYER_TWO_SECONDARY = -2;
    private static final int PLAYER_THREE = 3;
    private static final int PLAYER_THREE_SECONDARY = -3;
    private static final int PLAYER_FOUR = 4;
    private static final int PLAYER_FOUR_SECONDARY = -4;
    private static final int ACTIVE_PLAYER = 1; // currently the 'active' player on a surface
    private static final int PAUSED_PLAYER = 2; // currently 'active' but paused
    private static final int NOT_ACTIVE_PLAYER = 0; // currently NOT the 'active' player on a surface
    public int mPlayerState = ACTIVE_PLAYER;
    public int mSecondaryPlayerState = NOT_ACTIVE_PLAYER;
    public int mPlayer2State = ACTIVE_PLAYER;
    public int mSecondaryPlayer2State = NOT_ACTIVE_PLAYER;
    public int mPlayer3State = ACTIVE_PLAYER;
    public int mSecondaryPlayer3State = NOT_ACTIVE_PLAYER;
    public int mPlayer4State = ACTIVE_PLAYER;
    public int mSecondaryPlayer4State = NOT_ACTIVE_PLAYER;
    private boolean hasFirstVideo1Played = false;
    private boolean hasFirstVideo2Played = false;
    private boolean hasFirstVideo3Played = false;
    private boolean hasFirstVideo4Played = false;
    private ProgressBar mProgressBar;
    private Runnable mVideoRetryRunnable;
    private int mVideoRetryTimer = 0;
    private static final int VIDEO_RETRY_TIMEOUT = 4000;
    private int videoRetryTimer = 0;
    private int frameAvailableCount = 0;

    private Handler mVideoSurface1Handler;
    private boolean initVideoTexture1 = false;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    public SimpleExoPlayer mPlayer, mSecondaryPlayer, mPlayer2, mSecondaryPlayer2, mPlayer3, mSecondaryPlayer3, mPlayer4, mSecondaryPlayer4;
    private DefaultTrackSelector mTrackSelector;
    private TrackSelection.Factory mVideoTrackSelectionFactory;
    private boolean playWhenReady = false;

    private Context mContext;
    private static final float pi = 3.14159f;
    private boolean mStop = false;
    public boolean mRotateClockwise = false;
    public boolean mRotateCounterClockwise = false;


    private float screenHeight = 0f;
    private float screenWidth = 0f;
    private float screenRatio = 0f;
    private int numFaces = 4;

    private float[][] widthMatrix = new float[6][4];
    private float[][] heightMatrix = new float[6][4];
    private float[][] depthMatrix = new float[6][4];

    private FloatBuffer[][] mVertexBuffers;
    private FloatBuffer vertexBuffer1;
    private FloatBuffer vertexBuffer2;
    private FloatBuffer vertexBuffer3;
    private FloatBuffer vertexBuffer4;

    private FloatBuffer textureBuffer1;
    private FloatBuffer textureBuffer2;
    private FloatBuffer textureBuffer3;
    private FloatBuffer textureBuffer4;

    private float[] texCoords1;
    private float[] texCoords2;
    private float[] texCoords3;
    private float[] texCoords4;


    private ArrayList<float[]> mVertices = new ArrayList<>();
    private ArrayList<JSONArray> mResources = new ArrayList<>();

    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture2;
    private Surface mSurface2;
    private SurfaceTexture mSurfaceTexture3;
    private Surface mSurface3;
    private SurfaceTexture mSurfaceTexture4;
    private Surface mSurface4;
    private HashMap<Integer, ArrayList<MediaSource>> videoMediaSources = new HashMap<>();
//    private MediaSource[][] videoMediaSources;
    private boolean mUpdateST;
    private boolean mUpdateST2;
    private boolean mUpdateST3;
    private boolean mUpdateST4;
    private MyGLSurfaceView mGLView;


    private static float angleRectangle = 0f;
    private static float settledAngle = 0f;

    private int mVideo1Index = 0;
    private int mVideo2Index = 0;
    private int mVideo3Index = 0;
    private int mVideo4Index = 0;
    private boolean mFirstRotationSurface3 = true;
    private boolean mFirstRotationSurface4 = true;
    private int mNumRotations = 0;
    private int mHighestNumberMedia = 0;
    private int mNumResources = 0; //number of users who have uploaded stories
    private int mStartingResourceIndex = 0; //starting index
    private JSONArray mResourceIndices = new JSONArray();
    private boolean isImage1Set, isImage2Set, isImage3Set, isImage4Set = false;
    private static float depth = 0f;
    private static float dx = 0;
    private static float startPositionX = 0;
    private static float endPositionX = 0f;
    private static float position = 0.0f;

    //Video Progress Bars
    private MyProgressBar mCurrentProgressBar;
    private RelativeLayout mRelativeLayout;
    private LinearLayout mLinearLayout;
    private LinearLayout mLinearLayout2;
    private Handler mProgressHandler;
    private Runnable mProgressRunnable;
    private Runnable mProgressBarInitRunnable;
    private Handler mProgressBarInitHandler;
    private int mCurrentProgress = 0;
    private int mTotalDuration = 0;
    private int[] mIds;
    private boolean isProgressBarsInitialized = false;


    private float[][] colors = {  // Colors of the 6 faces
            {1.0f, 0.5f, 0.0f, 1.0f},  // 0. orange
            {1.0f, 0.0f, 1.0f, 1.0f},  // 1. violet
            {0.0f, 0.0f, 0.0f, 0.0f},  // 5. black
            {0.0f, 0.0f, 1.0f, 1.0f},  // 3. blue
            {1.0f, 0.0f, 0.0f, 1.0f},  // 4. red
            {0.0f, 0.0f, 0.0f, 0.0f}   // 5. white
    };

    private float[] backgroundVertices = {
            -20, -20, 1,
            20, -20, 1,
            -20, 20, 1,
            20, 20, 1,
    };

    private int[] textureId1 = new int[1];
    private int[] textureId2 = new int[1];
    private int[] textureId3 = new int[1];
    private int[] textureId4 = new int[1];


    private boolean isRotationEnabled = true;
    private boolean mAllowRotationClockwise = true;
    private float mAngleFinished = 0f;
    private float mStartingAngle = 0f;
    private ArrayList<SimpleTarget> mTargets = new ArrayList<>();
    private float mDefaultImageWidthScaleFactor = 0f;
    private float mDefaultImageHeightScaleFactor = 0f;


    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {

        if(angleRectangle <= mAngleFinished - 1){
            mAllowRotationClockwise = false;
            angleRectangle = mAngleFinished;
            correctRotation();
        }
        else{
            mAllowRotationClockwise = true;
        }

        if(isRotationEnabled && !mRotateClockwise && !mRotateCounterClockwise){
            if(mAllowRotationClockwise){
                dx = (position) - MyGLRenderer.position;
                if(mNumRotations > 0){
                    if(Math.abs(dx) < 100 && Math.abs(dx) > 1){
                        Log.d(TAG, "setPosition: dx: " + dx);
                        updateAngle(dx);
                    }
                }
                else if(mNumRotations == 0 && dx < 0){
                    if(Math.abs(dx) < 100 && Math.abs(dx) > 1){
                        Log.d(TAG, "setPosition: dx: " + dx);
                        updateAngle(dx);
                    }
                }
                MyGLRenderer.position = position;
                Log.d(TAG, "setPosition: position: " + MyGLRenderer.position);
            }
            else{
                dx = (position) - MyGLRenderer.position;
                if(dx > 1 && dx < 100){
                    Log.d(TAG, "setPosition: dx: " + dx);
                    updateAngle(dx);
                }
                MyGLRenderer.position = position;
                Log.d(TAG, "setPosition: position: " + MyGLRenderer.position);
            }
        }
    }


    public void updateAngle(float displacement) {
        Log.d(TAG, "updateAngle: displacement:" + displacement);
        if(angleRectangle + (displacement) * ( 90 / screenWidth) <= 0){
            angleRectangle = angleRectangle + (displacement) * ( 90 / screenWidth);
            Log.d(TAG, "updateAngle: angle:" + angleRectangle);
        }
    }

    public float getStartPositionX() {
        return startPositionX;
    }

    public void setStartPositionX(float positionX) {
        MyGLRenderer.startPositionX = positionX;
    }

    public float getDx() {
        return dx;
    }

    public void setDx(float dx) {
    }

    public boolean isStopped() {
//        Log.d(TAG, "isStopped: " + mStop);
        return mStop;
    }

    public void setStopped(boolean stop) {
//        Log.d(TAG, "setStopped: " + stop);
        mStop = stop;
        endPositionX = getPosition();
        MyGLRenderer.position = screenWidth / 2;
    }


//    ArrayList<UserStories> mMedia = new ArrayList<>();
    private JSONArray mUserStories = new JSONArray();

    public MyGLRenderer(Context context, float height, float width, JSONArray userStories, MyGLSurfaceView mGLView, int resourceIndex) throws JSONException{
        mContext = context;
        this.screenRatio = height / width;
        this.screenHeight = height;
        this.screenWidth = width;
        this.mGLView = mGLView;
        mUserStories = userStories;
//        this.mMedia = media;
        this.mStartingResourceIndex = resourceIndex;
//        mNumResources = media.size();
        mNumResources = mUserStories.length();
        mRelativeLayout = ((Activity) mContext).findViewById(R.id.parent_layout);
        mLinearLayout = ((Activity)mContext).findViewById(R.id.linLayout);
        mLinearLayout2 = ((Activity)mContext).findViewById(R.id.linLayout2);
        initProgressBar();
        initPlayers();

        int indices = mNumResources;
        if(mNumResources <= 4){
            indices = 4;
        }
        for(int i = 0; i < indices; i++){
            mResources.add(i, null);
            try{
//                int numMediaIndices = mMedia.get(i).getMedia().size();
                int numMediaIndices = mUserStories.getJSONObject(i).getJSONArray(mContext.getString(R.string.user_stories)).length();
                if(numMediaIndices > mHighestNumberMedia){
                    mHighestNumberMedia = numMediaIndices;
                }
            }catch (NullPointerException e){
                e.printStackTrace();
                break;
            }
            catch (JSONException e){
                e.printStackTrace();
            }

        }

//        if(mMedia.get(0).getMedia() != null){
        if(mUserStories.getJSONObject(0).getJSONArray(mContext.getString(R.string.user_stories)) != null){
            initResourceIndices();
            initDefaultImage();

            rotateToStartingIndex();
            mAngleFinished = -90 * (mNumResources - 1);
            initBlock();
        }
        else{
            Toast.makeText(mContext, "there is no media.", Toast.LENGTH_SHORT).show();
            ((Activity) mContext).finish();
        }

    }

    private float[][] rotationMatrix = {
            {0, -1, 2, 1},
            {1, 0, -1, 2},
            {2, 1, 0, -1},
            {-1, 2, 1, 0}
    };

    private void rotateToStartingIndex(){
        Log.d(TAG, "rotateToStartingIndex: rotating to starting index.");

        Log.d(TAG, "rotateToStartingIndex: starting resource index: " + mStartingResourceIndex);
        angleRectangle = -90 * mStartingResourceIndex;
        settledAngle = angleRectangle;
        mNumRotations = (int) Math.abs(angleRectangle / 90);
        Log.d(TAG, "rotateToStartingIndex: rotating block to angle: " + angleRectangle);
        Log.d(TAG, "rotateToStartingIndex: settled angle: " + settledAngle);
        Log.d(TAG, "rotateToStartingIndex: number rotations: " + mNumRotations);
        try{

            int fullBlockRotations = (int) mNumRotations / 4;
            Log.d(TAG, "rotateToStartingIndex: full block rotations: " + fullBlockRotations );
            int startingRotationIndex = mNumRotations - (fullBlockRotations * 4);
            Log.d(TAG, "rotateToStartingIndex: starting rotation index: " + startingRotationIndex);

            JSONObject object1 = mResourceIndices.getJSONObject(SURFACE_1);
            object1.put(mContext.getString(R.string.rotations), rotationMatrix[startingRotationIndex][0]);
            if(rotationMatrix[startingRotationIndex][0] == 0){
                mCurrentSurface = SURFACE_1;
                Log.d(TAG, "rotateToStartingIndex: current surface: " + 1);
            }
            Log.d(TAG, "rotateToStartingIndex: surface1 starting rotation index: " + rotationMatrix[startingRotationIndex][0]);
            mResourceIndices.put(SURFACE_1, object1);

            JSONObject object2 = mResourceIndices.getJSONObject(SURFACE_2);
            object2.put(mContext.getString(R.string.rotations), rotationMatrix[startingRotationIndex][1]);
            if(rotationMatrix[startingRotationIndex][1] == 0){
                mCurrentSurface = SURFACE_2;
                Log.d(TAG, "rotateToStartingIndex: current surface: " + 2);
            }
            Log.d(TAG, "rotateToStartingIndex: surface2 starting rotation index: " + rotationMatrix[startingRotationIndex][1]);
            mResourceIndices.put(SURFACE_2, object2);

            JSONObject object3 = mResourceIndices.getJSONObject(SURFACE_3);
            object3.put(mContext.getString(R.string.rotations), rotationMatrix[startingRotationIndex][2]);
            if(rotationMatrix[startingRotationIndex][2] == 0){
                mCurrentSurface = SURFACE_3;
                Log.d(TAG, "rotateToStartingIndex: current surface: " + 3);
            }
            Log.d(TAG, "rotateToStartingIndex: surface3 starting rotation index: " + rotationMatrix[startingRotationIndex][2]);
            mResourceIndices.put(SURFACE_3, object3);

            JSONObject object4 = mResourceIndices.getJSONObject(SURFACE_4);
            object4.put(mContext.getString(R.string.rotations), rotationMatrix[startingRotationIndex][3]);
            if(rotationMatrix[startingRotationIndex][3] == 0){
                mCurrentSurface = SURFACE_4;
                Log.d(TAG, "rotateToStartingIndex: current surface: " + 4);
            }
            Log.d(TAG, "rotateToStartingIndex: surface4 starting rotation index: " + rotationMatrix[startingRotationIndex][3]);
            mResourceIndices.put(SURFACE_4, object4);


        }catch (JSONException e){
            e.printStackTrace();
        }

        //init the first 4 surfaces
        if(mNumRotations > 0){
            try{

                int surface1MediaIndexMultiple = (int) mNumRotations / 3;
                Log.d(TAG, "rotateToStartingIndex: surface1MediaIndexMultiple: " + surface1MediaIndexMultiple);
    //            if(0 + (4 * surface1MediaIndexMultiple) < mMedia.size()){
                if(0 + (4 * surface1MediaIndexMultiple) < mNumResources){
    //                getMedia(mMedia.get(0 + (4 * surface1MediaIndexMultiple)).getMedia(), 0); // 0
                    getMedia(mUserStories.getJSONObject(0 + (4 * surface1MediaIndexMultiple))
                            .getJSONArray(mContext.getString(R.string.user_stories)), 0); // 0
                }
                else{
//                    getMedia(mMedia.get(0 + (4 * (surface1MediaIndexMultiple - 1))).getMedia(), 0); // 0
                    getMedia(mUserStories.getJSONObject(0 + (4 * (surface1MediaIndexMultiple - 1)))
                            .getJSONArray(mContext.getString(R.string.user_stories)), 0); // 0
                }

                int surface2MediaIndexMultiple = (int) (mNumRotations - 1) / 3;
                Log.d(TAG, "rotateToStartingIndex: surface2MediaIndexMultiple: " + surface2MediaIndexMultiple);
                if((1 + (4 * surface2MediaIndexMultiple)) < mNumResources){
//                    getMedia(mMedia.get(1 + (4 * surface2MediaIndexMultiple)).getMedia(), 1); // 1
                    getMedia(mUserStories.getJSONObject(1 + (4 * surface2MediaIndexMultiple))
                            .getJSONArray(mContext.getString(R.string.user_stories)), 1);
                }
                else{
//                    getMedia(mMedia.get(1 + (4 * (surface2MediaIndexMultiple - 1))).getMedia(), 1); // 1
                    getMedia(mUserStories.getJSONObject(1 + (4 * (surface2MediaIndexMultiple - 1)))
                            .getJSONArray(mContext.getString(R.string.user_stories)), 1);
                }

                int surface3MediaIndexMultiple = (int) (mNumRotations - 2) / 3;
                Log.d(TAG, "rotateToStartingIndex: surface3MediaIndexMultiple: " + surface3MediaIndexMultiple);
                if((2 + (4 * surface3MediaIndexMultiple)) < mNumResources){
//                    getMedia(mMedia.get(2 + (4 * surface3MediaIndexMultiple)).getMedia(), 2); // 2
                    getMedia(mUserStories.getJSONObject(2 + (4 * surface3MediaIndexMultiple))
                            .getJSONArray(mContext.getString(R.string.user_stories)), 2);
                }
                else{
//                    getMedia(mMedia.get(2 + (4 * (surface3MediaIndexMultiple - 1))).getMedia(), 2); // 2
                    getMedia(mUserStories.getJSONObject(2 + (4 * (surface3MediaIndexMultiple - 1)))
                            .getJSONArray(mContext.getString(R.string.user_stories)), 2);
                }

                int surface4MediaIndexMultiple = (int) (mNumRotations - 3) / 3;
                Log.d(TAG, "rotateToStartingIndex: surface4MediaIndexMultiple: " + surface4MediaIndexMultiple);
                if((3 + (4 * surface4MediaIndexMultiple)) < mNumResources){
//                    getMedia(mMedia.get(3 + (4 * surface4MediaIndexMultiple)).getMedia(), 3); // 3
                    getMedia(mUserStories.getJSONObject(3 + (4 * surface3MediaIndexMultiple ))
                            .getJSONArray(mContext.getString(R.string.user_stories)), 3);
                }
                else{
//                    getMedia(mMedia.get(3 + (4 * (surface4MediaIndexMultiple - 1))).getMedia(), 3); // 3
                    getMedia(mUserStories.getJSONObject(3 + (4 * (surface3MediaIndexMultiple - 1)))
                            .getJSONArray(mContext.getString(R.string.user_stories)), 3);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
        else if(mNumRotations == 0){
            try{
//                getMedia(mMedia.get(0).getMedia(), 0); // 0
//                getMedia(mMedia.get(1).getMedia(), 1); // 1
//                getMedia(mMedia.get(2).getMedia(), 2); // 2
//                getMedia(mMedia.get(3).getMedia(), 3); // 3

                getMedia(mUserStories.getJSONObject(0).getJSONArray(mContext.getString(R.string.user_stories)), 0); // 0
                getMedia(mUserStories.getJSONObject(1).getJSONArray(mContext.getString(R.string.user_stories)), 1); // 1
                getMedia(mUserStories.getJSONObject(2).getJSONArray(mContext.getString(R.string.user_stories)), 2); // 2
                getMedia(mUserStories.getJSONObject(3).getJSONArray(mContext.getString(R.string.user_stories)), 3); // 3

            }catch (JSONException e){
                e.printStackTrace();
            }
        }


    }

    private void initDefaultImage(){
        Log.d(TAG, "initDefaultImage: preparing default image scale factors.");
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.android_construction);
        float imageHeight = bitmap.getHeight();
        float imageWidth = bitmap.getWidth();
        float heightScaleFactor = 1f;
        float widthScaleFactor = 1f;

        Log.d(TAG, "initDefaultImage: IMAGE WIDTH: " + imageWidth);
        Log.d(TAG, "initDefaultImage: IMAGE HEIGHT: " + imageHeight);

        if (imageWidth > imageHeight) {
            //scale the height to match the width (#1)
            heightScaleFactor = (widthScaleFactor * imageHeight) / (screenRatio * imageWidth);
        } else if (imageHeight > imageWidth) {
            if (imageWidth < screenWidth) {
                //scale the width to match the height (#2)
//                                        widthScaleFactor = (imageWidth / imageHeight) * screenRatio;
                if (screenWidth / imageWidth < 2) {
                    heightScaleFactor = 2 - (screenWidth / imageWidth);
                }
            } else if (imageWidth > screenWidth) {
                // This one doesn't work for all cases. But the image sizes are so weird
                // that we shouldn't have to worry about it
                heightScaleFactor = (widthScaleFactor * imageHeight) / (screenRatio * imageWidth);
            }
        }
        mDefaultImageWidthScaleFactor = widthScaleFactor;
        mDefaultImageHeightScaleFactor = heightScaleFactor;
        Log.d(TAG, "initDefaultImage: WSF: " + widthScaleFactor);
        Log.d(TAG, "initDefaultImage: HSF: " + heightScaleFactor);
    }



    private void initResourceIndices() {

        try{
            JSONObject resource = new JSONObject();
            resource.put(mContext.getString(R.string.surface_number), SURFACE_1);
            resource.put(mContext.getString(R.string.rotations), 0);
            resource.put(mContext.getString(R.string.resource_index), 0);
            mResourceIndices.put(SURFACE_1, resource);
            JSONArray mediaIndexArray1 = new JSONArray();
            int mediaIndex = 0;
            //populate a bunch of media indices for each potential media.
            for(int i = 0; i < mHighestNumberMedia; i++){
                JSONObject mediaIndexObject = new JSONObject();
                mediaIndexObject.put(mContext.getString(R.string.media_index), mediaIndex);
                mediaIndexArray1.put(i, mediaIndexObject);
            }
            resource.put(mContext.getString(R.string.media_index), mediaIndexArray1);
            mResourceIndices.put(SURFACE_1).put(resource);
            Log.d(TAG, "initResourceIndices: resourceIndices1: " + mResourceIndices.get(SURFACE_1));

            resource = new JSONObject();
            resource.put(mContext.getString(R.string.surface_number), SURFACE_2);
            resource.put(mContext.getString(R.string.rotations), -1);
            resource.put(mContext.getString(R.string.resource_index), 1);
            mResourceIndices.put(SURFACE_2, resource);
            JSONArray mediaIndexArray2 = new JSONArray();
            //populate a bunch of media indices for each potential media.
            for(int i = 0; i < mHighestNumberMedia; i++){
                JSONObject mediaIndexObject = new JSONObject();
                mediaIndexObject.put(mContext.getString(R.string.media_index), mediaIndex);
                mediaIndexArray2.put(i, mediaIndexObject);
            }
            resource.put(mContext.getString(R.string.media_index), mediaIndexArray2);
            mResourceIndices.put(SURFACE_2).put(resource);
            Log.d(TAG, "initResourceIndices: resourceIndices2: " + mResourceIndices.get(SURFACE_2));

            resource = new JSONObject();
            resource.put(mContext.getString(R.string.surface_number), SURFACE_3);
            resource.put(mContext.getString(R.string.rotations), 2);
            resource.put(mContext.getString(R.string.resource_index), 2);
            mResourceIndices.put(SURFACE_3, resource);
            JSONArray mediaIndexArray3 = new JSONArray();
            //populate a bunch of media indices for each potential media.
            for(int i = 0; i < mHighestNumberMedia; i++){
                JSONObject mediaIndexObject = new JSONObject();
                mediaIndexObject.put(mContext.getString(R.string.media_index), mediaIndex);
                mediaIndexArray3.put(i, mediaIndexObject);
            }
            resource.put(mContext.getString(R.string.media_index), mediaIndexArray3);
            mResourceIndices.put(SURFACE_3).put(resource);
            Log.d(TAG, "initResourceIndices: resourceIndices3: " + mResourceIndices.get(SURFACE_3));

            resource = new JSONObject();
            resource.put(mContext.getString(R.string.surface_number), SURFACE_4);
            resource.put(mContext.getString(R.string.rotations), 1);
            resource.put(mContext.getString(R.string.resource_index), 3);
            mResourceIndices.put(SURFACE_4, resource);
            JSONArray mediaIndexArray4 = new JSONArray();
            //populate a bunch of media indices for each potential media.
            for(int i = 0; i < mHighestNumberMedia; i++){
                JSONObject mediaIndexObject = new JSONObject();
                mediaIndexObject.put(mContext.getString(R.string.media_index), mediaIndex);
                mediaIndexArray4.put(i, mediaIndexObject);
            }
            resource.put(mContext.getString(R.string.media_index), mediaIndexArray4);
            mResourceIndices.put(SURFACE_4).put(resource);
            Log.d(TAG, "initResourceIndices: resourceIndices4: " + mResourceIndices.get(SURFACE_4));

        }catch (JSONException e){
            Log.e(TAG, "initResourceIndices: JSONException: " + e.getMessage() );
        }

    }

    private boolean isMediaVideo(String uri){
        if(uri.contains(".mp4") || uri.contains(".wmv") || uri.contains(".flv") || uri.contains(".avi")){
            return true;
        }
        return false;
    }

//    private void getMedia(final ArrayList<Story> mediaSource, final int surfaceIndex){
    private void getMedia(final JSONArray mediaSource, final int surfaceIndex){
        Log.d(TAG, "getMedia: getting images from urls");

        Log.d(TAG, "getMedia: media source length: " + mediaSource.length());
        Log.d(TAG, "getMedia: getting media for surface index: " + surfaceIndex);
//        if(mResources.get(surfaceIndex) == null) {
            for(int i = 0; i < mediaSource.length(); i++) {

                try{
                    final int count = i;
                    String videoUri = "";
                    try{
                        videoUri = mediaSource.getJSONObject(count).get(mContext.getString(R.string.field_video_uri)).toString();
                    }catch (JSONException e){
                        e.printStackTrace();
                        videoUri = "";
                    }
//                    if (isMediaVideo(videoUri)) {
                    if (!videoUri.equals("")) {
                        JSONObject object = new JSONObject();
                        try {
                            object.put(mContext.getString(R.string.media_type), mContext.getString(R.string.video_uri));
                            object.put(mContext.getString(R.string.video_uri), videoUri);
                            object.put(mContext.getString(R.string.media_source), buildMediaSource(Uri.parse(videoUri)));
                            object.put(mContext.getString(R.string.duration), mediaSource.getJSONObject(count).get(mContext.getString(R.string.field_duration)));
                            Log.d(TAG, "getMedia: duration: " + mediaSource.getJSONObject(count).get(mContext.getString(R.string.field_duration)));
                            try{
                                JSONArray jsonArray = mResources.get(surfaceIndex);
                                jsonArray.put(count, object);
                                mResources.set(surfaceIndex, jsonArray);
                                Log.d(TAG, "getMedia: RESOURCES FOR " + (surfaceIndex + 1) + ": " + mResources.get(surfaceIndex));
                                Log.d(TAG, "getMedia: setting video for surface " + (surfaceIndex + 1) + ", " + object.get(mContext.getString(R.string.media_type)));
                                Log.d(TAG, "getMedia: setting video for surface " + (surfaceIndex + 1) + ", " + + count);
                            }catch (NullPointerException e) {
                                Log.e(TAG, "onResourceReady: First video added to resources array. " + e.getMessage() );
                                JSONArray jsonArray = new JSONArray();
                                jsonArray.put(count, object);
                                mResources.set(surfaceIndex, jsonArray);
                                Log.d(TAG, "getMedia: RESOURCES FOR " + (surfaceIndex + 1) + ": " + mResources.get(surfaceIndex));
                                Log.d(TAG, "getMedia: * setting video for surface " + (surfaceIndex + 1) + ", " + object.get(mContext.getString(R.string.media_type)));
                                Log.d(TAG, "getMedia: * setting video for surface " + (surfaceIndex + 1) + ", " + + count);
                            }
                            if(surfaceIndex == SURFACE_1){
                                mVideo1Index++;
                            }
                            else if(surfaceIndex == SURFACE_2){
                                mVideo2Index++;
                            }
                            else if(surfaceIndex == SURFACE_3){
                                mVideo3Index++;
                            }
                            else if(surfaceIndex == SURFACE_4){
                                mVideo4Index++;
                            }
                            if(mVideo1Index == 1 || mVideo2Index == 1 || mVideo3Index == 1 || mVideo4Index == 1){
                                Log.d(TAG, "getMedia: buffering first video for surface " + (surfaceIndex + 1));
                                bufferFirstVideo(surfaceIndex, count);
                            }
                            else if(mVideo1Index == 2 || mVideo2Index == 2 || mVideo3Index == 2 || mVideo4Index == 2){
                                Log.d(TAG, "getMedia: buffering second video for surface " + (surfaceIndex + 1));
                                bufferNextVideo(surfaceIndex);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {

                        SimpleTarget target = new SimpleTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(Bitmap bitmap, Transition transition) {
                                Log.d(TAG, "getMedia: new bitmap ready.");


                                float imageHeight = bitmap.getHeight();
                                float imageWidth = bitmap.getWidth();
                                float heightScaleFactor = 1f;
                                float widthScaleFactor = 1f;

    //                            Log.d(TAG, "getMedia: IMAGE WIDTH: " + imageWidth);
    //                            Log.d(TAG, "getMedia: IMAGE HEIGHT: " + imageHeight);

                                if (imageWidth > imageHeight) {
                                    //scale the height to match the width (#1)
                                    heightScaleFactor = (widthScaleFactor * imageHeight) / (screenRatio * imageWidth);
                                } else if (imageHeight > imageWidth) {
                                    if (imageWidth < screenWidth) {
                                        //scale the width to match the height (#2)
    //                                        widthScaleFactor = (imageWidth / imageHeight) * screenRatio;
                                        if (screenWidth / imageWidth < 2) {
                                            heightScaleFactor = 2 - (screenWidth / imageWidth);
                                        }
                                    } else if (imageWidth > screenWidth) {
                                        // This one doesn't work for all cases. But the image sizes are so weird
                                        // that we shouldn't have to worry about it
                                        heightScaleFactor = (widthScaleFactor * imageHeight) / (screenRatio * imageWidth);
                                    }
                                }

    //                            Log.d(TAG, "getMedia: count: " + count );
    //                            Log.d(TAG, "getMedia: wsf, hsf: " + widthScaleFactor + ", " + heightScaleFactor);

                                JSONObject object = new JSONObject();
                                try {
                                    // Log.d(TAG, "getMedia: getting " + count + " resource.");
                                    object.put(mContext.getString(R.string.media_type), mContext.getString(R.string.encoded_bitmap));
    //                                object.put(mContext.getString(R.string.encoded_bitmap), encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 100));
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
                                    object.put(mContext.getString(R.string.encoded_bitmap), compressedBitmap);
                                    object.put(mContext.getString(R.string.width_scale_factor), widthScaleFactor);
                                    object.put(mContext.getString(R.string.height_scale_factor), heightScaleFactor);

                                    try{
                                        JSONArray jsonArray = mResources.get(surfaceIndex);
                                        jsonArray.put(count, object);
                                        mResources.set(surfaceIndex, jsonArray);
                                        Log.d(TAG, "onResourceReady: RESOURCES FOR " + (surfaceIndex + 1) + ": " + mResources.get(surfaceIndex));
                                        Log.d(TAG, "onResourceReady: setting image for surface " + (surfaceIndex + 1) + ", " + object.get(mContext.getString(R.string.media_type)));
                                        Log.d(TAG, "onResourceReady: setting image for surface " + (surfaceIndex + 1) + ", " + + count);
                                    }catch (NullPointerException e) {
                                        Log.e(TAG, "onResourceReady: First photo added to resources array." + e.getMessage() );
                                        e.printStackTrace();
                                        JSONArray jsonArray = new JSONArray();
                                        jsonArray.put(count, object);
                                        mResources.set(surfaceIndex, jsonArray);
                                        Log.d(TAG, "onResourceReady: RESOURCES FOR " + (surfaceIndex + 1) + ": " + mResources.get(surfaceIndex));
                                        Log.d(TAG, "onResourceReady: * setting image for surface " + (surfaceIndex + 1) + ", " + object.get(mContext.getString(R.string.media_type)));
                                        Log.d(TAG, "onResourceReady: * setting image for surface " + (surfaceIndex + 1) + ", " + + count);
                                    }
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        String imageUri = mediaSource.getJSONObject(count).get(mContext.getString(R.string.field_image_uri)).toString();
                        Glide.with(mContext.getApplicationContext())
                                .asBitmap()
                                .load(imageUri)
                                .into(target);
                        mTargets.add(target);
                    }
    //                printCurrentResources();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
//        }

    }


    private void setImageToIndex(GL10 gl, int surfaceNumber, boolean imageRenderError){

//        if(surfaceNumber == mCurrentSurface){
//            hideProgressBar();
//        }
        try {
            final int resourceIndex = mResourceIndices.getJSONObject(surfaceNumber).getInt(mContext.getString(R.string.resource_index));
            final int mediaIndex = mResourceIndices.getJSONObject(surfaceNumber).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject(resourceIndex / 4).getInt(mContext.getString(R.string.media_index));

            if(mediaIndex >= 0){
                String resourceType = "";
                try{
                    resourceType = mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.media_type)).toString();
                }catch (JSONException e){
//                    Log.e(TAG, "setImageToIndex: JSONException: " + e.getMessage() );
                    resourceType = mContext.getString(R.string.image_load_error);
                }
                catch (NullPointerException e) {
                    Log.e(TAG, "setImageToIndex: NullPointerException: " + e.getMessage());
                    resourceType = mContext.getString(R.string.image_load_error);
                }
                if(resourceType.equals(mContext.getString(R.string.image_load_error)) || resourceType.equals(mContext.getString(R.string.encoded_bitmap))){
                    if(!isImage1Set && surfaceNumber == SURFACE_1) {
                        Log.d(TAG, "setImageToIndex: setting image to surface: " + (SURFACE_1 + 1) + ", Media Index: " + mediaIndex);

                        printCurrentResources();
                        if(mCurrentSurface == SURFACE_1 && mediaIndex == 0){
                            initProgressBars();
                        }

                        gl.glEnable(GL10.GL_TEXTURE_2D);
                        gl.glGenTextures(1, textureId1, 0);
                        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId1[0]);
                        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
                        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

//                            Bitmap bitmap = decodeBase64(mResources.get(mResourceIndices[0][0][0]).getJSONObject(mMediaIndex).get(mContext.getString(R.string.encoded_bitmap)).toString());
                        Bitmap bitmap = null;
                        if(imageRenderError){
                            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.android_construction);
                            Log.d(TAG, "setImageToIndex: setting default image to surface 1.");
                        }
                        else{
                            Log.d(TAG, "setImageToIndex: setting resource image to surface 1.");
//                            bitmap = decodeBase64(mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.encoded_bitmap)).toString());
                            bitmap = (Bitmap) mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.encoded_bitmap));
                            isImage1Set = true;
                        }
                        Log.d(TAG, "setImageToIndex: image1 media index: " + mediaIndex);
                        Log.d(TAG, "setImageToIndex: image1 bitmap: " + bitmap);
                        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

                    }
                    if(!isImage2Set &&  surfaceNumber == SURFACE_2) {
                        Log.d(TAG, "setImageToIndex: setting image to surface: " + (SURFACE_2 + 1) + ", Media Index: " + mediaIndex);

                        if(mCurrentSurface == SURFACE_2 && mediaIndex == 0){
                            initProgressBars();
                        }

                        gl.glEnable(GL10.GL_TEXTURE_2D);
                        gl.glGenTextures(1, textureId2, 0);
                        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId2[0]);
                        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
                        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

//                            Bitmap bitmap = decodeBase64(mResources.get(mResourceIndices[0][0][0]).getJSONObject(mMediaIndex).get(mContext.getString(R.string.encoded_bitmap)).toString());
                        Bitmap bitmap = null;
                        if(imageRenderError){
                            bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                    R.drawable.android_construction);
                        }
                        else{
//                            bitmap = decodeBase64(mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.encoded_bitmap)).toString());
                            bitmap = (Bitmap) mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.encoded_bitmap));
                            isImage2Set = true;
                        }
                        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);


                    }
                    if(!isImage3Set &&  surfaceNumber == SURFACE_3) {
                        Log.d(TAG, "setImageToIndex: setting image to surface: " + (SURFACE_3 + 1) + ", Media Index: " + mediaIndex);

                        if(mCurrentSurface == SURFACE_3 && mediaIndex == 0){
                            initProgressBars();
                        }

                        gl.glEnable(GL10.GL_TEXTURE_2D);
                        gl.glGenTextures(1, textureId3, 0);
                        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId3[0]);
                        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
                        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

//                            Bitmap bitmap = decodeBase64(mResources.get(mResourceIndices[0][0][0]).getJSONObject(mMediaIndex).get(mContext.getString(R.string.encoded_bitmap)).toString());
                        Bitmap bitmap = null;
                        if(imageRenderError){
                            bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                    R.drawable.android_construction);
                        }
                        else{
//                            bitmap = decodeBase64(mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.encoded_bitmap)).toString());
                            bitmap = (Bitmap) mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.encoded_bitmap));
                            isImage3Set = true;
                        }
                        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

                    }
                    if(!isImage4Set &&  surfaceNumber == SURFACE_4) {
                        Log.d(TAG, "setImageToIndex: setting image to surface: " + (SURFACE_4 + 1) + ", Media Index: " + mediaIndex);

                        if(mCurrentSurface == SURFACE_4 && mediaIndex == 0){
                            initProgressBars();
                        }

                        gl.glEnable(GL10.GL_TEXTURE_2D);
                        gl.glGenTextures(1, textureId4, 0);
                        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId4[0]);
                        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
                        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

//                            Bitmap bitmap = decodeBase64(mResources.get(mResourceIndices[0][0][0]).getJSONObject(mMediaIndex).get(mContext.getString(R.string.encoded_bitmap)).toString());
                        Bitmap bitmap = null;
                        if(imageRenderError){
                            bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                    R.drawable.android_construction);
                        }
                        else {
//                            bitmap = decodeBase64(mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.encoded_bitmap)).toString());
                            bitmap = (Bitmap) mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.encoded_bitmap));
                            isImage4Set = true;
                        }
                        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);


                    }

                }
            //scale the media
//                gl.glScalef(mScaleFactors[mSurfaceIndex][mMediaIndex][0], mScaleFactors[mSurfaceIndex][mMediaIndex][1], 1);
//                gl.glScalef(mScaleFactors[mSurfaceIndex][mMediaIndices[mSurfaceIndex][0]][0], mScaleFactors[mSurfaceIndex][mMediaIndices[mSurfaceIndex][0]][1], 1);
        }
        } catch (NullPointerException e) {
            Log.e(TAG, "setImageToIndex: NullPointerException: " + e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void onDrawFrame(GL10 gl) {

        // Draw background color
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);


//        Log.d(TAG, "onDrawFrame: angle: " + angleRectangle);
        setDepth(gl);
        if (Math.abs(startPositionX - endPositionX) > (screenWidth / 2) && isStopped() && isRotationEnabled && !mRotateCounterClockwise && !mRotateClockwise) {
            setStopped(false);
            if (startPositionX > endPositionX && mAllowRotationClockwise) {
                mRotateClockwise = true;
            } else if (startPositionX < endPositionX) {
                mRotateCounterClockwise = true;
            }
        } else if (Math.abs(startPositionX - endPositionX) < (screenWidth / 2) && isStopped() && isRotationEnabled && !mRotateCounterClockwise && !mRotateClockwise) {
            setStopped(false);
            if (startPositionX > endPositionX) {
                mRotateCounterClockwise = true;
            } else if (startPositionX < endPositionX && mAllowRotationClockwise) {
                mRotateClockwise = true;
            }
        } else if (!mRotateCounterClockwise && !mRotateClockwise && !isStopped()) {
//            Log.d(TAG, "onDrawFrame: THIS ONE" + angleRectangle);
            gl.glRotatef(angleRectangle, 0.0f, -1.0f, 0.0f);
        }

        if (mRotateClockwise && mAllowRotationClockwise) {
//            Log.d(TAG, "onDrawFrame: OR THIS ONE" + angleRectangle);
            rotateClockwise(gl);
        } else if (mRotateCounterClockwise) {
//            Log.d(TAG, "onDrawFrame: MAYBE THIS ONE: " + angleRectangle);
            rotateCounterClockwise(gl);
        }

        //////////////////////////////
        //surface 1 logic
        try {
            final int surface1ResourceIndex = mResourceIndices.getJSONObject(SURFACE_1).getInt(mContext.getString(R.string.resource_index));
            final int surface1MediaIndex = mResourceIndices.getJSONObject(SURFACE_1).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject(surface1ResourceIndex / 4).getInt(mContext.getString(R.string.media_index));
//            Log.d(TAG, "onDrawFrame: surface1 media index: " + surface1MediaIndex);
//            Log.d(TAG, "onDrawFrame: surface1 resource index: " + surface1ResourceIndex);
//            Log.d(TAG, "onDrawFrame: surface1 num rotations: " + surface1NumRotations);
            if (surface1MediaIndex >= 0) {
                String resourceType = "";
                boolean imageRenderError = false;
                try {
                    resourceType = mResources.get(surface1ResourceIndex).getJSONObject(surface1MediaIndex).get(mContext.getString(R.string.media_type)).toString();
                } catch (JSONException e) {
//                    Log.e(TAG, "onDrawFrame: error getting bitmap: " + e.getMessage() );
                    imageRenderError = true;
                }

                if (resourceType.equals(mContext.getString(R.string.encoded_bitmap)) || imageRenderError) {
//                    Log.d(TAG, "onDrawFrame: rendering SURFACE 1 image");

                    float widthScaleFactor = 1f;
                    float heightScaleFactor = 1f;
                    if (imageRenderError && !isImage1Set) {
                        widthScaleFactor = mDefaultImageWidthScaleFactor;
                        heightScaleFactor = mDefaultImageHeightScaleFactor;
                    } else {
                        widthScaleFactor = Float.valueOf(String.valueOf(mResources.get(surface1ResourceIndex).getJSONObject(surface1MediaIndex).get(mContext.getString(R.string.width_scale_factor))));
                        heightScaleFactor = Float.valueOf(String.valueOf(mResources.get(surface1ResourceIndex).getJSONObject(surface1MediaIndex).get(mContext.getString(R.string.height_scale_factor))));
                    }

                    // Define the vertices for this face
                    float[] imageVertices = {
                            widthMatrix[SURFACE_1][0] * widthScaleFactor, heightMatrix[SURFACE_1][0] * heightScaleFactor, depthMatrix[SURFACE_1][0],
                            widthMatrix[SURFACE_1][1] * widthScaleFactor, heightMatrix[SURFACE_1][1] * heightScaleFactor, depthMatrix[SURFACE_1][1],
                            widthMatrix[SURFACE_1][2] * widthScaleFactor, heightMatrix[SURFACE_1][2] * heightScaleFactor, depthMatrix[SURFACE_1][2],
                            widthMatrix[SURFACE_1][3] * widthScaleFactor, heightMatrix[SURFACE_1][3] * heightScaleFactor, depthMatrix[SURFACE_1][3],
                    };
                    if (vertexBuffer1 != null) {
                        vertexBuffer1.clear();
                    }
                    ByteBuffer vbb = ByteBuffer.allocateDirect(imageVertices.length * 6 * 4);
                    vbb.order(ByteOrder.nativeOrder());
                    vertexBuffer1 = vbb.asFloatBuffer();

                    vertexBuffer1.put(imageVertices);
                    vertexBuffer1.position(0);

                    //surface1 image media
                    gl.glLoadIdentity();
                    gl.glEnable(GL10.GL_TEXTURE_2D); //ENABLE IMAGE TEXTURES

                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer1);

                    setImageToIndex(gl, SURFACE_1, imageRenderError);

                    gl.glTranslatef(0, 0, depth);
                    gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer1);

                    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
                    gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId1[0]);
                    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);


                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glDisable(GL10.GL_TEXTURE_2D);


//                    if (mVertexBuffers[SURFACE_1][1] == null && widthScaleFactor != -1) {
                    if (widthScaleFactor != -1) {
//                        Log.d(TAG, "SCALE FACTOR: WSF: " + widthScaleFactor);
//                        Log.d(TAG, "SCALE FACTOR: HSF: " + heightScaleFactor);

                        float[] topVertices = {
                                widthMatrix[SURFACE_1][0] * widthScaleFactor, screenRatio * heightScaleFactor, depthMatrix[SURFACE_1][0],
                                widthMatrix[SURFACE_1][1] * widthScaleFactor, screenRatio * heightScaleFactor, depthMatrix[SURFACE_1][1],
                                widthMatrix[SURFACE_1][2] * widthScaleFactor, heightMatrix[SURFACE_1][2], depthMatrix[SURFACE_1][2],
                                widthMatrix[SURFACE_1][3] * widthScaleFactor, heightMatrix[SURFACE_1][3], depthMatrix[SURFACE_1][3],
                        };

                        vbb = ByteBuffer.allocateDirect(topVertices.length * 6 * 4);
                        vbb.order(ByteOrder.nativeOrder());
                        FloatBuffer bufferTop = vbb.asFloatBuffer();
                        mVertexBuffers[SURFACE_1][1] = bufferTop.put(topVertices);
                        mVertexBuffers[SURFACE_1][1].position(0);
                    }
//                    if (mVertexBuffers[SURFACE_1][2] == null && widthScaleFactor != -1) {
                    if (widthScaleFactor != -1) {
//                        Log.d(TAG, "SCALE FACTOR: WSF: " + widthScaleFactor);
//                        Log.d(TAG, "SCALE FACTOR: HSF: " + heightScaleFactor);

                        float[] botVertices = {
                                widthMatrix[SURFACE_1][0] * widthScaleFactor, heightMatrix[SURFACE_1][0], depthMatrix[SURFACE_1][0],
                                widthMatrix[SURFACE_1][1] * widthScaleFactor, heightMatrix[SURFACE_1][1], depthMatrix[SURFACE_1][1],
                                widthMatrix[SURFACE_1][2] * widthScaleFactor, -screenRatio * heightScaleFactor, depthMatrix[SURFACE_1][2],
                                widthMatrix[SURFACE_1][3] * widthScaleFactor, -screenRatio * heightScaleFactor, depthMatrix[SURFACE_1][3],
                        };

                        vbb = ByteBuffer.allocateDirect(botVertices.length * 6 * 4);
                        vbb.order(ByteOrder.nativeOrder());
                        FloatBuffer bufferBot = vbb.asFloatBuffer();
                        mVertexBuffers[SURFACE_1][2] = bufferBot.put(botVertices);
                        mVertexBuffers[SURFACE_1][2].position(0);
                    }


                    if (mVertexBuffers[SURFACE_1][2] != null) {
                        //surface1 bot
                        gl.glLoadIdentity();
                        gl.glDisable(GL10.GL_TEXTURE_2D); //DISABLE TEXTURE WHEN BUILDING FACES

                        gl.glTranslatef(0, 0, depth);
                        gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);

                        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffers[SURFACE_1][2]);

                        gl.glColor4f(0f, 0f, 0.0f, 1); //set the block black
                        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

                    }

                    if (mVertexBuffers[SURFACE_1][1] != null) {
                        //surface1 top
                        gl.glLoadIdentity();
                        gl.glDisable(GL10.GL_TEXTURE_2D); //DISABLE TEXTURE WHEN BUILDING FACES

                        gl.glTranslatef(0, 0, depth);
                        gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);

                        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffers[SURFACE_1][1]);

                        gl.glColor4f(0f, 0f, 0.0f, 1); //set the block black
                        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    }
                } else if (mResources.get(surface1ResourceIndex).getJSONObject(surface1MediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {

//                    Log.d(TAG, "onDrawFrame: rendering SURFACE 1 video");

                    // Define the vertices for this face
                    float[] imageVertices = {
                            widthMatrix[SURFACE_1][0], heightMatrix[SURFACE_1][0], depthMatrix[SURFACE_1][0],
                            widthMatrix[SURFACE_1][1], heightMatrix[SURFACE_1][1], depthMatrix[SURFACE_1][1],
                            widthMatrix[SURFACE_1][2], heightMatrix[SURFACE_1][2], depthMatrix[SURFACE_1][2],
                            widthMatrix[SURFACE_1][3], heightMatrix[SURFACE_1][3], depthMatrix[SURFACE_1][3],
                    };
                    if (vertexBuffer1 != null) {
                        vertexBuffer1.clear();
                    }
                    ByteBuffer vbb = ByteBuffer.allocateDirect(imageVertices.length * 6 * 4);
                    vbb.order(ByteOrder.nativeOrder());
                    vertexBuffer1 = vbb.asFloatBuffer();

                    vertexBuffer1.put(imageVertices);
                    vertexBuffer1.position(0);

                    if (mPlayerState == ACTIVE_PLAYER && !mPlayer.getPlayWhenReady() && mCurrentSurface == SURFACE_1) {
                        Log.d(TAG, "onDrawFrame: playing player 1");
                        mPlayer.setPlayWhenReady(true);
//                        retryPlayer = false;
//                        startProgressBar();
                    } else if (mSecondaryPlayerState == ACTIVE_PLAYER && !mSecondaryPlayer.getPlayWhenReady() && mCurrentSurface == SURFACE_1) {
                        Log.d(TAG, "onDrawFrame: playing secondary player 1");
                        mSecondaryPlayer.setPlayWhenReady(true);
//                        retrySecondaryPlayer = false;
//                        startProgressBar();
                    }


                    // surface 1 video media
                    gl.glLoadIdentity();
                    gl.glEnable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES); //ENABLE VIDEO TEXTURES

//                    if(initVideoTexture1){
//                        Log.d(TAG, "onDrawFrame: initializing video texture1.");
//                        gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId1[0]);
//                        gl.glGenTextures(1, textureId1 , 0);
//                        gl.glTexParameterf( GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT );
//                        gl.glTexParameterf( GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT );
//                        gl.glTexParameterf( GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST  );
//                        gl.glTexParameterf( GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR  );
//                        initVideoTexture1 = false;
//                    }
                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer1);

//                    gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId1[0]);

                    gl.glTranslatef(0, 0, depth);
                    gl.glRotatef(-(angleRectangle), 0.0f, -1.0f, 0.0f);
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer1);

//                    gl.glGenTextures(1, textureId1, 0);
//                    gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId1[0]); //causes error (doesn't crash)
//                    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
//                    gl.glTexEnvf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

                    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glDisable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES); //DISABLE VIDEO TEXTURES


                    if (mUpdateST) {
                        try {
                            //hideProgressBar();
                            mSurfaceTexture.updateTexImage();

                            Log.d(TAG, "onDrawFrame: updating surface1 frame");

                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onFrameAvailable: IllegalStateException surface1: " + e.getMessage());
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onFrameAvailable: RuntimeException surface1: " + e.getMessage());
                        }
                        mUpdateST = false;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
//            e.printStackTrace();
        }


        //////////////////////////////
        //surface 2 logic
        try {
            final int surface2ResourceIndex = mResourceIndices.getJSONObject(SURFACE_2).getInt(mContext.getString(R.string.resource_index));
            final int surface2MediaIndex = mResourceIndices.getJSONObject(SURFACE_2).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject(surface2ResourceIndex / 4).getInt(mContext.getString(R.string.media_index));
//            Log.d(TAG, "onDrawFrame: surface1 media index: " + surface1MediaIndex);
//            Log.d(TAG, "onDrawFrame: surface1 resource index: " + surface1ResourceIndex);
//            Log.d(TAG, "onDrawFrame: surface1 num rotations: " + surface1NumRotations);

            if (surface2MediaIndex >= 0) {
                String resourceType = "";
                boolean imageRenderError = false;
                try {
                    resourceType = mResources.get(surface2ResourceIndex).getJSONObject(surface2MediaIndex).get(mContext.getString(R.string.media_type)).toString();
                } catch (JSONException e) {
//                    Log.e(TAG, "onDrawFrame: error getting bitmap: " + e.getMessage() );
                    imageRenderError = true;
                }
                if (resourceType.equals(mContext.getString(R.string.encoded_bitmap)) || imageRenderError) {
//                    Log.d(TAG, "onDrawFrame: rendering surface 2 image");

                    float widthScaleFactor = 1f;
                    float heightScaleFactor = 1f;
                    if (imageRenderError) {
                        widthScaleFactor = mDefaultImageWidthScaleFactor;
                        heightScaleFactor = mDefaultImageHeightScaleFactor;
                    } else {
                        widthScaleFactor = Float.valueOf(String.valueOf(mResources.get(surface2ResourceIndex).getJSONObject(surface2MediaIndex).get(mContext.getString(R.string.width_scale_factor))));
                        heightScaleFactor = Float.valueOf(String.valueOf(mResources.get(surface2ResourceIndex).getJSONObject(surface2MediaIndex).get(mContext.getString(R.string.height_scale_factor))));
                    }
                    // Define the vertices for this face
                    float[] imageVertices = {
                            widthMatrix[SURFACE_2][0] * widthScaleFactor, heightMatrix[SURFACE_2][0] * heightScaleFactor, depthMatrix[SURFACE_2][0],
                            widthMatrix[SURFACE_2][1] * widthScaleFactor, heightMatrix[SURFACE_2][1] * heightScaleFactor, depthMatrix[SURFACE_2][1],
                            widthMatrix[SURFACE_2][2] * widthScaleFactor, heightMatrix[SURFACE_2][2] * heightScaleFactor, depthMatrix[SURFACE_2][2],
                            widthMatrix[SURFACE_2][3] * widthScaleFactor, heightMatrix[SURFACE_2][3] * heightScaleFactor, depthMatrix[SURFACE_2][3],
                    };
                    if (vertexBuffer2 != null) {
                        vertexBuffer2.clear();
                    }
                    ByteBuffer vbb = ByteBuffer.allocateDirect(imageVertices.length * 6 * 4);
                    vbb.order(ByteOrder.nativeOrder());
                    vertexBuffer2 = vbb.asFloatBuffer();

                    vertexBuffer2.put(imageVertices);
                    vertexBuffer2.position(0);

                    //surface2 image media
                    gl.glLoadIdentity();
                    gl.glEnable(GL10.GL_TEXTURE_2D); //ENABLE IMAGE TEXTURES

                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer2);

                    setImageToIndex(gl, SURFACE_2, imageRenderError);

                    gl.glTranslatef(0, 0, depth);
                    gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer2);


                    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
                    gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId2[0]);
                    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);


                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glDisable(GL10.GL_TEXTURE_2D);


//                    if (mVertexBuffers[SURFACE_2][1] == null && widthScaleFactor != -1) {
                    if (widthScaleFactor != -1) {
//                        Log.d(TAG, "SCALE FACTOR: WSF: " + widthScaleFactor);
//                        Log.d(TAG, "SCALE FACTOR: HSF: " + heightScaleFactor);

                        float[] topVertices = {
                                widthMatrix[SURFACE_2][0] * widthScaleFactor, screenRatio * heightScaleFactor, depthMatrix[SURFACE_2][0],
                                widthMatrix[SURFACE_2][1] * widthScaleFactor, screenRatio * heightScaleFactor, depthMatrix[SURFACE_2][1],
                                widthMatrix[SURFACE_2][2] * widthScaleFactor, heightMatrix[SURFACE_2][2], depthMatrix[SURFACE_2][2],
                                widthMatrix[SURFACE_2][3] * widthScaleFactor, heightMatrix[SURFACE_2][3], depthMatrix[SURFACE_2][3],
                        };
                        vbb = ByteBuffer.allocateDirect(topVertices.length * 6 * 4);
                        vbb.order(ByteOrder.nativeOrder());
                        FloatBuffer bufferTop = vbb.asFloatBuffer();
                        mVertexBuffers[SURFACE_2][1] = bufferTop.put(topVertices);
                        mVertexBuffers[SURFACE_2][1].position(0);
                    }
//                    if (mVertexBuffers[SURFACE_2][2] == null && widthScaleFactor != -1) {
                    if (widthScaleFactor != -1) {
//                        Log.d(TAG, "SCALE FACTOR: WSF: " + widthScaleFactor);
//                        Log.d(TAG, "SCALE FACTOR: HSF: " + heightScaleFactor);

                        float[] botVertices = {
                                widthMatrix[SURFACE_2][0] * widthScaleFactor, heightMatrix[SURFACE_2][0], depthMatrix[SURFACE_2][0],
                                widthMatrix[SURFACE_2][1] * widthScaleFactor, heightMatrix[SURFACE_2][1], depthMatrix[SURFACE_2][1],
                                widthMatrix[SURFACE_2][2] * widthScaleFactor, -screenRatio * heightScaleFactor, depthMatrix[SURFACE_2][2],
                                widthMatrix[SURFACE_2][3] * widthScaleFactor, -screenRatio * heightScaleFactor, depthMatrix[SURFACE_2][3],
                        };
                        vbb = ByteBuffer.allocateDirect(botVertices.length * 6 * 4);
                        vbb.order(ByteOrder.nativeOrder());
                        FloatBuffer bufferBot = vbb.asFloatBuffer();
                        mVertexBuffers[SURFACE_2][2] = bufferBot.put(botVertices);
                        mVertexBuffers[SURFACE_2][2].position(0);
                    }


                    if (mVertexBuffers[SURFACE_2][2] != null) {
                        //surface2 bot
                        gl.glLoadIdentity();
                        gl.glDisable(GL10.GL_TEXTURE_2D); //DISABLE TEXTURE WHEN BUILDING FACES

                        gl.glTranslatef(0, 0, depth);
                        gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);

                        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffers[SURFACE_2][2]);

                        gl.glColor4f(0f, 0f, 0.0f, 1); //set the block black
                        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    }

                    if (mVertexBuffers[SURFACE_2][1] != null) {
                        //surface2 top
                        gl.glLoadIdentity();
                        gl.glDisable(GL10.GL_TEXTURE_2D); //DISABLE TEXTURE WHEN BUILDING FACES

                        gl.glTranslatef(0, 0, depth);
                        gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);

                        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffers[SURFACE_2][1]);

                        gl.glColor4f(0f, 0f, 0.0f, 1); //set the block black
                        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    }
                } else if (mResources.get(surface2ResourceIndex).getJSONObject(surface2MediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
//                    Log.d(TAG, "onDrawFrame: rendering surface 2 video");

                    // Define the vertices for this face
                    float[] imageVertices = {
                            widthMatrix[SURFACE_2][0], heightMatrix[SURFACE_2][0], depthMatrix[SURFACE_2][0],
                            widthMatrix[SURFACE_2][1], heightMatrix[SURFACE_2][1], depthMatrix[SURFACE_2][1],
                            widthMatrix[SURFACE_2][2], heightMatrix[SURFACE_2][2], depthMatrix[SURFACE_2][2],
                            widthMatrix[SURFACE_2][3], heightMatrix[SURFACE_2][3], depthMatrix[SURFACE_2][3],
                    };
                    if (vertexBuffer2 != null) {
                        vertexBuffer2.clear();
                    }
                    ByteBuffer vbb = ByteBuffer.allocateDirect(imageVertices.length * 6 * 4);
                    vbb.order(ByteOrder.nativeOrder());
                    vertexBuffer2 = vbb.asFloatBuffer();

                    vertexBuffer2.put(imageVertices);
                    vertexBuffer2.position(0);

                    if (mPlayer2State == ACTIVE_PLAYER && !mPlayer2.getPlayWhenReady() && mCurrentSurface == SURFACE_2) {
//                        Log.d(TAG, "onDrawFrame: playing player 2");
                        mPlayer2.setPlayWhenReady(true);
//                        retryPlayer2 = false;
                    } else if (mSecondaryPlayer2State == ACTIVE_PLAYER && !mSecondaryPlayer2.getPlayWhenReady() && mCurrentSurface == SURFACE_2) {
                        Log.d(TAG, "onDrawFrame: playing secondary player 2");
                        mSecondaryPlayer2.setPlayWhenReady(true);
//                        retrySecondaryPlayer2 = false;
                    }


                    // surface 2 video media
                    gl.glLoadIdentity();
                    gl.glEnable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES); //ENABLE VIDEO TEXTURES

                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer2);

                    gl.glTranslatef(0, 0, depth);
                    gl.glRotatef(-(angleRectangle), 0.0f, -1.0f, 0.0f);
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer2);
//
//                    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
//                    gl.glGenTextures(1, textureId2, 0);
//                    gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId2[0]); //causes error (doesn't crash)

                    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glDisable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES); //ENABLE VIDEO TEXTURES

                    if (mUpdateST2) {
                        try {
//                            hideProgressBar();
                            mSurfaceTexture2.updateTexImage();
//                            Log.d(TAG, "onDrawFrame: updating surface2 frame");

                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onFrameAvailable: IllegalStateException surface2: " + e.getMessage());
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onFrameAvailable: RuntimeException surface2: " + e.getMessage());
                        }
                        mUpdateST2 = false;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
//            e.printStackTrace();
        }

        //////////////////////////////
        //surface 3 logic
        try {
            final int surface3ResourceIndex = mResourceIndices.getJSONObject(SURFACE_3).getInt(mContext.getString(R.string.resource_index));
            final int surface3MediaIndex = mResourceIndices.getJSONObject(SURFACE_3).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject(surface3ResourceIndex / 4).getInt(mContext.getString(R.string.media_index));
//            Log.d(TAG, "onDrawFrame: surface3 media index: " + surface3MediaIndex);
//            Log.d(TAG, "onDrawFrame: surface3 resource index: " + surface3ResourceIndex);
            if (surface3MediaIndex >= 0) {
                String resourceType = "";
                boolean imageRenderError = false;
                try {
                    resourceType = mResources.get(surface3ResourceIndex).getJSONObject(surface3MediaIndex).get(mContext.getString(R.string.media_type)).toString();
                } catch (JSONException e) {
//                    Log.e(TAG, "onDrawFrame: error getting bitmap: " + e.getMessage() );
                    imageRenderError = true;
                }
                if (resourceType.equals(mContext.getString(R.string.encoded_bitmap)) || imageRenderError) {
//                    Log.d(TAG, "onDrawFrame: rendering SURFACE 1 image");

                    float widthScaleFactor = 1f;
                    float heightScaleFactor = 1f;
                    if (imageRenderError) {
                        widthScaleFactor = mDefaultImageWidthScaleFactor;
                        heightScaleFactor = mDefaultImageHeightScaleFactor;
                    } else {
                        widthScaleFactor = Float.valueOf(String.valueOf(mResources.get(surface3ResourceIndex).getJSONObject(surface3MediaIndex).get(mContext.getString(R.string.width_scale_factor))));
                        heightScaleFactor = Float.valueOf(String.valueOf(mResources.get(surface3ResourceIndex).getJSONObject(surface3MediaIndex).get(mContext.getString(R.string.height_scale_factor))));
                    }
                    // Define the vertices for this face
                    float[] imageVertices = {
                            widthMatrix[SURFACE_3][0] * widthScaleFactor, heightMatrix[SURFACE_3][0] * heightScaleFactor, depthMatrix[SURFACE_3][0],
                            widthMatrix[SURFACE_3][1] * widthScaleFactor, heightMatrix[SURFACE_3][1] * heightScaleFactor, depthMatrix[SURFACE_3][1],
                            widthMatrix[SURFACE_3][2] * widthScaleFactor, heightMatrix[SURFACE_3][2] * heightScaleFactor, depthMatrix[SURFACE_3][2],
                            widthMatrix[SURFACE_3][3] * widthScaleFactor, heightMatrix[SURFACE_3][3] * heightScaleFactor, depthMatrix[SURFACE_3][3],
                    };
                    if (vertexBuffer3 != null) {
                        vertexBuffer3.clear();
                    }
                    ByteBuffer vbb = ByteBuffer.allocateDirect(imageVertices.length * 6 * 4);
                    vbb.order(ByteOrder.nativeOrder());
                    vertexBuffer3 = vbb.asFloatBuffer();

                    vertexBuffer3.put(imageVertices);
                    vertexBuffer3.position(0);

                    //surface3 image media
                    gl.glLoadIdentity();
                    gl.glEnable(GL10.GL_TEXTURE_2D); //ENABLE IMAGE TEXTURES

                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer3);

                    setImageToIndex(gl, SURFACE_3, imageRenderError);

                    gl.glTranslatef(0, 0, depth);
                    gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer3);


                    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
                    gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId3[0]);
                    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);


                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glDisable(GL10.GL_TEXTURE_2D);


//                    if (mVertexBuffers[SURFACE_3][1] == null && widthScaleFactor != -1) {
                    if (widthScaleFactor != -1) {
//                        Log.d(TAG, "SCALE FACTOR: WSF: " + widthScaleFactor);
//                        Log.d(TAG, "SCALE FACTOR: HSF: " + heightScaleFactor);

                        float[] topVertices = {
                                widthMatrix[SURFACE_3][0] * widthScaleFactor, screenRatio * heightScaleFactor, depthMatrix[SURFACE_3][0],
                                widthMatrix[SURFACE_3][1] * widthScaleFactor, screenRatio * heightScaleFactor, depthMatrix[SURFACE_3][1],
                                widthMatrix[SURFACE_3][2] * widthScaleFactor, heightMatrix[SURFACE_3][2], depthMatrix[SURFACE_3][2],
                                widthMatrix[SURFACE_3][3] * widthScaleFactor, heightMatrix[SURFACE_3][3], depthMatrix[SURFACE_3][3],
                        };

                        vbb = ByteBuffer.allocateDirect(topVertices.length * 6 * 4);
                        vbb.order(ByteOrder.nativeOrder());
                        FloatBuffer bufferTop = vbb.asFloatBuffer();
                        mVertexBuffers[SURFACE_3][1] = bufferTop.put(topVertices);
                        mVertexBuffers[SURFACE_3][1].position(0);
                    }
//                    if (mVertexBuffers[SURFACE_3][2] == null && widthScaleFactor != -1) {
                    if (widthScaleFactor != -1) {
//                        Log.d(TAG, "SCALE FACTOR: WSF: " + widthScaleFactor);
//                        Log.d(TAG, "SCALE FACTOR: HSF: " + heightScaleFactor);

                        float[] botVertices = {
                                widthMatrix[SURFACE_3][0] * widthScaleFactor, heightMatrix[SURFACE_3][0], depthMatrix[SURFACE_3][0],
                                widthMatrix[SURFACE_3][1] * widthScaleFactor, heightMatrix[SURFACE_3][1], depthMatrix[SURFACE_3][1],
                                widthMatrix[SURFACE_3][2] * widthScaleFactor, -screenRatio * heightScaleFactor, depthMatrix[SURFACE_3][2],
                                widthMatrix[SURFACE_3][3] * widthScaleFactor, -screenRatio * heightScaleFactor, depthMatrix[SURFACE_3][3],
                        };
                        vbb = ByteBuffer.allocateDirect(botVertices.length * 6 * 4);
                        vbb.order(ByteOrder.nativeOrder());
                        FloatBuffer bufferBot = vbb.asFloatBuffer();
                        mVertexBuffers[SURFACE_3][2] = bufferBot.put(botVertices);
                        mVertexBuffers[SURFACE_3][2].position(0);
                    }


                    if (mVertexBuffers[SURFACE_3][2] != null) {
                        //surface3 bot
                        gl.glLoadIdentity();
                        gl.glDisable(GL10.GL_TEXTURE_2D); //DISABLE TEXTURE WHEN BUILDING FACES

                        gl.glTranslatef(0, 0, depth);
                        gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);

                        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffers[SURFACE_3][2]);

                        gl.glColor4f(0f, 0f, 0.0f, 1); //set the block black
                        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    }

                    if (mVertexBuffers[SURFACE_3][1] != null) {
                        //surface3 top
                        gl.glLoadIdentity();
                        gl.glDisable(GL10.GL_TEXTURE_2D); //DISABLE TEXTURE WHEN BUILDING FACES

                        gl.glTranslatef(0, 0, depth);
                        gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);

                        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffers[SURFACE_3][1]);

                        gl.glColor4f(0f, 0f, 0.0f, 1); //set the block black
                        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    }
                } else if (mResources.get(surface3ResourceIndex).getJSONObject(surface3MediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
//                    Log.d(TAG, "onDrawFrame: rendering SURFACE 1 video");

                    // Define the vertices for this face
                    float[] imageVertices = {
                            widthMatrix[SURFACE_3][0], heightMatrix[SURFACE_3][0], depthMatrix[SURFACE_3][0],
                            widthMatrix[SURFACE_3][1], heightMatrix[SURFACE_3][1], depthMatrix[SURFACE_3][1],
                            widthMatrix[SURFACE_3][2], heightMatrix[SURFACE_3][2], depthMatrix[SURFACE_3][2],
                            widthMatrix[SURFACE_3][3], heightMatrix[SURFACE_3][3], depthMatrix[SURFACE_3][3],
                    };
                    if (vertexBuffer3 != null) {
                        vertexBuffer3.clear();
                    }
                    ByteBuffer vbb = ByteBuffer.allocateDirect(imageVertices.length * 6 * 4);
                    vbb.order(ByteOrder.nativeOrder());
                    vertexBuffer3 = vbb.asFloatBuffer();

                    vertexBuffer3.put(imageVertices);
                    vertexBuffer3.position(0);


                    if (mPlayer3State == ACTIVE_PLAYER && !mPlayer3.getPlayWhenReady() && mCurrentSurface == SURFACE_3) {
                        Log.d(TAG, "onDrawFrame: playing player 3");
                        mPlayer3.setPlayWhenReady(true);
//                        retryPlayer3 = false;
                    } else if (mSecondaryPlayer3State == ACTIVE_PLAYER && !mSecondaryPlayer3.getPlayWhenReady() && mCurrentSurface == SURFACE_3) {
                        Log.d(TAG, "onDrawFrame: playing secondary player 3");
                        mSecondaryPlayer3.setPlayWhenReady(true);
//                        retrySecondaryPlayer3 = false;
                    }

                    // surface 3 video media
                    gl.glLoadIdentity();
                    gl.glEnable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES); //ENABLE VIDEO TEXTURES

                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer3);


                    gl.glTranslatef(0, 0, depth);
                    gl.glRotatef(-(angleRectangle), 0.0f, -1.0f, 0.0f);
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer3);

//                    gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId1[0]); //causes error (doesn't crash)
                    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glDisable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES); //ENABLE VIDEO TEXTURES

//                    //double check to make sure the video is playing properly
//                    if (mPlayer3State == ACTIVE_PLAYER && mPlayer3.getPlayWhenReady() && mCurrentSurface == SURFACE_3
//                            && mPlayer3.getPlaybackState() != Player.STATE_READY && retryPlayer3 && mPlayer3.getCurrentPosition() < 0) {
//                        retryPlayer3 = false;
//                        Log.d(TAG, "onDrawFrame: player3 is trying to play.");
//                        Log.d(TAG, "onDrawFrame: player3 playback state: " + mPlayer3.getPlaybackState());
//                        retryPlayVideo(SURFACE_3, surface3ResourceIndex, surface3MediaIndex);
//                    } else if (mSecondaryPlayer3State == ACTIVE_PLAYER && mSecondaryPlayer3.getPlayWhenReady() && mCurrentSurface == SURFACE_3
//                            && mSecondaryPlayer3.getPlaybackState() != Player.STATE_READY && retrySecondaryPlayer3 && mSecondaryPlayer3.getCurrentPosition() < 0) {
//                        retrySecondaryPlayer3 = false;
//                        Log.d(TAG, "onDrawFrame: secondary player3 is trying to play.");
//                        Log.d(TAG, "onDrawFrame: secondary player3 playback state: " + mSecondaryPlayer3.getPlaybackState());
//                        retryPlayVideo(SURFACE_3, surface3ResourceIndex, surface3MediaIndex);
//                    }
//                    else if(mSecondaryPlayer3.getPlaybackState() != Player.STATE_READY || mPlayer3.getPlaybackState() != Player.STATE_READY){
//                        hideProgressBar();
//                    }

                    if (mUpdateST3) {
                        try {
//                            hideProgressBar();
                            mSurfaceTexture3.updateTexImage();
//                            Log.d(TAG, "onDrawFrame: updating surface3 frame");

                        } catch (IllegalStateException e) {
                            Log.e(TAG, "onFrameAvailable: IllegalStateException: " + e.getMessage());
                        } catch (RuntimeException e) {
                            Log.e(TAG, "onFrameAvailable: RuntimeException: " + e.getMessage());
                        }
                        mUpdateST3 = false;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
//            e.printStackTrace();
        }


        //////////////////////////////
        //surface 4 logic
        try {
            final int surface4ResourceIndex = mResourceIndices.getJSONObject(SURFACE_4).getInt(mContext.getString(R.string.resource_index));
            final int surface4MediaIndex = mResourceIndices.getJSONObject(SURFACE_4).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject(surface4ResourceIndex / 4).getInt(mContext.getString(R.string.media_index));
//            Log.d(TAG, "onDrawFrame: surface4 media index: " + surface4MediaIndex);
//            Log.d(TAG, "onDrawFrame: surface4 resource index: " + surface4ResourceIndex);
            if (surface4MediaIndex >= 0) {
                String resourceType = "";
                boolean imageRenderError = false;
                try {
                    resourceType = mResources.get(surface4ResourceIndex).getJSONObject(surface4MediaIndex).get(mContext.getString(R.string.media_type)).toString();
                } catch (JSONException e) {
//                    Log.e(TAG, "onDrawFrame: error getting bitmap: " + e.getMessage() );
                    imageRenderError = true;
                }
                if (resourceType.equals(mContext.getString(R.string.encoded_bitmap)) || imageRenderError) {
//                    Log.d(TAG, "onDrawFrame: rendering SURFACE 1 image");

                    float widthScaleFactor = 1f;
                    float heightScaleFactor = 1f;
                    if (imageRenderError) {
                        widthScaleFactor = mDefaultImageWidthScaleFactor;
                        heightScaleFactor = mDefaultImageHeightScaleFactor;
                    } else {
                        widthScaleFactor = Float.valueOf(String.valueOf(mResources.get(surface4ResourceIndex).getJSONObject(surface4MediaIndex).get(mContext.getString(R.string.width_scale_factor))));
                        heightScaleFactor = Float.valueOf(String.valueOf(mResources.get(surface4ResourceIndex).getJSONObject(surface4MediaIndex).get(mContext.getString(R.string.height_scale_factor))));
                    }
                    // Define the vertices for this face
                    float[] imageVertices = {
                            widthMatrix[SURFACE_4][0] * widthScaleFactor, heightMatrix[SURFACE_4][0] * heightScaleFactor, depthMatrix[SURFACE_4][0],
                            widthMatrix[SURFACE_4][1] * widthScaleFactor, heightMatrix[SURFACE_4][1] * heightScaleFactor, depthMatrix[SURFACE_4][1],
                            widthMatrix[SURFACE_4][2] * widthScaleFactor, heightMatrix[SURFACE_4][2] * heightScaleFactor, depthMatrix[SURFACE_4][2],
                            widthMatrix[SURFACE_4][3] * widthScaleFactor, heightMatrix[SURFACE_4][3] * heightScaleFactor, depthMatrix[SURFACE_4][3],
                    };
                    if (vertexBuffer4 != null) {
                        vertexBuffer4.clear();
                    }
                    ByteBuffer vbb = ByteBuffer.allocateDirect(imageVertices.length * 6 * 4);
                    vbb.order(ByteOrder.nativeOrder());
                    vertexBuffer4 = vbb.asFloatBuffer();

                    vertexBuffer4.put(imageVertices);
                    vertexBuffer4.position(0);

                    //surface4 image media
                    gl.glLoadIdentity();
                    gl.glEnable(GL10.GL_TEXTURE_2D); //ENABLE IMAGE TEXTURES

                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer4);

                    setImageToIndex(gl, SURFACE_4, imageRenderError);

                    gl.glTranslatef(0, 0, depth);
                    gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer4);


                    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
                    gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId4[0]);
                    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);


                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glDisable(GL10.GL_TEXTURE_2D);


//                    if (mVertexBuffers[SURFACE_4][1] == null && widthScaleFactor != -1) {
                    if (widthScaleFactor != -1) {
//                        Log.d(TAG, "SCALE FACTOR: WSF: " + widthScaleFactor);
//                        Log.d(TAG, "SCALE FACTOR: HSF: " + heightScaleFactor);

                        float[] topVertices = {
                                widthMatrix[SURFACE_4][0] * widthScaleFactor, screenRatio * heightScaleFactor, depthMatrix[SURFACE_4][0],
                                widthMatrix[SURFACE_4][1] * widthScaleFactor, screenRatio * heightScaleFactor, depthMatrix[SURFACE_4][1],
                                widthMatrix[SURFACE_4][2] * widthScaleFactor, heightMatrix[SURFACE_4][2], depthMatrix[SURFACE_4][2],
                                widthMatrix[SURFACE_4][3] * widthScaleFactor, heightMatrix[SURFACE_4][3], depthMatrix[SURFACE_4][3],
                        };

                        vbb = ByteBuffer.allocateDirect(topVertices.length * 6 * 4);
                        vbb.order(ByteOrder.nativeOrder());
                        FloatBuffer bufferTop = vbb.asFloatBuffer();
                        mVertexBuffers[SURFACE_4][1] = bufferTop.put(topVertices);
                        mVertexBuffers[SURFACE_4][1].position(0);
                    }
//                    if (mVertexBuffers[SURFACE_4][2] == null && widthScaleFactor != -1) {
                    if (widthScaleFactor != -1) {
//                        Log.d(TAG, "SCALE FACTOR: WSF: " + widthScaleFactor);
//                        Log.d(TAG, "SCALE FACTOR: HSF: " + heightScaleFactor);

                        float[] botVertices = {
                                widthMatrix[SURFACE_4][0] * widthScaleFactor, heightMatrix[SURFACE_4][0], depthMatrix[SURFACE_4][0],
                                widthMatrix[SURFACE_4][1] * widthScaleFactor, heightMatrix[SURFACE_4][1], depthMatrix[SURFACE_4][1],
                                widthMatrix[SURFACE_4][2] * widthScaleFactor, -screenRatio * heightScaleFactor, depthMatrix[SURFACE_4][2],
                                widthMatrix[SURFACE_4][3] * widthScaleFactor, -screenRatio * heightScaleFactor, depthMatrix[SURFACE_4][3],
                        };

                        vbb = ByteBuffer.allocateDirect(botVertices.length * 6 * 4);
                        vbb.order(ByteOrder.nativeOrder());
                        FloatBuffer bufferBot = vbb.asFloatBuffer();
                        mVertexBuffers[SURFACE_4][2] = bufferBot.put(botVertices);
                        mVertexBuffers[SURFACE_4][2].position(0);
                    }


                    if (mVertexBuffers[SURFACE_4][2] != null) {
                        //surface4 bot
                        gl.glLoadIdentity();
                        gl.glDisable(GL10.GL_TEXTURE_2D); //DISABLE TEXTURE WHEN BUILDING FACES

                        gl.glTranslatef(0, 0, depth);
                        gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);

                        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffers[SURFACE_4][2]);

                        gl.glColor4f(0f, 0f, 0.0f, 1); //set the block black
                        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    }

                    if (mVertexBuffers[SURFACE_4][1] != null) {
                        //surface4 top
                        gl.glLoadIdentity();
                        gl.glDisable(GL10.GL_TEXTURE_2D); //DISABLE TEXTURE WHEN BUILDING FACES

                        gl.glTranslatef(0, 0, depth);
                        gl.glRotatef(angleRectangle, 0.0f, 1.0f, 0.0f);

                        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffers[SURFACE_4][1]);

                        gl.glColor4f(0f, 0f, 0.0f, 1); //set the block black
                        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    }
                } else if (mResources.get(surface4ResourceIndex).getJSONObject(surface4MediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
//                    Log.d(TAG, "onDrawFrame: rendering SURFACE 1 video");

                    // Define the vertices for this face
                    float[] imageVertices = {
                            widthMatrix[SURFACE_4][0], heightMatrix[SURFACE_4][0], depthMatrix[SURFACE_4][0],
                            widthMatrix[SURFACE_4][1], heightMatrix[SURFACE_4][1], depthMatrix[SURFACE_4][1],
                            widthMatrix[SURFACE_4][2], heightMatrix[SURFACE_4][2], depthMatrix[SURFACE_4][2],
                            widthMatrix[SURFACE_4][3], heightMatrix[SURFACE_4][3], depthMatrix[SURFACE_4][3],
                    };
                    if (vertexBuffer4 != null) {
                        vertexBuffer4.clear();
                    }
                    ByteBuffer vbb = ByteBuffer.allocateDirect(imageVertices.length * 6 * 4);
                    vbb.order(ByteOrder.nativeOrder());
                    vertexBuffer4 = vbb.asFloatBuffer();

                    vertexBuffer4.put(imageVertices);
                    vertexBuffer4.position(0);


                    if (mPlayer4State == ACTIVE_PLAYER && !mPlayer4.getPlayWhenReady() && mCurrentSurface == SURFACE_4) {
                        Log.d(TAG, "onDrawFrame: playing player 4");
                        mPlayer4.setPlayWhenReady(true);
//                        retryPlayer4 = false;
                    } else if (mSecondaryPlayer4State == ACTIVE_PLAYER && !mSecondaryPlayer4.getPlayWhenReady() && mCurrentSurface == SURFACE_4) {
                        Log.d(TAG, "onDrawFrame: playing secondary player 4");
                        mSecondaryPlayer4.setPlayWhenReady(true);
//                        retrySecondaryPlayer4 = false;
                    }

                    // surface 4 video media
                    gl.glLoadIdentity();
                    gl.glEnable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES); //ENABLE VIDEO TEXTURES

                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer4);


                    gl.glTranslatef(0, 0, depth);
                    gl.glRotatef(-(angleRectangle), 0.0f, -1.0f, 0.0f);
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer4);

//                    gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId1[0]); //causes error (doesn't crash)
                    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    gl.glDisable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES); //ENABLE VIDEO TEXTURES

                    //double check to make sure the video is playing properly
//                    if (mPlayer4State == ACTIVE_PLAYER && mPlayer4.getPlayWhenReady() && mCurrentSurface == SURFACE_4
//                            && mPlayer4.getPlaybackState() != Player.STATE_READY && retryPlayer4 && mPlayer4.getCurrentPosition() < 0) {
//                        retryPlayer4 = false;
//                        Log.d(TAG, "onDrawFrame: player4 is trying to play.");
//                        Log.d(TAG, "onDrawFrame: player4 playback state: " + mPlayer4.getPlaybackState());
//                        retryPlayVideo(SURFACE_4, surface4ResourceIndex, surface4MediaIndex);
//                    } else if (mSecondaryPlayer4State == ACTIVE_PLAYER && mSecondaryPlayer4.getPlayWhenReady() && mCurrentSurface == SURFACE_4
//                            && mSecondaryPlayer4.getPlaybackState() != Player.STATE_READY && retrySecondaryPlayer4 && mSecondaryPlayer4.getCurrentPosition() < 0) {
//                        retrySecondaryPlayer4 = false;
//                        Log.d(TAG, "onDrawFrame: secondary player4 is trying to play.");
//                        Log.d(TAG, "onDrawFrame: secondary player4 playback state: " + mSecondaryPlayer4.getPlaybackState());
//                        retryPlayVideo(SURFACE_4, surface4ResourceIndex, surface4MediaIndex);
//                    }
//                    else if(mSecondaryPlayer4.getPlaybackState() != Player.STATE_READY || mPlayer4.getPlaybackState() != Player.STATE_READY){
//                        hideProgressBar();
//                    }

                    if (mUpdateST4) {
                        try {
//                            hideProgressBar();
                            mSurfaceTexture4.updateTexImage();
//                            Log.d(TAG, "onDrawFrame: updating surface4 frame");

                        } catch (IllegalStateException e) {
                            Log.e(TAG, "onFrameAvailable: IllegalStateException: " + e.getMessage());
                        } catch (RuntimeException e) {
                            Log.e(TAG, "onFrameAvailable: RuntimeException: " + e.getMessage());
                        }
                        mUpdateST4 = false;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
//            e.printStackTrace();
        }

//        if((isImage1Set || hasFirstVideo1Played)  && (isImage2Set || hasFirstVideo2Played)
//                && (isImage3Set || hasFirstVideo3Played) && (isImage4Set || hasFirstVideo4Played)) {
        ByteBuffer vBackground = ByteBuffer.allocateDirect(backgroundVertices.length * 6 * 4);
        vBackground.order(ByteOrder.nativeOrder());
        FloatBuffer bufferBackground = vBackground.asFloatBuffer();
        mVertexBuffers[BACKGROUND_SURFACE][1] = bufferBackground.put(backgroundVertices);
        mVertexBuffers[BACKGROUND_SURFACE][1].position(0);

        gl.glLoadIdentity();
        gl.glDisable(GL10.GL_TEXTURE_2D); //DISABLE TEXTURE WHEN BUILDING FACES

        gl.glTranslatef(0, 0, -9f);
        gl.glRotatef(0, 0.0f, 1.0f, 0.0f);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffers[BACKGROUND_SURFACE][1]);

        gl.glColor4f(1f, 1f, 1.0f, 1.0f); //set the block black
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
//        }
    }




    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated: SURFACE CREATED.");
        gl.glClearDepthf(1.0f);            // Set depth's clear-value to farthest
        gl.glEnable(GL10.GL_DEPTH_TEST);   // Enables depth-buffer for hidden surface removal
        gl.glDepthFunc(GL10.GL_LEQUAL);    // The type of depth testing to do
//        gl.glDepthFunc(GL10.GL_NEVER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  // nice perspective view
        gl.glShadeModel(GL10.GL_SMOOTH);   // Enable smooth shading of color
        gl.glDisable(GL10.GL_DITHER);      // Disable dithering for better performance
//        gl.glFrontFace(GL10.GL_CCW);
    }




    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: SURFACE CHANGED.");
        if (height == 0) height = 1;   // To prevent divide by zero
        float aspect = (float)width / height;

        // Set the viewport (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);

        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL10.GL_PROJECTION); // Select projection matrix
        gl.glLoadIdentity();                 // Reset projection matrix
        // Use perspective projection
        GLU.gluPerspective(gl, 45, aspect, 0.1f, 100.f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);  // Select model-view matrix
        gl.glLoadIdentity();                 // Reset
    }


    private void incrementSurface(){
        Log.d(TAG, "incrementSurface: incrementing current surface.");

        if(mCurrentSurface == SURFACE_1){
            mCurrentSurface = SURFACE_2;
            Log.d(TAG, "incrementSurface: CURRENT SURFACE: SURFACE 2");
        }
        else if(mCurrentSurface == SURFACE_2){
            mCurrentSurface = SURFACE_3;
            Log.d(TAG, "incrementSurface: CURRENT SURFACE: SURFACE 3");
        }
        else if(mCurrentSurface == SURFACE_3){
            mCurrentSurface = SURFACE_4;
            Log.d(TAG, "incrementSurface: CURRENT SURFACE: SURFACE 4");
        }
        else if(mCurrentSurface == SURFACE_4){
            mCurrentSurface = SURFACE_1;
            Log.d(TAG, "incrementSurface: CURRENT SURFACE: SURFACE 1");
        }
        updateRotations(1);
    }

    private void deincrementSurface(){
        Log.d(TAG, "deincrementSurface: deincrementing current surface.");

        if(mCurrentSurface == SURFACE_1){
            mCurrentSurface = SURFACE_4;
            Log.d(TAG, "deincrementSurface: CURRENT SURFACE: SURFACE 4");
        }
        else if(mCurrentSurface == SURFACE_2){
            mCurrentSurface = SURFACE_1;
            Log.d(TAG, "deincrementSurface: CURRENT SURFACE: SURFACE 1");
        }
        else if(mCurrentSurface == SURFACE_3){
            mCurrentSurface = SURFACE_2;
            Log.d(TAG, "deincrementSurface: CURRENT SURFACE: SURFACE 2");
        }
        else if(mCurrentSurface == SURFACE_4){
            mCurrentSurface = SURFACE_3;
            Log.d(TAG, "deincrementSurface: CURRENT SURFACE: SURFACE 3");
        }
        updateRotations(-1);
    }

    private void updateRotations(final int direction){
        if(direction > 0){
            mNumRotations++;
        }
        else{
            mNumRotations--;
        }
        Log.d(TAG, "updateRotations: NUM ROTATIONS: " + mNumRotations);
        try{
            int numRotations1 = mResourceIndices.getJSONObject(SURFACE_1).getInt(mContext.getString(R.string.rotations));
            numRotations1 = numRotations1 + direction;
            JSONObject object1 = mResourceIndices.getJSONObject(SURFACE_1);
            object1.put(mContext.getString(R.string.rotations), numRotations1);
            mResourceIndices.put(SURFACE_1, object1);
            Log.d(TAG, "updateRotations: num rotations1: " + numRotations1);

            int numRotations2 = mResourceIndices.getJSONObject(SURFACE_2).getInt(mContext.getString(R.string.rotations));
            numRotations2 = numRotations2 + direction;
            JSONObject object2 = mResourceIndices.getJSONObject(SURFACE_2);
            object2.put(mContext.getString(R.string.rotations), numRotations2);
            mResourceIndices.put(SURFACE_2, object2);
            Log.d(TAG, "updateRotations: num rotations2: " + numRotations2);

            int numRotations3 = mResourceIndices.getJSONObject(SURFACE_3).getInt(mContext.getString(R.string.rotations));
            numRotations3 = numRotations3 + direction;
            JSONObject object3 = mResourceIndices.getJSONObject(SURFACE_3);
            object3.put(mContext.getString(R.string.rotations), numRotations3);
            mResourceIndices.put(SURFACE_3, object3);
            Log.d(TAG, "updateRotations: num rotations3: " + numRotations3);

            int numRotations4 = mResourceIndices.getJSONObject(SURFACE_4).getInt(mContext.getString(R.string.rotations));
            numRotations4 = numRotations4 + direction;
            JSONObject object4 = mResourceIndices.getJSONObject(SURFACE_4);
            object4.put(mContext.getString(R.string.rotations), numRotations4);
            mResourceIndices.put(SURFACE_4, object4);
            Log.d(TAG, "updateRotations: num rotations4: " + numRotations4);

            if(numRotations1 % 3 == 0 && numRotations1 != 0){
                int temp = numRotations1;
                if(direction > 0){
                    numRotations1 = -1;
                }
                else{
                    numRotations1 = 1;
                }

                if(mSurfaceTexture == null){
                    initVideoSurface1(PLAYER_ONE);
                }
                object1.put(mContext.getString(R.string.rotations), numRotations1);
                mResourceIndices.put(SURFACE_1, object1);
                Log.d(TAG, "updateRotations: surface1 transition. Num rotations from: " + temp + " to " + numRotations1);

                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(mContext.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try{
                            //update the resource index to show new resource
                            int oldResourceIndex = mResourceIndices.getJSONObject(SURFACE_1).getInt(mContext.getString(R.string.resource_index));

                            int newResourceIndex = oldResourceIndex + 4*direction;

//                            if(newResourceIndex < mMedia.size() && direction > 0){
                            if(newResourceIndex < mNumResources){
//                                getMedia(mMedia.get(newResourceIndex), newResourceIndex);
//                                JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_1);
//                                tempObj.put(mContext.getString(R.string.resource_index), newResourceIndex);
//                                mResourceIndices.put(SURFACE_1, tempObj);
//                                Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_1).toString());
//
//                                //when a full rotation occurs, reset the media of that index
//                                resetMediaIndex(SURFACE_1, oldResourceIndex, direction);

                                if(direction > 0){
//                                    getMedia(mMedia.get(newResourceIndex).getMedia(), newResourceIndex);
                                    getMedia(mUserStories.getJSONObject(newResourceIndex)
                                            .getJSONArray(mContext.getString(R.string.user_stories)), newResourceIndex);
                                    JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_1);
                                    tempObj.put(mContext.getString(R.string.resource_index), newResourceIndex);
                                    mResourceIndices.put(SURFACE_1, tempObj);

                                    //when a full rotation occurs, reset the media of that index
//                                    resetMediaIndex(SURFACE_1, newResourceIndex, direction);

                                    Log.d(TAG, "updateRotations: old resource index: " + oldResourceIndex);
                                    Log.d(TAG, "updateRotations: new resource index: " + newResourceIndex);
                                    Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_1).toString());
                                }
                                else if(direction < 0 && oldResourceIndex > mNumRotations && oldResourceIndex >= 4){
//                                    getMedia(mMedia.get(oldResourceIndex - 4).getMedia(), oldResourceIndex - 4);
                                    getMedia(mUserStories.getJSONObject(oldResourceIndex - 4)
                                            .getJSONArray(mContext.getString(R.string.user_stories)), oldResourceIndex - 4);
                                    JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_1);
                                    tempObj.put(mContext.getString(R.string.resource_index), oldResourceIndex - 4);
                                    mResourceIndices.put(SURFACE_1, tempObj);
                                    //when a full rotation occurs, reset the media of that index
//                                    resetMediaIndex(SURFACE_1, oldResourceIndex - 4, direction);

                                    Log.d(TAG, "updateRotations: old resource index: " + oldResourceIndex);
                                    Log.d(TAG, "updateRotations: new resource index: " + (oldResourceIndex - 4));
                                    Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_1).toString());
                                }
//                                Log.d(TAG, "updateRotations: surface# 1 resource indices: " + mResourceIndices.getJSONObject(SURFACE_1));
//                                Log.d(TAG, "updateRotations: surface# 2 resource indices: " + mResourceIndices.getJSONObject(SURFACE_2));
//                                Log.d(TAG, "updateRotations: surface# 3 resource indices: " + mResourceIndices.getJSONObject(SURFACE_3));
//                                Log.d(TAG, "updateRotations: surface# 4 resource indices: " + mResourceIndices.getJSONObject(SURFACE_4));

                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                };
                mainHandler.post(myRunnable);
            }
            else if(numRotations2 % 3 == 0 && numRotations2 != 0){
                int temp = numRotations2;
                if(direction > 0){
                    numRotations2 = -1;
                }
                else{
                    numRotations2 = 1;
                }
                if(mSurfaceTexture2 == null){
                    initVideoSurface1(PLAYER_TWO);
                }
                object2.put(mContext.getString(R.string.rotations), numRotations2);
                mResourceIndices.put(SURFACE_2, object2);
                Log.d(TAG, "updateRotations: surface2 transition. Num rotations from: " + temp + " to " + numRotations2);

                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(mContext.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try{
                            //update the resource index to show new resource
                            int oldResourceIndex = mResourceIndices.getJSONObject(SURFACE_2).getInt(mContext.getString(R.string.resource_index));

                            int newResourceIndex = oldResourceIndex + 4*direction;

                            if(newResourceIndex < mNumResources){
//                                getMedia(mMedia.get(newResourceIndex), newResourceIndex);
//
//                                JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_2);
//                                tempObj.put(mContext.getString(R.string.resource_index), newResourceIndex);
//                                mResourceIndices.put(SURFACE_2, tempObj);
//                                Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_2).toString());
//
//                                //when a full rotation occurs, reset the media of that index
//                                resetMediaIndex(SURFACE_2, oldResourceIndex, direction);

                                if(direction > 0){
//                                    getMedia(mMedia.get(newResourceIndex).getMedia(), newResourceIndex);
                                    getMedia(mUserStories.getJSONObject(newResourceIndex)
                                            .getJSONArray(mContext.getString(R.string.user_stories)), newResourceIndex);
                                    JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_2);
                                    tempObj.put(mContext.getString(R.string.resource_index), newResourceIndex);
                                    mResourceIndices.put(SURFACE_2, tempObj);

                                    //when a full rotation occurs, reset the media of that index
//                                    resetMediaIndex(SURFACE_2, newResourceIndex, direction);

                                    Log.d(TAG, "updateRotations: old resource index: " + oldResourceIndex);
                                    Log.d(TAG, "updateRotations: new resource index: " + newResourceIndex);
                                    Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_2).toString());
                                }
                                else if(direction < 0 && oldResourceIndex > mNumRotations && oldResourceIndex >= 4){
//                                    getMedia(mMedia.get(oldResourceIndex - 4).getMedia(), oldResourceIndex - 4);
                                    getMedia(mUserStories.getJSONObject(oldResourceIndex - 4)
                                            .getJSONArray(mContext.getString(R.string.user_stories)), oldResourceIndex - 4);
                                    JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_2);
                                    tempObj.put(mContext.getString(R.string.resource_index), oldResourceIndex - 4);
                                    mResourceIndices.put(SURFACE_2, tempObj);
                                    //when a full rotation occurs, reset the media of that index
//                                    resetMediaIndex(SURFACE_2, oldResourceIndex - 4, direction);

                                    Log.d(TAG, "updateRotations: old resource index: " + oldResourceIndex);
                                    Log.d(TAG, "updateRotations: new resource index: " + (oldResourceIndex - 4));
                                    Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_2).toString());
                                }
//                                Log.d(TAG, "updateRotations: surface# 1 resource indices: " + mResourceIndices.getJSONObject(SURFACE_1));
//                                Log.d(TAG, "updateRotations: surface# 2 resource indices: " + mResourceIndices.getJSONObject(SURFACE_2));
//                                Log.d(TAG, "updateRotations: surface# 3 resource indices: " + mResourceIndices.getJSONObject(SURFACE_3));
//                                Log.d(TAG, "updateRotations: surface# 4 resource indices: " + mResourceIndices.getJSONObject(SURFACE_4));

                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                };
                mainHandler.post(myRunnable);
            }
            else if(numRotations3 % 3 == 0 && numRotations3 != 0){
                int temp = numRotations3;
                if(direction > 0){
                    numRotations3 = -1;
                }
                else{
                    numRotations3 = 1;
                }
                if(mSurfaceTexture3 == null){
                    initVideoSurface1(PLAYER_THREE);
                }
                object3.put(mContext.getString(R.string.rotations), numRotations3);
                mResourceIndices.put(SURFACE_3, object3);
                Log.d(TAG, "updateRotations: surface3 transition. Num rotations from: " + temp + " to " + numRotations3);

                // special case for first rotation for surface 3. Disable transition
                if(!mFirstRotationSurface3){
                    if(mNumRotations == 0){
                        mFirstRotationSurface3 = true;
                    }

                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            try{
                                //update the resource index to show new resource
                                int oldResourceIndex = mResourceIndices.getJSONObject(SURFACE_3).getInt(mContext.getString(R.string.resource_index));

                                int newResourceIndex = oldResourceIndex + 4*direction;

//                                if(newResourceIndex < mMedia.size() && direction > 0){
                                if(newResourceIndex < mNumResources){
//                                    getMedia(mMedia.get(newResourceIndex), newResourceIndex);
//
//                                    JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_3);
//                                    tempObj.put(mContext.getString(R.string.resource_index), newResourceIndex);
//                                    mResourceIndices.put(SURFACE_3, tempObj);
//                                    Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_3).toString());
//
//                                    //when a full rotation occurs, reset the media of that index
//                                    resetMediaIndex(SURFACE_3, oldResourceIndex, direction);

                                    if(direction > 0){
//                                        getMedia(mMedia.get(newResourceIndex).getMedia(), newResourceIndex);
                                        getMedia(mUserStories.getJSONObject(newResourceIndex)
                                                .getJSONArray(mContext.getString(R.string.user_stories)), newResourceIndex);
                                        JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_3);
                                        tempObj.put(mContext.getString(R.string.resource_index), newResourceIndex);
                                        mResourceIndices.put(SURFACE_3, tempObj);

                                        //when a full rotation occurs, reset the media of that index
//                                        resetMediaIndex(SURFACE_3, newResourceIndex, direction);

                                        Log.d(TAG, "updateRotations: old resource index: " + oldResourceIndex);
                                        Log.d(TAG, "updateRotations: new resource index: " + newResourceIndex);
                                        Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_3).toString());
                                    }
                                    else if(direction < 0 && oldResourceIndex > mNumRotations && oldResourceIndex >= 4){
//                                        getMedia(mMedia.get(oldResourceIndex - 4).getMedia(), oldResourceIndex - 4);
                                        getMedia(mUserStories.getJSONObject(oldResourceIndex - 4)
                                                .getJSONArray(mContext.getString(R.string.user_stories)), oldResourceIndex - 4);
                                        JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_3);
                                        tempObj.put(mContext.getString(R.string.resource_index), oldResourceIndex - 4);
                                        mResourceIndices.put(SURFACE_3, tempObj);
                                        //when a full rotation occurs, reset the media of that index
//                                        resetMediaIndex(SURFACE_3, oldResourceIndex - 4, direction);

                                        Log.d(TAG, "updateRotations: old resource index: " + oldResourceIndex);
                                        Log.d(TAG, "updateRotations: new resource index: " + (oldResourceIndex - 4));
                                        Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_3).toString());
                                    }
//                                    Log.d(TAG, "updateRotations: surface# 1 resource indices: " + mResourceIndices.getJSONObject(SURFACE_1));
//                                    Log.d(TAG, "updateRotations: surface# 2 resource indices: " + mResourceIndices.getJSONObject(SURFACE_2));
//                                    Log.d(TAG, "updateRotations: surface# 3 resource indices: " + mResourceIndices.getJSONObject(SURFACE_3));
//                                    Log.d(TAG, "updateRotations: surface# 4 resource indices: " + mResourceIndices.getJSONObject(SURFACE_4));

                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    };
                    mainHandler.post(myRunnable);
                }
                else{
                    Log.d(TAG, "updateRotations: first rotation of surface 3.");
                    mFirstRotationSurface3 = false;
//                    isImage3Set = false;

                }

            }

            else if(numRotations4 % 3 == 0 && numRotations4 != 0){
                int temp = numRotations4;
                if(direction > 0){
                    numRotations4 = -1;
                }
                else{
                    numRotations4 = 1;
                }
                if(mSurfaceTexture4 == null){
                    initVideoSurface1(PLAYER_FOUR);
                }
                object4.put(mContext.getString(R.string.rotations), numRotations4);
                mResourceIndices.put(SURFACE_4, object4);
                Log.d(TAG, "updateRotations: surface4 transition. Num rotations from: " + temp + " to " + numRotations4);

                // special case for first rotation for surface 4. Disable transition
                // Also use mNumRotations to catch special case for restarting completely

                if(!mFirstRotationSurface4){
                    if(mNumRotations == 0){
                        mFirstRotationSurface4 = true;

                    }
                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            try{
                                int oldResourceIndex = mResourceIndices.getJSONObject(SURFACE_4).getInt(mContext.getString(R.string.resource_index));

                                int newResourceIndex = oldResourceIndex + 4*direction;
//                                if(oldResourceIndex == 3){
//                                    return;
//                                }

//                                if(newResourceIndex < mMedia.size() && direction > 0){
                                if(newResourceIndex < mNumResources){
                                    if(direction > 0){
//                                        getMedia(mMedia.get(newResourceIndex).getMedia(), newResourceIndex);
                                        getMedia(mUserStories.getJSONObject(newResourceIndex)
                                                .getJSONArray(mContext.getString(R.string.user_stories)), newResourceIndex);
                                        JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_4);
                                        tempObj.put(mContext.getString(R.string.resource_index), newResourceIndex);
                                        mResourceIndices.put(SURFACE_4, tempObj);
                                        //when a full rotation occurs, reset the media of that index
//                                        resetMediaIndex(SURFACE_4, newResourceIndex, direction);

                                        Log.d(TAG, "updateRotations: old resource index: " + oldResourceIndex);
                                        Log.d(TAG, "updateRotations: new resource index: " + newResourceIndex);
                                        Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_4).toString());
                                    }
                                    else if(direction < 0 && oldResourceIndex > mNumRotations && oldResourceIndex >= 4){
//                                        getMedia(mMedia.get(oldResourceIndex - 4).getMedia(), oldResourceIndex - 4);
                                        getMedia(mUserStories.getJSONObject(oldResourceIndex - 4)
                                                .getJSONArray(mContext.getString(R.string.user_stories)), oldResourceIndex - 4);
                                        JSONObject tempObj = mResourceIndices.getJSONObject(SURFACE_4);
                                        tempObj.put(mContext.getString(R.string.resource_index), oldResourceIndex - 4);
                                        mResourceIndices.put(SURFACE_4, tempObj);
                                        //when a full rotation occurs, reset the media of that index
//                                        resetMediaIndex(SURFACE_4, oldResourceIndex - 4, direction);

                                        Log.d(TAG, "updateRotations: old resource index: " + oldResourceIndex);
                                        Log.d(TAG, "updateRotations: new resource index: " + (oldResourceIndex - 4));
                                        Log.d(TAG, "updateRotations: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(SURFACE_4).toString());
                                    }

//                                    Log.d(TAG, "updateRotations: surface# 1 resource indices: " + mResourceIndices.getJSONObject(SURFACE_1));
//                                    Log.d(TAG, "updateRotations: surface# 2 resource indices: " + mResourceIndices.getJSONObject(SURFACE_2));
//                                    Log.d(TAG, "updateRotations: surface# 3 resource indices: " + mResourceIndices.getJSONObject(SURFACE_3));
//                                    Log.d(TAG, "updateRotations: surface# 4 resource indices: " + mResourceIndices.getJSONObject(SURFACE_4));

                                }

                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    };
                    mainHandler.post(myRunnable);
                }
                else{
                    Log.d(TAG, "updateRotations: first rotation of surface 4.");
                    mFirstRotationSurface4 = false;
                }
            }

            Log.d(TAG, "updateRotations: surface# 1 resource indices: " + mResourceIndices.getJSONObject(SURFACE_1));
            Log.d(TAG, "updateRotations: surface# 2 resource indices: " + mResourceIndices.getJSONObject(SURFACE_2));
            Log.d(TAG, "updateRotations: surface# 3 resource indices: " + mResourceIndices.getJSONObject(SURFACE_3));
            Log.d(TAG, "updateRotations: surface# 4 resource indices: " + mResourceIndices.getJSONObject(SURFACE_4));


            hideProgressBar();
            //setup progress bars for the media sources
            isProgressBarsInitialized = false;
            initProgressBars();
            //create runnable that updates progress bars once the progress bars are initialized.
            mProgressBarInitHandler = new Handler(Looper.getMainLooper());
            mProgressBarInitRunnable = new Runnable() {
                @Override
                public void run() {
                    mProgressBarInitHandler.postDelayed(mProgressBarInitRunnable, 200);
                    Log.d(TAG, "rotateCounterClockwise: checking to see if progress bars are initialized.");
                    if(isProgressBarsInitialized){
                        try{
                            if(mCurrentSurface == SURFACE_1) {
                                final int surface1ResourceIndex = mResourceIndices.getJSONObject(SURFACE_1).getInt(mContext.getString(R.string.resource_index));
                                final int surface1MediaIndex = mResourceIndices.getJSONObject(SURFACE_1).getJSONArray(mContext.getString(R.string.media_index))
                                        .getJSONObject(surface1ResourceIndex / 4).getInt(mContext.getString(R.string.media_index));
                                Log.d(TAG, "correctRotation: RESOURCE SURFACE OBJECT: " + mResources.get(surface1ResourceIndex).getJSONObject(surface1MediaIndex).get(mContext.getString(R.string.media_type)));
                                if(mResources.get(surface1ResourceIndex).getJSONObject(surface1MediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
                                    Log.d(TAG, "correctRotation: playing video on surface 1.");
                                    setProgressBars(0); //by passing 0 it won't fill the most recent progress bar
                                    restartPlayer1();
                                    startProgressBar();
                                }
                                else{
                                    setProgressBars(1);
                                    restartProgressBarRunnable();
                                }
                            }
                            else if(mCurrentSurface == SURFACE_2) {
                                final int surface2ResourceIndex = mResourceIndices.getJSONObject(SURFACE_2).getInt(mContext.getString(R.string.resource_index));
                                final int surface2MediaIndex = mResourceIndices.getJSONObject(SURFACE_2).getJSONArray(mContext.getString(R.string.media_index))
                                        .getJSONObject(surface2ResourceIndex / 4).getInt(mContext.getString(R.string.media_index));
                                Log.d(TAG, "correctRotation: RESOURCE SURFACE OBJECT: " + mResources.get(surface2ResourceIndex).getJSONObject(surface2MediaIndex).get(mContext.getString(R.string.media_type)));
                                if(mResources.get(surface2ResourceIndex).getJSONObject(surface2MediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
                                    Log.d(TAG, "correctRotation: playing video on surface 2.");
                                    setProgressBars(0);
                                    restartPlayer2();
                                    startProgressBar();
                                }
                                else{
                                    setProgressBars(1);
                                    restartProgressBarRunnable();
                                }
                            }
                            else if(mCurrentSurface == SURFACE_3) {
                                final int surface3ResourceIndex = mResourceIndices.getJSONObject(SURFACE_3).getInt(mContext.getString(R.string.resource_index));
                                final int surface3MediaIndex = mResourceIndices.getJSONObject(SURFACE_3).getJSONArray(mContext.getString(R.string.media_index))
                                        .getJSONObject(surface3ResourceIndex / 4).getInt(mContext.getString(R.string.media_index));
                                Log.d(TAG, "correctRotation: RESOURCE SURFACE OBJECT: " + mResources.get(surface3ResourceIndex).getJSONObject(surface3MediaIndex).get(mContext.getString(R.string.media_type)));
                                if(mResources.get(surface3ResourceIndex).getJSONObject(surface3MediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
                                    Log.d(TAG, "correctRotation: playing video on surface 3.");
                                    setProgressBars(0);
                                    restartPlayer3();
                                    startProgressBar();
                                }
                                else{
                                    setProgressBars(1);
                                    restartProgressBarRunnable();
                                }
                            }
                            else if(mCurrentSurface == SURFACE_4) {
                                final int surface4ResourceIndex = mResourceIndices.getJSONObject(SURFACE_4).getInt(mContext.getString(R.string.resource_index));
                                final int surface4MediaIndex = mResourceIndices.getJSONObject(SURFACE_4).getJSONArray(mContext.getString(R.string.media_index))
                                        .getJSONObject(surface4ResourceIndex / 4).getInt(mContext.getString(R.string.media_index));
                                Log.d(TAG, "correctRotation: RESOURCE SURFACE OBJECT: " + mResources.get(surface4ResourceIndex).getJSONObject(surface4MediaIndex).get(mContext.getString(R.string.media_type)));
                                if(mResources.get(surface4ResourceIndex).getJSONObject(surface4MediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
                                    Log.d(TAG, "correctRotation: playing video on surface 4.");
                                    setProgressBars(0);
                                    restartPlayer4();
                                    startProgressBar();
                                }
                                else{
                                    setProgressBars(1);
                                    restartProgressBarRunnable();
                                }
                            }


                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        mProgressBarInitHandler.removeCallbacks(mProgressBarInitRunnable);
                    }
                }
            };
            mProgressBarInitRunnable.run();
            renderContinuously();


        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void printCurrentResources(){
        Log.d(TAG, "printCurrentResources: printing resources");

        int surface1ResourceIndex = 0;
        try{
            surface1ResourceIndex = mResourceIndices.getJSONObject(SURFACE_1).getInt(mContext.getString(R.string.resource_index));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "printCurrentResources: SURFACE 1 \n---------------------------------------------------------------------------------------");
        for(int i = 0; i < mHighestNumberMedia; i++){
            try {
                Log.d(TAG, "printCurrentResources: SURFACE 1 " + i + " : " + mResources.get(surface1ResourceIndex).getJSONObject(i).get(mContext.getString(R.string.media_type)));
            } catch (JSONException e) {
                Log.e(TAG, "printCurrentResources: SURFACE 1 JSONEXCEPTION: " + e.getMessage() );
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        int surface2ResourceIndex = 0;
        try{
            surface2ResourceIndex = mResourceIndices.getJSONObject(SURFACE_2).getInt(mContext.getString(R.string.resource_index));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "printCurrentResources: SURFACE 2 \n---------------------------------------------------------------------------------------");
        for(int i = 0; i < mHighestNumberMedia; i++){
            try {
                Log.d(TAG, "printCurrentResources: SURFACE 2 " + i + " : " + mResources.get(surface2ResourceIndex).getJSONObject(i).get(mContext.getString(R.string.media_type)));
            } catch (JSONException e) {
                Log.e(TAG, "printCurrentResources: SURFACE 2 JSONEXCEPTION: " + e.getMessage() );
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        int surface3ResourceIndex = 0;
        try{
            surface3ResourceIndex = mResourceIndices.getJSONObject(SURFACE_3).getInt(mContext.getString(R.string.resource_index));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "printCurrentResources: SURFACE 3 \n---------------------------------------------------------------------------------------");
        for(int i = 0; i < mHighestNumberMedia; i++){
            try {
                Log.d(TAG, "printCurrentResources: SURFACE 3 " + i + " : " + mResources.get(surface3ResourceIndex).getJSONObject(i).get(mContext.getString(R.string.media_type)));
            } catch (JSONException e) {
                Log.e(TAG, "printCurrentResources: SURFACE 3 JSONEXCEPTION: " + e.getMessage() );
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
        }


        int surface4ResourceIndex = 0;
        try{
            surface4ResourceIndex = mResourceIndices.getJSONObject(SURFACE_4).getInt(mContext.getString(R.string.resource_index));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "printCurrentResources: SURFACE 4 \n---------------------------------------------------------------------------------------");
        for(int i = 0; i < mHighestNumberMedia; i++){
            try {
                Log.d(TAG, "printCurrentResources: SURFACE 4 " + i + " : " + mResources.get(surface4ResourceIndex).getJSONObject(i).get(mContext.getString(R.string.media_type)));
            } catch (JSONException e) {
                Log.e(TAG, "printCurrentResources: SURFACE 4 JSONEXCEPTION: " + e.getMessage() );
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
        }


//        int surface5ResourceIndex = 0;
//        try{
//            surface5ResourceIndex = mResourceIndices.getJSONObject(SURFACE_1).getInt(mContext.getString(R.string.resource_index));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG, "printCurrentResources: SURFACE 5 \n---------------------------------------------------------------------------------------");
//        for(int i = 0; i < mHighestNumberMedia; i++){
//            try {
//                Log.d(TAG, "printCurrentResources: SURFACE 5 " + i + " : " + mResources.get(4).getJSONObject(i).get(mContext.getString(R.string.media_type)));
//            } catch (JSONException e) {
//                Log.e(TAG, "printCurrentResources: SURFACE 5 JSONEXCEPTION: " + e.getMessage() );
//            }
//            catch (NullPointerException e) {
//                Log.e(TAG, "printCurrentResources: SURFACE 5 NullPointerException: " + e.getMessage() );
//            }
//        }
//
//
//        int surface6ResourceIndex = 0;
//        try{
//            surface6ResourceIndex = mResourceIndices.getJSONObject(SURFACE_2).getInt(mContext.getString(R.string.resource_index));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG, "printCurrentResources: SURFACE 6 \n---------------------------------------------------------------------------------------");
//        for(int i = 0; i < mHighestNumberMedia; i++){
//            try {
//                Log.d(TAG, "printCurrentResources: SURFACE 6 " + i + " : " + mResources.get(5).getJSONObject(i).get(mContext.getString(R.string.media_type)));
//            } catch (JSONException e) {
//                Log.e(TAG, "printCurrentResources: SURFACE 6 JSONEXCEPTION: " + e.getMessage() );
//            }
//            catch (NullPointerException e) {
//                e.printStackTrace();
//            }
//        }
    }

//    private void resetMediaIndex(int surfaceNum, int resourceIndex, int direction) {
//        Log.d(TAG, "resetMediaIndex: direction: " + direction);
//        Log.d(TAG, "resetMediaIndex: resetting media index for resource " + resourceIndex);
//
//        stopPlayers();
//
//        resetPlayerDefaults(surfaceNum);
//        if(surfaceNum == SURFACE_1){
//            hasFirstVideo1Played = false;
//            isImage1Set = false;
//        }
//        else if(surfaceNum == SURFACE_2){
//            hasFirstVideo2Played = false;
//            isImage2Set = false;
//        }
//        else if(surfaceNum == SURFACE_3){
//            hasFirstVideo3Played = false;
//            isImage3Set = false;
//        }
//        else if(surfaceNum == SURFACE_4){
//            hasFirstVideo4Played = false;
//            isImage4Set = false;
//        }
//
//        Log.d(TAG, "resetMediaIndex: resource index: " + resourceIndex);
//
//
//        int numResourcesForIndex = 0;
//        try{
//            numResourcesForIndex = mResources.get(resourceIndex).length();
//        }catch (NullPointerException e){
//            e.printStackTrace();
//        }
//        Log.d(TAG, "resetMediaIndex: num resources for index: " + numResourcesForIndex);
////            Log.d(TAG, "resetMediaIndex: media index: " + mediaIndex);
//        boolean foundFirstVideo = false;
//        for (int i = 0; i < numResourcesForIndex; i++) {
//            Log.d(TAG, "resetMediaIndex: i: " + i);
//            String mediaType = "";
//            try {
//                mediaType = mResources.get(resourceIndex).getJSONObject(i).getString(mContext.getString(R.string.media_type));
//            } catch (NullPointerException e) {
//                mediaType = "none";
//                e.printStackTrace();
//            }
//            catch (JSONException e) {
//                mediaType = "none";
//                e.printStackTrace();
//            }
//            // find the first video and buffer it
//            if(mediaType.equals(mContext.getString(R.string.video_uri)) && !foundFirstVideo){
//                foundFirstVideo = true;
//                bufferFirstVideo(surfaceNum, i);
//            }
//            else if(mediaType.equals(mContext.getString(R.string.video_uri))){
//                bufferNextVideo(surfaceNum, i);
//                break;
//            }
//        }
//    }

    private void resetPlayerDefaults(int surfaceNum){
        if(surfaceNum == SURFACE_1){
            Log.d(TAG, "resetPlayerDefaults: resetting player 1 to defaults.");
            setPlayerState(ACTIVE_PLAYER);
            setSecondaryPlayerState(NOT_ACTIVE_PLAYER);
            mPlayer.release();
            mSecondaryPlayer.release();
            initPlayer1();
        }
        else if(surfaceNum == SURFACE_2){
            Log.d(TAG, "resetPlayerDefaults: resetting player 2 to defaults.");
            setPlayer2State(ACTIVE_PLAYER);
            setSecondaryPlayer2State(NOT_ACTIVE_PLAYER);
            mPlayer2.release();
            mSecondaryPlayer2.release();
            initPlayer2();
        }
        else if(surfaceNum == SURFACE_3){
            Log.d(TAG, "resetPlayerDefaults: resetting player 3 to defaults.");
            setPlayer3State(ACTIVE_PLAYER);
            setSecondaryPlayer3State(NOT_ACTIVE_PLAYER);
            mPlayer3.release();
            mSecondaryPlayer3.release();
            initPlayer3();
        }
        else if(surfaceNum == SURFACE_4){
            Log.d(TAG, "resetPlayerDefaults: resetting player 4 to defaults.");
            setPlayer4State(ACTIVE_PLAYER);
            setSecondaryPlayer4State(NOT_ACTIVE_PLAYER);
            mPlayer4.release();
            mSecondaryPlayer4.release();
            initPlayer4();
        }
    }


    private void rotateCounterClockwise(GL10 gl){
//        Log.d(TAG, "rotateCounterClockwise: rotating.");
//        Log.d(TAG, "rotateCounterClockwise: ");

        if(angleRectangle > 0){
            angleRectangle = 0;
            mRotateCounterClockwise = false;
            if(mCurrentSurface == SURFACE_1){
                unpausePlayer1();
            }
            else if(mCurrentSurface == SURFACE_2){
                unpausePlayer2();
            }
            else if(mCurrentSurface == SURFACE_3){
                unpausePlayer3();
            }
            else if(mCurrentSurface == SURFACE_4){
                unpausePlayer4();
            }
        }
        else if(angleRectangle < mAngleFinished){
            angleRectangle = mAngleFinished;
            mRotateCounterClockwise = false;
            if(mCurrentSurface == SURFACE_1){
                unpausePlayer1();
            }
            else if(mCurrentSurface == SURFACE_2){
                unpausePlayer2();
            }
            else if(mCurrentSurface == SURFACE_3){
                unpausePlayer3();
            }
            else if(mCurrentSurface == SURFACE_4){
                unpausePlayer4();
            }
        }
        else{
            if(Math.abs(angleRectangle) > (Math.abs(settledAngle) + 90)){
                mRotateCounterClockwise = false;
                correctRotation();
                return;
            }
            Log.d(TAG, "rotateCounterClockwise: rotating.");
            if(angleRectangle > settledAngle + 89){
                mRotateCounterClockwise = false;
                correctRotation();
                return;
            }
            else if(angleRectangle < settledAngle - 89){
                mRotateCounterClockwise = false;
                correctRotation();
                return;
            }
            angleRectangle = angleRectangle + STEP_SIZE;
//            Log.d(TAG, "rotateCounterClockwise: angle: " + angleRectangle);
//            Log.d(TAG, "rotateCounterClockwise: %: " + Math.abs(angleRectangle) % 90);
            if(Math.abs(angleRectangle) % 90 > 0 && Math.abs(angleRectangle) % 90 < STEP_SIZE){
                Log.d(TAG, "rotateCounterClockwise: Rotation Complete.");
                mRotateCounterClockwise = false;
                correctRotation();
            }
        }
    }

    private void rotateClockwise(GL10 gl){
//        Log.d(TAG, "rotateClockwise: rotating.");
//        Log.d(TAG, "rotateClockwise: ");
        if(angleRectangle > 0){
            angleRectangle = 0;
            mRotateClockwise = false;
            if(mCurrentSurface == SURFACE_1){
                unpausePlayer1();
            }
            else if(mCurrentSurface == SURFACE_2){
                unpausePlayer2();
            }
            else if(mCurrentSurface == SURFACE_3){
                unpausePlayer3();
            }
            else if(mCurrentSurface == SURFACE_4){
                unpausePlayer4();
            }
        }
        else if(angleRectangle < mAngleFinished){
            angleRectangle = mAngleFinished;
            mRotateClockwise = false;
            if(mCurrentSurface == SURFACE_1){
                unpausePlayer1();
            }
            else if(mCurrentSurface == SURFACE_2){
                unpausePlayer2();
            }
            else if(mCurrentSurface == SURFACE_3){
                unpausePlayer3();
            }
            else if(mCurrentSurface == SURFACE_4){
                unpausePlayer4();
            }
        }
        else{
            if(Math.abs(angleRectangle) > (Math.abs(settledAngle) + 90)){
                mRotateClockwise = false;
                correctRotation();
                return;
            }

            Log.d(TAG, "rotateClockwise: rotating.");
            if(angleRectangle > settledAngle + 89){
                mRotateClockwise = false;
                correctRotation();
                return;
            }
            else if(angleRectangle < settledAngle - 89){
                mRotateClockwise = false;
                correctRotation();
                return;
            }
            angleRectangle = angleRectangle - STEP_SIZE;
//            Log.d(TAG, "rotateClockwise: angle: " + angleRectangle);
//            Log.d(TAG, "rotateClockwise: %: " + Math.abs(angleRectangle) % 90);
            if(Math.abs(angleRectangle) % 90 > 0 && Math.abs(angleRectangle) % 90 < STEP_SIZE && mStartingAngle < 89){
//                Log.d(TAG, "rotateClockwise: Rotation Complete.");
                mRotateClockwise = false;
                correctRotation();
            }
        }
    }

    private void correctRotation(){
        angleRectangle = round(angleRectangle / 10) * 10;
        if(angleRectangle > settledAngle + 89){
            deincrementSurface();
            settledAngle = angleRectangle;
        }
        else if(angleRectangle < settledAngle - 89){
            incrementSurface();
            settledAngle = angleRectangle;
        }
        else{
            try{
                final int surfaceResourceIndex = mResourceIndices.getJSONObject(mCurrentSurface).getInt(mContext.getString(R.string.resource_index));
                final int surfaceMediaIndex = mResourceIndices.getJSONObject(mCurrentSurface).getJSONArray(mContext.getString(R.string.media_index))
                        .getJSONObject(surfaceResourceIndex / 4).getInt(mContext.getString(R.string.media_index));
                Log.d(TAG, "correctRotation: RESOURCE SURFACE OBJECT: " + mResources.get(surfaceResourceIndex).getJSONObject(surfaceMediaIndex).get(mContext.getString(R.string.media_type)));
                if(mCurrentSurface == SURFACE_1) {
                    Log.d(TAG, "correctRotation: playing video on surface 1.");
                    if(mResources.get(surfaceResourceIndex).getJSONObject(surfaceMediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
                        unpausePlayer1();
                    }
                }
                else if(mCurrentSurface == SURFACE_2) {
                    Log.d(TAG, "correctRotation: playing video on surface 2.");
                    if(mResources.get(surfaceResourceIndex).getJSONObject(surfaceMediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
                        unpausePlayer2();
                    }
                }
                else if(mCurrentSurface == SURFACE_3) {
                    Log.d(TAG, "correctRotation: playing video on surface 3.");
                    if(mResources.get(surfaceResourceIndex).getJSONObject(surfaceMediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
                        unpausePlayer3();
                    }
                }
                else if(mCurrentSurface == SURFACE_4) {
                    Log.d(TAG, "correctRotation: playing video on surface 4.");
                    if(mResources.get(surfaceResourceIndex).getJSONObject(surfaceMediaIndex).get(mContext.getString(R.string.media_type)).equals(mContext.getString(R.string.video_uri))) {
                        unpausePlayer4();
                    }
                }

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        Log.d(TAG, "correctRotation: angle: " + angleRectangle);
    }

    private void setDepth(GL10 gl){
        float max = -5.25f;
        float min = -6.25f;
        float A = (max - min) / 2;
        float z;
        double temp = A * -Math.abs(Math.sin(2 * (round(angleRectangle) * pi / 180))) + max;
        String s = String.format("%.2f", temp);
        z = Float.parseFloat(s);
        depth = z;
    }



    /**
     * setup the height, width and depth matrices
     * @param width
     * @param height
     */
    private void setMatrices(float width, float height){

        float[][] heightMatrix = {
                {-height, -height, height, height},
                {-height, -height, height, height},
                {-height, -height, height, height},
                {-height, -height, height, height},
        };
        this.heightMatrix = heightMatrix;

        float[][] widthMatrix = {
                {-width, width, -width, width},
                {width, width, width, width},
                {width, -width, width, -width},
                {-width, -width, -width, -width},
        };
        this.widthMatrix =  widthMatrix;

        float[][] depthMatrix = {
                {width, width, width, width},
                {width, -width, width, -width},
                {-width, -width, -width, -width},
                {-width, width, -width, width},
        };
        this.depthMatrix = depthMatrix;

    }



    public void pausePlayer(){
        //////////////////
        // Player1
        if(mPlayerState == ACTIVE_PLAYER && mPlayer.getPlayWhenReady()){
            Log.d(TAG, "pausePlayer: pausing Player1");
            setPlayerState(PAUSED_PLAYER);
            mPlayer.setPlayWhenReady(false);
        }
        else if (mSecondaryPlayerState == ACTIVE_PLAYER && mSecondaryPlayer.getPlayWhenReady()){
            Log.d(TAG, "pausePlayer: pausing Secondary Player1");
            setSecondaryPlayerState(PAUSED_PLAYER);
            mSecondaryPlayer.setPlayWhenReady(false);
        }

        //////////////////
        // Player2
        else if (mPlayer2State == ACTIVE_PLAYER && mPlayer2.getPlayWhenReady()){
            Log.d(TAG, "pausePlayer: pausing Player2");
            setPlayer2State(PAUSED_PLAYER);
            mPlayer2.setPlayWhenReady(false);
        }
        else if (mSecondaryPlayer2State == ACTIVE_PLAYER && mSecondaryPlayer2.getPlayWhenReady()){
            Log.d(TAG, "pausePlayer: pausing Secondary Player2");
            setSecondaryPlayer2State(PAUSED_PLAYER);
            mSecondaryPlayer2.setPlayWhenReady(false);
        }

        //////////////////
        // Player3
        else if (mPlayer3State == ACTIVE_PLAYER && mPlayer3.getPlayWhenReady()){
            Log.d(TAG, "pausePlayer: pausing Player3");
            setPlayer3State(PAUSED_PLAYER);
            mPlayer3.setPlayWhenReady(false);
        }
        else if (mSecondaryPlayer3State == ACTIVE_PLAYER && mSecondaryPlayer3.getPlayWhenReady()){
            Log.d(TAG, "pausePlayer: pausing Secondary Player3");
            setSecondaryPlayer3State(PAUSED_PLAYER);
            mSecondaryPlayer3.setPlayWhenReady(false);
        }

        //////////////////
        // Player4
        else if (mPlayer4State == ACTIVE_PLAYER && mPlayer4.getPlayWhenReady()){
            Log.d(TAG, "pausePlayer: pausing Player4");
            setPlayer4State(PAUSED_PLAYER);
            mPlayer4.setPlayWhenReady(false);
        }
        else if (mSecondaryPlayer4State == ACTIVE_PLAYER && mSecondaryPlayer4.getPlayWhenReady()){
            Log.d(TAG, "pausePlayer: pausing Secondary Player4");
            setSecondaryPlayer4State(PAUSED_PLAYER);
            mSecondaryPlayer4.setPlayWhenReady(false);
        }
    }

    private void restartProgressBarRunnable(){
        Log.d(TAG, "restartProgressBarRunnable: removing progress runnable callback.");
        if(mProgressRunnable != null){
            mProgressHandler.removeCallbacks(mProgressRunnable);
        }
    }

    public void restartPlayer1(){

//        mCurrentProgress = 0;
        if(mPlayerState == PAUSED_PLAYER && !mPlayer.getPlayWhenReady()){
            Log.d(TAG, "restartPlayer1: unpausing Player1");
            mPlayer.seekTo(0);

            setPlayerState(ACTIVE_PLAYER);
        }
        else if (mSecondaryPlayerState == PAUSED_PLAYER && !mSecondaryPlayer.getPlayWhenReady()){
            Log.d(TAG, "restartPlayer1: unpausing secondary Player1");
            mSecondaryPlayer.seekTo(0);
            setSecondaryPlayerState(ACTIVE_PLAYER);
        }

    }

    public void restartPlayer2(){
//        mCurrentProgress = 0;
        if(mPlayer2State == PAUSED_PLAYER && !mPlayer2.getPlayWhenReady()){
            Log.d(TAG, "restartPlayer2: unpausing Player2");
            mPlayer2.seekTo(0);
            setPlayer2State(ACTIVE_PLAYER);
        }
        else if (mSecondaryPlayer2State == PAUSED_PLAYER && !mSecondaryPlayer2.getPlayWhenReady()){
            Log.d(TAG, "restartPlayer2: unpausing secondary Player2");
            mSecondaryPlayer2.seekTo(0);
            setSecondaryPlayer2State(ACTIVE_PLAYER);
        }
    }

    public void restartPlayer3(){
//        mCurrentProgress = 0;
        if(mPlayer3State == PAUSED_PLAYER && !mPlayer3.getPlayWhenReady()){
            Log.d(TAG, "restartPlayer3: unpausing Player3");
            mPlayer3.seekTo(0);
            setPlayer3State(ACTIVE_PLAYER);
        }
        else if (mSecondaryPlayer3State == PAUSED_PLAYER && !mSecondaryPlayer3.getPlayWhenReady()){
            Log.d(TAG, "restartPlayer3: unpausing secondary Player3");
            mSecondaryPlayer3.seekTo(0);
            setSecondaryPlayer3State(ACTIVE_PLAYER);
        }
    }

    public void restartPlayer4(){
//        mCurrentProgress = 0;
        if(mPlayer4State == PAUSED_PLAYER && !mPlayer4.getPlayWhenReady()){
            Log.d(TAG, "restartPlayer4: unpausing Player4");
            mPlayer4.seekTo(0);
            setPlayer4State(ACTIVE_PLAYER);
        }
        else if (mSecondaryPlayer4State == PAUSED_PLAYER && !mSecondaryPlayer4.getPlayWhenReady()){
            Log.d(TAG, "restartPlayer4: unpausing secondary Player4");
            mSecondaryPlayer4.seekTo(0);
            setSecondaryPlayer4State(ACTIVE_PLAYER);
        }
    }

    public void unpausePlayer1(){
        if(mPlayerState == PAUSED_PLAYER && !mPlayer.getPlayWhenReady()){
            Log.d(TAG, "unpausePlayer1: unpausing Player1");
            setPlayerState(ACTIVE_PLAYER);
        }
        else if (mSecondaryPlayerState == PAUSED_PLAYER && !mSecondaryPlayer.getPlayWhenReady()){
            Log.d(TAG, "unpausePlayer1: unpausing secondary Player1");
            setSecondaryPlayerState(ACTIVE_PLAYER);
        }
    }

    public void unpausePlayer2(){
        if(mPlayer2State == PAUSED_PLAYER && !mPlayer2.getPlayWhenReady()){
            Log.d(TAG, "unpausePlayer2: unpausing Player2");
            setPlayer2State(ACTIVE_PLAYER);
        }
        else if (mSecondaryPlayer2State == PAUSED_PLAYER && !mSecondaryPlayer2.getPlayWhenReady()){
            Log.d(TAG, "unpausePlayer2: unpausing secondary Player2");
            setSecondaryPlayer2State(ACTIVE_PLAYER);
        }
    }

    public void unpausePlayer3(){
        if(mPlayer3State == PAUSED_PLAYER && !mPlayer3.getPlayWhenReady()){
            Log.d(TAG, "unpausePlayer3: unpausing Player3");
            setPlayer3State(ACTIVE_PLAYER);
        }
        else if (mSecondaryPlayer3State == PAUSED_PLAYER && !mSecondaryPlayer3.getPlayWhenReady()){
            Log.d(TAG, "unpausePlayer3: unpausing secondary Player3");
            setSecondaryPlayer3State(ACTIVE_PLAYER);
        }
    }

    public void unpausePlayer4(){
        if(mPlayer4State == PAUSED_PLAYER && !mPlayer4.getPlayWhenReady()){
            Log.d(TAG, "unpausePlayer4: unpausing Player4");
            setPlayer4State(ACTIVE_PLAYER);
        }
        else if (mSecondaryPlayer4State == PAUSED_PLAYER && !mSecondaryPlayer4.getPlayWhenReady()){
            Log.d(TAG, "unpausePlayer4: unpausing secondary Player4");
            setSecondaryPlayer4State(ACTIVE_PLAYER);
        }
    }

    private void pauseRendering(){
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void renderContinuously(){
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void incrementMediaIndex() throws JSONException {
        fillCurrentProgressBar();
        pausePlayer();
        int resourceIndex = mResourceIndices.getJSONObject(mCurrentSurface).getInt(mContext.getString(R.string.resource_index));
        int mediaIndex = mResourceIndices.getJSONObject(mCurrentSurface).getJSONArray(mContext.getString(R.string.media_index))
                .getJSONObject( resourceIndex / 4).getInt(mContext.getString(R.string.media_index));
        Log.d(TAG, "incrementMediaIndex: media index: " + mediaIndex);

        int numResourcesForIndex = mResources.get(resourceIndex).length();
        if(mediaIndex < numResourcesForIndex - 1) {
            Log.d(TAG, "incrementMediaIndex: incrementing index.");
            mediaIndex++;
            JSONObject surfaceObject = mResourceIndices.getJSONObject(mCurrentSurface);
            JSONArray objectArray = surfaceObject.getJSONArray(mContext.getString(R.string.media_index));
            JSONObject mediaIndexObj = objectArray.getJSONObject(resourceIndex / 4);
            mediaIndexObj.put(mContext.getString(R.string.media_index), mediaIndex);
            objectArray.put(resourceIndex / 4, mediaIndexObj);
            surfaceObject.put(mContext.getString(R.string.media_index), objectArray);
            mResourceIndices.put(mCurrentSurface, surfaceObject);
            Log.d(TAG, "incrementMediaIndex: RESOURCE SURFACE OBJECT: " + mResourceIndices.get(mCurrentSurface).toString());
            mVertexBuffers[mCurrentSurface][1] = null;
            mVertexBuffers[mCurrentSurface][2] = null;
            if (mCurrentSurface == SURFACE_1) {
                isImage1Set = false;
            } else if (mCurrentSurface == SURFACE_2) {
                isImage2Set = false;
            } else if (mCurrentSurface == SURFACE_3) {
                isImage3Set = false;
            } else if (mCurrentSurface == SURFACE_4) {
                isImage4Set = false;
            }

            playNextVideo();
            startProgressBar();
        }
        else if(mediaIndex == numResourcesForIndex - 1){
            Log.d(TAG, "incrementMediaIndex: rotating to next surface.");
            mRotateClockwise = true;
        }

    }

    private void stopPlayers(){
        Log.d(TAG, "stopPlayers: stopping players");
        mPlayer.setPlayWhenReady(false);
        mSecondaryPlayer.setPlayWhenReady(false);
        mPlayer2.setPlayWhenReady(false);
        mSecondaryPlayer2.setPlayWhenReady(false);
        mPlayer3.setPlayWhenReady(false);
        mSecondaryPlayer3.setPlayWhenReady(false);
        mPlayer4.setPlayWhenReady(false);
        mSecondaryPlayer4.setPlayWhenReady(false);
    }

    private void bufferFirstVideo(int surfaceNum, int mediaIndex){
        try {
            Log.d(TAG, "bufferFirstVideo: buffering first video.");
            int resourceIndex = mResourceIndices.getJSONObject(surfaceNum).getInt(mContext.getString(R.string.resource_index));
            Log.d(TAG, "bufferFirstVideo: resource index: " + resourceIndex);
            Log.d(TAG, "bufferFirstVideo: media index: " + mediaIndex);
            MediaSource firstMediaSource = (MediaSource) mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.media_source));

            if (firstMediaSource != null) {
                if(surfaceNum == SURFACE_1 && !hasFirstVideo1Played){
                    mPlayer.setPlayWhenReady(false);
                    Log.d(TAG, "bufferFirstVideo: buffering first video for player 1.");
                    mPlayer.prepare(firstMediaSource);
                    if(mediaIndex == 0){
                        initVideoSurface1(PLAYER_ONE);
                        hasFirstVideo1Played = true;
                        initProgressBars();
                    }
                }
                else if(surfaceNum == SURFACE_2 && !hasFirstVideo2Played){
                    mPlayer2.setPlayWhenReady(false);
                    Log.d(TAG, "bufferFirstVideo: buffering first video for player 2.");
                    mPlayer2.prepare(firstMediaSource);
                    if(mediaIndex == 0){
                        initVideoSurface2(PLAYER_TWO);
                        hasFirstVideo2Played = true;
                        initProgressBars();
                    }
                }
                else if(surfaceNum == SURFACE_3 && !hasFirstVideo3Played){
                    mPlayer3.setPlayWhenReady(false);
                    Log.d(TAG, "bufferFirstVideo: buffering first video for player 3.");
                    mPlayer3.prepare(firstMediaSource);
                    if(mediaIndex == 0){
                        initVideoSurface3(PLAYER_THREE);
                        hasFirstVideo3Played = true;
                        initProgressBars();
                    }
                }
                else if(surfaceNum == SURFACE_4 && !hasFirstVideo4Played){
                    mPlayer4.setPlayWhenReady(false);
                    Log.d(TAG, "bufferFirstVideo: buffering first video for player 4.");
                    mPlayer4.prepare(firstMediaSource);
                    if(mediaIndex == 0){
                        initVideoSurface4(PLAYER_FOUR);
                        hasFirstVideo4Played = true;
                        initProgressBars();
                    }
                }
            }
        } catch (JSONException e){
            Log.e(TAG, "bufferFirstVideo: NullPointerException: " + e.getMessage() ); //null pointer for if there is no video
        }
    }


    private void bufferNextVideo(int surfaceNum){
        try {
            // Log.d(TAG, "bufferNextVideo: buffering next video");
            //look for the next video
            int resourceIndex = mResourceIndices.getJSONObject(surfaceNum).getInt(mContext.getString(R.string.resource_index));
            int mediaIndex = mResourceIndices.getJSONObject(surfaceNum).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject(resourceIndex / 4).getInt(mContext.getString(R.string.media_index));
            int numResourcesForIndex = mResources.get(resourceIndex).length();
            Log.d(TAG, "bufferNextVideo: num resources for index: " + numResourcesForIndex);
            MediaSource nextMediaSource = null;
            Log.d(TAG, "bufferNextVideo: media index: " + mediaIndex);
            for (int i = mediaIndex + 1; i < numResourcesForIndex; i++) {
                Log.d(TAG, "bufferNextVideo: i: " + i);
                String mediaType = "";
                try {
                    mediaType = mResources.get(resourceIndex).getJSONObject(i).getString(mContext.getString(R.string.media_type));
                } catch (NullPointerException e) {
                    mediaType = "none";
                    e.printStackTrace();
                }
                catch (JSONException e) {
                    mediaType = "none";
                    e.printStackTrace();
                }

                if (mediaType.equals(mContext.getString(R.string.video_uri))) {
                    //then we have our next video resource
                    nextMediaSource = (MediaSource) mResources.get(resourceIndex).getJSONObject(i).get(mContext.getString(R.string.media_source));
                    Log.d(TAG, "bufferNextVideo: media index, next video: " + i +" , " + mResources.get(resourceIndex).getJSONObject(i).get(mContext.getString(R.string.video_uri)));
                    break;
                }
            }


            if (nextMediaSource != null) {
                bufferMediaSource(surfaceNum, nextMediaSource);
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }


    private void bufferMediaSource(int surfaceNum, MediaSource nextMediaSource){
        Log.d(TAG, "bufferMediaSource: buffering next media source.");
        Log.d(TAG, "bufferMediaSource: surface #: " + surfaceNum);

        if(surfaceNum == SURFACE_1){
            if (mPlayerState == ACTIVE_PLAYER) {
                mSecondaryPlayer.setPlayWhenReady(false);
                Log.d(TAG, "bufferMediaSource: buffering next video for secondary player.");
                mSecondaryPlayer.prepare(nextMediaSource);
            } else if (mSecondaryPlayerState == ACTIVE_PLAYER) {
                mPlayer.setPlayWhenReady(false);
                Log.d(TAG, "bufferMediaSource: buffering next video for player 1.");
                mPlayer.prepare(nextMediaSource);
            }
        }
        else if(surfaceNum == SURFACE_2){
            if (mPlayer2State == ACTIVE_PLAYER) {
                mSecondaryPlayer2.setPlayWhenReady(false);
                Log.d(TAG, "bufferMediaSource: buffering next video for secondary player 2.");
                mSecondaryPlayer2.prepare(nextMediaSource);
            } else if (mSecondaryPlayer2State == ACTIVE_PLAYER) {
                mPlayer2.setPlayWhenReady(false);
                Log.d(TAG, "bufferMediaSource: buffering next video for player 2.");
                mPlayer2.prepare(nextMediaSource);
            }
        }
        else if(surfaceNum == SURFACE_3){
            if (mPlayer3State == ACTIVE_PLAYER) {
                mSecondaryPlayer3.setPlayWhenReady(false);
                Log.d(TAG, "bufferMediaSource: buffering next video for secondary player 3.");
                mSecondaryPlayer3.prepare(nextMediaSource);
            } else if (mSecondaryPlayer3State == ACTIVE_PLAYER) {
                mPlayer3.setPlayWhenReady(false);
                Log.d(TAG, "bufferMediaSource: buffering next video for player 3.");
                mPlayer3.prepare(nextMediaSource);
            }
        }
        else if(surfaceNum == SURFACE_4){
            if (mPlayer4State == ACTIVE_PLAYER) {
                mSecondaryPlayer4.setPlayWhenReady(false);
                Log.d(TAG, "bufferMediaSource: buffering next video for secondary player 4.");
                mSecondaryPlayer4.prepare(nextMediaSource);
            } else if (mSecondaryPlayer4State == ACTIVE_PLAYER) {
                mPlayer4.setPlayWhenReady(false);
                Log.d(TAG, "bufferMediaSource: buffering next video for player 4.");
                mPlayer4.prepare(nextMediaSource);
            }
        }
    }

    private void bufferNextVideo(int surfaceNum, int mediaIndex){
        try {
            int resourceIndex = mResourceIndices.getJSONObject(surfaceNum).getInt(mContext.getString(R.string.resource_index));
            MediaSource nextMediaSource  = (MediaSource) mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.media_source));

            if (nextMediaSource != null) {
                if(surfaceNum == SURFACE_1){
                        if (mPlayerState == ACTIVE_PLAYER) {
                            mSecondaryPlayer.setPlayWhenReady(false);
                            Log.d(TAG, "bufferNextVideo: buffering next video for secondary player1.");
                            mSecondaryPlayer.prepare(nextMediaSource);
                        }
                        else if (mSecondaryPlayerState == ACTIVE_PLAYER) {
                            mPlayer.setPlayWhenReady(false);
                            Log.d(TAG, "bufferNextVideo: buffering next video for player 1.");
                            mPlayer.prepare(nextMediaSource);
                        }
                    }
                    else if(surfaceNum == SURFACE_2){
                        if (mPlayer2State == ACTIVE_PLAYER) {
                            mSecondaryPlayer2.setPlayWhenReady(false);
                            Log.d(TAG, "bufferNextVideo: buffering next video for secondary player 2.");
                            mSecondaryPlayer2.prepare(nextMediaSource);
                        }
                        else if (mSecondaryPlayer2State == ACTIVE_PLAYER) {
                            mPlayer2.setPlayWhenReady(false);
                            Log.d(TAG, "bufferNextVideo: buffering next video for player 2.");
                            mPlayer2.prepare(nextMediaSource);
                        }
                    }
                    else if(surfaceNum == SURFACE_3){
                        if (mPlayer3State == ACTIVE_PLAYER) {
                            mSecondaryPlayer3.setPlayWhenReady(false);
                            Log.d(TAG, "bufferNextVideo: buffering next video for secondary player 3.");
                            mSecondaryPlayer3.prepare(nextMediaSource);
                        }
                        else if (mSecondaryPlayer3State == ACTIVE_PLAYER) {
                            mPlayer3.setPlayWhenReady(false);
                            Log.d(TAG, "bufferNextVideo: buffering next video for player 3.");
                            mPlayer3.prepare(nextMediaSource);
                        }
                    }
                    else if(surfaceNum == SURFACE_4){
                        if (mPlayer4State == ACTIVE_PLAYER) {
                            mSecondaryPlayer4.setPlayWhenReady(false);
                            Log.d(TAG, "bufferNextVideo: buffering next video for secondary player 4.");
                            mSecondaryPlayer4.prepare(nextMediaSource);
                        }
                        else if (mSecondaryPlayer4State == ACTIVE_PLAYER) {
                            mPlayer4.setPlayWhenReady(false);
                            Log.d(TAG, "bufferNextVideo: buffering next video for player 4.");
                            mPlayer4.prepare(nextMediaSource);
                        }
                }
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void playNextVideo(){
        try{
            int resourceIndex = mResourceIndices.getJSONObject(mCurrentSurface).getInt(mContext.getString(R.string.resource_index));
            int mediaIndex = mResourceIndices.getJSONObject(mCurrentSurface).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject(resourceIndex / 4).getInt(mContext.getString(R.string.media_index));
            String currentMediaType = mResources.get(resourceIndex).getJSONObject(mediaIndex).getString(mContext.getString(R.string.media_type));
            Log.d(TAG, "playNextVideo: resource: " + mResourceIndices.getJSONObject(mCurrentSurface));
            if(currentMediaType.equals(mContext.getString(R.string.video_uri))){
                if(mCurrentSurface == SURFACE_1) {
                    Log.d(TAG, "playNextVideo: current surface is 1.");
                    if(hasFirstVideo1Played){
                        Log.d(TAG, "playNextVideo: first video on surface 1 has played.");
                        if(mPlayerState == ACTIVE_PLAYER || mPlayerState == PAUSED_PLAYER){
                            Log.d(TAG, "playNextVideo: init secondary player1 surface.");
                            setPlayerState(NOT_ACTIVE_PLAYER);
                            initVideoSurface1(PLAYER_ONE_SECONDARY);
                            setSecondaryPlayerState(ACTIVE_PLAYER);
                        }
                        else if(mSecondaryPlayerState == ACTIVE_PLAYER || mSecondaryPlayerState == PAUSED_PLAYER){
                            Log.d(TAG, "playNextVideo: init player1 surface.");
                            setSecondaryPlayerState(NOT_ACTIVE_PLAYER);
                            initVideoSurface1(PLAYER_ONE);
                            setPlayerState(ACTIVE_PLAYER);
                        }
                    }
                    else{
                        Log.d(TAG, "playNextVideo: hasFirstVideoPlayed1 is now TRUE.");
                        initVideoSurface1(PLAYER_ONE);
                        hasFirstVideo1Played = true;
                    }

                    bufferNextVideo(SURFACE_1);
                }
                else if(mCurrentSurface == SURFACE_2) {
                    Log.d(TAG, "playNextVideo: current surface is 2.");
                    if(hasFirstVideo2Played){
                        Log.d(TAG, "playNextVideo: first video on surface 2 has played.");
                        if(mPlayer2State == ACTIVE_PLAYER || mPlayer2State == PAUSED_PLAYER){
                            Log.d(TAG, "playNextVideo: init secondary player2 surface.");
                            setPlayer2State(NOT_ACTIVE_PLAYER);
                            initVideoSurface2(PLAYER_TWO_SECONDARY);
                            setSecondaryPlayer2State(ACTIVE_PLAYER);
                        }
                        else if(mSecondaryPlayer2State == ACTIVE_PLAYER || mSecondaryPlayer2State == PAUSED_PLAYER){
                            Log.d(TAG, "playNextVideo: init player2 surface.");
                            setSecondaryPlayer2State(NOT_ACTIVE_PLAYER);
                            initVideoSurface2(PLAYER_TWO);
                            setPlayer2State(ACTIVE_PLAYER);
                        }
                    }
                    else{
                        Log.d(TAG, "playNextVideo: hasFirstVideoPlayed2 is now TRUE.");
                        initVideoSurface2(PLAYER_TWO);
                        hasFirstVideo2Played = true;
                    }

                    bufferNextVideo(SURFACE_2);
                }
                else if(mCurrentSurface == SURFACE_3) {
                    Log.d(TAG, "playNextVideo: current surface is 3.");
                    if(hasFirstVideo3Played){
                        Log.d(TAG, "playNextVideo: first video on surface 3 has played.");
                        if(mPlayer3State == ACTIVE_PLAYER || mPlayer3State == PAUSED_PLAYER){
                            Log.d(TAG, "playNextVideo: init secondary player3 surface.");
                            setPlayer3State(NOT_ACTIVE_PLAYER);
                            initVideoSurface3(PLAYER_THREE_SECONDARY);
                            setSecondaryPlayer3State(ACTIVE_PLAYER);
                        }
                        else if(mSecondaryPlayer3State == ACTIVE_PLAYER || mSecondaryPlayer3State == PAUSED_PLAYER){
                            Log.d(TAG, "playNextVideo: init player3 surface.");
                            setSecondaryPlayer3State(NOT_ACTIVE_PLAYER);
                            initVideoSurface3(PLAYER_THREE);
                            setPlayer3State(ACTIVE_PLAYER);
                        }
                    }
                    else{
                        Log.d(TAG, "playNextVideo: hasFirstVideoPlayed3 is now TRUE.");
                        initVideoSurface3(PLAYER_THREE);
                        hasFirstVideo3Played = true;
                    }

                    bufferNextVideo(SURFACE_3);
                }
                else if(mCurrentSurface == SURFACE_4) {
                    Log.d(TAG, "playNextVideo: current surface is 4.");
                    if(hasFirstVideo4Played){
                        Log.d(TAG, "playNextVideo: first video on surface 4 has played.");
                        if(mPlayer4State == ACTIVE_PLAYER || mPlayer4State == PAUSED_PLAYER){
                            Log.d(TAG, "playNextVideo: init secondary player4 surface.");
                            setPlayer4State(NOT_ACTIVE_PLAYER);
                            initVideoSurface4(PLAYER_FOUR_SECONDARY);
                            setSecondaryPlayer4State(ACTIVE_PLAYER);
                        }
                        else if(mSecondaryPlayer4State == ACTIVE_PLAYER || mSecondaryPlayer4State == PAUSED_PLAYER){
                            Log.d(TAG, "playNextVideo: init player4 surface.");
                            setSecondaryPlayer4State(NOT_ACTIVE_PLAYER);
                            initVideoSurface4(PLAYER_FOUR);
                            setPlayer4State(ACTIVE_PLAYER);
                        }
                    }
                    else{
                        Log.d(TAG, "playNextVideo: hasFirstVideoPlayed4 is now TRUE.");
                        initVideoSurface4(PLAYER_FOUR);
                        hasFirstVideo4Played = true;
                    }

                    bufferNextVideo(SURFACE_4);
                }
            }

    } catch (JSONException e){
        Log.e(TAG, "bufferNextVideo: NullPointerException: " + e.getMessage() ); //null pointer for if there is no video
    }
    }


    private void initVideoSurface1(final int player){
        mSurfaceTexture = new SurfaceTexture(textureId1[0]);

        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mUpdateST = true;
                mGLView.requestRender();

            }
        });

        mSurface = new Surface(mSurfaceTexture);
        if(player == PLAYER_ONE){
            mPlayer.setVideoSurface(mSurface);
        }
        else if(player == PLAYER_ONE_SECONDARY){
            mSecondaryPlayer.setVideoSurface(mSurface);
        }
        
    }



    private void initVideoSurface2(final int player){

        mSurfaceTexture2 = new SurfaceTexture(textureId2[0]);

        mSurfaceTexture2.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mUpdateST2 = true;
                mGLView.requestRender();
            }
        });

        mSurface2 = new Surface(mSurfaceTexture2);
        if(player == PLAYER_TWO){
            mPlayer2.setVideoSurface(mSurface2);
        }
        else if(player == PLAYER_TWO_SECONDARY){
            mSecondaryPlayer2.setVideoSurface(mSurface2);
        }
    }

    private void initVideoSurface3(final int player){

        mSurfaceTexture3 = new SurfaceTexture(textureId3[0]);
        mSurfaceTexture3.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mUpdateST3 = true;
                mGLView.requestRender();
            }
        });
        mSurface3 = new Surface(mSurfaceTexture3);
        if(player == PLAYER_THREE){
            mPlayer3.setVideoSurface(mSurface3);
        }
        else if(player == PLAYER_THREE_SECONDARY){
            mSecondaryPlayer3.setVideoSurface(mSurface3);
        }
    }

    private void initVideoSurface4(final int player){
        mSurfaceTexture4 = new SurfaceTexture(textureId4[0]);
        mSurfaceTexture4.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mUpdateST4 = true;
                mGLView.requestRender();
            }
        });
        mSurface4 = new Surface(mSurfaceTexture4);
        if(player == PLAYER_FOUR){
            mPlayer4.setVideoSurface(mSurface4);
        }
        else if(player == PLAYER_FOUR_SECONDARY){
            mSecondaryPlayer4.setVideoSurface(mSurface4);
        }
    }



    private void setSecondaryPlayerState(int state){
        mSecondaryPlayerState = state;
    }
    private void setPlayerState(int state){
        mPlayerState = state;
    }
    private void setSecondaryPlayer2State(int state){
        mSecondaryPlayer2State = state;
    }
    private void setPlayer2State(int state){
        mPlayer2State = state;
    }
    private void setSecondaryPlayer3State(int state){
        mSecondaryPlayer3State = state;
    }
    private void setPlayer3State(int state){
        mPlayer3State = state;
    }
    private void setSecondaryPlayer4State(int state){
        mSecondaryPlayer4State = state;
    }
    private void setPlayer4State(int state){
        mPlayer4State = state;
    }


    private void initBlock(){
        float screenRatio = screenHeight / screenWidth;
        float width = 1;
        float height = screenRatio;
        Log.d(TAG, "initBlock: SCREEN WIDTH: " + screenWidth);
        Log.d(TAG, "initBlock: SCREEN HEIGHT: " + screenHeight);

        setMatrices(width, height);
        float widthScaleFactor = 1f;
        float heightScaleFactor = 1f;

        //set the vertices and bitmaps to textures
        for (int i = 0; i < numFaces; i++) {
            Log.d(TAG, "initBlock: adding vertices to list.");

            // Define the vertices for this face
            float[] imageVertices = {
                    widthMatrix[i][0] * widthScaleFactor, heightMatrix[i][0] * heightScaleFactor, depthMatrix[i][0],
                    widthMatrix[i][1] * widthScaleFactor, heightMatrix[i][1] * heightScaleFactor, depthMatrix[i][1],
                    widthMatrix[i][2] * widthScaleFactor, heightMatrix[i][2] * heightScaleFactor, depthMatrix[i][2],
                    widthMatrix[i][3] * widthScaleFactor, heightMatrix[i][3] * heightScaleFactor, depthMatrix[i][3],
            };
            mVertices.add(imageVertices);

        }
//
        mVertexBuffers = new FloatBuffer[5][3];
//
        ByteBuffer vbb = ByteBuffer.allocateDirect(mVertices.get(0).length * 6 * 4);
        vbb.order(ByteOrder.nativeOrder());

        float[] tempTexCoords = { // Allocate texture buffer. An float has 4 bytes. Repeat for 4 faces.
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
        };

        texCoords1 = tempTexCoords;
        texCoords2 = tempTexCoords;
        texCoords3 = tempTexCoords;
        texCoords4 = tempTexCoords;


        // Setup texture-coords-array buffer, in float. An float has 4 bytes (NEW)
        // There's 8 coordinates per face and 4 texture coordinate buffers
        // so 8 x 4 x 4 or texCoords.length x 4 x 4
        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords1.length * 4 * 4);
        tbb.order(ByteOrder.nativeOrder());
        textureBuffer1 = tbb.asFloatBuffer();
        textureBuffer2 = tbb.asFloatBuffer();
        textureBuffer3 = tbb.asFloatBuffer();
        textureBuffer4 = tbb.asFloatBuffer();

        textureBuffer1.put(texCoords1);
        textureBuffer1.position(0);

        textureBuffer2.put(texCoords2);
        textureBuffer2.position(0);

        textureBuffer3.put(texCoords3);
        textureBuffer3.position(0);

        textureBuffer4.put(texCoords4);
        textureBuffer4.position(0);

//        rotateToStartingIndex();
    }


    public void releasePlayers(){
        mPlayer.release();
        mSecondaryPlayer.release();
        mPlayer2.release();
        mSecondaryPlayer2.release();
        mPlayer3.release();
        mSecondaryPlayer3.release();
        mPlayer4.release();
        mSecondaryPlayer4.release();

        removeAllCallbacks();
    }

    private void removeAllCallbacks(){
        if(mProgressBarInitHandler != null){
            mProgressBarInitHandler.removeCallbacks(mProgressBarInitRunnable);
        }
        if(mProgressHandler != null){
            mProgressHandler.removeCallbacks(mProgressRunnable);
        }

    }

    public void releasePlayer1(){
        mPlayer.release();
    }

    public void releasePlayer2(){
        mSecondaryPlayer.release();
    }

    private void initPlayers(){
        mVideoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        mTrackSelector = new DefaultTrackSelector(mVideoTrackSelectionFactory);

        initPlayer1();
        initPlayer2();
        initPlayer3();
        initPlayer4();
    }

    private void initPlayer1(){
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector);
        mSecondaryPlayer = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector);
        mPlayer.setPlayWhenReady(false);
        mSecondaryPlayer.setPlayWhenReady(false);
    }

    private void initPlayer2(){
        mPlayer2 = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector);
        mSecondaryPlayer2 = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector);
        mPlayer2.setPlayWhenReady(false);
        mSecondaryPlayer2.setPlayWhenReady(false);
    }

    private void initPlayer3(){
        mPlayer3 = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector);
        mSecondaryPlayer3 = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector);
        mPlayer3.setPlayWhenReady(false);
        mSecondaryPlayer3.setPlayWhenReady(false);
    }

    private void initPlayer4(){
        mPlayer4 = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector);
        mSecondaryPlayer4 = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector);
        mPlayer4.setPlayWhenReady(false);
        mSecondaryPlayer4.setPlayWhenReady(false);
    }


    public MediaSource buildMediaSource(Uri uri){
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, "MitchsApp"), null);
        return new ExtractorMediaSource(uri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);
    }

    private void showProgressBar(){
        Log.d(TAG, "showProgressBar: showing progress bar.");
//        ProgressBar mProgressBar = ((Activity)mContext).findViewById(PROGRESS_BAR_ID);
//        mProgressBar.setVisibility(View.VISIBLE);
//        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(mContext.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                ProgressBar mProgressBar = ((Activity)mContext).findViewById(PROGRESS_BAR_ID);
                mProgressBar.bringToFront();
                mProgressBar.setVisibility(View.VISIBLE);
            }
        };
        mainHandler.post(myRunnable);
    }

    private void hideProgressBar(){
        Log.d(TAG, "hideProgressBar: hiding progress bar.");
//        ProgressBar mProgressBar = ((Activity)mContext).findViewById(PROGRESS_BAR_ID);
//        mProgressBar.setVisibility(View.INVISIBLE);
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(mContext.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                ProgressBar mProgressBar = ((Activity)mContext).findViewById(PROGRESS_BAR_ID);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        };
        mainHandler.post(myRunnable);
    }

    private static final int PROGRESS_BAR_ID = 123456;
    private void initProgressBar(){
        Log.d(TAG, "initProgressBar: initializing progress bar.");

        //post to main ui thread
        Handler handler = new Handler(mContext.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(180,180);
                rlp.addRule(RelativeLayout.CENTER_IN_PARENT);

                ProgressBar mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleLarge);
                mProgressBar.setId(PROGRESS_BAR_ID);
                mProgressBar.setVisibility(View.INVISIBLE);
                mProgressBar.setLayoutParams(rlp);
                mRelativeLayout.addView(mProgressBar);

            }
        };
        handler.post(runnable);
    }

    private void initProgressBars(){
        Log.d(TAG, "initProgressBars: initializing progress bar widgets.");

        try{
            //remove the previous surface's progress bars
            Handler handler = new Handler(mContext.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mLinearLayout.removeAllViews();
                    mLinearLayout2.removeAllViews();
                }
            };
            handler.post(runnable);

            //divide the width evenly between the media sources for this index
            final int resourceIndex = mResourceIndices.getJSONObject(mCurrentSurface).getInt(mContext.getString(R.string.resource_index));
//            int numSources = mMedia.get(resourceIndex).getMedia().size();
            int numSources = mUserStories.getJSONObject(resourceIndex)
                    .getJSONArray(mContext.getString(R.string.user_stories)).length();
            final int mediaIndex = mResourceIndices.getJSONObject(mCurrentSurface).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject(resourceIndex / 4).getInt(mContext.getString(R.string.media_index));
            Log.d(TAG, "initProgressBars: media index: " + mediaIndex);
            Log.d(TAG, "initProgressBars: sources: " + numSources);
            final int width = ((int) screenWidth / numSources) - (int) (screenWidth * 0.01);
            mIds = new int[numSources];

            //make the progress bars and add them to the layout
            //their id's will be saved in the 'mIds' array
            for(int i = 0; i < numSources; i++){
                Log.d(TAG, "initProgressBars: i: " + i);

                final int count = i;
                //take action on main UI thread
                Handler mainHandler = new Handler(mContext.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "initProgressBars: adding a progress bar. count: " + count);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                width,
                                5
//                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                        layoutParams.setMargins(5, 0 ,0, 0);
                        MyProgressBar progressBar = new MyProgressBar(mContext,
                                null,
                                android.R.attr.progressBarStyleHorizontal);
                        progressBar.setLayoutParams(layoutParams);

                        Drawable progressDrawable = progressBar.getProgressDrawable().mutate();
                        progressDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                        progressBar.setProgressDrawable(progressDrawable);

                        progressBar.setId(count);
                        mIds[count] = progressBar.getId();
                        mLinearLayout.addView(progressBar);

                        if(count == 0){
                            //set the flag so we know the progress bars are initialized
                            isProgressBarsInitialized = true;
                            startProgressBar();

                            LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                                    100,
                                    100
                            );

                            //get the profile image from array
                            int resourceIndex = -1;
                            String profileUrl = "";
                            String username = "";
                            try {
                                resourceIndex = mResourceIndices.getJSONObject(mCurrentSurface).getInt(mContext.getString(R.string.resource_index));
                                profileUrl = mUserStories.getJSONObject(resourceIndex).getJSONObject(mContext.getString(R.string.user_account_settings))
                                        .get(mContext.getString(R.string.field_profile_photo)).toString();
                                username = mUserStories.getJSONObject(resourceIndex).getJSONObject(mContext.getString(R.string.user_account_settings))
                                        .get(mContext.getString(R.string.field_username)).toString();
                                Log.d(TAG, "initProgressBars: got the profile url: " + profileUrl);
                                Log.d(TAG, "initProgressBars: got the username: " + username);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            imageViewParams.setMargins(15, 8, 0, 0);
                            //add circle image view
                            CircleImageView profileImage = new CircleImageView(mContext);
                            profileImage.setVisibility(View.VISIBLE);
                            profileImage.bringToFront();
                            profileImage.setLayoutParams(imageViewParams);
                            if(!profileUrl.equals("")){
                                Glide.with(mContext)
                                        .load(profileUrl)
                                        .into(profileImage);
                            }
                            else{
                                profileImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.android_construction));
                            }

                            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            textViewParams.setMargins(20, 25, 0, 0);
                            TextView name = new TextView(mContext);
                            name.setVisibility(View.VISIBLE);
                            name.bringToFront();
                            name.setTextSize(14f);
                            name.setTextColor(Color.WHITE);
                            name.setLayoutParams(textViewParams);
                            if(!username.equals("")){
                                name.setText(username);
                            }
                            else{
                                name.setText("N/A");
                            }

                            mLinearLayout2.addView(profileImage);
                            mLinearLayout2.addView(name);
                            mLinearLayout2.bringToFront();
                            mLinearLayout2.setVisibility(View.VISIBLE);
                        }

                    }
                };
                mainHandler.post(myRunnable);

            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void setProgressBars(int number){
        Log.d(TAG, "setProgressBars: setting progress bar to match media index.");
        try{
            int resourceIndex = mResourceIndices.getJSONObject(mCurrentSurface).getInt(mContext.getString(R.string.resource_index));
            int surfaceMediaIndexCount = mResourceIndices.getJSONObject(mCurrentSurface).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject( resourceIndex / 4).getInt(mContext.getString(R.string.media_index));
            Log.d(TAG, "setProgressBars: media index count: " + surfaceMediaIndexCount);
            //iterate through the media index and fill the progress bars
            for(int i = 0; i < surfaceMediaIndexCount + number; i++){
                Log.d(TAG, "setProgressBars: filling progress bar with id = " + i);
                MyProgressBar progressBar = ((Activity) mContext).findViewById(mIds[i]);

                progressBar.setMax(1);
                progressBar.setProgress(1);

                //MyProgressBar stuff
                progressBar.setCurrentProgress(1);
                progressBar.setTotalDuration(1);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

//        startProgressBar(); //fill the current bar
    }



    private void startProgressBar(){
        Log.d(TAG, "startProgressBar: starting progress bar.");
        try{
            int resourceIndex = mResourceIndices.getJSONObject(mCurrentSurface).getInt(mContext.getString(R.string.resource_index));
            int surfaceMediaIndex = mResourceIndices.getJSONObject(mCurrentSurface).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject( resourceIndex / 4).getInt(mContext.getString(R.string.media_index));
            mCurrentProgressBar = ((Activity) mContext).findViewById(mIds[surfaceMediaIndex]);

            if(mCurrentProgressBar != null){
                Log.d(TAG, "startProgressBar: current progress bar is not null.");

                String resourceType = "";
                boolean imageRenderError = false;
                try{
                    resourceType = mResources.get(resourceIndex).getJSONObject(surfaceMediaIndex).get(mContext.getString(R.string.media_type)).toString();
                }catch (JSONException e){
                    imageRenderError = true;
                }

                mCurrentProgress = 0;
                if (resourceType.equals(mContext.getString(R.string.encoded_bitmap)) || imageRenderError) {
                    Log.d(TAG, "startProgressBar: next resource is an image.");
                    mCurrentProgress = 1;
                    mCurrentProgressBar.setMax(mCurrentProgress);
                    mCurrentProgressBar.setProgress(mCurrentProgress);

                    //MyProgressBar stuff
                    mCurrentProgressBar.setCurrentProgress(mCurrentProgress);
                    mCurrentProgressBar.setTotalDuration(mCurrentProgress);

                    //hide the circular progress bar if it's showing
                    hideProgressBar();
                }
                else if(resourceType.equals(mContext.getString(R.string.video_uri))){
                    Log.d(TAG, "startProgressBar: next resource is a video.");

                    //get the total duration
                    try{
                        mTotalDuration = Integer.parseInt(mResources.get(resourceIndex).getJSONObject(surfaceMediaIndex)
                                .get(mContext.getString(R.string.duration)).toString()) * 1000;
                    }catch (JSONException e){
                        mTotalDuration = MEDIA_TIMEOUT;
                    }

                    if(getCurrentPlayer() == PLAYER_ONE){
                        Log.d(TAG, "startProgressBar: starting progress bar for player1");
                        startProgressRunnable(mPlayer);
                    }
                    else if(getCurrentPlayer() == PLAYER_ONE_SECONDARY){
                        Log.d(TAG, "startProgressBar: starting progress bar for secondary player1");
                        startProgressRunnable(mSecondaryPlayer);
                    }
                    else if(getCurrentPlayer() == PLAYER_TWO){
                        Log.d(TAG, "startProgressBar: starting progress bar for player2");
                        startProgressRunnable(mPlayer2);
                    }
                    else if(getCurrentPlayer() == PLAYER_TWO_SECONDARY){
                        Log.d(TAG, "startProgressBar: starting progress bar for secondary player2");
                        startProgressRunnable(mSecondaryPlayer2);
                    }
                    else if(getCurrentPlayer() == PLAYER_THREE){
                        Log.d(TAG, "startProgressBar: starting progress bar for player3");
                        startProgressRunnable(mPlayer3);
                    }
                    else if(getCurrentPlayer() == PLAYER_THREE_SECONDARY){
                        Log.d(TAG, "startProgressBar: starting progress bar for secondary player3");
                        startProgressRunnable(mSecondaryPlayer3);
                    }
                    else if(getCurrentPlayer() == PLAYER_FOUR){
                        Log.d(TAG, "startProgressBar: starting progress bar for player4");
                        startProgressRunnable(mPlayer4);
                    }
                    else if(getCurrentPlayer() == PLAYER_FOUR_SECONDARY){
                        Log.d(TAG, "startProgressBar: starting progress bar for secondary player4");
                        startProgressRunnable(mSecondaryPlayer4);
                    }
                }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void startProgressRunnable(final Player player_){
        Log.d(TAG, "startProgressRunnable: attempting to start progress runnable.");
        if(player_ != null){
            Log.d(TAG, "startProgressRunnable: starting the progress runnable for progress updates.");

            if(mProgressRunnable != null){
                Log.d(TAG, "startProgressRunnable: TIMEOUT.");
                mProgressHandler.removeCallbacks(mProgressRunnable);
                mProgressRunnable = null;
            }
            mCurrentProgress = 0;
            frameAvailableCount = 0;
            videoRetryTimer = 0;
            mProgressHandler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == INIT_VIDEO_PROGRESS_BAR){
                        Log.d(TAG, "startProgressRunnable: initializing progress bar for video: " + mCurrentProgress);
                        mCurrentProgressBar.setMax(mTotalDuration);
                        mCurrentProgressBar.setProgress(mCurrentProgress);

                        //MyProgressBar stuff
                        mCurrentProgressBar.setCurrentProgress(mCurrentProgress);
                        mCurrentProgressBar.setTotalDuration(mTotalDuration);

                        //show the circular progress bar
                        showProgressBar();
                    }
                    else if(msg.what == UPDATE_UI_WITH_VIDEO_PROGRESS){
                        Log.d(TAG, "startProgressRunnable: updating UI thread with progress: " + mCurrentProgress);
                        mCurrentProgressBar.setProgress(mCurrentProgress);
                    }
                    else if(msg.what == REMOVE_PROGRESS_BAR_CALLBACKS){
                        Log.d(TAG, "startProgressRunnable: updating UI thread with progress: " + mCurrentProgress);
                        mProgressHandler.removeCallbacks(mProgressRunnable);
                    }
                    else if(msg.what == HIDE_PROGRESS_BAR){
                        Log.d(TAG, "startProgressRunnable: Hiding circular progress bar from UI Thread");
                        hideProgressBar();
                    }
                    else if(msg.what == getCurrentPlayer()) {
                        Log.d(TAG, "startProgressRunnable: Retrying video playback.");
                        showProgressBar();
                        try {
                            int resourceIndex = mResourceIndices.getJSONObject(mCurrentSurface).getInt(mContext.getString(R.string.resource_index));
                            int mediaIndex = mResourceIndices.getJSONObject(mCurrentSurface).getJSONArray(mContext.getString(R.string.media_index))
                                    .getJSONObject( resourceIndex / 4).getInt(mContext.getString(R.string.media_index));
                            MediaSource nextMediaSource = (MediaSource) mResources.get(resourceIndex).getJSONObject(mediaIndex).get(mContext.getString(R.string.media_source));
                            if(getCurrentPlayer() == PLAYER_ONE){
                                Log.d(TAG, "startProgressRunnable: attempting to restart player1");
                                mPlayer.prepare(nextMediaSource);
                                mPlayer.setPlayWhenReady(true);
                            }
                            else if(getCurrentPlayer() == PLAYER_ONE_SECONDARY){
                                Log.d(TAG, "startProgressRunnable: attempting to restart secondary player1");
                                mSecondaryPlayer.prepare(nextMediaSource);
                                mSecondaryPlayer.setPlayWhenReady(true);
                            }
                            else if(getCurrentPlayer() == PLAYER_TWO){
                                Log.d(TAG, "startProgressRunnable: attempting to restart player2");
                                mPlayer2.prepare(nextMediaSource);
                                mPlayer2.setPlayWhenReady(true);
                            }
                            else if(getCurrentPlayer() == PLAYER_TWO_SECONDARY){
                                Log.d(TAG, "startProgressRunnable: attempting to restart secondary player2");
                                mSecondaryPlayer2.prepare(nextMediaSource);
                                mSecondaryPlayer2.setPlayWhenReady(true);
                            }
                            else if(getCurrentPlayer() == PLAYER_THREE){
                                Log.d(TAG, "startProgressRunnable: attempting to restart player3");
                                mPlayer3.prepare(nextMediaSource);
                                mPlayer3.setPlayWhenReady(true);
                            }
                            else if(getCurrentPlayer() == PLAYER_THREE_SECONDARY){
                                Log.d(TAG, "startProgressRunnable: attempting to restart secondary player3");
                                mSecondaryPlayer3.prepare(nextMediaSource);
                                mSecondaryPlayer3.setPlayWhenReady(true);
                            }
                            else if(getCurrentPlayer() == PLAYER_FOUR){
                                Log.d(TAG, "startProgressRunnable: attempting to restart player4");
                                mPlayer4.prepare(nextMediaSource);
                                mPlayer4.setPlayWhenReady(true);
                            }
                            else if(getCurrentPlayer() == PLAYER_FOUR_SECONDARY){
                                Log.d(TAG, "startProgressRunnable: attempting to restart secondary player4");
                                mSecondaryPlayer4.prepare(nextMediaSource);
                                mSecondaryPlayer4.setPlayWhenReady(true);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            mProgressRunnable = new Runnable() {
                @Override
                public void run() {
                    mProgressHandler.postDelayed(this, 200);
                    int progress = 0;
                    boolean isPlaying = false;
                    if(getCurrentPlayer() == PLAYER_ONE){
                        progress = (int) mPlayer.getCurrentPosition();
                        if(mPlayer.getPlayWhenReady()){
                            isPlaying = true;
                        }
                        if(!mUpdateST){
                            frameAvailableCount++;
                            Log.d(TAG, "startProgressRunnable: player1 frame Available count: " + frameAvailableCount);
                            if(frameAvailableCount >= VIDEO_REFRESH_COUNT_LIMIT){
                                Log.d(TAG, "startProgressRunnable: forcing frame refresh on player1.");
                                mUpdateST = true;
                            }
                        }
                        else{
                            frameAvailableCount = 0;
                        }
                        Log.d(TAG, "startProgressRunnable: player 1 current progress: " + progress);
                    }
                    else if(getCurrentPlayer() == PLAYER_ONE_SECONDARY){
                        progress = (int) mSecondaryPlayer.getCurrentPosition();
                        if(mSecondaryPlayer.getPlayWhenReady()){
                            isPlaying = true;
                        }
                        if(!mUpdateST){
                            frameAvailableCount++;
                            Log.d(TAG, "startProgressRunnable: secondary player1 frame Available count: " + frameAvailableCount);
                            if(frameAvailableCount >= VIDEO_REFRESH_COUNT_LIMIT){
                                Log.d(TAG, "startProgressRunnable: forcing frame refresh on secondary player1.");
                                mUpdateST = true;
                            }
                        }
                        else{
                            frameAvailableCount = 0;
                        }
                        Log.d(TAG, "startProgressRunnable: current progress: " + progress);
                    }
                    else if(getCurrentPlayer() == PLAYER_TWO){
                        progress = (int) mPlayer2.getCurrentPosition();
                        if(mPlayer2.getPlayWhenReady()){
                            isPlaying = true;
                        }
                        if(!mUpdateST2){
                            frameAvailableCount++;
                            Log.d(TAG, "startProgressRunnable: player2 frame Available count: " + frameAvailableCount);
                            if(frameAvailableCount >= VIDEO_REFRESH_COUNT_LIMIT){
                                Log.d(TAG, "startProgressRunnable: forcing frame refresh on player2.");
                                mUpdateST2 = true;
                            }
                        }
                        else{
                            frameAvailableCount = 0;
                        }
                        Log.d(TAG, "startProgressRunnable: current progress: " + progress);
                    }
                    else if(getCurrentPlayer() == PLAYER_TWO_SECONDARY){
                        progress = (int) mSecondaryPlayer2.getCurrentPosition();
                        if(mSecondaryPlayer2.getPlayWhenReady()){
                            isPlaying = true;
                        }
                        if(!mUpdateST2){
                            frameAvailableCount++;
                            Log.d(TAG, "startProgressRunnable: secondary player2 frame Available count: " + frameAvailableCount);
                            if(frameAvailableCount >= VIDEO_REFRESH_COUNT_LIMIT){
                                Log.d(TAG, "startProgressRunnable: forcing frame refresh on secondary player2.");
                                mUpdateST2 = true;
                            }
                        }
                        else{
                            frameAvailableCount = 0;
                        }
                        Log.d(TAG, "startProgressRunnable: current progress: " + progress);
                    }
                    else if(getCurrentPlayer() == PLAYER_THREE){
                        progress = (int) mPlayer3.getCurrentPosition();
                        if(mPlayer3.getPlayWhenReady()){
                            isPlaying = true;
                        }
                        if(!mUpdateST3){
                            frameAvailableCount++;
                            Log.d(TAG, "startProgressRunnable: player3 frame Available count: " + frameAvailableCount);
                            if(frameAvailableCount >= VIDEO_REFRESH_COUNT_LIMIT){
                                Log.d(TAG, "startProgressRunnable: forcing frame refresh on player3.");
                                mUpdateST3 = true;
                            }
                        }
                        else{
                            frameAvailableCount = 0;
                        }
                        Log.d(TAG, "startProgressRunnable: current progress: " + progress);
                    }
                    else if(getCurrentPlayer() == PLAYER_THREE_SECONDARY){
                        progress = (int) mSecondaryPlayer3.getCurrentPosition();
                        if(mSecondaryPlayer3.getPlayWhenReady()){
                            isPlaying = true;
                        }
                        if(!mUpdateST3){
                            frameAvailableCount++;
                            Log.d(TAG, "startProgressRunnable: secondary player3 frame Available count: " + frameAvailableCount);
                            if(frameAvailableCount >= VIDEO_REFRESH_COUNT_LIMIT){
                                Log.d(TAG, "startProgressRunnable: forcing frame refresh on secondary player3.");
                                mUpdateST3= true;
                            }
                        }
                        else{
                            frameAvailableCount = 0;
                        }
                        Log.d(TAG, "startProgressRunnable: current progress: " + progress);
                    }
                    else if(getCurrentPlayer() == PLAYER_FOUR){
                        progress = (int) mPlayer4.getCurrentPosition();
                        if(mPlayer4.getPlayWhenReady()){
                            isPlaying = true;
                        }
                        if(!mUpdateST4){
                            frameAvailableCount++;
                            Log.d(TAG, "startProgressRunnable: player4 frame Available count: " + frameAvailableCount);
                            if(frameAvailableCount >= VIDEO_REFRESH_COUNT_LIMIT){
                                Log.d(TAG, "startProgressRunnable: forcing frame refresh on player4.");
                                mUpdateST4 = true;
                            }
                        }
                        else{
                            frameAvailableCount = 0;
                        }
                        Log.d(TAG, "startProgressRunnable: current progress: " + progress);
                    }
                    else if(getCurrentPlayer() == PLAYER_FOUR_SECONDARY){
                        progress = (int) mSecondaryPlayer4.getCurrentPosition();
                        if(mSecondaryPlayer4.getPlayWhenReady()){
                            isPlaying = true;
                        }
                        if(!mUpdateST4){
                            frameAvailableCount++;
                            Log.d(TAG, "startProgressRunnable: secondary player4 frame Available count: " + frameAvailableCount);
                            if(frameAvailableCount >= VIDEO_REFRESH_COUNT_LIMIT){
                                Log.d(TAG, "startProgressRunnable: forcing frame refresh on secondary player4.");
                                mUpdateST4 = true;
                            }
                        }
                        else{
                            frameAvailableCount = 0;
                        }
                        Log.d(TAG, "startProgressRunnable: current progress: " + progress);
                    }

                    if(mCurrentProgress == 0){
                        mCurrentProgress = 1;
                        Log.d(TAG, "startProgressRunnable: dispatching message from progress handler. progress: " + mCurrentProgress);
                        mProgressHandler.dispatchMessage(Message.obtain(mProgressHandler, INIT_VIDEO_PROGRESS_BAR));
                    }
                    if (progress > 0 && isPlaying) {
                        //hide the progress bar if it's showing
                        mProgressHandler.dispatchMessage(Message.obtain(mProgressHandler, HIDE_PROGRESS_BAR));
                        mCurrentProgress += 200;
                        Log.d(TAG, "startProgressRunnable: dispatching message from progress handler. progress: " + mCurrentProgress);
                        mProgressHandler.dispatchMessage(Message.obtain(mProgressHandler, UPDATE_UI_WITH_VIDEO_PROGRESS));
                    }
                    if(mCurrentProgress >= mTotalDuration){
                        Log.d(TAG, "startProgressRunnable: DONE.");
                        mProgressHandler.dispatchMessage(Message.obtain(mProgressHandler, REMOVE_PROGRESS_BAR_CALLBACKS));
                    }
                    if(progress == 0 && isPlaying){ // video is not playing. Might have to retry
                        videoRetryTimer += 200;
                        if(videoRetryTimer >= 2000){ // Retry playing the video if it's been trying for 3 seconds
                            Log.d(TAG, "startProgressRunnable: attempting to retry playing the video.");
                            videoRetryTimer = 0;
                            mProgressHandler.dispatchMessage(Message.obtain(mProgressHandler, getCurrentPlayer()));
                        }
                    }

//                    // if the progress has exceeded the total duration then stop the thread.
//                    if(mCurrentProgress >= mTotalDuration){
//                        Log.d(TAG, "startProgressRunnable: progress has exceeded duration. Stopping thread.");
//                        mProgressHandler.removeCallbacks(mProgressRunnable);
//                    }
                }
            };
            mProgressRunnable.run();

        }
    }

    private void fillCurrentProgressBar(){
        Log.d(TAG, "fillCurrentProgressBar: filling current progress bar.");

        try{
            //make sure the progress bar is full
            int resourceIndex = mResourceIndices.getJSONObject(mCurrentSurface).getInt(mContext.getString(R.string.resource_index));
            int surfaceMediaIndex = mResourceIndices.getJSONObject(mCurrentSurface).getJSONArray(mContext.getString(R.string.media_index))
                    .getJSONObject( resourceIndex / 4).getInt(mContext.getString(R.string.media_index));
            String resourceType = "";
            boolean imageRenderError = false;
            try{
                resourceType = mResources.get(resourceIndex).getJSONObject(surfaceMediaIndex).get(mContext.getString(R.string.media_type)).toString();
            }catch (JSONException e){
                imageRenderError = true;
                Log.e(TAG, "fillCurrentProgressBar: JSONException: " + e.getMessage() );
            }
            catch (NullPointerException e){
                imageRenderError = true;
                Log.e(TAG, "fillCurrentProgressBar: NullPointerException: " + e.getMessage() );
            }

            if (resourceType.equals(mContext.getString(R.string.encoded_bitmap)) || imageRenderError) {
                Log.d(TAG, "fillCurrentProgressBar: current resource is an image.");
                MyProgressBar progressBar = ((Activity) mContext).findViewById(mIds[surfaceMediaIndex]);
                progressBar.setProgress(1);
                progressBar.setCurrentProgress(1);
            }
            else if(resourceType.equals(mContext.getString(R.string.video_uri))){
                Log.d(TAG, "fillCurrentProgressBar: current resource is a video.");
                //fill the previous progress bar before working with the new one
                if(mCurrentProgressBar != null){
                    mCurrentProgress = MEDIA_TIMEOUT;
                    mCurrentProgressBar.setProgress(mCurrentProgress);
                    if(mProgressRunnable != null){
                        Log.d(TAG, "incrementMediaIndex: TIMEOUT.");
                        mProgressHandler.removeCallbacks(mProgressRunnable);
                        mProgressRunnable = null;
                    }
                }
            }

        }catch (JSONException e){
            Log.d(TAG, "fillCurrentProgressBar: JSONException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getCurrentPlayer(){
        Log.d(TAG, "getCurrentPlayer: getting the current player.");

        if(mCurrentSurface == SURFACE_1){
            if (mPlayerState == ACTIVE_PLAYER) {
                return PLAYER_ONE;
            }
            else if (mSecondaryPlayerState == ACTIVE_PLAYER) {
                return PLAYER_ONE_SECONDARY;
            }
        }
        else if(mCurrentSurface == SURFACE_2){
            if (mPlayer2State == ACTIVE_PLAYER) {
                return PLAYER_TWO;
            }
            else if (mSecondaryPlayer2State == ACTIVE_PLAYER) {
                return PLAYER_TWO_SECONDARY;
            }
        }
        else if(mCurrentSurface == SURFACE_3){
            if (mPlayer3State == ACTIVE_PLAYER) {
                return PLAYER_THREE;
            }
            else if (mSecondaryPlayer3State == ACTIVE_PLAYER) {
                return PLAYER_THREE_SECONDARY;
            }
        }
        else if(mCurrentSurface == SURFACE_4){
            if (mPlayer4State == ACTIVE_PLAYER) {
                return PLAYER_FOUR;
            }
            else if (mSecondaryPlayer4State == ACTIVE_PLAYER) {
                return PLAYER_FOUR_SECONDARY;
            }
        }
        return 0;
    }


}






























