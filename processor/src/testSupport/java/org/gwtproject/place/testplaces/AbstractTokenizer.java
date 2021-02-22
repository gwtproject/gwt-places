/*
 * Copyright 2012 The GWT Project Authors
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

/**
 * Used by tests of {@code org.gwtproject.place.processor.PlaceHistoryMapperProcessor}.
 *
 * @param <P> A place type (for testing generics).
 */
public abstract class AbstractTokenizer<P extends Place1> implements PlaceTokenizer<P> {
  @Override
  public abstract P getPlace(String token);

  @Override
  public String getToken(P place) {
    return place.content;
  }
}
