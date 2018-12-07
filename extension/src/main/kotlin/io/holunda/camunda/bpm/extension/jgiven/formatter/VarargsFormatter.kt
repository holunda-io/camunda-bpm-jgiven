package io.holunda.camunda.bpm.extension.jgiven.formatter

import com.tngtech.jgiven.format.ArgumentFormatter
import com.tngtech.jgiven.format.PrintfFormatter

/**
 * Argument formatter dealing with varargs and displaying them as a comma-separated list of quoted strings.
 * Delegates on the Printf-Formatter for actual formatting.
 */
open class VarargsFormatter : ArgumentFormatter<Any> {

  private val printfFormatter = PrintfFormatter()

  override fun format(argumentToFormat: Any?, vararg formatterArguments: String?): String = if (argumentToFormat is Array<*>) {
    argumentToFormat.joinToString(", ") { printfFormatter.format(it, *formatterArguments) }
  } else {
    printfFormatter.format(argumentToFormat, *formatterArguments)
  }

}
