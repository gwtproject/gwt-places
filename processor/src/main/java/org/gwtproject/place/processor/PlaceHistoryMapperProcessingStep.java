/*
 * Copyright 2017 The GWT Project Authors
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
package org.gwtproject.place.processor;

import com.google.auto.common.BasicAnnotationProcessor.Step;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.gwtproject.place.impl.AbstractPlaceHistoryMapper;
import org.gwtproject.place.shared.PlaceTokenizer;
import org.gwtproject.place.shared.WithFactory;
import org.gwtproject.place.shared.WithTokenizers;

class PlaceHistoryMapperProcessingStep implements Step {
  private static final ClassName PREFIX_AND_TOKEN_CLASS_NAME =
      ClassName.get(AbstractPlaceHistoryMapper.PrefixAndToken.class);
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

  PlaceHistoryMapperProcessingStep(Messager messager, Filer file, Types types, Elements elements) {
    this.messager = messager;
    this.filer = file;
    this.types = types;
    this.elements = elements;

    this.abstractPlaceHistoryMapperType =
        elements.getTypeElement(AbstractPlaceHistoryMapper.class.getCanonicalName());

    this.getPrefixAndTokenMethod =
        ElementFilter.methodsIn(abstractPlaceHistoryMapperType.getEnclosedElements()).stream()
            .filter(method -> method.getSimpleName().contentEquals("getPrefixAndToken"))
            .findFirst()
            .get();
    this.getPrefixAndTokenMethodParameter = getPrefixAndTokenMethod.getParameters().get(0);
    this.getTokenizerMethod =
        ElementFilter.methodsIn(abstractPlaceHistoryMapperType.getEnclosedElements()).stream()
            .filter(method -> method.getSimpleName().contentEquals("getTokenizer"))
            .findFirst()
            .get();
    this.getTokenizerMethodParameter = getTokenizerMethod.getParameters().get(0);
  }

  @Override
  public Set<String> annotations() {
    return ImmutableSet.of(
        WithTokenizers.class.getCanonicalName(), WithFactory.class.getCanonicalName());
  }

  @Override
  public Set<Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
    for (Element element :
        Sets.union(
            elementsByAnnotation.get(WithTokenizers.class.getCanonicalName()),
            elementsByAnnotation.get(WithFactory.class.getCanonicalName()))) {
      PlaceHistoryGeneratorContext context =
          PlaceHistoryGeneratorContext.create(messager, types, elements, element);
      if (context == null) {
        continue; // error message already emitted
      }
      try {
        generate(context);
      } catch (IOException ioe) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println(
            "Error generating source file for type " + context.interfaceType.getQualifiedName());
        ioe.printStackTrace(pw);
        pw.close();
        messager.printMessage(Diagnostic.Kind.ERROR, sw.toString());
      }
    }
    return Collections.emptySet();
  }

  private void generate(PlaceHistoryGeneratorContext context) throws IOException {
    TypeSpec.Builder builder =
        TypeSpec.classBuilder(context.implName)
            .addOriginatingElement(context.interfaceType)
            // TODO: add @Generated annotation
            .addModifiers(Modifier.PUBLIC)
            .superclass(ClassName.get(abstractPlaceHistoryMapperType))
            .addSuperinterface(ClassName.get(context.interfaceType));
    if (context.factoryType != null) {
      builder
          .addField(ClassName.get(context.factoryType), "factory", Modifier.PRIVATE, Modifier.FINAL)
          .addMethod(
              MethodSpec.constructorBuilder()
                  .addModifiers(Modifier.PUBLIC)
                  .addParameter(ClassName.get(context.factoryType), "factory")
                  .addStatement("this.factory = factory")
                  .build());
    }
    builder.addMethod(generateGetPrefixAndToken(context)).addMethod(generateGetTokenizer(context));
    JavaFile.builder(context.packageName, builder.build()).build().writeTo(filer);
  }

  private MethodSpec generateGetPrefixAndToken(PlaceHistoryGeneratorContext context) {
    MethodSpec.Builder builder = MethodSpec.overriding(getPrefixAndTokenMethod);
    for (TypeElement placeType : context.getPlaceTypes()) {
      String prefix = context.getPrefix(placeType);

      builder
          .beginControlFlow(
              "if ($N instanceof $T)", getPrefixAndTokenMethodParameter.getSimpleName(), placeType)
          .addStatement(
              "$1T place = ($1T) $2N", placeType, getPrefixAndTokenMethodParameter.getSimpleName());

      ExecutableElement getter = context.getTokenizerGetter(prefix);
      if (getter != null) {
        builder.addStatement(
            "return new $T($S, this.factory.$N().getToken(place))",
            PREFIX_AND_TOKEN_CLASS_NAME,
            prefix,
            getter.getSimpleName());
      } else {
        builder
            .addStatement(
                "$T t = new $T()",
                ParameterizedTypeName.get(PLACE_TOKENIZER_CLASS_NAME, ClassName.get(placeType)),
                context.getTokenizerType(prefix))
            .addStatement(
                "return new $T($S, t.getToken(place))", PREFIX_AND_TOKEN_CLASS_NAME, prefix);
      }
      builder.endControlFlow();
    }
    builder.addStatement("return null");
    return builder.build();
  }

  private MethodSpec generateGetTokenizer(PlaceHistoryGeneratorContext context) {
    MethodSpec.Builder builder = MethodSpec.overriding(getTokenizerMethod);
    for (String prefix : context.getPrefixes()) {
      builder.beginControlFlow(
          "if ($S.equals($N))", prefix, getTokenizerMethodParameter.getSimpleName());

      ExecutableElement getter = context.getTokenizerGetter(prefix);
      if (getter != null) {
        builder.addStatement("return this.factory.$N()", getter.getSimpleName());
      } else {
        builder.addStatement("return new $T()", context.getTokenizerType(prefix));
      }
      builder.endControlFlow();
    }
    builder.addStatement("return null");
    return builder.build();
  }
}
