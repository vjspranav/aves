package deckers.thibault.aves.channel.streams;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import deckers.thibault.aves.decoder.VideoThumbnail;
import deckers.thibault.aves.utils.MimeTypes;
import io.flutter.plugin.common.EventChannel;

public class ImageByteStreamHandler implements EventChannel.StreamHandler {
    public static final String CHANNEL = "deckers.thibault/aves/imagebytestream";

    private Activity activity;
    private Uri uri;
    private String mimeType;
    private int orientationDegrees;
    private EventChannel.EventSink eventSink;
    private Handler handler;

    @SuppressWarnings("unchecked")
    public ImageByteStreamHandler(Activity activity, Object arguments) {
        this.activity = activity;
        if (arguments instanceof Map) {
            Map<String, Object> argMap = (Map<String, Object>) arguments;
            this.mimeType = (String) argMap.get("mimeType");
            this.uri = Uri.parse((String) argMap.get("uri"));
            this.orientationDegrees = (int) argMap.get("orientationDegrees");
        }
    }

    @Override
    public void onListen(Object o, final EventChannel.EventSink eventSink) {
        this.eventSink = eventSink;
        this.handler = new Handler(Looper.getMainLooper());
        new Thread(this::getImage).start();
    }

    @Override
    public void onCancel(Object o) {
    }

    private void success(final byte[] bytes) {
        handler.post(() -> eventSink.success(bytes));
    }

    private void error(final String errorCode, final String errorMessage, final Object errorDetails) {
        handler.post(() -> eventSink.error(errorCode, errorMessage, errorDetails));
    }

    private void endOfStream() {
        handler.post(() -> eventSink.endOfStream());
    }

    private void getImage() {
        if (mimeType != null && mimeType.startsWith(MimeTypes.VIDEO)) {
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
            FutureTarget<Bitmap> target = Glide.with(activity)
                    .asBitmap()
                    .apply(options)
                    .load(new VideoThumbnail(activity, uri))
                    .submit();
            try {
                Bitmap bitmap = target.get();
                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    // we compress the bitmap because Dart Image.memory cannot decode the raw bytes
                    // Bitmap.CompressFormat.PNG is slower than JPEG
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    success(stream.toByteArray());
                } else {
                    error("getImage-video-null", "failed to get image from uri=" + uri, null);
                }
            } catch (Exception e) {
                error("getImage-video-exception", "failed to get image from uri=" + uri, e.getMessage());
            } finally {
                Glide.with(activity).clear(target);
            }
        } else {
            ContentResolver cr = activity.getContentResolver();
            if (MimeTypes.DNG.equals(mimeType) || MimeTypes.HEIC.equals(mimeType) || MimeTypes.HEIF.equals(mimeType)) {
                // as of Flutter v1.20, Dart Image.memory cannot decode DNG/HEIC/HEIF images
                // so we convert the image on platform side first
                FutureTarget<Bitmap> target = Glide.with(activity)
                        .asBitmap()
                        .load(uri)
                        .submit();
                try {
                    Bitmap bitmap = target.get();
                    if (bitmap != null) {
                        bitmap = TransformationUtils.rotateImage(bitmap, orientationDegrees);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        // we compress the bitmap because Dart Image.memory cannot decode the raw bytes
                        // Bitmap.CompressFormat.PNG is slower than JPEG
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        success(stream.toByteArray());
                    } else {
                        error("getImage-image-decode-null", "failed to get image from uri=" + uri, null);
                    }
                } catch (Exception e) {
                    error("getImage-image-decode-exception", "failed to get image from uri=" + uri, e.getMessage());
                } finally {
                    Glide.with(activity).clear(target);
                }
            } else {
                try (InputStream is = cr.openInputStream(uri)) {
                    if (is != null) {
                        streamBytes(is);
                    } else {
                        error("getImage-image-read-null", "failed to get image from uri=" + uri, null);
                    }
                } catch (IOException e) {
                    error("getImage-image-read-exception", "failed to get image from uri=" + uri, e.getMessage());
                }
            }
        }
        endOfStream();
    }

    private void streamBytes(InputStream inputStream) throws IOException {
        int bufferSize = 2 << 17; // 256kB
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            // cannot decode image on Flutter side when using `buffer` directly...
            byte[] sub = new byte[len];
            System.arraycopy(buffer, 0, sub, 0, len);
            success(sub);
        }
    }
}
