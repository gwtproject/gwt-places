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
package org.gwtproject.place.testplacemappers;

import org.gwtproject.place.shared.PlaceHistoryMapper;
import org.gwtproject.place.shared.WithTokenizers;
import org.gwtproject.place.testplaces.Place6;
import org.gwtproject.place.testplaces.Tokenizer4;
import org.gwtproject.place.testplaces.TokenizerFactory;

/** Used by tests of {@link org.gwtproject.place.rebind.PlaceHistoryMapperGenerator}. */
@org.gwtproject.place.shared.WithFactory(TokenizerFactory.class)
@WithTokenizers({Tokenizer4.class, Place6.Tokenizer.class})
public interface WithFactory extends PlaceHistoryMapper {}
