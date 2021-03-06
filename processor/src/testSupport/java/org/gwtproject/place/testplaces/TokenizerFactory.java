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
import org.gwtproject.place.shared.Prefix;

/** Used by tests of {@link org.gwtproject.place.processor.PlaceHistoryMapperProcessor}. */
public class TokenizerFactory {
  public static final String PLACE2_PREFIX = "p2";

  public final Place1.Tokenizer tokenizer = new Place1.Tokenizer();
  public final Tokenizer2 tokenizer2 = new Tokenizer2();
  public final Tokenizer3 tokenizer3 = new Tokenizer3();

  public Place1.Tokenizer getTokenizer1() {
    return tokenizer;
  }

  @Prefix(PLACE2_PREFIX)
  public final Tokenizer2 getTokenizer2() {
    return tokenizer2;
  }

  public PlaceTokenizer<Place3> getTokenizer3() {
    return tokenizer3;
  }
}
