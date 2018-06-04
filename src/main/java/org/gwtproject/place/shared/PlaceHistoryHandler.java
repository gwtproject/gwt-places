/*
 * Copyright 2010 The GWT Project Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gwtproject.place.shared;

import java.util.logging.Logger;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.EventBus;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.user.history.client.History;

/** Monitors {@link PlaceChangeEvent}s and {@link History} events and keep them in sync. */
public class PlaceHistoryHandler {
  private static final Logger log = Logger.getLogger(PlaceHistoryHandler.class.getName());

  /** Default implementation of {@link Historian}, based on {@link History}. */
  public static class DefaultHistorian implements Historian {
    @Override
    public HandlerRegistration addValueChangeHandler(
        ValueChangeHandler<String> valueChangeHandler) {
      return History.addValueChangeHandler(valueChangeHandler);
    }

    @Override
    public String getToken() {
      return History.getToken();
    }

    @Override
    public void newItem(String token, boolean issueEvent) {
      History.newItem(token, issueEvent);
    }
  }

  /**
   * Optional delegate in charge of History related events. Provides nice isolation for unit
   * testing, and allows pre- or post-processing of tokens. Methods correspond to the like named
   * methods on {@link History}.
   */
  public interface Historian {
    HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> valueChangeHandler);

    /** @return the current history token. */
    String getToken();

    /**
     * Adds a new browser history entry. Calling this method will cause the value change handler to
     * be called as well.
     */
    void newItem(String token, boolean issueEvent);
  }

  private final Historian historian;

  private final PlaceHistoryMapper mapper;

  private PlaceController placeController;

  private Place defaultPlace = Place.NOWHERE;

  /**
   * Create a new PlaceHistoryHandler with a {@link DefaultHistorian}.
   *
   * @param mapper a {@link PlaceHistoryMapper} instance
   */
  public PlaceHistoryHandler(PlaceHistoryMapper mapper) {
    this(mapper, new DefaultHistorian());
  }

  /**
   * Create a new PlaceHistoryHandler.
   *
   * @param mapper a {@link PlaceHistoryMapper} instance
   * @param historian a {@link Historian} instance
   */
  public PlaceHistoryHandler(PlaceHistoryMapper mapper, Historian historian) {
    this.mapper = mapper;
    this.historian = historian;
  }

  /**
   * Handle the current history token. Typically called at application start, to ensure bookmark
   * launches work.
   */
  public void handleCurrentHistory() {
    handleHistoryToken(historian.getToken());
  }

  /**
   * Initialize this place history handler.
   *
   * @return a registration object to de-register the handler
   */
  public HandlerRegistration register(
      PlaceController placeController, EventBus eventBus, Place defaultPlace) {
    this.placeController = placeController;
    this.defaultPlace = defaultPlace;

    final HandlerRegistration placeReg =
        eventBus.addHandler(
            PlaceChangeEvent.TYPE,
            event -> {
              Place newPlace = event.getNewPlace();
              historian.newItem(tokenForPlace(newPlace), false);
            });

    final HandlerRegistration historyReg =
        historian.addValueChangeHandler(event -> handleHistoryToken(event.getValue()));

    return () -> {
      PlaceHistoryHandler.this.defaultPlace = Place.NOWHERE;
      PlaceHistoryHandler.this.placeController = null;
      placeReg.removeHandler();
      historyReg.removeHandler();
    };
  }

  /** Visible for testing. */
  Logger log() {
    return log;
  }

  private void handleHistoryToken(String token) {

    Place newPlace = null;

    if ("".equals(token)) {
      newPlace = defaultPlace;
    }

    if (newPlace == null) {
      newPlace = mapper.getPlace(token);
    }

    if (newPlace == null) {
      log().warning("Unrecognized history token: " + token);
      newPlace = defaultPlace;
    }

    placeController.goTo(newPlace);
  }

  private String tokenForPlace(Place newPlace) {
    if (defaultPlace.equals(newPlace)) {
      return "";
    }

    String token = mapper.getToken(newPlace);
    if (token != null) {
      return token;
    }

    log().warning("Place not mapped to a token: " + newPlace);
    return "";
  }
}
