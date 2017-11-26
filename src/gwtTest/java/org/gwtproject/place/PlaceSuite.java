package org.gwtproject.place;

import org.gwtproject.place.shared.PlaceChangeRequestEventTest;
import org.gwtproject.place.shared.PlaceControllerTest;
import org.gwtproject.place.shared.PlaceHistoryHandlerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  PlaceChangeRequestEventTest.class,
  PlaceControllerTest.class,
  PlaceHistoryHandlerTest.class
})
public class PlaceSuite {}
