package com.kakin.router_compiler.processor;

import com.kakin.router_compiler.utils.Const;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * TestProcessor
 * Created by kakin on 2019/7/14.
 */

@AutoService(Processor.class)
/**
 * 指定使用的Java版本，替代{@link AbstractProcessor#getSupportedSourceVersion()}
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)

/**
 * 注冊給哪些注解,替代{@link AbstractProcessor#getSupportedAnnotationTypes()}
 */
@SupportedAnnotationTypes({Const.ANN_TYPE_ROUTE})
public class TestProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnv.getFiler();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "===========init=======");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "===========process=======");
        for (TypeElement element : set) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, element.getQualifiedName());
            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "args")
                    .addStatement("$T.out.println($S)", System.class, "Hello, kakin!")
                    .build();

            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(main)
                    .build();

            JavaFile javaFile = JavaFile.builder("com.kakin.helloworld", helloWorld)
                    .build();

            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMessager.printMessage(Diagnostic.Kind.WARNING, "===========process=======");
        return false;
    }
}
