package io.meeds.deeds.elasticsearch.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import io.meeds.deeds.constant.TrialStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "trial_contacts")
public class TrialContactInformation {
    
  @Id
  private Long                id;

  @Field(type = FieldType.Text)
  private String              firstName;

  @Field(type = FieldType.Text)
  private String              lastName;

  @Field(type = FieldType.Text)
  private String              position;

  @Field(type = FieldType.Text)
  private String              organization;

  @Field(type = FieldType.Text)
  private String              email;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime       date;

  @Field(type = FieldType.Auto)
  private TrialStatus         status;

  private Map<String, String> properties;

}
