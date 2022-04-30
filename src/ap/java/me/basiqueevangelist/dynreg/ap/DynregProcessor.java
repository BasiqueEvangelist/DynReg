package me.basiqueevangelist.dynreg.ap;

import com.squareup.javapoet.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({"me.basiqueevangelist.dynreg.ap.NamesFor"})
public class DynregProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, ClassBuilder> classes = new HashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(NamesFor.class)) {
            var enclosingType = (TypeElement) element.getEnclosingElement();

            var fieldType = (DeclaredType) element.asType();

            var elementType = fieldType.getTypeArguments().get(1);

            var namesFor = element.getAnnotation(NamesFor.class);
            TypeMirror targetType;

            try {
                namesFor.value();

                throw new IllegalStateException();
            } catch (MirroredTypeException mte) {
                targetType = mte.getTypeMirror();
            }

            TypeElement target = (TypeElement) ((DeclaredType) targetType).asElement();

            var builder = classes.computeIfAbsent(enclosingType.getQualifiedName().toString(), unused -> new ClassBuilder(enclosingType.getSimpleName().toString()));

            target
                .getEnclosedElements()
                .stream()
                .filter(x -> x.getKind() == ElementKind.FIELD)
                .map(x -> (VariableElement) x)
                .filter(x -> processingEnv.getTypeUtils().isSameType(x.asType(), elementType))
                .forEach(x -> {
                    builder.b.addStatement("$T.$L.put($S, $T.$L)", ClassName.get(enclosingType), element.getSimpleName(), x.getSimpleName(), ClassName.get(target), x.getSimpleName());
                });
        }

        for (ClassBuilder builder : classes.values()) {
            try {
                JavaFile.builder("me.basiqueevangelist.dynreg.generated",
                        builder.build())
                    .addFileComment("NOTE: This was generated by the Dynreg AP.")
                    .build()
                    .writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Couldn't create class " + builder.name + ": " + e);
            }
        }

        return false;
    }

    private static class ClassBuilder {
        private final MethodSpec.Builder b;
        private final String name;

        public ClassBuilder(String name) {
            this.name = name + "Setters";
            this.b = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(TypeName.VOID);
        }

        public TypeSpec build() {
            return TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addStatement("throw new UnsupportedOperationException()")
                    .build())
                .addMethod(b.build())
                .build();
        }
    }
}
