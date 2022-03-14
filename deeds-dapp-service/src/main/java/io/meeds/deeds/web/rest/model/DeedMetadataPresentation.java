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
package io.meeds.deeds.web.rest.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.meeds.deeds.model.DeedMetadata;
import io.meeds.deeds.model.DeedMetadataAttribute;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
public class DeedMetadataPresentation {

  @JsonProperty("name")
  @NonNull
  @EqualsAndHashCode.Exclude
  private String                     name;

  @JsonProperty("description")
  @NonNull
  @EqualsAndHashCode.Exclude
  private String                     description;

  @JsonProperty("image")
  @NonNull
  @EqualsAndHashCode.Exclude
  private String                     imageUrl;

  @JsonProperty("background_color")
  @EqualsAndHashCode.Exclude
  private String                     backgroundColor;

  @JsonProperty("external_url")
  @EqualsAndHashCode.Exclude
  private String                     externalUrl;

  @JsonProperty("attributes")
  @EqualsAndHashCode.Exclude
  private Set<DeedMetadataAttribute> attributes = new HashSet<>();

  public static DeedMetadataPresentation build(DeedMetadata deedMetadata) {
    if (deedMetadata == null) {
      return null;
    }
    Set<DeedMetadataAttribute> attributes = deedMetadata.getAttributes() == null ? new HashSet<>()
                                                                                 : new HashSet<>(deedMetadata.getAttributes());
    return new DeedMetadataPresentation(deedMetadata.getName(),
                                        deedMetadata.getDescription(),
                                        deedMetadata.getImageUrl(),
                                        deedMetadata.getBackgroundColor(),
                                        deedMetadata.getExternalUrl(),
                                        attributes);
  }
}
