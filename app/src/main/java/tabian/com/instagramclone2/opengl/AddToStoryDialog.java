package tabian.com.instagramclone2.opengl;

import android.Manifest;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import tabian.com.instagramclone2.Home.HomeActivity;
import tabian.com.instagramclone2.R;


/**
 * Created by User on 1/8/2018.
 */

public class AddToStoryDialog extends DialogFragment {

    private static final String TAG = "AddToStoryDialog";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 29;

    //widgets
    private LinearLayout layout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_to_story, container, false);
        layout = view.findViewById(R.id.linLayout1);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || getActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                Log.d(TAG, "Already granted access");
                init();
            }
        }


        return view;
    }

    private void init(){
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: preparing to record new story.");
                ((HomeActivity)getActivity()).openNewStoryActivity();
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission Granted");
                    init();
                } else {
                    Log.d(TAG, "Permission Failed");
                    Toast.makeText(getActivity().getBaseContext(), "You must allow permission to record audio to your mobile device.", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            }
        }
    }
}
