/*
 * Copyright 2010 The GWT Project Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gwtproject.place.shared;

import org.gwtproject.event.shared.Event;

/** Event thrown when the user has reached a new location in the app. */
public class PlaceChangeEvent extends Event<PlaceChangeEvent.Handler> {

  /** Implemented by handlers of PlaceChangeEvent. */
  public interface Handler {
    /**
     * Called when a {@link PlaceChangeEvent} is fired.
     *
     * @param event the {@link PlaceChangeEvent}
     */
    void onPlaceChange(PlaceChangeEvent event);
  }

  /** A singleton instance of Type&lt;Handler&gt;. */
  public static final Type<Handler> TYPE = new Type<>();

  private final Place newPlace;

  /**
   * Constructs a PlaceChangeEvent for the given {@link Place}.
   *
   * @param newPlace a {@link Place} instance
   */
  public PlaceChangeEvent(Place newPlace) {
    this.newPlace = newPlace;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  /**
   * Return the new {@link Place}.
   *
   * @return a {@link Place} instance
   */
  public Place getNewPlace() {
    return newPlace;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onPlaceChange(this);
  }
}
