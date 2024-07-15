package com.neptune.ai.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.neptune.ai.task.domain.Stats;
import com.neptune.ai.task.exception.BadRequestException;
import com.neptune.ai.task.request.PointsRequest;
import com.neptune.ai.task.request.StatsRequest;
import com.neptune.ai.task.service.DataPointService;
import org.junit.jupiter.api.Test;

class ServiceTest {

  private final DataPointService service = new DataPointService();

  @Test
  void shouldGetStatsWhenKthIsEqualToSymbolSize() {
    PointsRequest request = new PointsRequest("ABC", new double[] {10.0, 10.0, 20.0, 40.0});

    service.addPoints(request);

    Stats stats = service.latestKPoints(new StatsRequest("ABC", 4));

    assertNotNull(stats);
    assertEquals(20.0, stats.avg());
    assertEquals(10.0, stats.min());
    assertEquals(40.0, stats.max());
    assertEquals(40.0, stats.last());
    assertEquals(150, stats.var());
  }

  @Test
  void shouldGetStatsWhenKthIsLessThanSymbolSize() {
    PointsRequest request = new PointsRequest("ABC", new double[] {10.0, 10.0, 20.0, 40.0});

    service.addPoints(request);

    Stats stats = service.latestKPoints(new StatsRequest("ABC", 3));

    assertNotNull(stats);
    assertEquals(23.333333333333332, stats.avg());
    assertEquals(10.0, stats.min());
    assertEquals(40.0, stats.max());
    assertEquals(40.0, stats.last());
    assertEquals(155.55555555555566, stats.var());
  }

  @Test
  void shouldGetStatsOnlyForAvailablePortionOfK() {
    PointsRequest request = new PointsRequest("ABC", new double[] {10.0, 10.0, 20.0, 40.0});

    service.addPoints(request);

    assertThrows(
        BadRequestException.class, () -> service.latestKPoints(new StatsRequest("ABC", 5)));
  }

  @Test
  void shouldThrowWhenStatsAreNotAvailable() {
    assertThrows(
        BadRequestException.class, () -> service.latestKPoints(new StatsRequest("ABC", 3)));
  }
}
