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
  protected val runtimeService: RuntimeService,
  private var lastSeenInstance: ProcessInstance? = null
) : Supplier<ProcessInstance> {

  /**
   * Retrieves the process instance.
   */
  override fun get(): ProcessInstance {
    val instance: ProcessInstance? = runtimeService
      .createProcessInstanceQuery()
      .active()
      .processInstanceId(processInstanceId)
      .singleResult()

    if (instance != null) {
      this.lastSeenInstance = instance
    }

    return this.lastSeenInstance
      ?: throw IllegalArgumentException("Could not find process instance with id: $processInstanceId.")
  }
}
