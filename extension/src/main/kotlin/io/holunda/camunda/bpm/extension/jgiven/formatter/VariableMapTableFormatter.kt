package io.holunda.camunda.bpm.extension.jgiven.formatter

import com.tngtech.jgiven.annotation.Table
import com.tngtech.jgiven.config.FormatterConfiguration
import com.tngtech.jgiven.format.ObjectFormatter
import com.tngtech.jgiven.format.table.DefaultTableFormatter
import com.tngtech.jgiven.format.table.TableFormatterFactory
import com.tngtech.jgiven.report.model.DataTable
import org.camunda.bpm.engine.variable.VariableMap

/**
 * Formatter for process variables.
 * @param formatterConfiguration config.
 * @param objectFormatter formatter for single objects.
 */
class VariableMapTableFormatter(formatterConfiguration: FormatterConfiguration, objectFormatter: ObjectFormatter<*>) :
  DefaultTableFormatter(formatterConfiguration, objectFormatter) {

  // FIXME: if we use meta-annotation, specify it here.
  override fun format(argument: Any, tableAnnotation: Table, parameterName: String, vararg allAnnotations: Annotation): DataTable {
    return if (argument is VariableMap) {
      super.format(argument.entries, tableAnnotation, parameterName, *allAnnotations)
    } else {
      super.format(argument, tableAnnotation, parameterName, *allAnnotations)
    }
  }

  /**
   * Factory.
   */
  class Factory : TableFormatterFactory {
    override fun create(formatterConfiguration: FormatterConfiguration, objectFormatter: ObjectFormatter<*>) = VariableMapTableFormatter(formatterConfiguration, objectFormatter)
  }
}