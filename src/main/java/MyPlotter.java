import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

public class MyPlotter {
  @Deprecated
  public static String plot(MySupplier supplier) {
    XYChart chart =
        new XYChartBuilder()
            .width(800)
            .height(400)
            .title("Area Chart")
            .xAxisTitle("x")
            .yAxisTitle("y")
            .build();
    for (int i = 0; i < supplier.numberOfSeries(); i++) {
      double[][] series = supplier.generateSeries(i);
      double[] xData = new double[series.length];
      double[] yData = new double[series.length];
      for (int j = 0; j < series.length; j++) {
        xData[j] = series[j][0];
        yData[j] = series[j][1];
      }
      chart.addSeries(supplier.getTitle(i), xData, yData);
    }
    return imgToBase64String(BitmapEncoder.getBufferedImage(chart));
  }

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
