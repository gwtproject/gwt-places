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
package org.gwtproject.place.testplaces;

import org.gwtproject.place.shared.PlaceTokenizer;

/** Used by tests of {@link org.gwtproject.place.processor.PlaceHistoryMapperProcessor}. */
public class Tokenizer2 implements PlaceTokenizer<Place2> {
  @Override
  public Place2 getPlace(String token) {
    return new Place2(token);
  }

  @Override
  public String getToken(Place2 place) {
    return place.content;
  }
}
