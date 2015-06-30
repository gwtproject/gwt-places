package net.ltgt.gwt.place.processor;

import static com.google.common.truth.Truth.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.PlaceHistoryMapperWithFactory;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.gwt.place.shared.WithTokenizers;
import com.google.testing.compile.CompilationRule;

import net.ltgt.gwt.place.testplacemappers.NoFactory;
import net.ltgt.gwt.place.testplacemappers.WithFactory;
import net.ltgt.gwt.place.testplaces.Place1;
import net.ltgt.gwt.place.testplaces.Place2;
import net.ltgt.gwt.place.testplaces.Place3;
import net.ltgt.gwt.place.testplaces.Place4;
import net.ltgt.gwt.place.testplaces.Place6;
import net.ltgt.gwt.place.testplaces.Tokenizer2;
import net.ltgt.gwt.place.testplaces.Tokenizer3;
import net.ltgt.gwt.place.testplaces.Tokenizer4;
import net.ltgt.gwt.place.testplaces.TokenizerFactory;

public class PlaceHistoryGeneratorContextTest {
  @Rule public CompilationRule compilationRule = new CompilationRule();
  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock Messager messager;

  @WithTokenizers({}) static abstract class MyAbstractPlaceHistoryMapper implements PlaceHistoryMapper {}
  @Test public void testCreateNotAnInterface() {
    PlaceHistoryGeneratorContext context = PlaceHistoryGeneratorContext.create(
        messager, compilationRule.getTypes(), compilationRule.getElements(),
        compilationRule.getElements()
            .getTypeElement(MyAbstractPlaceHistoryMapper.class.getCanonicalName()));

    assertThat(context).isNull();
  }

  @Test public void testCreateNoFactory() {
    doTestCreate(NoFactory.class, null);
  }

  @Test public void testCreateWithFactory() {
    doTestCreate(WithFactory.class, TokenizerFactory.class);
  }

  @Test public void testNoFactory() {

    TypeElement place1 = compilationRule.getElements().getTypeElement(
        Place1.class.getCanonicalName());
    TypeElement place2 = compilationRule.getElements().getTypeElement(
        Place2.class.getCanonicalName());
    TypeElement place3 = compilationRule.getElements().getTypeElement(
        Place3.class.getCanonicalName());
    TypeElement place4 = compilationRule.getElements().getTypeElement(
        Place4.class.getCanonicalName());
    TypeElement place6 = compilationRule.getElements().getTypeElement(
        Place6.class.getCanonicalName());

    PlaceHistoryGeneratorContext context = createContext(NoFactory.class.getCanonicalName(), null);

    // Found all place prefixes?
    assertThat(context.getPrefixes()).containsExactly(
        "", Place1.Tokenizer.PREFIX, "Place2", "Place3", "Place4");

    // Found all place types and correctly sorted them?
    assertThat(context.getPlaceTypes()).containsExactly(place3, place4, place1, place2, place6).inOrder();

    // correctly maps place types to their prefixes?
    assertThat(context.getPrefix(place1)).isEqualTo(Place1.Tokenizer.PREFIX);
    assertThat(context.getPrefix(place2)).isEqualTo("Place2");
    assertThat(context.getPrefix(place3)).isEqualTo("Place3");
    assertThat(context.getPrefix(place4)).isEqualTo("Place4");
    assertThat(context.getPrefix(place6)).isEqualTo("");

    // there obviously shouldn't be factory methods
    assertThat(context.getTokenizerGetter(Place1.Tokenizer.PREFIX)).isNull();
    assertThat(context.getTokenizerGetter("Place2")).isNull();
    assertThat(context.getTokenizerGetter("Place3")).isNull();
    assertThat(context.getTokenizerGetter("Place4")).isNull();
    assertThat(context.getTokenizerGetter("")).isNull();

    // correctly maps prefixes to their tokenizer type?
    assertThat(context.getTokenizerType(Place1.Tokenizer.PREFIX)).isEqualTo(
        compilationRule.getElements().getTypeElement(Place1.Tokenizer.class.getCanonicalName()));
    assertThat(context.getTokenizerType("Place2")).isEqualTo(
        compilationRule.getElements().getTypeElement(Tokenizer2.class.getCanonicalName()));
    assertThat(context.getTokenizerType("Place3")).isEqualTo(
        compilationRule.getElements().getTypeElement(Tokenizer3.class.getCanonicalName()));
    assertThat(context.getTokenizerType("Place4")).isEqualTo(
        compilationRule.getElements().getTypeElement(Tokenizer4.class.getCanonicalName()));
    assertThat(context.getTokenizerType("")).isEqualTo(
        compilationRule.getElements().getTypeElement(Place6.Tokenizer.class.getCanonicalName()));
  }

