public interface MySupplier {
  int numberOfCharts();

  int numberOfSeries(int chartNumber);

  String getTitle(int chartNumber, int seriesIndex);

  double[][] generateSeries(int chartNumber, int seriesIndex);

  String plot();
}
