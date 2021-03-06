package io.holunda.camunda.bpm.extension.jgiven

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.*
import com.tngtech.jgiven.base.ScenarioTestBase
import io.holunda.camunda.bpm.extension.jgiven.formatter.QuotedVarargs
import io.holunda.camunda.bpm.extension.jgiven.formatter.VariableMapFormat
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions.*
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables
import java.time.Period
import java.util.*
import java.util.function.Supplier

/**
 * Alias for the when
 */
@Deprecated(message = "Please use JGiven-Kotlin instead")
fun <G, W, T> ScenarioTestBase<G, W, T>.whenever() = `when`()

/**
 * Alias for the when
 */
@Deprecated(message = "Please use JGiven-Kotlin instead")
fun <G, W, T> ScenarioTestBase<G, W, T>.WHEN() = `when`()

/**
 * Alias for the given
 */
@Deprecated(message = "Please use JGiven-Kotlin instead")
fun <G, W, T> ScenarioTestBase<G, W, T>.GIVEN() = given()

/**
 * Alias for the then
 */
@Deprecated(message = "Please use JGiven-Kotlin instead")
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

  /**
   * Process engine to work on.
   */
  @ExpectedScenarioState(required = true)
  lateinit var camunda: ProcessEngineRule

  /**
   * Instance supplier.
   */
  @ScenarioState
  lateinit var processInstanceSupplier: PROCESS_BEAN

  /**
   * Checks if the process instance is waiting in a activity with specified id.
   * @param activityId definition id.
   * @return fluent stage.
   */
  @As("process waits in $")
  fun process_waits_in(@Quoted activityId: String): SELF = step {
    assertThat(processInstanceSupplier.get()).isWaitingAt(activityId)
  }

  /**
   * Asserts that the process is deployed.
   * @param processDefinitionKey process definition key.
   * @return fluent stage.
   */
  @As("process $ is deployed")
  fun process_is_deployed(@Quoted processDefinitionKey: String): SELF = step {
    assertThat(
      camunda.repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .latestVersion()
        .singleResult()
    ).isNotNull

  }

  /**
   * Asserts that the process is finished.
   * @return fluent stage.
   */
  fun process_is_finished(): SELF = step {
    assertThat(processInstanceSupplier.get()).isEnded
  }

  /**
   * Asserts that the process has passed in order elements.
   * @param elements element definition ids.
   * @return fluent stage.
   */
  @As("process has passed element(s) $")
  fun process_has_passed(@QuotedVarargs vararg elements: String): SELF = step {
    assertThat(processInstanceSupplier.get()).hasPassedInOrder(*elements)
  }

  /**
   * Asserts that the process has not passed elements.
   * @param elements element definition ids.
   * @return fluent stage.
   */
  @As("process has not passed element(s) $")
  fun process_has_not_passed(@QuotedVarargs vararg elements: String): SELF = step {
    assertThat(processInstanceSupplier.get()).hasNotPassed(*elements)
  }

  /**
   * Asserts that a task is assigned to a user.
   * @param user username of the assignee.
   * @return fluent stage.
   */
  fun task_is_assigned_to_user(@Quoted user: String): SELF = step {
    assertThat(task()).isAssignedTo(user)
  }

  /**
   * Asserts that a task is visible to user.
   * @param users list of candidate users that must be candidate.
   * @return fluent stage.
   */
  fun task_is_visible_to_users(@QuotedVarargs users: Array<String>): SELF = step {
    val task = task()
    users.forEach { user -> assertThat(task).hasCandidateUser(user) }
  }

  /**
   * Asserts that a task is visible to groups.
   * @param groups list of candidate groups that must be candidate.
   * @return fluent stage.
   */
  fun task_is_visible_to_groups(@QuotedVarargs groups: Array<String>): SELF = step {
    val task = task()
    groups.forEach { group -> assertThat(task).hasCandidateGroup(group) }
  }

  /**
   * Asserts the task task has a follow-up date
   * @param followUpDatePeriod period calculated from creation.
   * @return fluent stage.
   */
  @As("task's follow-up date is $ after its creation")
  fun task_has_follow_up_date_after(followUpDatePeriod: Period): SELF = step {
    assertThat(task().followUpDate).isInSameSecondWindowAs(
      Date.from(
        task().createTime.toInstant().plus(followUpDatePeriod)
      )
    )
  }

  /**
   * Asserts task priority.
   * @param priority specified priority.
   * @return fluent stage.
   */
  @As("task's priority is $")
  fun task_has_priority(priority: Int): SELF = step {
    assertThat(task().priority)
      .`as`("Expecting task priority to be %d, but it was %d.", priority, task().priority)
      .isEqualTo(priority)
  }

  /**
   * Asserts priority.
   * @param priority not the priority.
   * @return fluent stage.
   */
  @As("task's priority is not $")
  fun task_has_priority_other_than(priority: Int): SELF = step {
    assertThat(task().priority)
      .`as`("Expecting task priority to not equal to %d, but it was %d.", priority, task().priority)
      .isNotEqualTo(priority)
  }

  /**
   * Asserts priority.
   * @param priority lower bound.
   * @return fluent stage.
   */
  @As("task's priority is greater than $")
  fun task_has_priority_greater_than(priority: Int): SELF = step {
    assertThat(task().priority)
      .`as`("Expecting task priority to be greater than %d, but it was %d.", priority, task().priority)
      .isGreaterThan(priority)
  }

  /**
   * Asserts priority.
   * @param priority upper bound.
   * @return fluent stage.
   */
  @As("task's priority is less than $")
  fun task_has_priority_less_than(priority: Int): SELF = step {
    assertThat(task().priority)
      .`as`("Expecting task priority to be less than %d, but it was %d.", priority, task().priority)
      .isLessThan(priority)
  }

  /**
   * Asserts priority.
   * @param lower lower bound.
   * @param upper upper bound.
   * @return fluent stage.
   */
  @As("task's priority is between \$lower and \$upper")
  fun task_priority_is_between(lower: Int = 0, upper: Int = 100): SELF = step {
    assertThat(task().priority)
      .`as`("Expecting task priority to be between %d and %d, but it was %d.", lower, upper, task().priority)
      .isBetween(lower, upper)
  }

  /**
   * Asserts that variable is set.
   * @param variableName name of the variable.
   * @param value value of the variable.
   * @return fluent stage.
   */
  @As("variable \$variableName is set to \$value")
  fun variable_is_set(@Quoted variableName: String, @Quoted value: Any): SELF = step {
    assertThat(processInstanceSupplier.get()).hasVariables(variableName)
    assertThat(processInstanceSupplier.get()).variables().containsEntry(variableName, value)
  }

  /**
   * Asserts that variable is not set.
   * @param variableName name of the variable.
   * @return fluent stage.
   */
  @As("variables $ are not present")
  fun variable_is_not_present(@QuotedVarargs vararg variableName: String): SELF = step {
    assertThat(processInstanceSupplier.get())
      .`as`("variable $variableName should not be present")
      .variables().doesNotContainKeys(*variableName)
  }

  /**
   * Completes a task with variables. If the task is marked as async-after, the execution will not continue.
   * @param variables optional map with variables
   * @return fluent stage.
   */
  @As("task is completed with variables $")
  fun task_is_completed_with_variables(@VariableMapFormat variables: VariableMap = createVariables()): SELF = step {
    task_is_completed_with_variables(variables, false)
  }

  /**
   * Completes a task with variables and continues if the task is marked as async-after.
   * @param variables optional map with variables
   * @param isAsyncAfter if <code>true</code> expects that the task is marked as async-after and continues the execution
   * after completion. Defaults to <code>false</code>.
   * @return fluent stage.
   */
  @As("task is completed with variables $")
  fun task_is_completed_with_variables(
    @VariableMapFormat variables: VariableMap = createVariables(),
    @Hidden isAsyncAfter: Boolean = false
  ): SELF = step {
    val taskDefinitionKey = task().taskDefinitionKey
    taskService().complete(task().id, variables)
    if (isAsyncAfter) {
      assertThat(processInstanceSupplier.get())
        .`as`("Expecting the task to be marked as async after and continue on complete.")
        .isWaitingAt(taskDefinitionKey)
      execute(job())
    }
  }

  /**
   * No op.
   * @return fluent stage.
   */
  fun no_job_is_executed(): SELF = step {
    // empty
  }

  /**
   * Executes current job.
   * @return fluent stage.
   */
  fun job_is_executed(): SELF = step {
    assertThat(processInstanceSupplier.get()).isNotNull
    execute(job())
  }

  /**
   * Executes current job.
   * @return fluent stage.
   */
  @As("process continues")
  fun process_continues(): SELF = step {
    assertThat(processInstanceSupplier.get()).isNotNull
    execute(job())
  }

  /**
   * Asserts that external task exists.
   * @param topicName name of the topic.
   * @return fluent stage.
   */
  @As("external task exists on topic \$topicName")
  fun external_task_exists(@Quoted topicName: String): SELF = step {
    val externalTasks = camunda
      .externalTaskService
      .createExternalTaskQuery()
      .topicName(topicName)
      .list()
    assertThat(externalTasks).isNotEmpty
  }

  /**
   * Completes external task. If the task is marked as async-after, the process will not continue.
   * @param topicName name of the topic.
   * @param variables variables to set on completion.
   * @return fluent stage.
   */
  @As("external task on topic \$topicName is completed with variables \$variables")
  fun external_task_is_completed(
    @Quoted topicName: String,
    @VariableMapFormat variables: VariableMap = createVariables()
  ): SELF = step {
    external_task_is_completed(topicName, variables, false)
  }

  /**
   * Completes external task.
   * @param topicName name of the topic.
   * @param variables variables to set on completion.
   * @param isAsyncAfter executes the async job after completion.
   * @return fluent stage.
   */
  @As("external task on topic \$topicName is completed with variables \$variables")
  fun external_task_is_completed(
    @Quoted topicName: String,
    @VariableMapFormat variables: VariableMap = createVariables(),
    isAsyncAfter: Boolean = false
  ): SELF = step {
    camunda
      .externalTaskService
      .fetchAndLock(10, "test-worker")
      .topic(topicName, 1_000)
      .execute()
      .forEach {
        camunda
          .externalTaskService
          .complete(it.id, "test-worker", variables)

      }

    if (isAsyncAfter) {
      process_continues()
    }
  }

  /**
   * Correlates message.
   * @param messageName name of the message.
   * @param variables variables to set on correlation.
   * @return fluent stage.
   */
  @As("message \$messageName is received setting variables \$variables")
  fun message_is_received(@Quoted messageName: String, variables: VariableMap = createVariables()): SELF = step {

    // exactly one subscription
    assertThat(
      camunda.processEngine.runtimeService
        .createEventSubscriptionQuery()
        .processInstanceId(processInstanceSupplier.get().processInstanceId)
        .eventType("message")
        .eventName(messageName).count()
    ).isEqualTo(1)

    camunda.processEngine.runtimeService
      .createMessageCorrelation(messageName)
      .setVariables(variables)
      .correlate()
  }

}
