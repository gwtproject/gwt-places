# gwt-places
GWT PlaceHistoryMapper generator as an annotation processor

**This is an experiment** (for now), in preparation for the removal of GWT generators in GWT 3.0.

## How to use it?

1. Make sure your [`PlaceHistoryMapper`] subinterface is annotated with [`@WithTokenizers`],
   even if that means using an empty list of place tokenizers: `@WithTokenizers({})`
   (because all your place tokenizers are provided by [your factory][PlaceHistoryMapperWithFactory]).

2. Replace the call to `GWT.create()` for your interface with a direct instantiation of the generated class.
    
   The generated class has the same name as your interface with an `Impl` suffix;
   if your interface is nested inside a class or another interface,
   the generated class's name will be prefixed with all enclosing class/interface names joined with a `_`.

3. Add the JAR, along with `gwt-user.jar` to the _processor path_ or the classpath when compiling your interface.

   With Maven:  
   ```xml
   <dependency>
     <groupId>net.ltgt.gwt</groupId>
     <artifactId>gwt-places</artifactId>
     <version>[VERSION]</version>
     <optional>true</optional>
   </dependency>
   ```
   
   With Gradle, using the [`war` plugin]:  
   ```groovy
   dependencies {
     providedCompile 'net.ltgt.gwt:gwt-places:<VERSION>'
   }
   ```
   
   With Gradle, using the [`net.ltgt.apt` plugin]:  
   ```groovy
   dependencies {
     apt 'net.ltgt.gwt:gwt-places:<VERSION>'
   }
   ```
   
   With Gradle, using the [`gwt-gradle-plugin`]:  
   ```groovy
   dependencies {
     gwt 'net.ltgt.gwt:gwt-places:<VERSION>'
   }
   ```

 [`PlaceHistoryMapper`]: http://www.gwtproject.org/javadoc/latest/com/google/gwt/place/shared/PlaceHistoryMapper.html
 [`@WithTokenizers`]: http://www.gwtproject.org/javadoc/latest/com/google/gwt/place/shared/WithTokenizers.html
 [PlaceHistoryMapperWithFactory]: http://www.gwtproject.org/javadoc/latest/com/google/gwt/place/shared/PlaceHistoryMapperWithFactory.html
 [`war` plugin]: https://docs.gradle.org/current/userguide/war_plugin.html
 [`net.ltgt.apt` plugin]: https://plugins.gradle.org/plugin/net.ltgt.apt
 [`gwt-gradle-plugin`]: https://steffenschaefer.github.io/gwt-gradle-plugin/doc/latest/
