package io.holunda.testing.examples.basic.junit5

import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.junit5.ScenarioTest
import io.holunda.camunda.bpm.extension.jgiven.ActivityTrackingExternalTaskWorker
import io.holunda.testing.examples.basic.ApprovalProcessBean
import io.holunda.testing.examples.basic.ApprovalProcessBean.Elements
import io.holunda.testing.examples.basic.ApprovalProcessBean.Expressions
import io.holunda.testing.examples.basic.ApprovalProcessBean.Expressions.ApprovalDecision.APPROVE
import io.holunda.testing.examples.basic.ApprovalProcessBean.Expressions.ApprovalDecision.REJECT
import io.holunda.testing.examples.basic.ApprovalProcessBean.Variables.APPROVAL_DECISION
import io.toolisticon.testing.jgiven.AND
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.variable.Variables.putValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Period
import java.util.*

@Deployment(resources = [ApprovalProcessBean.RESOURCE])
internal class ApprovalProcessTest :
  ScenarioTest<ApprovalProcessActionStage, ApprovalProcessActionStage, ApprovalProcessThenStage>() {

  companion object {
    @JvmField
    @RegisterExtension
    val extension = TestProcessEngine.DEFAULT
  }

  @ScenarioState
  val camunda = extension.processEngine

  private val ppi = ApprovalProcessBean(camunda)

  @Test
  fun `should deploy`() {
    THEN
      .process_is_deployed(ApprovalProcessBean.KEY)
  }

  @Test
  fun `should start asynchronously`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
    WHEN
      .process_is_started_for_request(approvalRequestId)
    THEN
      .process_waits_in(Elements.START)
  }

  @Test
  fun `should wait for automatic approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)

    WHEN
      .process_continues()

    THEN
      .process_waits_in(Elements.SERVICE_AUTO_APPROVE)
      .AND
      .process_does_not_wait_in(Elements.USER_APPROVE_REQUEST)
      .AND
      .external_task_exists("approve-request")

  }

  @Test
  fun `should automatic approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .using_process_instance_starter {
        ppi.let {
          it.start(approvalRequestId)
          it.processInstance
        }
      }
      .AND
      .using_process_instance_supplier { _ -> ppi }
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)
      .AND
      .process_continues()

    WHEN
      .automatic_approval_returns(APPROVE)

    THEN
      .process_is_finished(Elements.END_APPROVED)
      .AND
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE)
      .AND
      .variable_was_set(APPROVAL_DECISION, APPROVE)

  }

  @Test
  fun `should automatic approve with custom worker`() {

    val worker = ActivityTrackingExternalTaskWorker(
      externalTaskService = camunda.externalTaskService,
      topicName = "approve-request",
      variablesToSet = putValue(APPROVAL_DECISION, APPROVE)
    )

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)
      .AND
      .process_continues()

    // not jgiven, but let's check here if the custom worker lambda works
    assertThat(worker.activities).isEmpty()

    WHEN
      .external_task_is_completed_by_worker(
        workerName = worker.workerName,
        topicName = worker.topicName,
        isAsyncAfter = false,
        worker = worker
      )

    THEN
      .process_is_finished()
      .AND
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE, Elements.END_APPROVED)

    // not jgiven, but let's check here if the custom worker lambda works
    assertThat(worker.activities).containsExactly("service_auto_approve_request")
  }


  @Test
  fun `should automatically reject`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)
      .AND
      .process_continues()

    WHEN
      .automatic_approval_returns(REJECT)

    THEN
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE)
      .AND
      .process_waits_in_external_task(Elements.EXTERNAL_INFORM_REJECTION)
      .AND
      .external_task_exists("inform-about-rejection")
  }

  @Test
  fun `should manually reject`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.MANUAL)
      .AND
      .process_continues()
      .AND
      .process_waits_in(Elements.USER_APPROVE_REQUEST)
      .AND
      .task_priority_is_between(10, 30)
      .AND
      .task_has_follow_up_date_after(Period.ofDays(1))

    WHEN
      .task_is_completed_with_variables(
        putValue(APPROVAL_DECISION, REJECT),
        isAsyncAfter = true
      )

    THEN
      .process_has_passed(Elements.USER_APPROVE_REQUEST)
      .AND
      .process_waits_in_external_task(Elements.EXTERNAL_INFORM_REJECTION)
      .AND
      .external_task_exists("inform-about-rejection")

  }

  @Test
  fun `should inform about rejection`() {
    val worker = ActivityTrackingExternalTaskWorker(
      externalTaskService = camunda.externalTaskService,
      topicName = "inform-about-rejection"
    )
    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)
      .AND
      .process_continues()
      .AND
      .automatic_approval_returns(REJECT)
      .AND
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE)
      .AND
      .process_waits_in_external_task(Elements.EXTERNAL_INFORM_REJECTION)
      .AND
      .external_task_exists("inform-about-rejection")

    WHEN
      .external_task_is_completed_by_worker(
        workerName = worker.workerName,
        topicName = worker.topicName,
        isAsyncAfter = true,
        worker = worker
      )

    THEN
      .process_is_finished(Elements.END_REJECTED)
      .AND
      .variable_was_set(APPROVAL_DECISION, REJECT)
  }

  @Test
  fun `should manually approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.MANUAL)
      .AND
      .process_continues()
      .AND
      .process_waits_in(Elements.USER_APPROVE_REQUEST)

    WHEN
      .task_is_completed_with_variables(
        putValue(APPROVAL_DECISION, APPROVE),
        isAsyncAfter = true
      )

    THEN
      .process_is_finished()
      .AND
      .process_has_passed(Elements.END_APPROVED)

  }
}
