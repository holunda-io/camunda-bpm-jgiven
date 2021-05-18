package io.holunda.camunda.bpm.extension.jgiven.formatter

import com.tngtech.jgiven.annotation.Table
import com.tngtech.jgiven.config.FormatterConfiguration
import com.tngtech.jgiven.format.DefaultFormatter
import com.tngtech.jgiven.format.ObjectFormatter
import com.tngtech.jgiven.format.table.DefaultTableFormatter
import com.tngtech.jgiven.format.table.RowFormatter
import com.tngtech.jgiven.format.table.RowFormatterFactory
import com.tngtech.jgiven.format.table.TableFormatterFactory
import com.tngtech.jgiven.report.model.DataTable
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue


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

/**
 * Formatter for single row, which is <code>Map.Entry</code>
 * @param tableAnnotation for column setup.
 */
class EntryRowFormatter(private val tableAnnotation: Table) : RowFormatter() {

  /**
   * Object formatter.
   */
  private val formatter: ObjectFormatter<Any> = DefaultFormatter.INSTANCE

  override fun header(): MutableList<String> {
    return tableAnnotation.columnTitles.toMutableList()
  }

  override fun formatRow(row: Any?): MutableList<String> {
    val columns = mutableListOf<String>()
    if (row is Map.Entry<*, *>) {
      columns.add(formatter.format(row.key))
      val value = row.value
      if (value is TypedValue) {
        columns.add(formatter.format(value.value))
        columns.add(formatter.format(value.isTransient))
      } else {
        columns.add(formatter.format(value))
        columns.add("n/a")
      }
    } else {
      columns.add(formatter.format(row))
    }
    return columns
  }

  /**
   * Factory.
   */
  class Factory : RowFormatterFactory {
    override fun create(
      parameterType: Class<*>,
      parameterName: String,
      tableAnnotation: Table,
      annotations: Array<out Annotation>,
      configuration: FormatterConfiguration,
      objectFormatter: ObjectFormatter<*>
    ): RowFormatter {
      return EntryRowFormatter(tableAnnotation)
    }
  }
}
