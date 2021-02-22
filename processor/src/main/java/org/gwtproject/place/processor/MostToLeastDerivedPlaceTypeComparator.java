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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

class MostToLeastDerivedPlaceTypeComparator implements Comparator<TypeElement> {
  private final Comparator<TypeElement> realComparator =
      Comparator.comparingInt(this::getHierarchyDepth)
          .reversed()
          .thenComparing(TypeElement::getQualifiedName, Comparator.comparing(Name::toString));

  private final Map<TypeElement, Integer> hierarchyDepths = new LinkedHashMap<>();

  @Override
  public int compare(TypeElement o1, TypeElement o2) {
    return realComparator.compare(o1, o2);
  }

  private int getHierarchyDepth(TypeElement typeElement) {
    Integer depth = hierarchyDepths.get(typeElement);
    if (depth == null) {
      if (typeElement.getSuperclass().getKind() == TypeKind.NONE) {
        depth = 0;
        hierarchyDepths.put(typeElement, depth);
      } else {
        depth =
            getHierarchyDepth(
                    (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement())
                + 1;
        hierarchyDepths.put(typeElement, depth);
      }
    }
    return depth;
  }
}
