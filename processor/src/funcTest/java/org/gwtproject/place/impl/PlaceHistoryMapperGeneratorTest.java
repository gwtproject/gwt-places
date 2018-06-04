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
package org.gwtproject.place.impl;

import junit.framework.TestCase;
import org.gwtproject.place.shared.Place;
import org.gwtproject.place.shared.PlaceHistoryMapper;
import org.gwtproject.place.shared.WithFactory;
import org.gwtproject.place.shared.WithTokenizers;
import org.gwtproject.place.testplacemappers.NoFactoryImpl;
import org.gwtproject.place.testplacemappers.WithFactoryImpl;
import org.gwtproject.place.testplaces.Place1;
import org.gwtproject.place.testplaces.Place2;
import org.gwtproject.place.testplaces.Place3;
import org.gwtproject.place.testplaces.Place4;
import org.gwtproject.place.testplaces.Place5;
import org.gwtproject.place.testplaces.Place6;
import org.gwtproject.place.testplaces.Tokenizer2;
import org.gwtproject.place.testplaces.Tokenizer3;
import org.gwtproject.place.testplaces.Tokenizer4;
import org.gwtproject.place.testplaces.TokenizerFactory;

/** Functional test of PlaceHistoryMapperGenerator. */
public class PlaceHistoryMapperGeneratorTest extends TestCase {
  @WithTokenizers({
    Place1.Tokenizer.class,
    Tokenizer2.class,
    Tokenizer3.class,
    Tokenizer4.class,
    Place6.Tokenizer.class
  })
  interface LocalNoFactory extends PlaceHistoryMapper {}

  @WithFactory(TokenizerFactory.class)
  @WithTokenizers({Tokenizer4.class, Place6.Tokenizer.class})
  interface LocalWithFactory extends PlaceHistoryMapper {}

  // Note: tokenizer order here is important for the test!
  @WithTokenizers({Tokenizer2.class, Place1.Tokenizer.class, Tokenizer3.class})
  interface SortOrder extends PlaceHistoryMapper {}

  Place1 place1 = new Place1("able");
  Place2 place2 = new Place2("baker");
  Place3 place3 = new Place3("charlie");
  Place4 place4 = new Place4("delta");
  Place5 place5 = new Place5("echo");
  Place6 place6 = new Place6("foxtrot");

  public void testTopLevelWithoutFactory() {
    AbstractPlaceHistoryMapper subject = new NoFactoryImpl();

    doTest(subject, null);
  }

  public void testTopLevelWithFactory() {
    TokenizerFactory factory = new TokenizerFactory();
    AbstractPlaceHistoryMapper subject = new WithFactoryImpl(factory);

    doTest(subject, factory);
  }

  public void testNestedWithoutFactory() {
    AbstractPlaceHistoryMapper subject = new PlaceHistoryMapperGeneratorTest_LocalNoFactoryImpl();

    doTest(subject, null);
  }

  public void testNestedWithFactory() {
    TokenizerFactory factory = new TokenizerFactory();
    AbstractPlaceHistoryMapper subject =
        new PlaceHistoryMapperGeneratorTest_LocalWithFactoryImpl(factory);

    doTest(subject, factory);
  }

  /** See https://github.com/gwtproject/gwt/issues/8033 */
  public void testSortOrder() {
    PlaceHistoryMapper subject = new PlaceHistoryMapperGeneratorTest_SortOrderImpl();
    assertEquals(Place1.Tokenizer.PREFIX + ":" + place1.content, subject.getToken(place1));
    assertEquals("Place2:" + place2.content, subject.getToken(place2));
    assertEquals("Place3:" + place3.content, subject.getToken(place3));
  }

  // CHECKSTYLE_OFF
  private void doTest(AbstractPlaceHistoryMapper subject, TokenizerFactory factory) {
    String history1 = subject.getPrefixAndToken(place1).toString();
    assertEquals(Place1.Tokenizer.PREFIX + ":" + place1.content, history1);

    String history2 = subject.getPrefixAndToken(place2).toString();
    if (factory != null) {
      assertEquals(TokenizerFactory.PLACE2_PREFIX + ":" + place2.content, history2);
    } else {
      assertEquals("Place2:" + place2.content, history2);
    }

    String history3 = subject.getPrefixAndToken(place3).toString();
    assertEquals("Place3:" + place3.content, history3);

    String history4 = subject.getPrefixAndToken(place4).toString();
    assertEquals("Place4:" + place4.content, history4);

    // Place 5 extends Place3 and does not have its own PlaceTokenizer
    String history5 = subject.getPrefixAndToken(place5).toString();
    assertEquals("Place3:" + place5.content, history5);

    if (factory != null) {
      assertEquals(factory.tokenizer, subject.getTokenizer(Place1.Tokenizer.PREFIX));
      assertEquals(factory.tokenizer2, subject.getTokenizer(TokenizerFactory.PLACE2_PREFIX));
      assertEquals(factory.tokenizer3, subject.getTokenizer("Place3"));
    } else {
      assertTrue(subject.getTokenizer(Place1.Tokenizer.PREFIX) instanceof Place1.Tokenizer);
      assertTrue(subject.getTokenizer("Place2") instanceof Tokenizer2);
      assertTrue(subject.getTokenizer("Place3") instanceof Tokenizer3);
    }
    assertTrue(subject.getTokenizer("Place4") instanceof Tokenizer4);

    // Empty prefix
    String history6 = subject.getPrefixAndToken(place6).toString();
    assertEquals(place6.content, history6);
    assertTrue(subject.getTokenizer("") instanceof Place6.Tokenizer);
    assertTrue(subject.getPlace("noPrefix") instanceof Place6);

    Place place = new Place() {};
    assertNull(subject.getPrefixAndToken(place));
    assertNull(subject.getTokenizer("snot"));
  }
  // CHECKSTYLE_ON
}
