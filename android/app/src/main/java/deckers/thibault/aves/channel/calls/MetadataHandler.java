package deckers.thibault.aves.channel.calls;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adobe.internal.xmp.XMPException;
import com.adobe.internal.xmp.XMPIterator;
import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.properties.XMPProperty;
import com.adobe.internal.xmp.properties.XMPPropertyInfo;
import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileTypeDirectory;
import com.drew.metadata.gif.GifAnimationDirectory;
import com.drew.metadata.webp.WebpDirectory;
import com.drew.metadata.xmp.XmpDirectory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import deckers.thibault.aves.utils.MetadataHelper;
import deckers.thibault.aves.utils.MimeTypes;
import deckers.thibault.aves.utils.StorageUtils;
import deckers.thibault.aves.utils.Utils;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MetadataHandler implements MethodChannel.MethodCallHandler {
    private static final String LOG_TAG = Utils.createLogTag(MetadataHandler.class);

    public static final String CHANNEL = "deckers.thibault/aves/metadata";

    // catalog metadata
    private static final String KEY_MIME_TYPE = "mimeType";
    private static final String KEY_DATE_MILLIS = "dateMillis";
    private static final String KEY_IS_ANIMATED = "isAnimated";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_VIDEO_ROTATION = "videoRotation";
    private static final String KEY_XMP_SUBJECTS = "xmpSubjects";
    private static final String KEY_XMP_TITLE_DESCRIPTION = "xmpTitleDescription";

    // overlay metadata
    private static final String KEY_APERTURE = "aperture";
    private static final String KEY_EXPOSURE_TIME = "exposureTime";
    private static final String KEY_FOCAL_LENGTH = "focalLength";
    private static final String KEY_ISO = "iso";

    // XMP
    private static final String XMP_DC_SCHEMA_NS = "http://purl.org/dc/elements/1.1/";
    private static final String XMP_SUBJECT_PROP_NAME = "dc:subject";
    private static final String XMP_TITLE_PROP_NAME = "dc:title";
    private static final String XMP_DESCRIPTION_PROP_NAME = "dc:description";
    private static final String XMP_GENERIC_LANG = "";
    private static final String XMP_SPECIFIC_LANG = "en-US";

    // video metadata keys, from android.media.MediaMetadataRetriever
    private static final Map<Integer, String> VIDEO_MEDIA_METADATA_KEYS = new HashMap<Integer, String>() {
        {
            put(MediaMetadataRetriever.METADATA_KEY_ALBUM, "Album");
            put(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, "Album Artist");
            put(MediaMetadataRetriever.METADATA_KEY_ARTIST, "Artist");
            put(MediaMetadataRetriever.METADATA_KEY_AUTHOR, "Author");
            put(MediaMetadataRetriever.METADATA_KEY_BITRATE, "Bitrate");
            put(MediaMetadataRetriever.METADATA_KEY_COMPOSER, "Composer");
            put(MediaMetadataRetriever.METADATA_KEY_DATE, "Date");
            put(MediaMetadataRetriever.METADATA_KEY_GENRE, "Content Type");
            put(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO, "Has Audio");
            put(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO, "Has Video");
            put(MediaMetadataRetriever.METADATA_KEY_LOCATION, "Location");
            put(MediaMetadataRetriever.METADATA_KEY_MIMETYPE, "MIME Type");
            put(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS, "Number of Tracks");
            put(MediaMetadataRetriever.METADATA_KEY_TITLE, "Title");
            put(MediaMetadataRetriever.METADATA_KEY_WRITER, "Writer");
            put(MediaMetadataRetriever.METADATA_KEY_YEAR, "Year");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                put(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT, "Frame Count");
            }
            // TODO TLAD comment? category?
        }
    };

    // Pattern to extract latitude & longitude from a video location tag (cf ISO 6709)
    // Examples:
    // "+37.5090+127.0243/" (Samsung)
    // "+51.3328-000.7053+113.474/" (Apple)
    private static final Pattern VIDEO_LOCATION_PATTERN = Pattern.compile("([+-][.0-9]+)([+-][.0-9]+).*");

    private Context context;

    public MetadataHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "getAllMetadata":
                new Thread(() -> getAllMetadata(call, new MethodResultWrapper(result))).start();
                break;
            case "getCatalogMetadata":
                new Thread(() -> getCatalogMetadata(call, new MethodResultWrapper(result))).start();
                break;
            case "getOverlayMetadata":
                new Thread(() -> getOverlayMetadata(call, new MethodResultWrapper(result))).start();
                break;
            case "getContentResolverMetadata":
                new Thread(() -> getContentResolverMetadata(call, new MethodResultWrapper(result))).start();
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private boolean isVideo(@Nullable String mimeType) {
        return mimeType != null && mimeType.startsWith(MimeTypes.VIDEO);
    }

    private void getAllMetadata(MethodCall call, MethodChannel.Result result) {
        String mimeType = call.argument("mimeType");
        String uri = call.argument("uri");

        Map<String, Map<String, String>> metadataMap = new HashMap<>();

        try (InputStream is = StorageUtils.openInputStream(context, Uri.parse(uri))) {
            Metadata metadata = ImageMetadataReader.readMetadata(is);
            for (Directory dir : metadata.getDirectories()) {
                if (dir.getTagCount() > 0) {
                    // directory name
                    String dirName = dir.getName();
                    Map<String, String> dirMap = Objects.requireNonNull(metadataMap.getOrDefault(dirName, new HashMap<>()));
                    metadataMap.put(dirName, dirMap);

                    // tags
                    for (Tag tag : dir.getTags()) {
                        dirMap.put(tag.getTagName(), tag.getDescription());
                    }
                    if (dir instanceof XmpDirectory) {
                        try {
                            XmpDirectory xmpDir = (XmpDirectory) dir;
                            XMPMeta xmpMeta = xmpDir.getXMPMeta();
                            xmpMeta.sort();
                            XMPIterator xmpIterator = xmpMeta.iterator();
                            while (xmpIterator.hasNext()) {
                                XMPPropertyInfo prop = (XMPPropertyInfo) xmpIterator.next();
                                String xmpPath = prop.getPath();
                                String xmpValue = prop.getValue();
                                if (xmpPath != null && !xmpPath.isEmpty() && xmpValue != null && !xmpValue.isEmpty()) {
                                    dirMap.put(xmpPath, xmpValue);
                                }
                            }
                        } catch (XMPException e) {
                            Log.w(LOG_TAG, "failed to read XMP directory for uri=" + uri, e);
                        }
                    }
                }
            }
        } catch (Exception | NoClassDefFoundError e) {
            Log.w(LOG_TAG, "failed to get video metadata by ImageMetadataReader for uri=" + uri, e);
        }

        if (isVideo(mimeType)) {
            Map<String, String> videoDir = getVideoAllMetadataByMediaMetadataRetriever(uri);
            if (!videoDir.isEmpty()) {
                metadataMap.put("Video", videoDir);
            }
        }

        if (metadataMap.isEmpty()) {
            result.error("getAllMetadata-failure", "failed to get metadata for uri=" + uri, null);
        } else {
            result.success(metadataMap);
        }
    }

    private Map<String, String> getVideoAllMetadataByMediaMetadataRetriever(String uri) {
        Map<String, String> dirMap = new HashMap<>();
        MediaMetadataRetriever retriever = StorageUtils.openMetadataRetriever(context, Uri.parse(uri));
        try {
            for (Map.Entry<Integer, String> kv : VIDEO_MEDIA_METADATA_KEYS.entrySet()) {
                Integer key = kv.getKey();
                String value = retriever.extractMetadata(key);
                if (value != null) {
                    switch (key) {
                        case MediaMetadataRetriever.METADATA_KEY_BITRATE:
                            value = Formatter.formatFileSize(context, Long.parseLong(value)) + "/sec";
                            break;
                        case MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION:
                            value += "°";
                            break;
                    }
                    dirMap.put(kv.getValue(), value);
                }
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "failed to get video metadata by MediaMetadataRetriever for uri=" + uri, e);
        } finally {
            // cannot rely on `MediaMetadataRetriever` being `AutoCloseable` on older APIs
            retriever.release();
        }
        return dirMap;
    }

    private void getCatalogMetadata(MethodCall call, MethodChannel.Result result) {
        String mimeType = call.argument("mimeType");
        String uri = call.argument("uri");

        Map<String, Object> metadataMap = new HashMap<>(getCatalogMetadataByImageMetadataReader(uri, mimeType));
        if (isVideo(mimeType)) {
            metadataMap.putAll(getVideoCatalogMetadataByMediaMetadataRetriever(uri));
        }

        if (metadataMap.isEmpty()) {
            result.error("getCatalogMetadata-failure", "failed to get catalog metadata for uri=" + uri, null);
        } else {
            result.success(metadataMap);
        }
    }

    private Map<String, Object> getCatalogMetadataByImageMetadataReader(String uri, String mimeType) {
        Map<String, Object> metadataMap = new HashMap<>();

        // as of metadata-extractor 2.14.0, MP2T files are not supported
        if (MimeTypes.MP2T.equals(mimeType)) return metadataMap;

        try (InputStream is = StorageUtils.openInputStream(context, Uri.parse(uri))) {
            Metadata metadata = ImageMetadataReader.readMetadata(is);

            // File type
            for (FileTypeDirectory dir : metadata.getDirectoriesOfType(FileTypeDirectory.class)) {
                // the reported `mimeType` (e.g. from Media Store) is sometimes incorrect
                // file extension is unreliable
                // `context.getContentResolver().getType()` sometimes return incorrect value
                // `MediaMetadataRetriever.setDataSource()` sometimes fail with `status = 0x80000000`
                if (dir.containsTag(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE)) {
                    metadataMap.put(KEY_MIME_TYPE, dir.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE));
                }
            }

            // EXIF
            putDateFromDirectoryTag(metadataMap, KEY_DATE_MILLIS, metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (!metadataMap.containsKey(KEY_DATE_MILLIS)) {
                putDateFromDirectoryTag(metadataMap, KEY_DATE_MILLIS, metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_DATETIME);
            }

            // GPS
            for (GpsDirectory dir : metadata.getDirectoriesOfType(GpsDirectory.class)) {
                GeoLocation geoLocation = dir.getGeoLocation();
                if (geoLocation != null) {
                    metadataMap.put(KEY_LATITUDE, geoLocation.getLatitude());
                    metadataMap.put(KEY_LONGITUDE, geoLocation.getLongitude());
                }
            }

            // XMP
            for (XmpDirectory dir : metadata.getDirectoriesOfType(XmpDirectory.class)) {
                XMPMeta xmpMeta = dir.getXMPMeta();
                try {
                    if (xmpMeta.doesPropertyExist(XMP_DC_SCHEMA_NS, XMP_SUBJECT_PROP_NAME)) {
                        StringBuilder sb = new StringBuilder();
                        int count = xmpMeta.countArrayItems(XMP_DC_SCHEMA_NS, XMP_SUBJECT_PROP_NAME);
                        for (int i = 1; i < count + 1; i++) {
                            XMPProperty item = xmpMeta.getArrayItem(XMP_DC_SCHEMA_NS, XMP_SUBJECT_PROP_NAME, i);
                            sb.append(";").append(item.getValue());
                        }
                        metadataMap.put(KEY_XMP_SUBJECTS, sb.toString());
                    }

                    putLocalizedTextFromXmp(metadataMap, KEY_XMP_TITLE_DESCRIPTION, xmpMeta, XMP_TITLE_PROP_NAME);
                    if (!metadataMap.containsKey(KEY_XMP_TITLE_DESCRIPTION)) {
                        putLocalizedTextFromXmp(metadataMap, KEY_XMP_TITLE_DESCRIPTION, xmpMeta, XMP_DESCRIPTION_PROP_NAME);
                    }
                } catch (XMPException e) {
                    Log.w(LOG_TAG, "failed to read XMP directory for uri=" + uri, e);
                }
            }

            // Animated GIF & WEBP
            if (MimeTypes.GIF.equals(mimeType)) {
                metadataMap.put(KEY_IS_ANIMATED, metadata.containsDirectoryOfType(GifAnimationDirectory.class));
            } else if (MimeTypes.WEBP.equals(mimeType)) {
                for (WebpDirectory dir : metadata.getDirectoriesOfType(WebpDirectory.class)) {
                    if (dir.containsTag(WebpDirectory.TAG_IS_ANIMATION)) {
                        metadataMap.put(KEY_IS_ANIMATED, dir.getBoolean(WebpDirectory.TAG_IS_ANIMATION));
                    }
                }
            }
        } catch (Exception | NoClassDefFoundError e) {
            Log.w(LOG_TAG, "failed to get catalog metadata by ImageMetadataReader for uri=" + uri, e);
        }
        return metadataMap;
    }

    private Map<String, Object> getVideoCatalogMetadataByMediaMetadataRetriever(String uri) {
        Map<String, Object> metadataMap = new HashMap<>();
        MediaMetadataRetriever retriever = StorageUtils.openMetadataRetriever(context, Uri.parse(uri));
        try {
            String dateString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
            String rotationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            String locationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);

            if (dateString != null) {
                long dateMillis = MetadataHelper.parseVideoMetadataDate(dateString);
                // some entries have an invalid default date (19040101T000000.000Z) that is before Epoch time
                if (dateMillis > 0) {
                    metadataMap.put(KEY_DATE_MILLIS, dateMillis);
                }
            }
            if (rotationString != null) {
                metadataMap.put(KEY_VIDEO_ROTATION, Integer.parseInt(rotationString));
            }
            if (locationString != null) {
                Matcher locationMatcher = VIDEO_LOCATION_PATTERN.matcher(locationString);
                if (locationMatcher.find() && locationMatcher.groupCount() >= 2) {
                    String latitudeString = locationMatcher.group(1);
                    String longitudeString = locationMatcher.group(2);
                    if (latitudeString != null && longitudeString != null) {
                        try {
                            double latitude = Double.parseDouble(latitudeString);
                            double longitude = Double.parseDouble(longitudeString);
                            if (latitude != 0 && longitude != 0) {
                                metadataMap.put(KEY_LATITUDE, latitude);
                                metadataMap.put(KEY_LONGITUDE, longitude);
                            }
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "failed to get catalog metadata by MediaMetadataRetriever for uri=" + uri, e);
        } finally {
            // cannot rely on `MediaMetadataRetriever` being `AutoCloseable` on older APIs
            retriever.release();
        }
        return metadataMap;
    }

    private void getOverlayMetadata(MethodCall call, MethodChannel.Result result) {
        String mimeType = call.argument("mimeType");
        String uri = call.argument("uri");

        Map<String, Object> metadataMap = new HashMap<>();

        if (isVideo(mimeType)) {
            result.success(metadataMap);
            return;
        }

        try (InputStream is = StorageUtils.openInputStream(context, Uri.parse(uri))) {
            Metadata metadata = ImageMetadataReader.readMetadata(is);
            for (ExifSubIFDDirectory directory : metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)) {
                putDescriptionFromTag(metadataMap, KEY_APERTURE, directory, ExifSubIFDDirectory.TAG_FNUMBER);
                putDescriptionFromTag(metadataMap, KEY_FOCAL_LENGTH, directory, ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
                if (directory.containsTag(ExifSubIFDDirectory.TAG_EXPOSURE_TIME)) {
                    // TAG_EXPOSURE_TIME as a string is sometimes a ratio, sometimes a decimal
                    // so we explicitly request it as a rational (e.g. 1/100, 1/14, 71428571/1000000000, 4000/1000, 2000000000/500000000)
                    // and process it to make sure the numerator is `1` when the ratio value is less than 1
                    Rational rational = directory.getRational(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
                    long num = rational.getNumerator();
                    long denom = rational.getDenominator();
                    if (num > denom) {
                        metadataMap.put(KEY_EXPOSURE_TIME, rational.toSimpleString(true) + "″");
                    } else {
                        if (num != 1 && num != 0) {
                            rational = new Rational(1, Math.round(denom / (double) num));
                        }
                        metadataMap.put(KEY_EXPOSURE_TIME, rational.toString());
                    }
                }
                if (directory.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT)) {
                    metadataMap.put(KEY_ISO, "ISO" + directory.getDescription(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
                }
            }
            result.success(metadataMap);
        } catch (Exception | NoClassDefFoundError e) {
            result.error("getOverlayMetadata-exception", "failed to get metadata for uri=" + uri, e.getMessage());
        }
    }

    private void getContentResolverMetadata(MethodCall call, MethodChannel.Result result) {
        String mimeType = call.argument("mimeType");
        String uriString = call.argument("uri");
        if (mimeType == null || uriString == null) {
            result.error("getContentResolverMetadata-args", "failed because of missing arguments", null);
            return;
        }

        Uri uri = Uri.parse(uriString);
        long id = ContentUris.parseId(uri);
        Uri contentUri = uri;
        if (mimeType.startsWith(MimeTypes.IMAGE)) {
            contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        } else if (mimeType.startsWith(MimeTypes.VIDEO)) {
            contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentUri = MediaStore.setRequireOriginal(contentUri);
        }

        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Map<String, Object> metadataMap = new HashMap<>();
            int columnCount = cursor.getColumnCount();
            String[] columnNames = cursor.getColumnNames();
            for (int i = 0; i < columnCount; i++) {
                String key = columnNames[i];
                try {
                    switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_NULL:
                        default:
                            metadataMap.put(key, null);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            metadataMap.put(key, cursor.getLong(i));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            metadataMap.put(key, cursor.getFloat(i));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            metadataMap.put(key, cursor.getString(i));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            metadataMap.put(key, cursor.getBlob(i));
                            break;
                    }
                } catch (Exception e) {
                    Log.w(LOG_TAG, "failed to get value for key=" + key, e);
                }
            }
            cursor.close();
            result.success(metadataMap);
        } else {
            result.error("getContentResolverMetadata-null", "failed to get cursor for contentUri=" + contentUri, null);
        }
    }

    // convenience methods

    private static <T extends Directory> void putDateFromDirectoryTag(Map<String, Object> metadataMap, String key, Metadata metadata, Class<T> dirClass, int tag) {
        for (T dir : metadata.getDirectoriesOfType(dirClass)) {
            putDateFromTag(metadataMap, key, dir, tag);
        }
    }

    private static void putDateFromTag(Map<String, Object> metadataMap, String key, Directory dir, int tag) {
        if (dir.containsTag(tag)) {
            metadataMap.put(key, dir.getDate(tag, null, TimeZone.getDefault()).getTime());
        }
    }

    private static void putDescriptionFromTag(Map<String, Object> metadataMap, String key, Directory dir, int tag) {
        if (dir.containsTag(tag)) {
            metadataMap.put(key, dir.getDescription(tag));
        }
    }

    private static void putLocalizedTextFromXmp(Map<String, Object> metadataMap, String key, XMPMeta xmpMeta, String propName) throws XMPException {
        if (xmpMeta.doesPropertyExist(XMP_DC_SCHEMA_NS, propName)) {
            XMPProperty item = xmpMeta.getLocalizedText(XMP_DC_SCHEMA_NS, propName, XMP_GENERIC_LANG, XMP_SPECIFIC_LANG);
            // double check retrieved items as the property sometimes is reported to exist but it is actually null
            if (item != null) {
                metadataMap.put(key, item.getValue());
            }
        }
    }
}