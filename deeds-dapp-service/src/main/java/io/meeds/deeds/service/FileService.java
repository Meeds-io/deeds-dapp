/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
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
package io.meeds.deeds.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.deeds.elasticsearch.model.DeedFileBinary;
import io.meeds.deeds.model.FileBinary;
import io.meeds.deeds.storage.DeedFileRepository;

@Component
public class FileService {

  @Autowired
  private DeedFileRepository fileRepository;

  public String saveFile(FileBinary fileBinary) throws IOException {
    if (fileBinary == null) {
      throw new IllegalArgumentException("wom.fileIsMandatory");
    }
    DeedFileBinary file = new DeedFileBinary(fileBinary.getId(),
                                             fileBinary.getName(),
                                             fileBinary.getMimeType(),
                                             Base64.encodeBase64String(IOUtils.toByteArray(fileBinary.getBinary())),
                                             Instant.now());
    file = fileRepository.save(file);
    return file.getId();
  }

  public FileBinary getFile(String fileId) {
    DeedFileBinary file = fileRepository.findById(fileId).orElse(null);
    return file == null ? null
                        : new FileBinary(file.getId(),
                                         file.getName(),
                                         file.getMimeType(),
                                         new ByteArrayInputStream(Base64.decodeBase64(file.getBinary())),
                                         file.getUpdatedDate());
  }

}
