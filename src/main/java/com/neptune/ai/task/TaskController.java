package com.neptune.ai.task;

import com.neptune.ai.task.domain.Point;
import com.neptune.ai.task.domain.Stats;
import com.neptune.ai.task.exception.BadRequestException;
import com.neptune.ai.task.request.PointRequest;
import com.neptune.ai.task.request.PointsRequest;
import com.neptune.ai.task.request.StatsRequest;
import com.neptune.ai.task.service.DataPointService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

  private final DataPointService service = new DataPointService();
  private final int maxExp = 8;

  @PostMapping(
      value = "/add",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Point> addPoint(@RequestBody PointRequest pointRequest) {
    return new ResponseEntity<>(service.addPoint(pointRequest), HttpStatus.CREATED);
  }

  @PostMapping(
      value = "/add_batch",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Point>> addPoints(@RequestBody PointsRequest pointsRequest) {
    return new ResponseEntity<>(service.addPoints(pointsRequest), HttpStatus.CREATED);
  }

  @GetMapping(
      value = "/stats",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Stats> getStats(StatsRequest statsRequest) {

    if (!isPowerOfTen(statsRequest.k())) {
      throw new BadRequestException(
          String.format("k must be an exponential of 10 and %s as a max", 10000000));
    }

    return ResponseEntity.ok(service.latestKPoints(statsRequest));
  }

  public boolean isPowerOfTen(int number) {
    if (number <= 0) {
      return false;
    }

    int it = 0;

    while (number != 1 && it < maxExp) {
      if (number % 10 != 0) {
        return false;
      }
      number /= 10;
      it++;
    }

    return true;
  }
}
