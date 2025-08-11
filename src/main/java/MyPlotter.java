import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

public class MyPlotter {
  public static String plot(Object obj) throws Exception {
    Method numberOfSeries = obj.getClass().getMethod("numberOfSeries");
    Method generateSeries = obj.getClass().getMethod("generateSeries", int.class);
    Method getTitle = obj.getClass().getMethod("getTitle", int.class);
    final int numberOfSeriesValue = (Integer) numberOfSeries.invoke(obj);

    XYChart chart =
        new XYChartBuilder()
            .width(800)
            .height(400)
            .title("Area Chart")
            .xAxisTitle("x")
            .yAxisTitle("y")
            .build();
    for (int i = 0; i < numberOfSeriesValue; i++) {
      double[][] series = (double[][]) generateSeries.invoke(obj, i);
      double[] xData = new double[series.length];
      double[] yData = new double[series.length];
      for (int j = 0; j < series.length; j++) {
        xData[j] = series[j][0];
        yData[j] = series[j][1];
      }
      chart.addSeries((String) getTitle.invoke(obj, i), xData, yData);
    }
    return imgToBase64String(BitmapEncoder.getBufferedImage(chart));
  }

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

  private static String imgToBase64String(RenderedImage img) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      ImageIO.write(img, "PNG", os);
      return "data:image/png;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
  }
}
