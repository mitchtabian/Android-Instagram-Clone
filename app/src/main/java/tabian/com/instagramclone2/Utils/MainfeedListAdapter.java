package tabian.com.instagramclone2.Utils;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Array;
import java.security.PolicySpi;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import tabian.com.instagramclone2.Home.HomeActivity;
import tabian.com.instagramclone2.Profile.ProfileActivity;
import tabian.com.instagramclone2.R;
import tabian.com.instagramclone2.models.Comment;
import tabian.com.instagramclone2.models.Like;
import tabian.com.instagramclone2.models.Photo;
import tabian.com.instagramclone2.models.User;
import tabian.com.instagramclone2.models.UserAccountSettings;

/**
 * Created by User on 9/22/2017.
 */

public class MainFeedListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    private static final String TAG = "MainFeedListAdapter";

    private LayoutInflater mInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";

    public MainFeedListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();

//        for(Photo photo: objects){
//            Log.d(TAG, "MainFeedListAdapter: photo id: " + photo.getPhoto_id());
//        }
    }

    static class ViewHolder{
        CircleImageView mprofileImage;
        String likesString;
        TextView username, timeDetla, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user  = new User();
        StringBuilder users;
        String mLikesString;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image = (SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.comment = (ImageView) convertView.findViewById(R.id.speech_bubble);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comments_link);
            holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.timeDetla = (TextView) convertView.findViewById(R.id.image_time_posted);
            holder.mprofileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.photo = getItem(position);
        holder.detector = new GestureDetector(mContext, new GestureListener(holder));
        holder.users = new StringBuilder();
        holder.heart = new Heart(holder.heartWhite, holder.heartRed);

        //get the current users username (need for checking likes string)
        getCurrentUsername();

        //get likes string
        getLikesString(holder);

        //set the caption
        holder.caption.setText(getItem(position).getCaption());

        //set the comment
        List<Comment> comments = getItem(position).getComments();
        holder.comments.setText("View all " + comments.size() + " comments");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: loading comment thread for " + getItem(position).getPhoto_id());
                ((HomeActivity)mContext).onCommentThreadSelected(getItem(position),
                        mContext.getString(R.string.home_activity));

                //going to need to do something else?
                ((HomeActivity)mContext).hideLayout();

            }
        });

        //set the time it was posted
        String timestampDifference = getTimestampDifference(getItem(position));
        if(!timestampDifference.equals("0")){
            holder.timeDetla.setText(timestampDifference + " DAYS AGO");
        }else{
            holder.timeDetla.setText("TODAY");
        }

        //set the profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);


        //get the profile image and username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                   // currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();

                    Log.d(TAG, "onDataChange: found user: "
                            + singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " +
                                    holder.user.getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });

                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.mprofileImage);
                    holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " +
                                    holder.user.getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });



                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity)mContext).onCommentThreadSelected(getItem(position),
                                    mContext.getString(R.string.home_activity));

                            //another thing?
                            ((HomeActivity)mContext).hideLayout();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //get the user object
        Query userQuery = mReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +
                    singleSnapshot.getValue(User.class).getUsername());

                    holder.user = singleSnapshot.getValue(User.class);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(reachedEndOfList(position)){
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfList(int position){
        return position == getCount() - 1;
    }

    private void loadMoreData(){

        try{
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();
        }catch (ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }

        try{
            mOnLoadMoreItemsListener.onLoadMoreItems();
        }catch (NullPointerException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        ViewHolder mHolder;
        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            Log.d(TAG, "onDoubleTap: clicked on photo: " + mHolder.photo.getPhoto_id());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        String keyID = singleSnapshot.getKey();

                        //case1: Then user already liked the photo
                        if(mHolder.likeByCurrentUser
//                                && singleSnapshot.getValue(Like.class).getUser_id()
//                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                ){

                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
///
                            mReference.child(mContext.getString(R.string.dbname_user_photos))
//                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getUser_id())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }
                        //case2: The user has not liked the photo
                        else if(!mHolder.likeByCurrentUser){
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        //add new like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewLike(final ViewHolder holder){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: retrieving user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getLikesString(final ViewHolder holder){
        Log.d(TAG, "getLikesString: getting likes string");

        Log.d(TAG, "getLikesString: photo id: " + holder.photo.getPhoto_id());
        try{
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.users = new StringBuilder();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(mContext.getString(R.string.dbname_users))
                            .orderByChild(mContext.getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: found like: " +
                                        singleSnapshot.getValue(User.class).getUsername());

                                holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                holder.users.append(",");
                            }

                            String[] splitUsers = holder.users.toString().split(",");

                            if(holder.users.toString().contains(currentUsername + ",")){//mitch, mitchell.tabian
                                holder.likeByCurrentUser = true;
                            }else{
                                holder.likeByCurrentUser = false;
                            }

                            int length = splitUsers.length;
                            if(length == 1){
                                holder.likesString = "Liked by " + splitUsers[0];
                            }
                            else if(length == 2){
                                holder.likesString = "Liked by " + splitUsers[0]
                                        + " and " + splitUsers[1];
                            }
                            else if(length == 3){
                                holder.likesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + " and " + splitUsers[2];

                            }
                            else if(length == 4){
                                holder.likesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + splitUsers[3];
                            }
                            else if(length > 4){
                                holder.likesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + (splitUsers.length - 3) + " others";
                            }
                            Log.d(TAG, "onDataChange: likes string: " + holder.likesString);
                            //setup likes string
                            setupLikesString(holder, holder.likesString);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if(!dataSnapshot.exists()){
                    holder.likesString = "";
                    holder.likeByCurrentUser = false;
                    //setup likes string
                    setupLikesString(holder, holder.likesString);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        }catch (NullPointerException e){
            Log.e(TAG, "getLikesString: NullPointerException: " + e.getMessage() );
            holder.likesString = "";
            holder.likeByCurrentUser = false;
            //setup likes string
            setupLikesString(holder, holder.likesString);
        }
    }

    private void setupLikesString(final ViewHolder holder, String likesString){
        Log.d(TAG, "setupLikesString: likes string:" + holder.likesString);

        Log.d(TAG, "setupLikesString: photo id: " + holder.photo.getPhoto_id());
        if(holder.likeByCurrentUser){
            Log.d(TAG, "setupLikesString: photo is liked by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }else{
            Log.d(TAG, "setupLikesString: photo is not liked by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.likes.setText(likesString);
    }

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(Photo photo){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }

}





























