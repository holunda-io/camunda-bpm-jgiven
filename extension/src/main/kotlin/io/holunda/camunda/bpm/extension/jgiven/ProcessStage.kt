package io.holunda.camunda.bpm.extension.jgiven

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.As
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import com.tngtech.jgiven.annotation.Quoted
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.base.ScenarioTestBase
import io.holunda.camunda.bpm.extension.jgiven.formatter.QuotedVarargs
import org.assertj.core.api.Assertions.*
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import java.time.Period
import java.util.*
import java.util.function.Supplier

/**
 * Alias for the when
 */
fun <G, W, T> ScenarioTestBase<G, W, T>.whenever() = `when`()
/**
 * Alias for the when
 */
fun <G, W, T> ScenarioTestBase<G, W, T>.WHEN() = `when`()
/**
 * Alias for the given
 */
fun <G, W, T> ScenarioTestBase<G, W, T>.GIVEN() = given()
/**
 * Alias for the then
 */
fun <G, W, T> ScenarioTestBase<G, W, T>.THEN() = then()


/**
 * Annotation to mark jgiven process stages in order to make them open by all-open compiler plugin.
 */
annotation class JGivenProcessStage

/**
 * Process stage contains some basic methods to operate with process engine.
 * @param <SELF> sub-type of the ProcessStage. Will be returned from <code>self()</code> to maintain fluent API.
 */
@JGivenProcessStage
class ProcessStage<SELF : ProcessStage<SELF, PROCESS_BEAN>, PROCESS_BEAN : Supplier<ProcessInstance>> : Stage<SELF>() {

  @ExpectedScenarioState(required = true)
  lateinit var camunda: ProcessEngineRule

  @ScenarioState
  lateinit var processInstanceSupplier: PROCESS_BEAN

  @As("process waits in $")
  fun process_waits_in(@Quoted activityId: String): SELF {
    assertThat(processInstanceSupplier.get()).isWaitingAt(activityId)
    return self()
  }

  @As("process $ is deployed")
  fun process_is_deployed(@Quoted processDefinitionKey: String): SELF {
    assertThat(camunda.repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey)
      .latestVersion()
      .singleResult()).isNotNull
    return self()
  }

  fun process_is_finished(): SELF {
    assertThat(processInstanceSupplier.get()).isEnded
    return self()
  }

  fun task_is_assigned_to_user(@Quoted user: String): SELF {
    assertThat(task()).isAssignedTo(user)
    return self()
  }

  fun task_is_visible_to_users(@QuotedVarargs users: Array<String>): SELF {
    val task = task()
    Arrays.stream(users).forEach { user -> assertThat(task).hasCandidateUser(user) }
    return self()
  }

  fun task_is_visible_to_groups(@QuotedVarargs groups: Array<String>): SELF {
    val task = task()
    Arrays.stream(groups).forEach { group -> assertThat(task).hasCandidateGroup(group) }
    return self()
  }

  @As("process has passed element(s) $")
  fun process_has_passed(@QuotedVarargs vararg elements: String): SELF {
    assertThat(processInstanceSupplier.get()).hasPassedInOrder(*elements)
    return self()
  }

  @As("process has not passed element(s) $")
  fun process_has_not_passed(@QuotedVarargs vararg elements: String): SELF {
    assertThat(processInstanceSupplier.get()).hasNotPassed(*elements)
    return self()
  }

  @As("task's follow-up date is $ after its creation")
  fun task_has_follow_up_date_after(followUpDatePeriod: Period): SELF {
    assertThat(task().followUpDate).isInSameSecondWindowAs(Date.from(task().createTime.toInstant().plus(followUpDatePeriod)))
    return self()
  }

  @As("task's priority is $")
  fun task_has_priority(priority: Int): SELF {
    assertThat(task().priority)
      .`as`("Expecting task priority to be %d, but it was %d.", priority, task().priority)
      .isEqualTo(priority)
    return self()
  }

  @As("task's priority is not $")
  fun task_has_priority_other_than(priority: Int): SELF {
    assertThat(task().priority)
      .`as`("Expecting task priority to not equal to %d, but it was %d.", priority, task().priority)
      .isNotEqualTo(priority)
    return self()
  }

  @As("task's priority is greater than $")
  fun task_has_priority_greater_than(priority: Int): SELF {
    assertThat(task().priority)
      .`as`("Expecting task priority to be greater than %d, but it was %d.", priority, task().priority)
      .isGreaterThan(priority)
    return self()
  }

  @As("task's priority is less than $")
  fun task_has_priority_less_than(priority: Int): SELF {
    assertThat(task().priority)
      .`as`("Expecting task priority to be less than %d, but it was %d.", priority, task().priority)
      .isLessThan(priority)
    return self()
  }

  @As("task's priority is between \$lower \$upper")
  fun task_priority_is_between(lower: Int = 0, upper: Int = 100): SELF {
    assertThat(task().priority)
      .`as`("Expecting task priority to be between %d and %d, but it was %d.", lower, upper, task().priority)
      .isBetween(lower, upper)
    return self()
  }


  @As("variable \$variableName is set to \$value")
  fun variable_is_set(@Quoted variableName: String, @Quoted value: Any): SELF {
    assertThat(processInstanceSupplier.get()).hasVariables(variableName)
    assertThat(processInstanceSupplier.get()).variables().containsEntry(variableName, value)
    return self()
  }

  @As("variables $ are not present")
  fun variable_is_not_present(@QuotedVarargs vararg variableName: String): SELF {
    assertThat(processInstanceSupplier.get())
      .`as`("variable $variableName should not be present")
      .variables().doesNotContainKeys(*variableName)
    return self()
  }

  /**
   * Completes a task with variables and continues if the task is marked as async-after.
   * @param variables optional map with variables
   * @param continueIfAsync if <code>true</code> expects that the task is marked as async-after and continues the execution
   * after completion. Defaults to <code>false</code>.
   */
  fun task_is_completed_with_variables(variables: Map<String, Any> = mapOf(), continueIfAsync: Boolean = false): SELF {
    val taskDefinitionKey = task().taskDefinitionKey
    taskService().complete(task().id, variables)
    if (continueIfAsync) {
      assertThat(processInstanceSupplier.get())
        .`as`("Expecting the task to be marked as async after and continue on complete.")
        .isWaitingAt(taskDefinitionKey)
      execute(job())
    }
    return self()
  }

  fun no_job_is_executed(): SELF {
    // empty
    return self()
  }

  fun job_is_executed(): SELF {
    assertThat(processInstanceSupplier.get()).isNotNull
    execute(job())
    return self()
  }

  @As("process continues")
  fun process_continues(): SELF {
    assertThat(processInstanceSupplier.get()).isNotNull
    execute(job())
    return self()
  }
}
