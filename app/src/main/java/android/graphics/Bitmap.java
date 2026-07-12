package android.graphics;

import java.io.OutputStream;

public class Bitmap {
    public int getWidth() { return 0; }
    public int getHeight() { return 0; }
    public void recycle() {}
    public enum CompressFormat {
        JPEG,
        PNG,
        WEBP
    }
    public boolean compress(CompressFormat format, int quality, OutputStream stream) { return false; }
}
