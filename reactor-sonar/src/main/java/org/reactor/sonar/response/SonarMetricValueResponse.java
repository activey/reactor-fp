package org.reactor.sonar.response;

import org.reactor.sonar.data.MetricWithValueResource;

public class SonarMetricValueResponse extends AbstractMetricValueResponse {

    private final MetricWithValueResource metricValueResource;

    public SonarMetricValueResponse(MetricWithValueResource metricValueResource) {
        this.metricValueResource = metricValueResource;
    }

    @Override
    protected double getMetricValue() {
        return metricValueResource.getMetricValue();
    }

    @Override
    protected String getMetricValueDescription() {
        return metricValueResource.getMetricDesription();
    }

}
