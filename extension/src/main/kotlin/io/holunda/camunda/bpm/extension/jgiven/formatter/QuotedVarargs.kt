package io.holunda.camunda.bpm.extension.jgiven.formatter

import com.tngtech.jgiven.annotation.Format
import com.tngtech.jgiven.format.ArgumentFormatter
import com.tngtech.jgiven.format.PrintfFormatter

/**
 * Annotation using the Vargarg Argument formatter.
 */
@MustBeDocumented
@Format(value = VarargsFormatter::class, args = ["\"%s\""])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FIELD)
annotation class QuotedVarargs

/**
 * Argument formatter dealing with varargs and displaying them as a comma-separated list of quoted strings.
 * Delegates on the Printf-Formatter for actual formatting.
 */
open class VarargsFormatter : ArgumentFormatter<Any> {

  /*
   * Formatter.
   */
  private val printfFormatter = PrintfFormatter()

  override fun format(argumentToFormat: Any?, vararg formatterArguments: String?): String = if (argumentToFormat is Array<*>) {
    argumentToFormat.joinToString(", ") { printfFormatter.format(it, *formatterArguments) }
  } else {
    printfFormatter.format(argumentToFormat, *formatterArguments)
  }

}
