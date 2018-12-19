package io.holunda.camunda.bpm.extension.jgiven

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.As
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import com.tngtech.jgiven.annotation.Quoted
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.base.ScenarioTestBase
import io.holunda.camunda.bpm.extension.jgiven.formatter.QuotedVarargs
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import java.util.*
import java.util.function.Supplier

/**
 * Alias for the when
 */
fun <G, W, T> ScenarioTestBase<G, W, T>.whenever() = `when`()

/**
 * Process stage contains some basic methods to operate with process engine.
 * @param <SELF> sub-type of the ProcessStage. Will be returned from <code>self()</code> to maintain fluent API.
 */
open class ProcessStage<SELF : ProcessStage<SELF, PROCESS_BEAN>, PROCESS_BEAN : Supplier<ProcessInstance>> : Stage<SELF>() {

  @ExpectedScenarioState(required = true)
  lateinit var camunda: ProcessEngineRule

  @ScenarioState
  lateinit var processInstanceSupplier: PROCESS_BEAN

  @As("process waits in $")
  open fun process_waits_in(activityId: String): SELF {
    assertThat(processInstanceSupplier.get()).isWaitingAt(activityId)
    return self()
  }

  @As("process $ is deployed")
  open fun process_is_deployed(@Quoted processDefinitionKey: String): SELF {
    assertThat(camunda.repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey)
      .latestVersion()
      .singleResult()).isNotNull
    return self()
  }

  open fun process_is_finished(): SELF {
    assertThat(processInstanceSupplier.get()).isEnded
    return self()
  }

  open fun task_is_assigned_to_user(@Quoted user: String): SELF {
    assertThat(task()).isAssignedTo(user)
    return self()
  }

  open fun task_is_visible_to_users(@QuotedVarargs users: Array<String>): SELF {
    val task = task()
    Arrays.stream(users).forEach { user -> assertThat(task).hasCandidateUser(user) }
    return self()
  }

  open fun task_is_visible_to_groups(@QuotedVarargs groups: Array<String>): SELF {
    val task = task()
    Arrays.stream(groups).forEach { group -> assertThat(task).hasCandidateGroup(group) }
    return self()
  }

  @As("process has passed element(s) $")
  open fun process_has_passed(@QuotedVarargs vararg elements: String): SELF {
    assertThat(processInstanceSupplier.get()).hasPassedInOrder(*elements)
    return self()
  }

  @As("process has not passed element(s) $")
  open fun process_has_not_passed(@QuotedVarargs vararg elements: String): SELF {
    assertThat(processInstanceSupplier.get()).hasNotPassed(*elements)
    return self()
  }

  @As("variable \$variableName is set to \$value")
  open fun variable_is_set(@Quoted variableName: String, @Quoted value: Any): SELF {
    assertThat(processInstanceSupplier.get()).hasVariables(variableName)
    assertThat(processInstanceSupplier.get()).variables().containsEntry(variableName, value)
    return self()
  }

  @As("variables $ are not present")
  open fun variable_is_not_present(@QuotedVarargs vararg variableName: String): SELF {
    assertThat(processInstanceSupplier.get())
      .`as`("variable $variableName should not be present")
      .variables().doesNotContainKeys(*variableName)
    return self()
  }

  open fun no_job_is_executed(): SELF {
    // empty
    return self()
  }

  open fun job_is_executed(): SELF {
    execute(job())
    return self()
  }

  @As("process continues")
  open fun process_continues(): SELF {
    execute(job())
    return self()
  }
}
