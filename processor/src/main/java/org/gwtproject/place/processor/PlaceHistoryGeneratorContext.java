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

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.gwtproject.place.shared.PlaceHistoryMapper;
import org.gwtproject.place.shared.PlaceTokenizer;
import org.gwtproject.place.shared.Prefix;
import org.gwtproject.place.shared.WithFactory;
import org.gwtproject.place.shared.WithTokenizers;

class PlaceHistoryGeneratorContext {
  final TypeElement stringType;
  final TypeElement placeTokenizerType;
  final Messager messager;
  final Types types;
  final Elements elements;
  final TypeElement interfaceType;
  final DeclaredType factoryType;
  final String implName;
  final String packageName;

  private HashMap<String, Element> tokenizers;
  private TreeMap<TypeElement, String> placeTypes =
      new TreeMap<>(new MostToLeastDerivedPlaceTypeComparator());

  static PlaceHistoryGeneratorContext create(
      Messager messager, Types types, Elements elements, Element element) {
    TypeElement stringType = requireType(elements, String.class);
    TypeElement placeTokenizerType = requireType(elements, PlaceTokenizer.class);
    TypeElement placeHistoryMapperType = requireType(elements, PlaceHistoryMapper.class);

    if (element.getKind() != ElementKind.INTERFACE) {
      messager.printMessage(
          Diagnostic.Kind.WARNING,
          String.format(
              "%s applied on a type that's not an interface; ignoring.",
              WithTokenizers.class.getCanonicalName()));
      return null;
    }
    if (!types.isSubtype(element.asType(), placeHistoryMapperType.asType())) {
      messager.printMessage(
          Diagnostic.Kind.WARNING,
          String.format(
              "%s applied on a type that doesn't implement %s; ignoring.",
              WithTokenizers.class.getCanonicalName(),
              PlaceHistoryMapper.class.getCanonicalName()));
      return null;
    }

    TypeElement interfaceType = (TypeElement) element;
    DeclaredType factoryType = findFactoryType(interfaceType);
    ClassName interfaceName = ClassName.get(interfaceType);
    String implName =
        ClassName.get(interfaceType).simpleNames().stream().collect(Collectors.joining("_"))
            + "Impl";
    return new PlaceHistoryGeneratorContext(
        messager,
        types,
        elements,
        interfaceType,
        factoryType,
        stringType,
        placeTokenizerType,
        interfaceName.packageName(),
        implName);
  }

  private static DeclaredType findFactoryType(TypeElement interfaceType) {
    return MoreElements.getAnnotationMirror(interfaceType, WithFactory.class)
        .toJavaUtil()
        .map(
            annotationMirror ->
                (DeclaredType)
                    AnnotationMirrors.getAnnotationValue(annotationMirror, "value").getValue())
        .orElse(null);
  }

  private static TypeElement requireType(Elements elements, Class<?> clazz) {
    TypeElement type = elements.getTypeElement(clazz.getCanonicalName());
    if (type == null) {
      throw new AssertionError();
    }
    return type;
  }

  PlaceHistoryGeneratorContext(
      Messager messager,
      Types types,
      Elements elements,
      TypeElement interfaceType,
      DeclaredType factoryType,
      TypeElement stringType,
      TypeElement placeTokenizerType,
      String packageName,
      String implName) {
    this.messager = messager;
    this.types = types;
    this.elements = elements;
    this.interfaceType = interfaceType;
    this.factoryType = factoryType;
    this.stringType = stringType;
    this.placeTokenizerType = placeTokenizerType;
    this.packageName = packageName;
    this.implName = implName;
  }

  public Set<TypeElement> getPlaceTypes() {
    this.ensureInitialized();
    return this.placeTypes.keySet();
  }

  public String getPrefix(TypeElement placeType) {
    this.ensureInitialized();
    return this.placeTypes.get(placeType);
  }

  public Set<String> getPrefixes() {
    this.ensureInitialized();
    return this.tokenizers.keySet();
  }

  public ExecutableElement getTokenizerGetter(String prefix) {
    this.ensureInitialized();
    Element tokenizerGetter = this.tokenizers.get(prefix);
    return tokenizerGetter.getKind() == ElementKind.METHOD
        ? (ExecutableElement) tokenizerGetter
        : null;
  }

  public TypeElement getTokenizerType(String prefix) {
    this.ensureInitialized();
    Element tokenizerType = this.tokenizers.get(prefix);
    return tokenizerType.getKind() == ElementKind.CLASS ? (TypeElement) tokenizerType : null;
  }

  void ensureInitialized() {
    if (this.tokenizers == null) {
      assert this.placeTypes.isEmpty();

      this.tokenizers = new HashMap<>();
      this.initTokenizerGetters();
      this.initTokenizersWithoutGetters();
    }
  }

