import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import javax.imageio.ImageIO;

public class MyPlotter {
  public static String imgToBase64String(RenderedImage img) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      ImageIO.write(img, "PNG", os);
      return "data:image/png;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
  }
}
