package tabian.com.instagramclone2.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by User on 1/15/2018.
 */

public class TouchableRelativeLayout extends RelativeLayout {
    public TouchableRelativeLayout(Context context) {
        super(context);
    }

    public TouchableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
