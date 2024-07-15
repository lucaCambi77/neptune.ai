package com.neptune.ai.task.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Point(int sequence, String symbol, double value, @JsonIgnore Stats stats) {}
