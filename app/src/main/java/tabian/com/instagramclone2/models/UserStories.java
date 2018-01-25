package tabian.com.instagramclone2.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by User on 1/7/2018.
 */

public class UserStories implements Parcelable{

    private ArrayList<Story> media;
    private UserAccountSettings user_account_settings;

    public UserStories(ArrayList<Story> media, UserAccountSettings user_account_settings) {
        this.media = media;
        this.user_account_settings = user_account_settings;
    }

    public UserStories() {

    }


    protected UserStories(Parcel in) {
        user_account_settings = in.readParcelable(UserAccountSettings.class.getClassLoader());
    }

    public static final Creator<UserStories> CREATOR = new Creator<UserStories>() {
        @Override
        public UserStories createFromParcel(Parcel in) {
            return new UserStories(in);
        }

        @Override
        public UserStories[] newArray(int size) {
            return new UserStories[size];
        }
    };

    public ArrayList<Story> getMedia() {
        return media;
    }

    public void setMedia(ArrayList<Story> media) {
        this.media = media;
    }

    public UserAccountSettings getUser_account_settings() {
        return user_account_settings;
    }

    public void setUser_account_settings(UserAccountSettings user_account_settings) {
        this.user_account_settings = user_account_settings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(user_account_settings, i);
    }
}
