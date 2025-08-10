public class MySupplier {
  public int numberOfSeries() {
    return 2; // Return the number of series to be generated
  }

  public String getTitle(int seriesIndex) {
    return "Series " + (seriesIndex + 1); // Return a title for each series
  }

  public double[][] generateSeries(int seriesIndex) {
    double[][] series = new double[10][2];
    double a = seriesIndex * 10; // Example coefficient based on series index
    for (int i = 0; i < series.length; i++) {
      double y = f(i, a);
      series[i][0] = i; // x value
      series[i][1] = y; // y value based on the function
    }
    return series;
  }

  private double f(double x, double a) {
    return x * x * a; // Example function
  }
}
