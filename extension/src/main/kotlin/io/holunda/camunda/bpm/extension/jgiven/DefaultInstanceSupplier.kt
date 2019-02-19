package io.holunda.camunda.bpm.extension.jgiven

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import java.util.function.Supplier

/**
 * Default implementation holding one process instance. This class is intended to be subclassed.
 * Consider implementing a start method to start the process.
 */
open class DefaultInstanceSupplier(
  private val runtimeService: RuntimeService,
  protected var processInstance: ProcessInstance?
) : Supplier<ProcessInstance> {

  override fun get(): ProcessInstance {
    require(processInstance != null) { "Process has not been started. Consider starting it before accessing the process instance." }
    return processInstance!!
  }


}
