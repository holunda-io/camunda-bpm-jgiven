package io.holunda.camunda.bpm.extension.jgiven.formatter

import com.tngtech.jgiven.annotation.Table


/**
 * Marker to render process variables.
 */
@MustBeDocumented
@Table(
  formatter = VariableMapTableFormatter.Factory::class,
  rowFormatter = EntryRowFormatter.Factory::class,
  columnTitles = ["Name", "Value", "Transient"],
  header = Table.HeaderType.HORIZONTAL
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FIELD)
annotation class VariableMapFormat

