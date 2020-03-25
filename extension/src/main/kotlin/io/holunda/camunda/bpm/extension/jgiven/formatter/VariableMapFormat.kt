package io.holunda.camunda.bpm.extension.jgiven.formatter

import com.tngtech.jgiven.annotation.Table
import com.tngtech.jgiven.config.FormatterConfiguration
import com.tngtech.jgiven.format.ObjectFormatter
import com.tngtech.jgiven.format.table.DefaultTableFormatter
import com.tngtech.jgiven.format.table.TableFormatter
import com.tngtech.jgiven.format.table.TableFormatterFactory
import com.tngtech.jgiven.report.model.DataTable


@Retention(AnnotationRetention.RUNTIME)
@Table(columnTitles = ["name", "value"], formatter = VariableTableFormatter.Factory::class)
annotation class Variables

class VariableTableFormatter(formatterConfiguration: FormatterConfiguration, objectFormatter: ObjectFormatter<*>) : DefaultTableFormatter(formatterConfiguration, objectFormatter) {

    class Factory : TableFormatterFactory {
        override fun create(formatterConfiguration: FormatterConfiguration, objectFormatter: ObjectFormatter<*>) = VariableTableFormatter(formatterConfiguration, objectFormatter)
    }

    override fun format(tableArgument: Any?, tableAnnotation: Table?, parameterName: String?, vararg allAnnotations: Annotation?): DataTable {

        return super.format(tableArgument, tableAnnotation, parameterName, *allAnnotations)
    }
}