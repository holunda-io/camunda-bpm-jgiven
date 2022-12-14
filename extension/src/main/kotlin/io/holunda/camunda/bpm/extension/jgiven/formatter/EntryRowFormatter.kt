package io.holunda.camunda.bpm.extension.jgiven.formatter

import com.tngtech.jgiven.annotation.Table
import com.tngtech.jgiven.config.FormatterConfiguration
import com.tngtech.jgiven.format.DefaultFormatter
import com.tngtech.jgiven.format.ObjectFormatter
import com.tngtech.jgiven.format.table.RowFormatter
import com.tngtech.jgiven.format.table.RowFormatterFactory
import org.camunda.bpm.engine.variable.value.TypedValue

/**
 * Formatter for single row, which is <code>Map.Entry</code>
 * @param tableAnnotation for column setup.
 */
class EntryRowFormatter(private val tableAnnotation: Table) : RowFormatter() {

  /*
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