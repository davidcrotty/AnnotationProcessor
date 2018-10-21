package net.davidcrotty.annotationprocessor.test;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import net.davidcrotty.viewbinder.NewIntent;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class BindViewProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementsUtil;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnv.getMessager();
        elementsUtil = processingEnv.getElementUtils();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        HashMap<String, String> activitesWithPackage = new HashMap();


        if(true) {
            throw new RuntimeException("sssdd");
        }

        //grab the classes we wish to inject into
        for(Element element : roundEnvironment.getElementsAnnotatedWith(NewIntent.class)) {

            //eliminate edge cases
            if(element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can only be applied to class");
                return false;
            }

            //can safely cast as a typeElement (fancy name for class)
            //see table: https://medium.com/@iammert/annotation-processing-dont-repeat-yourself-generate-your-code-8425e60c6657
            TypeElement typeElement = (TypeElement) element;
            activitesWithPackage.put(
                    typeElement.getSimpleName().toString(),
                    elementsUtil.getPackageOf(typeElement).getQualifiedName().toString()
            );
        }

        // Build a class
        TypeSpec.Builder navigatorClass = TypeSpec
                .classBuilder("Navigator")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        //get our intent class
        ClassName intent = ClassName.get("android.content.Intent", "Intent");
        ClassName context = ClassName.get("android.content.Context", "Context");

        for (Map.Entry<String, String> element : activitesWithPackage.entrySet()) {
            //create our specification
            String activityName = element.getKey();
            String packageName = element.getValue();
            ClassName activity = ClassName.get(packageName, activityName);
            MethodSpec launchMethod = MethodSpec.methodBuilder("intentFor" + activityName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(intent)
                    .addParameter(context, "context")
                    .addStatement("return new $T($L, $L)", intent, "context", activity + ".class")
                    .build();
            navigatorClass.addMethod(launchMethod);
        }

        //create the file
        try {
            JavaFile.builder("net.davidcrotty.annotationprocessor", navigatorClass.build())
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(
                NewIntent.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
