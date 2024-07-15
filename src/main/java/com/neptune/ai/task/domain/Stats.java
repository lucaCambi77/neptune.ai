package com.neptune.ai.task.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

@Builder
public record Stats(
    @JsonIgnore double sum,
    @JsonIgnore double sumSq,
    double min,
    double max,
    double last,
    double avg,
    double var) {}
