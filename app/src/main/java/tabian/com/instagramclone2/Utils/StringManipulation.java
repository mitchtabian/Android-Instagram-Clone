package tabian.com.instagramclone2.Utils;

/**
 * Created by User on 6/26/2017.
 */

public class StringManipulation {

    public static String expandUsername(String username){
        return username.replace(".", " ");
    }

    public static String condenseUsername(String username){
        return username.replace(" " , ".");
    }
}
