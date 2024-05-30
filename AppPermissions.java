package com.app.smartparking.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.multidex.BuildConfig;

import com.app.smartparking.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class AppPermissions {

    public static final String[] LOCATION_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET};
    public static final String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA};
    /*public static final String[] STORAGE_PERMISSION = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static final String[] STORAGE_PERMISSION_API_S = {Manifest.permission.READ_EXTERNAL_STORAGE};
    public static final String[] CALENDAR_PERMISSION = {Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR};
    public static final String[] AUDIO_PERMISSION = {Manifest.permission.RECORD_AUDIO};
    public static final String[] CONTACT_PERMISSION = {Manifest.permission.READ_CONTACTS};
    public static final String[] CALL_LOG_PERMISSION = {Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE};
    public static final String[] TELEPHONE_PERMISSION = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS};*/
    public static final int REQ_CODE = 999;
    private final Activity mActivity;
    private final View mView;
    private String rationaleMsg = "";
    private boolean rationaleIndefinite = false;

    public AppPermissions(@NonNull Activity activity) {
        mActivity = activity;
        mView = activity.findViewById(android.R.id.content);
        rationaleMsg = mActivity.getString(R.string.permission_required);
    }

    public AppPermissions(@NonNull Fragment fragment) {
        mActivity = fragment.getActivity();
        mView = fragment.getView();
        rationaleMsg = mActivity.getString(R.string.permission_required);
    }

    public void setRationaleMessage(String rationaleMsg) {
        this.rationaleMsg = rationaleMsg == null ? "" : rationaleMsg;
    }

    public void setRationaleMessage(@StringRes int rationaleMsgRes) {
        this.rationaleMsg = mActivity.getString(rationaleMsgRes);
    }

    public void setRationaleIndefinite(boolean rationaleIndefinite) {
        this.rationaleIndefinite = rationaleIndefinite;
    }

    public boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return ContextCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_GRANTED;
        else
            return true;
    }

    public boolean hasPermission(String[] permissionsList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            for (String permission : permissionsList) {
                if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        return true;
    }

    public void requestPermission(String permission, int requestCode) {
        requestPermission(permission, requestCode, rationaleMsg);
    }

    public void requestPermission(String permission, int requestCode, @StringRes int stringRes) {
        requestPermission(permission, requestCode, mActivity.getString(stringRes));
    }

    public void requestPermission(String permission, int requestCode, String rationaleMsg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                    showRationaleSnakebar(rationaleMsg);
                } else {
                    ActivityCompat.requestPermissions(mActivity, new String[]{permission}, requestCode);
                }
            }
    }

    public void requestPermission(String[] permissionsList, int requestCode) {
        requestPermission(permissionsList, requestCode, rationaleMsg);
    }

    public void requestPermission(String[] permissionsList, int requestCode, @StringRes int stringRes) {
        requestPermission(permissionsList, requestCode, mActivity.getString(stringRes));
    }

    public void requestPermission(String[] permissionsList, int requestCode, String rationaleMsg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionNeeded = new ArrayList<>();
            List<String> rationaleNeeded = new ArrayList<>();
            for (String permission : permissionsList) {
                if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionNeeded.add(permission);
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                        rationaleNeeded.add(permission);
                    }
                }
            }
            if (permissionNeeded.size() > 0) {
                if (rationaleNeeded.size() > 0) {
                    showRationaleSnakebar(rationaleMsg);
                } else {
                    ActivityCompat.requestPermissions(mActivity,
                            permissionNeeded.toArray(new String[permissionNeeded.size()]), requestCode);
                }
            }
        }
    }

    public void showRationaleSnakebar(@StringRes int strRes) {
        showRationaleSnakebar(mActivity.getString(strRes));
    }

    public void showRationaleSnakebar(@NonNull String msg) {
        msg = msg == null ? "" : msg;
        Snackbar sb = Snackbar.make(mView, msg, rationaleIndefinite ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG);
        TextView tv = sb.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setGravity(Gravity.LEFT);
        tv.setMaxLines(5);
        sb.setAction(R.string.allow, view -> openSettings(REQ_CODE));
        sb.show();
    }

    public void openSettings(int reqCode) {
        mActivity.startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)), reqCode);
    }

    public boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager)
                mActivity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void showGpsSettingDialog(int reqCode) {
        new AlertDialog.Builder(mActivity)
                .setCancelable(!rationaleIndefinite)
                .setTitle(R.string.turn_on_gps)
                .setMessage(R.string.turn_on_gps_desc)
                .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> {
                    mActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), reqCode);
                }).show();
    }


/*    public boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return hasPermission(STORAGE_PERMISSION_API_S);
        } else
            return hasPermission(STORAGE_PERMISSION);
    }

    public void requestStoragePermission(int reqCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermission(STORAGE_PERMISSION_API_S, reqCode, R.string.to_allow_storage_permission);
        } else
            requestPermission(STORAGE_PERMISSION, reqCode, R.string.to_allow_storage_permission);

    }*/
}
