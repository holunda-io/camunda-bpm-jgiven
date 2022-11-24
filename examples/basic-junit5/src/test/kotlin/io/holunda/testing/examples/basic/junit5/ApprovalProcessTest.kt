package io.holunda.testing.examples.basic.junit5

import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.junit5.ScenarioTest
import io.holunda.testing.examples.basic.ApprovalProcessBean
import io.holunda.testing.examples.basic.ApprovalProcessBean.Elements
import io.holunda.testing.examples.basic.ApprovalProcessBean.Expressions
import io.toolisticon.testing.jgiven.AND
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
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

  @Test
  internal fun `should deploy`() {
    THEN
      .process_is_deployed(ApprovalProcessBean.KEY)
  }

  @Test
  internal fun `should start asynchronously`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
    WHEN
      .process_is_started_for_request(approvalRequestId)
    THEN
      .process_waits_in(Elements.START)
  }

  @Test
  internal fun `should wait for automatic approve`() {

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
      .external_task_exists("approve-request")

  }

  @Test
  internal fun `should automatic approve`() {

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
      .automatic_approval_returns(Expressions.ApprovalDecision.APPROVE)

    THEN
      .process_is_finished()
      .AND
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE, Elements.END_APPROVED)

  }


  @Test
  internal fun `should automatically reject`() {

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
      .automatic_approval_returns(Expressions.ApprovalDecision.REJECT)

    THEN
      .process_is_finished()
      .AND
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE, Elements.END_REJECTED)

  }

  @Test
  internal fun `should manually reject`() {

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
        putValue(ApprovalProcessBean.Variables.APPROVAL_DECISION, Expressions.ApprovalDecision.REJECT),
        isAsyncAfter = true
      )

    THEN
      .process_is_finished()
      .AND
      .process_has_passed(Elements.END_REJECTED)

  }

  @Test
  internal fun `should manually approve`() {

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
        putValue(ApprovalProcessBean.Variables.APPROVAL_DECISION, Expressions.ApprovalDecision.APPROVE),
        isAsyncAfter = true
      )

    THEN
      .process_is_finished()
      .AND
      .process_has_passed(Elements.END_APPROVED)

  }
}
