package com.mikelau.croperino;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.mikelau.magictoast.MagicToast;

import java.io.File;
import java.io.IOException;

/**
 * Created by Mike on 9/15/2016.
 */
public class Croperino {

    public enum ScreenOrientation {

        SCREEN_ORIENTATION_BEHIND(3),
        SCREEN_ORIENTATION_FULL_SENSOR(10),
        SCREEN_ORIENTATION_FULL_USER(13),
        SCREEN_ORIENTATION_LANDSCAPE(0),
        SCREEN_ORIENTATION_LOCKED(14),
        SCREEN_ORIENTATION_NOSENSOR(5),
        SCREEN_ORIENTATION_PORTRAIT(1),
        SCREEN_ORIENTATION_REVERSE_LANDSCAPE(8),
        SCREEN_ORIENTATION_REVERSE_PORTRAIT(9),
        SCREEN_ORIENTATION_SENSOR(4),
        SCREEN_ORIENTATION_SENSOR_LANDSCAPE(6),
        SCREEN_ORIENTATION_SENSOR_PORTRAIT(7),
        SCREEN_ORIENTATION_UNSPECIFIED(-1),
        SCREEN_ORIENTATION_USER(2),
        SCREEN_ORIENTATION_USER_LANDSCAPE(11),
        SCREEN_ORIENTATION_USER_PORTRAIT(12);

        private final int value;

        ScreenOrientation(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static void runCropImage(File file, Activity ctx, boolean isScalable, int aspectX, int aspectY, int color, int bgColor, ScreenOrientation screenOrientation) {
        Intent intent = new Intent(ctx, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, file.getPath());
        intent.putExtra(CropImage.SCALE, isScalable);
        intent.putExtra(CropImage.ASPECT_X, aspectX);
        intent.putExtra(CropImage.ASPECT_Y, aspectY);
        intent.putExtra("color", color);
        intent.putExtra("bgColor", bgColor);
        intent.putExtra("orientation", screenOrientation.getValue());
        ctx.startActivityForResult(intent, CroperinoConfig.REQUEST_CROP_PHOTO);
    }

    public static void prepareChooser(final Activity ctx, String message, int color) {
        CameraDialog.getConfirmDialog(ctx, ctx.getResources().getString(R.string.app_name),
                message,
                "CAMERA",
                "GALLERY",
                "CLOSE",
                color,
                true,
                new AlertInterface.WithNeutral() {
                    @Override
                    public void PositiveMethod(final DialogInterface dialog, final int id) {
                        if (CroperinoFileUtil.verifyCameraPermissions(ctx)) {
                            prepareCamera(ctx);
                        }
                    }

                    @Override
                    public void NeutralMethod(final DialogInterface dialog, final int id) {
                        if (CroperinoFileUtil.verifyStoragePermissions(ctx)) {
                            prepareGallery(ctx);
                        }
                    }

                    @Override
                    public void NegativeMethod(final DialogInterface dialog, final int id) {

                    }
                });
    }

    public static void prepareCamera(Activity ctx) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri mImageCaptureUri;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                if (Uri.fromFile(CroperinoFileUtil.newCameraFile()) != null) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        mImageCaptureUri = FileProvider.getUriForFile(ctx,
                                ctx.getApplicationContext().getPackageName() + ".provider",
                                CroperinoFileUtil.newCameraFile());
                    } else {
                        mImageCaptureUri = Uri.fromFile(CroperinoFileUtil.newCameraFile());
                    }
                } else {
                    mImageCaptureUri = FileProvider.getUriForFile(ctx,
                            ctx.getApplicationContext().getPackageName() + ".provider",
                            CroperinoFileUtil.newCameraFile());
                }
            } else {
                mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
            }
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            ctx.startActivityForResult(intent, CroperinoConfig.REQUEST_TAKE_PHOTO);
        } catch (Exception e) {
            if (e instanceof ActivityNotFoundException) {
                MagicToast.showError(ctx, "Activity not found.");
            } else if (e instanceof IOException) {
                MagicToast.showError(ctx, "Image file captured not found.");
            } else {
                MagicToast.showError(ctx, "Camera access failed.");
            }
        }
    }

    public static void prepareGallery(Activity ctx) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        ctx.startActivityForResult(i, CroperinoConfig.REQUEST_PICK_FILE);
    }
}
