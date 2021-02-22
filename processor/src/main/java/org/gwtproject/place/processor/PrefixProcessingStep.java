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
import com.google.auto.common.BasicAnnotationProcessor.Step;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSetMultimap;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.gwtproject.place.shared.PlaceTokenizer;
import org.gwtproject.place.shared.Prefix;

public class PrefixProcessingStep implements Step {
  private final Messager messager;
  private final Types types;

  private final TypeElement placeTokenizerTypeElement;

  public PrefixProcessingStep(Messager messager, Types types, Elements elements) {
    this.messager = messager;
    this.types = types;

    this.placeTokenizerTypeElement =
        elements.getTypeElement(PlaceTokenizer.class.getCanonicalName());
  }

  @Override
  public Set<String> annotations() {
    return Collections.singleton(Prefix.class.getCanonicalName());
  }

  @Override
  public Set<Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
    Set<Element> elements = elementsByAnnotation.get(Prefix.class.getCanonicalName());
    for (final Element element : elements) {
      switch (element.getKind()) {
        case CLASS:
        case INTERFACE:
          if (!types.isSubtype(
              element.asType(), types.getDeclaredType(placeTokenizerTypeElement))) {
            messager.printMessage(
                Diagnostic.Kind.WARNING,
                String.format(
                    "%s applied on a type that doesn't implement %s.",
                    Prefix.class.getCanonicalName(), placeTokenizerTypeElement.getQualifiedName()),
                element,
                MoreElements.getAnnotationMirror(element, Prefix.class).get());
            continue;
          }
          break;
        case METHOD:
          if (!types.isSubtype(
              ((ExecutableElement) element).getReturnType(),
              types.getDeclaredType(placeTokenizerTypeElement))) {
            messager.printMessage(
                Diagnostic.Kind.WARNING,
                String.format(
                    "%s applied on a method whose return type doesn't implement %s.",
                    Prefix.class.getCanonicalName(), placeTokenizerTypeElement.getQualifiedName()),
                element,
                MoreElements.getAnnotationMirror(element, Prefix.class).get());
            continue;
          }
          break;
        default:
          // Error should be reported by the compiler when checking annotation's @Target.
          continue;
      }
      // We should only be here if there was no previous warning (to avoid errors on "unused"
      // annotations)
      AnnotationMirror annotation = MoreElements.getAnnotationMirror(element, Prefix.class).get();
      AnnotationValue annotationValue = AnnotationMirrors.getAnnotationValue(annotation, "value");
      if (annotationValue.getValue() instanceof String) {
        if (((String) annotationValue.getValue()).contains(":")) {
          messager.printMessage(
              Diagnostic.Kind.ERROR,
              String.format(
                  "Found place prefix \"%s\" containing separator char \":\", on %s",
                  annotationValue.getValue(), element),
              element,
              annotation,
              annotationValue);
        }
      } // else error should be reported by the compiler
    }
    return Collections.emptySet();
  }
}