  @Test public void testWithFactory() {

    TypeElement place1 = compilationRule.getElements().getTypeElement(
        Place1.class.getCanonicalName());
    TypeElement place2 = compilationRule.getElements().getTypeElement(
        Place2.class.getCanonicalName());
    TypeElement place3 = compilationRule.getElements().getTypeElement(
        Place3.class.getCanonicalName());
    TypeElement place4 = compilationRule.getElements().getTypeElement(
        Place4.class.getCanonicalName());
    TypeElement place6 = compilationRule.getElements().getTypeElement(
        Place6.class.getCanonicalName());
    TypeElement factory = compilationRule.getElements().getTypeElement(
        TokenizerFactory.class.getCanonicalName());

    PlaceHistoryGeneratorContext context = createContext(
        WithFactory.class.getCanonicalName(),
        TokenizerFactory.class.getCanonicalName());

    // Found all place prefixes?
    assertThat(context.getPrefixes()).containsExactly("", Place1.Tokenizer.PREFIX, TokenizerFactory.PLACE2_PREFIX, "Place3", "Place4");

    // Found all place types and correctly sorted them?
    assertThat(context.getPlaceTypes()).containsExactly(place3, place4, place1, place2,
        place6).inOrder();

    // correctly maps place types to their prefixes?
    assertThat(context.getPrefix(place1)).isEqualTo(Place1.Tokenizer.PREFIX);
    assertThat(context.getPrefix(place2)).isEqualTo(TokenizerFactory.PLACE2_PREFIX);
    assertThat(context.getPrefix(place3)).isEqualTo("Place3");
    assertThat(context.getPrefix(place4)).isEqualTo("Place4");
    assertThat(context.getPrefix(place6)).isEqualTo("");

    // correctly map prefixes to their factory method (or null)?
    assertThat(context.getTokenizerGetter(Place1.Tokenizer.PREFIX)).isEqualTo(ElementFilter
        .methodsIn(factory.getEnclosedElements()).stream()
        .filter(method -> method.getSimpleName().contentEquals("getTokenizer1") && method
            .getParameters().isEmpty())
        .findFirst().get());
    assertThat(context.getTokenizerGetter(TokenizerFactory.PLACE2_PREFIX)).isEqualTo(ElementFilter
        .methodsIn(factory.getEnclosedElements()).stream()
        .filter(method -> method.getSimpleName().contentEquals("getTokenizer2") && method
            .getParameters().isEmpty())
        .findFirst().get());
    assertThat(context.getTokenizerGetter("Place3")).isEqualTo(ElementFilter
        .methodsIn(factory.getEnclosedElements()).stream()
        .filter(method -> method.getSimpleName().contentEquals("getTokenizer3") && method
            .getParameters().isEmpty())
        .findFirst().get());
    assertThat(context.getTokenizerGetter("Place4")).isNull();
    assertThat(context.getTokenizerGetter("")).isNull();

    // correctly maps prefixes to their tokenizer type (or null)?
    assertThat(context.getTokenizerType(Place1.Tokenizer.PREFIX)).isNull();
    assertThat(context.getTokenizerType(TokenizerFactory.PLACE2_PREFIX)).isNull();
    assertThat(context.getTokenizerType("Place3")).isNull();
    assertThat(context.getTokenizerType("Place4")).isEqualTo(
        compilationRule.getElements().getTypeElement(Tokenizer4.class.getCanonicalName()));
    assertThat(context.getTokenizerType("")).isEqualTo(
        compilationRule.getElements().getTypeElement(Place6.Tokenizer.class.getCanonicalName()));
  }

  @WithTokenizers(Place1.Tokenizer.class)
  interface MapperWithDuplicatePrefix extends PlaceHistoryMapperWithFactory<MapperWithDuplicatePrefix.Factory> {
    interface Factory {
      @Prefix(Place1.Tokenizer.PREFIX) Tokenizer2 tokenizer2();
    }
  }
  @Test public void testDuplicatePrefix() {
    PlaceHistoryGeneratorContext context = createContext(
        MapperWithDuplicatePrefix.class.getCanonicalName(),
        MapperWithDuplicatePrefix.Factory.class.getCanonicalName());

    context.ensureInitialized();

    verify(messager).printMessage(eq(Diagnostic.Kind.ERROR), eq(String.format(
            "Found duplicate place prefix \"%s\" on %s, already seen on %s",
            Place1.Tokenizer.PREFIX, Place1.Tokenizer.class.getCanonicalName(),
            MapperWithDuplicatePrefix.Factory.class.getCanonicalName() + "#tokenizer2()")),
        any(Element.class));
  }

