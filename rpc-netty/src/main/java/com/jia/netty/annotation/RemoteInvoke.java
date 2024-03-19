package com.jia.netty.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RemoteInvoke {

}

/*
 * @Component: 表示被标记的类是一个 Spring 管理的组件。被标记的类会被 Spring 自动扫描并且实例化为 bean，并且可以通过依赖注入来使用。
 * @Documented：在生成 Java 文档时，被标记了的信息会包含在文档中。
 * @Target({ElementType.TYPE, ElementType.METHOD})：被定义的注解可以应用在哪些地方。
 * 									在类（ElementType.TYPE）和方法（ElementType.METHOD）上
 * 				ElementType.FIELD 表示该注解可以应用在类的字段（即成员变量）上
 */
