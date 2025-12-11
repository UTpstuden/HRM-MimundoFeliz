package com.utp.hcm.dto;

public record TimeSeriesPoint(
        String label,
        double value,
        boolean projected
) {}