  @WithTokenizers(Place1.Tokenizer.class)
  interface MapperWithDuplicatePlaceType extends PlaceHistoryMapperWithFactory<MapperWithDuplicatePlaceType.Factory> {
    interface Factory {
      @Prefix("anotherPrefix") PlaceTokenizer<Place1> bar();
    }
  }
  @Test public void testDuplicatePlaceType() {
    PlaceHistoryGeneratorContext context = createContext(
        MapperWithDuplicatePlaceType.class.getCanonicalName(),
        MapperWithDuplicatePlaceType.Factory.class.getCanonicalName());

    context.ensureInitialized();

    verify(messager).printMessage(eq(Diagnostic.Kind.ERROR), eq(String.format(
            "Found duplicate tokenizer's place type \"%s\" on %s, already seen on %s",
            Place1.class.getCanonicalName(), Place1.Tokenizer.class.getCanonicalName(),
            MapperWithDuplicatePlaceType.Factory.class.getCanonicalName() + "#bar()")),
        any(Element.class));
  }

  @WithTokenizers({}) interface MapperWithPrefixContainingColon extends PlaceHistoryMapperWithFactory<MapperWithPrefixContainingColon.Factory> {
    interface Factory {
      @Prefix("foo:bar") PlaceTokenizer<Place> foo_bar();
    }
  }
  @Test public void testPrefixContainingColon() {
    PlaceHistoryGeneratorContext context = createContext(
        MapperWithPrefixContainingColon.class.getCanonicalName(),
        MapperWithPrefixContainingColon.Factory.class.getCanonicalName());

    context.ensureInitialized();

    verify(messager).printMessage(eq(Diagnostic.Kind.ERROR), eq(
            "Found place prefix \"foo:bar\" containing separator char \":\", on "
            + MapperWithPrefixContainingColon.Factory.class.getCanonicalName() + "#foo_bar()"),
        any(Element.class));
  }

  private void doTestCreate(Class<? extends PlaceHistoryMapper> intf, Class<?> factory) {

    PlaceHistoryGeneratorContext context = PlaceHistoryGeneratorContext.create(
        messager, compilationRule.getTypes(), compilationRule.getElements(),
        compilationRule.getElements().getTypeElement(intf.getCanonicalName()));

    assertThat(context.stringType)
        .isEqualTo(compilationRule.getElements().getTypeElement(String.class.getCanonicalName()));
    assertThat(context.placeTokenizerType).isEqualTo(
        compilationRule.getElements().getTypeElement(PlaceTokenizer.class.getCanonicalName()));
    assertThat(context.messager).isSameAs(messager);
    assertThat(context.types).isSameAs(compilationRule.getTypes());
    assertThat(context.elements).isSameAs(compilationRule.getElements());

    assertThat(context.interfaceType).isEqualTo(
        compilationRule.getElements().getTypeElement(intf.getCanonicalName()));

    if (factory == null) {
      assertThat(context.factoryType).isNull();
    } else {
      // XXX: the use of asElement() here makes it less accurate, but OK given our inputs.
      assertThat(context.factoryType.asElement()).isEqualTo(compilationRule.getElements().getTypeElement(factory.getCanonicalName()));
    }

    assertThat(context.implName).isEqualTo(intf.getSimpleName() + "Impl");
    assertThat(context.packageName).isEqualTo(intf.getPackage().getName());

    verifyZeroInteractions(messager);
  }

  private PlaceHistoryGeneratorContext createContext(String interfaceName, String factoryName) {
    return new PlaceHistoryGeneratorContext(
        messager, //
        compilationRule.getTypes(), //
        compilationRule.getElements(), //
        compilationRule.getElements().getTypeElement(interfaceName), //
        factoryName == null ? null : (DeclaredType) compilationRule.getElements().getTypeElement(factoryName).asType(), //
        compilationRule.getElements().getTypeElement(String.class.getName()), //
        compilationRule.getElements().getTypeElement(PlaceTokenizer.class.getName()), //
        null, null);
  }
}
