public interface MySupplier {
  /**
   * Returns the number of series to be generated.
   *
   * @return the number of series
   */
  int numberOfSeries();

  /**
   * Returns the title for a specific series.
   *
   * @param seriesIndex the index of the series
   * @return the title of the series
   */
  String getTitle(int seriesIndex);

  /**
   * Generates a series of data points for a specific series.
   *
   * @param seriesIndex the index of the series
   * @return a 2D array where each row contains [x, y] values for the series
   */
  double[][] generateSeries(int seriesIndex);

  /**
   * Generates a plot of the series data.
   *
   * @return a base64 encoded string representing the plot image
   */
  String plot();
}
