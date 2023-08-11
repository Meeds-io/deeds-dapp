/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.elasticsearch.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.deeds.model.DeedMetadataAttribute;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "deed_nft_metadata", createIndex = false)
@Setting(replicas = 0, shards = 1)
@JsonInclude(value = Include.NON_EMPTY)
public class DeedMetadata implements Cloneable {

  @Id
  private long                       nftId;

  private String                     name;

  private String                     description;

  @JsonProperty("image")
  private String                     imageUrl;

  @JsonProperty("background_color")
  private String                     backgroundColor;

  @JsonProperty("external_url")
  private String                     externalUrl;

  @JsonProperty("external_link")
  private String                     externalLink;

  @JsonProperty("seller_fee_basis_points")
  private String                     sellerFeeBasisPoints;

  @JsonProperty("fee_recipient")
  private String                     feeRecipient;

  private Set<DeedMetadataAttribute> attributes = new HashSet<>();

  @Field(type = FieldType.Date, format = DateFormat.year_month_day)
  private LocalDate                  date;

  @Override
  public DeedMetadata clone() {// NOSONAR
    return new DeedMetadata(nftId,
                            name,
                            description,
                            imageUrl,
                            backgroundColor,
                            externalUrl,
                            externalLink,
                            sellerFeeBasisPoints,
                            feeRecipient,
                            attributes == null ? new HashSet<>() : new HashSet<>(attributes),
                            date);
  }

}
