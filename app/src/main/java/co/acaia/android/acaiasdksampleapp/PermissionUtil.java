package co.acaia.android.acaiasdksampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by Dennis on 2019-09-24
 */
public class PermissionUtil {
    public static boolean allPermissionsGranted(int[] grantResults){
        if(grantResults.length > 0){
            boolean allPermissionsGranted = true;
            for(int g : grantResults){
                if(g != PackageManager.PERMISSION_GRANTED){
                    allPermissionsGranted = false;
                    break;
                }
            }
            return allPermissionsGranted;
        }else {
            return false;
        }
    }

    public static boolean somePermissionForeverDenied(Activity activity, String[] permissions){
        boolean somePermissionsForeverDenied = false;
        for (String permission : permissions) {
            somePermissionsForeverDenied = isPermissionForeverDenied(activity, permission);
        }
        return somePermissionsForeverDenied;
    }

    public static boolean isPermissionForeverDenied(Activity activity, String permission){
        boolean isForeverDenied = false;
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            //denied
        } else {
            if (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                //allowed
            } else {
                //set to never ask again
                isForeverDenied = true;
            }
        }
        return isForeverDenied;
    }
}
