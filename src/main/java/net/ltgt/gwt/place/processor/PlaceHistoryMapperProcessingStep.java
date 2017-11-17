package net.ltgt.gwt.place.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.gwt.place.impl.AbstractPlaceHistoryMapper;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.WithTokenizers;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

class PlaceHistoryMapperProcessingStep implements ProcessingStep {
  private static final ClassName PREFIX_AND_TOKEN_CLASS_NAME = ClassName.get(
      AbstractPlaceHistoryMapper.PrefixAndToken.class);
  private static final ClassName PLACE_TOKENIZER_CLASS_NAME = ClassName.get(PlaceTokenizer.class);

  private final Messager messager;
  private final Filer filer;
  private final Types types;
  private final Elements elements;

  private final TypeElement abstractPlaceHistoryMapperType;
  private final ExecutableElement getPrefixAndTokenMethod;
  private final VariableElement getPrefixAndTokenMethodParameter;
  private final ExecutableElement getTokenizerMethod;
  private final VariableElement getTokenizerMethodParameter;

  PlaceHistoryMapperProcessingStep(Messager messager, Filer file, Types types,
      Elements elements) {
    this.messager = messager;
    this.filer = file;
    this.types = types;
    this.elements = elements;

    this.abstractPlaceHistoryMapperType = elements.getTypeElement(AbstractPlaceHistoryMapper.class.getCanonicalName());

    this.getPrefixAndTokenMethod = ElementFilter.methodsIn(abstractPlaceHistoryMapperType.getEnclosedElements())
        .stream().filter(method -> method.getSimpleName().contentEquals("getPrefixAndToken"))
        .findFirst().get();
    this.getPrefixAndTokenMethodParameter = getPrefixAndTokenMethod.getParameters().get(0);
    this.getTokenizerMethod = ElementFilter.methodsIn(abstractPlaceHistoryMapperType.getEnclosedElements())
        .stream().filter(method -> method.getSimpleName().contentEquals("getTokenizer"))
        .findFirst().get();
    this.getTokenizerMethodParameter = getTokenizerMethod.getParameters().get(0);
  }

  public Set<? extends Class<? extends Annotation>> annotations() {
    return Collections.singleton(WithTokenizers.class);
  }

  public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
    for (Element element : elementsByAnnotation.get(WithTokenizers.class)) {
      PlaceHistoryGeneratorContext context = PlaceHistoryGeneratorContext.create(messager, types, elements, element);
      if (context == null) {
        continue; // error message already emitted
      }
      try {
        generate(context);
      } catch (IOException ioe) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Error generating source file for type " + context.interfaceType.getQualifiedName());
        ioe.printStackTrace(pw);
        pw.close();
        messager.printMessage(Diagnostic.Kind.ERROR, sw.toString());
      }
    }
    return ImmutableSet.of();
  }

  private void generate(PlaceHistoryGeneratorContext context) throws IOException {
    JavaFile.builder(context.packageName,
        TypeSpec.classBuilder(context.implName)
            // XXX: add factoryType, place types and tokenizer types as originating elements?
            .addOriginatingElement(context.interfaceType)
            // TODO: add @Generated annotation
            .addModifiers(Modifier.PUBLIC)
            .superclass(ParameterizedTypeName.get(ClassName.get(abstractPlaceHistoryMapperType),
                context.factoryType == null ? TypeName.VOID.box() : TypeName.get(context.factoryType)))
            .addSuperinterface(ClassName.get(context.interfaceType))
            .addMethod(generateGetPrefixAndToken(context))
            .addMethod(generateGetTokenizer(context))
            .build())
        .build()
        .writeTo(filer);
  }

  private MethodSpec generateGetPrefixAndToken(PlaceHistoryGeneratorContext context) {
    MethodSpec.Builder builder = MethodSpec.overriding(getPrefixAndTokenMethod);
    for (TypeElement placeType : context.getPlaceTypes()) {
      String prefix = context.getPrefix(placeType);

      builder.addCode("if ($N instanceof $T) {\n$>$T place = ($T) $N;\n",
          getPrefixAndTokenMethodParameter.getSimpleName(), placeType,
          placeType, placeType, getPrefixAndTokenMethodParameter.getSimpleName());

      ExecutableElement getter = context.getTokenizerGetter(prefix);
      if (getter != null) {
        builder.addCode("return new $T($S, this.factory.$N().getToken(place));\n",
            PREFIX_AND_TOKEN_CLASS_NAME, prefix, getter.getSimpleName());
      } else {
        builder.addCode("$T t = new $T();\nreturn new $T($S, t.getToken(place));\n",
            ParameterizedTypeName.get(PLACE_TOKENIZER_CLASS_NAME, ClassName.get(placeType)),
            context.getTokenizerType(prefix), PREFIX_AND_TOKEN_CLASS_NAME, prefix);
      }
      builder.addCode("$<}\n");
    }
    builder.addCode("return null;");
    return builder.build();
  }

  private MethodSpec generateGetTokenizer(PlaceHistoryGeneratorContext context) {
    MethodSpec.Builder builder = MethodSpec.overriding(getTokenizerMethod);
    for (String prefix : context.getPrefixes()) {
      builder.addCode("if ($S.equals($N)) {\n$>", prefix, getTokenizerMethodParameter.getSimpleName());

      ExecutableElement getter = context.getTokenizerGetter(prefix);
      if (getter != null) {
        builder.addCode("return this.factory.$N();\n", getter.getSimpleName());
      } else {
        builder.addCode("return new $T();", context.getTokenizerType(prefix));
      }
      builder.addCode("$<}\n");
    }
    builder.addCode("return null;");
    return builder.build();
  }
}
