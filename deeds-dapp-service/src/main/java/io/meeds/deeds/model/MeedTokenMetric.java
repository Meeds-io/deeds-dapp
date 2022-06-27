package io.meeds.deeds.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.annotations.Setting.SortOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "meed_token_metrics", createIndex = true)
@Setting(
    sortFields = "date",
    sortOrders = SortOrder.desc,
    replicas = 0,
    shards = 1
)
public class MeedTokenMetric {

  @Id
  @Field(type = FieldType.Date, format = DateFormat.year_month_day)
  private LocalDate               date;

  private BigDecimal              totalSupply;

  private Map<String, BigDecimal> lockedValues;

  private Map<String, BigDecimal> reserveValues;

  private BigDecimal              circulatingSupply;

}
