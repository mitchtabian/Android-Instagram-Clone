package tabian.com.instagramclone2.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import tabian.com.instagramclone2.Home.HomeActivity;
import tabian.com.instagramclone2.R;
import tabian.com.instagramclone2.opengl.OpenGLES10Activity;


/**
 * Created by User on 12/27/2017.
 */

public class StoriesRecyclerViewAdapter extends RecyclerView.Adapter<StoriesRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private static final int CLICK_DURATION = 500;
    private static final int NEW_STORY_REQUEST = 2349;

    //vars
    private HashMap<Integer, ViewHolder> mViewHolders = new HashMap<>();
//    private ArrayList<UserStories> mUserStories = new ArrayList<>();
    private JSONArray mMasterStoriesArray = new JSONArray();
    private Runnable mOnTouchRunnable;
    private boolean isRunning = false;
    private Context mContext;
    private boolean down = false;
    private boolean up = false;
    private float x1 = 0;
    private float y1 = 0;
    private float x2 = 0;
    private float y2 = 0;
//    private long t1 = 0;
//    private long t2 = 0;
    private long runningTime = 0;


    public StoriesRecyclerViewAdapter(JSONArray masterStoriesArray, Context context) {
        mMasterStoriesArray = masterStoriesArray;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_stories_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        try{
            if(mMasterStoriesArray.getJSONObject(position).getJSONObject(mContext.getString(R.string.user_account_settings))
                    .get(mContext.getString(R.string.field_user_id)).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                int numStories = 0;
                try{
                    numStories =  mMasterStoriesArray.getJSONObject(position).getJSONArray(mContext.getString(R.string.user_stories)).length();
                }catch (JSONException e){
                    Log.e(TAG, "onBindViewHolder: authenticated user has no stories.");
                }
                Log.d(TAG, "onBindViewHolder: user: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                Log.d(TAG, "onBindViewHolder: number of stories for this user: " + numStories);
                if(numStories == 0){
                    Log.d(TAG, "onBindViewHolder: no stories for authenticated user.");
                    holder.plusIcon.setVisibility(View.VISIBLE);
                    holder.layout.setBackground(null);
                }
                else{
                    Log.d(TAG, "onBindViewHolder: found stories for authenticated user.");
                    holder.hasStories = true;
                    holder.plusIcon.setVisibility(View.INVISIBLE);
                    holder.layout.setBackground(mContext.getResources().getDrawable(R.drawable.circle_grey));
                }
            }
            else{
                Log.d(TAG, "onBindViewHolder: not the authenticated user.");
                holder.plusIcon.setVisibility(View.INVISIBLE);
                holder.layout.setBackground(mContext.getResources().getDrawable(R.drawable.circle_red));
            }


            mViewHolders.put(position, holder);

            Glide.with(mContext)
                    .asBitmap()
                    .load(mMasterStoriesArray.getJSONObject(position).getJSONObject(mContext.getString(R.string.user_account_settings))
                            .get(mContext.getString(R.string.field_profile_photo)))
                    .into(holder.image);

            holder.name.setText(mMasterStoriesArray.getJSONObject(position).getJSONObject(mContext.getString(R.string.user_account_settings))
                    .get(mContext.getString(R.string.field_username)).toString());


        }catch (JSONException e){
            e.printStackTrace();
        }
        holder.layout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, final MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: ACTION UP.");
                        if(down){
                            up = true;
                            down = false;
                            x2 = event.getX();
                            y2 = event.getY();
                            runningTime = 0;
                        }

                    case MotionEvent.ACTION_DOWN:
                        if(!up){
                            Log.d(TAG, "onTouch: ACTION DOWN");
                            x1 = event.getX();
                            y1 = event.getY();
//                        t1 = System.currentTimeMillis();
//                        Log.d(TAG, "onTouch: t1: " + t1);
                            down = true;
                            if(!isRunning){
                                isRunning = true;
                                final Handler handler = new Handler();
                                mOnTouchRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if(isRunning){
                                            handler.postDelayed(mOnTouchRunnable, 200);
                                            try{

                                                if(runningTime >= CLICK_DURATION){
                                                    Log.d(TAG, "onTouch: long click. opening add to story dialog.");
                                                    isRunning = false;
                                                    if(mMasterStoriesArray.getJSONObject(position).getJSONObject(mContext.getString(R.string.user_account_settings))
                                                            .get(mContext.getString(R.string.field_user_id)).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                        ((HomeActivity)mContext).showAddToStoryDialog();
                                                    }
                                                }
                                                else{
                                                    runningTime += 200;
                                                }
                                                if (x1 == x2 && y1 == y2 && runningTime < CLICK_DURATION && isRunning) {
                                                    String userId = mMasterStoriesArray.getJSONObject(position)
                                                            .getJSONObject(mContext.getString(R.string.user_account_settings)).get(mContext.getString(R.string.field_user_id)).toString();
                                                    Log.d(TAG, "onTouch: clicked on: " + mMasterStoriesArray.getJSONObject(position)
                                                            .getJSONObject(mContext.getString(R.string.user_account_settings)).get(mContext.getString(R.string.field_username)));

                                                    if(userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && holder.hasStories){
                                                        Intent intent = new Intent(mContext, OpenGLES10Activity.class);
                                                        intent.putExtra(mContext.getString(R.string.user_stories), mMasterStoriesArray.toString());
                                                        intent.putExtra(mContext.getString(R.string.resource_index), position);
                                                        mContext.startActivity(intent);
                                                        isRunning = false;
                                                    }
                                                    else if(userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && !holder.hasStories){
                                                        ((HomeActivity)mContext).showAddToStoryDialog();
                                                        isRunning = false;
                                                    }
                                                    else if(!userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                        Intent intent = new Intent(mContext, OpenGLES10Activity.class);
                                                        intent.putExtra(mContext.getString(R.string.user_stories), mMasterStoriesArray.toString());
                                                        intent.putExtra(mContext.getString(R.string.resource_index), position);
                                                        mContext.startActivity(intent);
                                                        isRunning = false;
                                                    }


                                                }
                                            }catch (JSONException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                };
                                mOnTouchRunnable.run();
                            }
                        }
                        else{
                            up = false;
                        }


                        return true;
                }

                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
//        return mUserStories.size();
        return mMasterStoriesArray.length();
    }

    public void startProgressBar(){
        Log.d(TAG, "startProgressBar: starting story upload progress bar.");
        mViewHolders.get(0).progressBar.setVisibility(View.VISIBLE);
    }

    public void stopProgressBar(){
        Log.d(TAG, "stopProgressBar: stopping story upload progress bar.");
        mViewHolders.get(0).progressBar.setVisibility(View.GONE);
        mViewHolders.get(0).plusIcon.setVisibility(View.GONE);
        mViewHolders.get(0).layout.setBackground(mContext.getResources().getDrawable(R.drawable.circle_grey));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView image;
        TextView name;
        ImageView plusIcon;
        TouchableRelativeLayout layout;
        ProgressBar progressBar;
        Boolean hasStories;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            plusIcon = itemView.findViewById(R.id.plus_icon);
            layout = itemView.findViewById(R.id.relLayout1);
            progressBar = itemView.findViewById(R.id.story_upload_progress_bar);
            hasStories = false;
        }
    }
}

