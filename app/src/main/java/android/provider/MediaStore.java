package android.provider;
public class MediaStore {
    public static final String AUTHORITY = "media";
    public static class Images {
        public static class Media {
            public static final String DATA = "_data";
        }
        public static class Thumbnails {
            public static final int MINI_KIND = 1;
            public static final int MICRO_KIND = 3;
            public static final String DATA = "_data";
        }
    }
    public static class Video {
        public static final int MINI_KIND = 1;
    }
}
