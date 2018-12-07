package io.holunda.camunda.bpm.extension.jgiven.formatter

import com.tngtech.jgiven.annotation.Format

/**
 * Annotation using the Vargarg Argument formatter.
 */
@MustBeDocumented
@Format(value = VarargsFormatter::class, args = ["\"%s\""])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FIELD)
annotation class QuotedVarargs
