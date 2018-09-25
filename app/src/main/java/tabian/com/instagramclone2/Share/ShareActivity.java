package tabian.com.instagramclone2.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.health.PackageHealthStats;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import tabian.com.instagramclone2.R;
import tabian.com.instagramclone2.Utils.BottomNavigationViewHelper;
import tabian.com.instagramclone2.Utils.Permissions;
import tabian.com.instagramclone2.Utils.SectionsPagerAdapter;

/**
 * Created by User on 5/28/2017.
 */

public class ShareActivity extends AppCompatActivity{
    private static final String TAG = "ShareActivity";

    //constants
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager mViewPager;


    private Context mContext = ShareActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: started.");

        if(checkPermissionsArray(Permissions.PERMISSIONS)){
            setupViewPager();
        }else{
            verifyPermissions(Permissions.PERMISSIONS);
        }

    }

    /**
     * return the current tab number
     * 0 = GalleryFragment
     * 1 = PhotoFragment
     * @return
     */
    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }

    /**
     * setup viewpager for manager the tabs
     */
    private void setupViewPager(){
        SectionsPagerAdapter adapter =  new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));

    }

    public int getTask(){
        Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());
        return getIntent().getFlags();
    }

    /**
     * verifiy all the permissions passed to the array
     * @param permissions
     */
    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions){
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for(int i = 0; i< permissions.length; i++){
            String check = permissions[i];
            if(!checkPermissions(check)){
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission is it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission){
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        }
        else{
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
