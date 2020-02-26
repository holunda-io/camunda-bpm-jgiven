package io.holunda.testing.examples.basic

import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.junit.ScenarioTest
import io.holunda.camunda.bpm.extension.jgiven.whenever
import io.holunda.testing.examples.basic.ApprovalProcessBean.Elements
import io.holunda.testing.examples.basic.ApprovalProcessBean.Expressions
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.variable.Variables.putValue
import org.camunda.bpm.spring.boot.starter.test.helper.StandaloneInMemoryTestConfiguration
import org.junit.Rule
import org.junit.Test
import java.time.Period
import java.util.*


@Deployment(resources = [ApprovalProcessBean.RESOURCE])
open class ApprovalProcessTest : ScenarioTest<ApprovalProcessActionStage, ApprovalProcessActionStage, ApprovalProcessThenStage>() {

  companion object {
    val processEngineRule = StandaloneInMemoryTestConfiguration().rule()
  }

  @get: Rule
  @ScenarioState
  val camunda: ProcessEngineRule = processEngineRule


  @Test
  fun `should deploy`() {

    given()
      .process_is_deployed(ApprovalProcessBean.KEY)
    then()
  }

  @Test
  fun `should start asynchronously`() {

    val approvalRequestId = UUID.randomUUID().toString()

    given()
      .process_is_deployed(ApprovalProcessBean.KEY)
    whenever()
      .process_is_started_for_request(approvalRequestId)
    then()
      .process_waits_in(Elements.START)
  }

  @Test
  fun `should automatically approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    given()
      .process_is_deployed(ApprovalProcessBean.KEY)
      .and()
      .process_is_started_for_request(approvalRequestId)
      .and()
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)
      .and()
      .automatic_approval_returns(Expressions.ApprovalDecision.APPROVE)

    whenever()
      .process_continues()

    then()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE, Elements.END_APPROVED)

  }

  @Test
  fun `should automatically reject`() {

    val approvalRequestId = UUID.randomUUID().toString()

    given()
      .process_is_deployed(ApprovalProcessBean.KEY)
      .and()
      .process_is_started_for_request(approvalRequestId)
      .and()
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)
      .and()
      .automatic_approval_returns(Expressions.ApprovalDecision.REJECT)

    whenever()
      .process_continues()

    then()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE, Elements.END_REJECTED)

  }

  @Test
  fun `should manually reject`() {

    val approvalRequestId = UUID.randomUUID().toString()

    given()
      .process_is_deployed(ApprovalProcessBean.KEY)
      .and()
      .process_is_started_for_request(approvalRequestId)
      .and()
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.MANUAL)
      .and()
      .process_continues()
      .and()
      .process_waits_in(Elements.USER_APPROVE_REQUEST)
      .and()
      .task_priority_is_between(10, 30)
      .and()
      .task_has_follow_up_date_after(Period.ofDays(1))

    whenever()
      .task_is_completed_with_variables(
        putValue(ApprovalProcessBean.Variables.APPROVAL_DECISION, Expressions.ApprovalDecision.REJECT),
        continueIfAsync = true
      )

    then()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.END_REJECTED)

  }

  @Test
  fun `should manually approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    given()
      .process_is_deployed(ApprovalProcessBean.KEY)
      .and()
      .process_is_started_for_request(approvalRequestId)
      .and()
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.MANUAL)
      .and()
      .process_continues()
      .and()
      .process_waits_in(Elements.USER_APPROVE_REQUEST)

    whenever()
      .task_is_completed_with_variables(
        putValue(ApprovalProcessBean.Variables.APPROVAL_DECISION, Expressions.ApprovalDecision.APPROVE),
        continueIfAsync = true
      )

    then()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.END_APPROVED)

  }
}
