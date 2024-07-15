package com.neptune.ai.task.service;

import com.neptune.ai.task.domain.Point;
import com.neptune.ai.task.domain.Stats;
import com.neptune.ai.task.exception.BadRequestException;
import com.neptune.ai.task.request.PointRequest;
import com.neptune.ai.task.request.PointsRequest;
import com.neptune.ai.task.request.StatsRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataPointService {

  private final Map<String, List<Point>> symbolToPointsMap = new HashMap<>();

  /**
   * Add a point as symbol and value. It also calculates cumulative statistics of this point
   * compared and merged with the previous ones
   *
   * @param pointRequest include the symbol and the value
   * @return new Point
   */
  public Point addPoint(PointRequest pointRequest) {
    List<Point> pointList =
        symbolToPointsMap.getOrDefault(pointRequest.symbol(), new ArrayList<>());

    Point point =
        new Point(
            pointList.size() + 1,
            pointRequest.symbol(),
            pointRequest.value(),
            stats(pointRequest.value(), latest(pointRequest.symbol())));

    pointList.add(pointList.size(), point);
    symbolToPointsMap.put(pointRequest.symbol(), pointList);

    return point;
  }

  /**
   * Add points in batch as symbol and array of values. It also calculates cumulative statistics of
   * these points compared and merged with the previous ones
   *
   * @param pointsRequest include the symbol and a batch of values
   * @return new Points
   */
  public List<Point> addPoints(PointsRequest pointsRequest) {

    Point latest = latest(pointsRequest.symbol());

    List<Point> pointList =
        symbolToPointsMap.getOrDefault(pointsRequest.symbol(), new ArrayList<>());

    List<Point> points = new ArrayList<>();

    for (double newValue : pointsRequest.values()) {
      Point p =
          new Point(
              pointList.size() + 1, pointsRequest.symbol(), newValue, stats(newValue, latest));
      pointList.add(pointList.size(), p);
      symbolToPointsMap.put(pointsRequest.symbol(), pointList);
      latest = p;
      points.add(p);
    }

    return points;
  }

  /**
   * Calculate stats for a new point given the latest point. This way we keep track of the stats of
   * every point, so we can access them in constant time when requested in {@link #latestKPoints}
   *
   * @param newValue new point's value
   * @param latest point to merge with the new value and calculate statistics
   * @return stats of a specific point
   */
  private Stats stats(Double newValue, Point latest) {
    if (latest == null) {
      return Stats.builder()
          .sum(newValue)
          .sumSq(newValue * newValue)
          .min(newValue)
          .max(newValue)
          .last(newValue)
          .build();
    }

    return Stats.builder()
        .sum(latest.stats().sum() + newValue)
        .sumSq(latest.stats().sumSq() + newValue * newValue)
        .min(Math.min(latest.stats().min(), newValue))
        .max(Math.max(latest.stats().max(), newValue))
        .last(newValue)
        .build();
  }

  public Stats latestKPoints(StatsRequest request) {

    Point latest = latest(request.symbol());

    if (null == latest) {
      throw new BadRequestException(
          String.format("No stats available for symbol %s", request.symbol()));
    }

    List<Point> pointList = symbolToPointsMap.get(request.symbol());

    if (request.k() > pointList.size()) {
      throw new BadRequestException(
          String.format(
              "Only %s points are available for symbol %s", pointList.size(), request.symbol()));
    }

    int pos = pointList.size() - request.k() - 1;

    // k is equal to the symbol's point list size
    if (pos < 0) {
      return Stats.builder()
          .min(latest.stats().min())
          .max(latest.stats().max())
          .last(latest.value())
          .avg(latest.stats().sum() / request.k())
          .var(variance(latest.stats().sumSq(), request.k(), latest.stats().sum() / request.k()))
          .build();
    }

    Point limit = pointList.get(pos);

    double sumLastK = latest.stats().sum() - limit.stats().sum();
    double sumSqLastK = latest.stats().sumSq() - limit.stats().sumSq();

    return Stats.builder()
        .min(Math.min(latest.stats().min(), limit.stats().min()))
        .max(Math.max(latest.stats().max(), limit.stats().max()))
        .last(latest.value())
        .avg(sumLastK / request.k())
        .var(variance(sumSqLastK, request.k(), sumLastK / request.k()))
        .build();
  }

  /**
   * Get the latest inserted point, corresponding to a symbol's points list size if exists
   *
   * @param symbol string for which we want to get the latest point
   * @return latest point
   */
  private Point latest(String symbol) {
    List<Point> p = symbolToPointsMap.getOrDefault(symbol, new ArrayList<>());

    if (!p.isEmpty()) {
      return symbolToPointsMap.get(symbol).get(p.size() - 1);
    }

    return null;
  }

  /**
   * Calculate the variance of last k values given the sum of squares. This formula derives from the
   * classic calculation of sums of the squares of dispersion of points from the mean
   *
   * @param sumSq sum of the point's squares
   * @param k last points for which we want to calculate the variance
   * @param meanLastK mean of the point's squares
   * @return the variance of last k points
   */
  private double variance(double sumSq, int k, double meanLastK) {
    return (sumSq / k) - (meanLastK * meanLastK);
  }
}
