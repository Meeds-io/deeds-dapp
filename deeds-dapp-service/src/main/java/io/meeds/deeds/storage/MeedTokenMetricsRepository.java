package io.meeds.deeds.storage;

import java.time.LocalDate;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import io.meeds.deeds.model.MeedTokenMetric;

public interface MeedTokenMetricsRepository extends ElasticsearchRepository<MeedTokenMetric, LocalDate> {

}
