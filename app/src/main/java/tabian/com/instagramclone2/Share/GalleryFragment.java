package tabian.com.instagramclone2.Share;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tabian.com.instagramclone2.R;

/**
 * Created by User on 5/28/2017.
 */

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        Log.d(TAG, "onCreateView: started.");

        return view;
    }
}