  private void addPlaceTokenizer(
      Element tokenizerClassOrGetter, String prefix, DeclaredType tokenizerType) {
    if (prefix.contains(":")) {
      this.messager.printMessage(
          Diagnostic.Kind.ERROR,
          String.format(
              "Found place prefix \"%s\" containing separator char \":\", on %s",
              prefix, this.getLogMessage(tokenizerClassOrGetter)),
          tokenizerClassOrGetter);
    } else if (this.tokenizers.containsKey(prefix)) {
      this.messager.printMessage(
          Diagnostic.Kind.ERROR,
          String.format(
              "Found duplicate place prefix \"%s\" on %s, already seen on %s",
              prefix,
              this.getLogMessage(tokenizerClassOrGetter),
              this.getLogMessage(this.tokenizers.get(prefix))),
          tokenizerClassOrGetter);
    } else {
      TypeElement placeType = this.getPlaceTypeForTokenizerType(tokenizerType);
      if (placeType == null) {
        return; // there was an error
      }
      if (this.placeTypes.containsKey(placeType)) {
        this.messager.printMessage(
            Diagnostic.Kind.ERROR,
            String.format(
                "Found duplicate tokenizer\'s place type \"%s\" on %s, already seen on %s",
                placeType.getQualifiedName(),
                this.getLogMessage(tokenizerClassOrGetter),
                this.getLogMessage(this.tokenizers.get(this.placeTypes.get(placeType)))),
            tokenizerClassOrGetter);
      } else {
        this.tokenizers.put(prefix, tokenizerClassOrGetter);
        this.placeTypes.put(placeType, prefix);
      }
    }
  }

  private String getLogMessage(Element methodOrClass) {
    switch (methodOrClass.getKind()) {
      case METHOD:
        return ((TypeElement) methodOrClass.getEnclosingElement()).getQualifiedName()
            + "#"
            + methodOrClass.getSimpleName()
            + "()";
      case CLASS:
        return ((TypeElement) methodOrClass).getQualifiedName().toString();
      default:
        throw new AssertionError();
    }
  }

  private TypeElement getPlaceTypeForTokenizerType(DeclaredType tokenizerType) {
    TypeMirror placeType =
        types.asMemberOf(tokenizerType, this.placeTokenizerType.getTypeParameters().get(0));
    if (placeType.getKind() != TypeKind.DECLARED) {
      this.messager.printMessage(
          Diagnostic.Kind.ERROR,
          "Found no Place type for "
              + ((TypeElement) types.asElement(tokenizerType)).getQualifiedName());
      return null;
    } else {
      return (TypeElement) types.asElement(placeType);
    }
  }

  private String getPrefixForTokenizerGetter(ExecutableElement method, ExecutableType methodType) {
    Prefix annotation = method.getAnnotation(Prefix.class);
    if (annotation != null) {
      return annotation.value();
    } else {
      return getPrefixForTokenizerType(MoreTypes.asDeclared(methodType.getReturnType()));
    }
  }

  private String getPrefixForTokenizerType(DeclaredType returnType) {
    Prefix annotation = returnType.asElement().getAnnotation(Prefix.class);
    if (annotation != null) {
      return annotation.value();
    }
    TypeElement placeType = getPlaceTypeForTokenizerType(returnType);
    if (placeType == null) {
      return null; // there was an error
    }
    return placeType.getSimpleName().toString();
  }

  @SuppressWarnings("unchecked")
  private Set<TypeElement> getWithTokenizerEntries() {
    return MoreElements.getAnnotationMirror(interfaceType, WithTokenizers.class)
        .toJavaUtil()
        .map(
            annotationMirror ->
                ((List<AnnotationValue>)
                        AnnotationMirrors.getAnnotationValue(annotationMirror, "value").getValue())
                    .stream()
                        .map(
                            annotationValue -> {
                              DeclaredType tokenizerType =
                                  (DeclaredType) annotationValue.getValue();
                              return (TypeElement) tokenizerType.asElement();
                            })
                        .collect(Collectors.toSet()))
        .orElse(Collections.emptySet());
  }

  private void initTokenizerGetters() {
    if (this.factoryType != null) {
      for (ExecutableElement method :
          ElementFilter.methodsIn(elements.getAllMembers((TypeElement) factoryType.asElement()))) {
        if (!method.getParameters().isEmpty()) {
          continue;
        }
        if (!method.getModifiers().contains(Modifier.PUBLIC)) {
          // TODO: support non-public but callable/visible methods (package-private or protected,
          // when in same package)
          continue;
        }
        ExecutableType methodType = (ExecutableType) types.asMemberOf(factoryType, method);
        TypeMirror returnType = methodType.getReturnType();
        if (!types.isSubtype(returnType, types.getDeclaredType(placeTokenizerType))) {
          continue;
        }
        addPlaceTokenizer(
            method,
            getPrefixForTokenizerGetter(method, methodType),
            MoreTypes.asDeclared(returnType));
      }
    }
  }

  private void initTokenizersWithoutGetters() {
    for (TypeElement tokenizerType : this.getWithTokenizerEntries()) {
      DeclaredType tokenizerDeclaredType = (DeclaredType) tokenizerType.asType();
      this.addPlaceTokenizer(
          tokenizerType,
          this.getPrefixForTokenizerType(tokenizerDeclaredType),
          tokenizerDeclaredType);
    }
  }
}
