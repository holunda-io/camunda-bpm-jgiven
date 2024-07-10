package io.holunda.camunda.bpm.extension.jgiven

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import java.util.function.Supplier

/**
 * Implementation holding one process instance id and loading it from runtime service on every request.
 * This class is intended to be subclassed.
 */
open class SelfUpdatingInstanceSupplier(
  protected val processInstanceId: String,
  protected val runtimeService: RuntimeService
) : Supplier<ProcessInstance> {

  /**
   * Retrieves the process instance.
   */
  override fun get(): ProcessInstance {
    val instances = runtimeService.createProcessInstanceQuery().active().processInstanceId(processInstanceId).list()
    return when (instances.size) {
      1 -> instances.first()
      else -> throw IllegalArgumentException("Could not find process instance with id: $processInstanceId.")
    }
  }
}
