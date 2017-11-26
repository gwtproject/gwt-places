package org.gwtproject.place.processor;

import static com.google.common.truth.Truth.*;

import com.google.testing.compile.CompilationRule;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.gwtproject.place.shared.Place;
import org.gwtproject.place.testplaces.Place1;
import org.gwtproject.place.testplaces.Place2;
import org.gwtproject.place.testplaces.Place3;
import org.gwtproject.place.testplaces.Place4;
import org.gwtproject.place.testplaces.Place5;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MostToLeastDerivedPlaceTypeComparatorTest {
  @Rule public CompilationRule compilationRule = new CompilationRule();

  private MostToLeastDerivedPlaceTypeComparator comparator;

  private TypeElement place;
  private TypeElement place1;
  private TypeElement place2;
  private TypeElement place3;
  private TypeElement place4;
  private TypeElement place5;

  @Before
  public void setUp() {
    comparator = new MostToLeastDerivedPlaceTypeComparator();

    place = compilationRule.getElements().getTypeElement(Place.class.getCanonicalName());
    assertThat(place).isNotNull();
    place1 = compilationRule.getElements().getTypeElement(Place1.class.getCanonicalName());
    assertThat(place1).isNotNull();
    place2 = compilationRule.getElements().getTypeElement(Place2.class.getCanonicalName());
    assertThat(place2).isNotNull();
    place3 = compilationRule.getElements().getTypeElement(Place3.class.getCanonicalName());
    assertThat(place3).isNotNull();
    place4 = compilationRule.getElements().getTypeElement(Place4.class.getCanonicalName());
    assertThat(place4).isNotNull();
    place5 = compilationRule.getElements().getTypeElement(Place5.class.getCanonicalName());
    assertThat(place5).isNotNull();
  }

  @Test
  public void testEquality() {
    for (TypeElement p : new TypeElement[] {place, place1, place2, place3, place4, place5}) {
      assertThat(comparator.compare(p, p)).isEqualTo(0);
    }
  }

  @Test
  public void testPlaceComparesGreaterThanAnyDerivedClass() {
    for (TypeElement p : new TypeElement[] {place1, place2, place3, place4, place5}) {
      assertThat(comparator.compare(place, p))
          .named(String.format("compare(place, %s)", p))
          .isGreaterThan(0);
      assertThat(comparator.compare(p, place))
          .named(String.format("compare(%s, place)", p))
          .isLessThan(0);
    }
  }

  @Test
  public void testPlaceInheritanceOrder() {
    // Place3 extends Place1
    assertThat(comparator.compare(place1, place3)).isGreaterThan(0);
    assertThat(comparator.compare(place3, place1)).isLessThan(0);

    // Place5 extends Place3 extends Place1
    assertThat(comparator.compare(place1, place5)).isGreaterThan(0);
    assertThat(comparator.compare(place5, place1)).isLessThan(0);

    // Place4 extends Place1
    assertThat(comparator.compare(place1, place4)).isGreaterThan(0);
    assertThat(comparator.compare(place4, place1)).isLessThan(0);

    // Place5 extends Place3
    assertThat(comparator.compare(place3, place5)).isGreaterThan(0);
    assertThat(comparator.compare(place5, place3)).isLessThan(0);
  }

  @Test
  public void testFallbackToClassName() {
    TypeElement[][] places = {
      {place3, place4}, // place3 and place4 both extend directly from place1
      {place1, place2}, // place1 and place2 both extend directly from place
    };
    for (TypeElement[] pair : places) {
      assertThat(comparator.compare(pair[0], pair[1]))
          .named(String.format("compare(%s, %s)", pair[0], pair[1]))
          .isLessThan(0);
      assertThat(comparator.compare(pair[1], pair[0]))
          .named(String.format("compare(%s, %s)", pair[1], pair[0]))
          .isGreaterThan(0);
    }
  }

  @Test
  public void testCollectionSort() {
    List<TypeElement> actual = Arrays.asList(place, place1, place3, place5);
    actual.sort(comparator);
    assertThat(actual).containsExactly(place5, place3, place1, place).inOrder();

    actual = Arrays.asList(place5, place3, place1, place);
    actual.sort(comparator);
    assertThat(actual).containsExactly(place5, place3, place1, place).inOrder();

    actual = Arrays.asList(place, place1, place2, place3, place4, place5);
    actual.sort(comparator);
    assertThat(actual).containsExactly(place5, place3, place4, place1, place2, place).inOrder();

    actual = Arrays.asList(place5, place4, place3, place2, place1, place);
    actual.sort(comparator);
    assertThat(actual).containsExactly(place5, place3, place4, place1, place2, place).inOrder();

    // This is equivalent to the test-case from issue 8036
    // https://code.google.com/p/google-web-toolkit/issues/detail?id=8036
    actual = Arrays.asList(place2, place1, place3);
    actual.sort(comparator);
    assertThat(actual).containsExactly(place3, place1, place2).inOrder();
  }
}
