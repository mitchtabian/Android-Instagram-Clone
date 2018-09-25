package tabian.com.instagramclone2.materialcamera.util;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import tabian.com.instagramclone2.materialcamera.ICallback;

import static tabian.com.instagramclone2.materialcamera.util.Degrees.DEGREES_270;
import static tabian.com.instagramclone2.materialcamera.util.Degrees.DEGREES_90;

/** Created by tomiurankar on 06/03/16. */
public class ImageUtil {
  private static final String TAG = "ImageUtil";
  /**
   * Saves byte[] array to disk
   *
   * @param input byte array
   * @param output path to output file
   * @param callback will always return in originating thread
   */
  public static void saveToDiskAsync(
          final byte[] input, final File output, final ICallback callback) {
    final Handler handler = new Handler();
    new Thread() {
      @Override
      public void run() {
        try {
          FileOutputStream outputStream = new FileOutputStream(output);
//          outputStream.write(input);
          outputStream.write(compress(input));
          outputStream.flush();
          outputStream.close();

          handler.post(
                  new Runnable() {
                    @Override
                    public void run() {
                      callback.done(null);
                    }
                  });
        } catch (final Exception e) {
          handler.post(
                  new Runnable() {
                    @Override
                    public void run() {
                      callback.done(e);
                    }
                  });
        }
      }
    }.start();
  }

  public static byte[] compress(byte[] data) throws IOException {
    Deflater deflater = new Deflater();
    deflater.setInput(data);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
    deflater.finish();
    byte[] buffer = new byte[1024];
    while (!deflater.finished()) {
      int count = deflater.deflate(buffer); // returns the generated code... index
      outputStream.write(buffer, 0, count);
    }
    outputStream.close();
    byte[] output = outputStream.toByteArray();
    Log.d(TAG, "Original: " + data.length / 1024 + " Kb");
    Log.d(TAG, "Compressed: " + output.length / 1024 + " Kb");
    return output;
  }

  /**
   * Rotates the bitmap per their EXIF flag. This is a recursive function that will be called again
   * if the image needs to be downsized more.
   *
   * @param inputFile Expects an JPEG file if corrected orientation wants to be set.
   * @return rotated bitmap or null
   */
  @Nullable
  public static Bitmap getRotatedBitmap(String inputFile, int reqWidth, int reqHeight) {
    final int rotationInDegrees = getExifDegreesFromJpeg(inputFile);

    Log.d(TAG, "getRotatedBitmap: rotation: " + rotationInDegrees);
    final BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(inputFile, opts);
    opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight, rotationInDegrees);
    opts.inJustDecodeBounds = false;

    Log.d(TAG, "getRotatedBitmap: input file: " + inputFile);
    final Bitmap origBitmap = BitmapFactory.decodeFile(inputFile, opts);
    Log.d(TAG, "getRotatedBitmap: original bitmap: " + origBitmap);

    if (origBitmap == null) return null;

    Matrix matrix = new Matrix();
    matrix.preRotate(rotationInDegrees);
    // we need not check if the rotation is not needed, since the below function will then return the same bitmap. Thus no memory loss occurs.

    return Bitmap.createBitmap(
        origBitmap, 0, 0, origBitmap.getWidth(), origBitmap.getHeight(), matrix, true);
  }

  private static int calculateInSampleSize(
      BitmapFactory.Options options, int reqWidth, int reqHeight, int rotationInDegrees) {

    // Raw height and width of image
    final int height;
    final int width;
    int inSampleSize = 1;

    // Check for rotation
    if (rotationInDegrees == DEGREES_90 || rotationInDegrees == DEGREES_270) {
      width = options.outHeight;
      height = options.outWidth;
    } else {
      height = options.outHeight;
      width = options.outWidth;
    }

    if (height > reqHeight || width > reqWidth) {
      // Calculate ratios of height and width to requested height and width
      final int heightRatio = Math.round((float) height / (float) reqHeight);
      final int widthRatio = Math.round((float) width / (float) reqWidth);

      // Choose the smallest ratio as inSampleSize value, this will guarantee
      // a final image with both dimensions larger than or equal to the
      // requested height and width.
      inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    }
    return inSampleSize;
  }

  private static int getExifDegreesFromJpeg(String inputFile) {
    try {
      final ExifInterface exif = new ExifInterface(inputFile);
      final int exifOrientation =
          exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
      if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
        return 90;
      } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
        return 180;
      } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
        return 270;
      }
    } catch (IOException e) {
      Log.e("exif", "Error when trying to get exif data from : " + inputFile, e);
    }
    return 0;
  }
}
