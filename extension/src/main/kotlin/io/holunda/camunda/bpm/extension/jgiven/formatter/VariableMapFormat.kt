package io.holunda.camunda.bpm.extension.jgiven.formatter

import com.tngtech.jgiven.annotation.Table
import com.tngtech.jgiven.config.FormatterConfiguration
import com.tngtech.jgiven.format.DefaultFormatter
import com.tngtech.jgiven.format.ObjectFormatter
import com.tngtech.jgiven.format.table.*
import com.tngtech.jgiven.report.model.DataTable
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue


@MustBeDocumented
@Table(
    formatter = VariableMapTableFormatter.Factory::class,
    rowFormatter = EntryRowFormatter.Factory::class,
    columnTitles = ["Name", "Value", "Transient"],
    header = Table.HeaderType.HORIZONTAL
)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FIELD)
annotation class VariableMapFormat

class VariableMapTableFormatter(formatterConfiguration: FormatterConfiguration, objectFormatter: ObjectFormatter<*>) : DefaultTableFormatter(formatterConfiguration, objectFormatter) {

    class Factory : TableFormatterFactory {
        override fun create(formatterConfiguration: FormatterConfiguration, objectFormatter: ObjectFormatter<*>) = VariableMapTableFormatter(formatterConfiguration, objectFormatter)
    }

    override fun format(tableArgument: Any, tableAnnotation: Table, parameterName: String, vararg allAnnotations: Annotation): DataTable {
        return if (tableArgument is VariableMap) {
            super.format(tableArgument.entries, tableAnnotation, parameterName, *allAnnotations)
        } else {
            super.format(tableArgument, tableAnnotation, parameterName, *allAnnotations)
        }
    }
}

class EntryRowFormatter(private val tableAnnotation: Table) : RowFormatter() {

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

    class Factory : RowFormatterFactory {
        override fun create(parameterType: Class<*>, parameterName: String, tableAnnotation: Table, annotations: Array<out Annotation>, configuration: FormatterConfiguration, objectFormatter: ObjectFormatter<*>): RowFormatter {
            return EntryRowFormatter(tableAnnotation)
        }
    }

}

class EntryRowFormatterFactory : RowFormatterFactory {
    override fun create(parameterType: Class<*>?, parameterName: String?, tableAnnotation: Table?, annotations: Array<out Annotation>?, configuration: FormatterConfiguration?, objectFormatter: ObjectFormatter<*>?): RowFormatter {
        return FieldBasedRowFormatter.Factory().create(parameterType, parameterName, tableAnnotation, annotations, configuration, objectFormatter)
    }

}