package tabian.com.instagramclone2.Utils;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import tabian.com.instagramclone2.R;
import tabian.com.instagramclone2.models.User;
import tabian.com.instagramclone2.models.UserAccountSettings;

/**
 * Created by User on 9/17/2017.
 */

public class UserListAdapter extends ArrayAdapter<User>{

    private static final String TAG = "UserListAdapter";


    private LayoutInflater mInflater;
    private List<User> mUsers = null;
    private int layoutResource;
    private Context mContext;

    public UserListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mUsers = objects;
    }

    private static class ViewHolder{
        TextView username, email;
        CircleImageView profileImage;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.email = (TextView) convertView.findViewById(R.id.email);
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }


        holder.username.setText(getItem(position).getUsername());
        holder.email.setText(getItem(position).getEmail());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.getValue(UserAccountSettings.class).toString());

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return convertView;
    }
}

























