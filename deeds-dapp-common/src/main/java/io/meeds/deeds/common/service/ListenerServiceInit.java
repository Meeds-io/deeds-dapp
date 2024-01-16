/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.deeds.common.service;

import static io.meeds.deeds.common.service.ListenerService.EVENT_LISTENERS;
import static io.meeds.deeds.common.service.ListenerService.LISTENERS;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.meeds.deeds.common.listener.EventListener;

/**
 * A class to initialize ListenerService to avoid having cyclic dependency:
 * ListenerService -> EventListeners -> Service Layer Components ->
 * ListenerService
 */
@Service
public class ListenerServiceInit implements ApplicationContextAware {

  @Autowired
  private ListenerService    listenerService;

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void init() {
    if (!LISTENERS.isEmpty()) {
      // Disable ES scanning
      ListenerService.persistentFeatureEnabled = false; // NOSONAR
    }
    registerListeners();
  }

  /**
   * Retrieves and registers automatically the list of listeners registered in
   * Spring {@link ApplicationContext} as a {@link Component} and that extends
   * {@link EventListener}
   */
  @SuppressWarnings("rawtypes")
  private synchronized void registerListeners() {
    Map<String, EventListener> eventListenerBeans = applicationContext.getBeansOfType(EventListener.class);
    for (EventListener<?> eventListener : eventListenerBeans.values()) {
      EVENT_LISTENERS.add(eventListener);
    }
    // Reorder Listeners by Event Name and use a new Map
    // In order to allow programmatically add/remove listeners
    if (!CollectionUtils.isEmpty(EVENT_LISTENERS)) {
      EVENT_LISTENERS.forEach(listenerService::addListener);
    }
  }

}
