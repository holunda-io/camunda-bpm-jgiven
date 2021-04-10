package io.holunda.testing.examples.basic

import com.tngtech.jgiven.annotation.ProvidedScenarioState
import io.holunda.camunda.bpm.extension.jgiven.GIVEN
import io.holunda.camunda.bpm.extension.jgiven.THEN
import io.holunda.camunda.bpm.extension.jgiven.WHEN
import io.holunda.testing.examples.basic.ApprovalProcessBean.Elements
import io.holunda.testing.examples.basic.ApprovalProcessBean.Expressions
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.variable.Variables.putValue
import org.camunda.bpm.spring.boot.starter.test.helper.StandaloneInMemoryTestConfiguration
import org.junit.AfterClass
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import java.time.Period
import java.util.*


@Deployment(resources = [ApprovalProcessBean.RESOURCE])
open class ApprovalProcessTest : CoverageScenarioProcessTest<ApprovalProcessActionStage, ApprovalProcessThenStage>() {

  companion object {

//    private val coverageDataHolder = CoverageDataHolder()
//
//    @JvmField
//    @Rule
//    @ClassRule
//    var coverageRule: ProcessEngineRule = initializeRule(coverageDataHolder, StandaloneInMemoryTestConfiguration().processEngine)

    @JvmStatic
    @AfterClass
    fun report() {
      coverageDataHolder.reportCoverage(ApprovalProcessTest::class)
    }
  }

  @Test
  fun `should deploy`() {
    THEN()
      .process_is_deployed(ApprovalProcessBean.KEY)
  }

  @Test
  fun `should start asynchronously`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN()
      .process_is_deployed(ApprovalProcessBean.KEY)
    WHEN()
      .process_is_started_for_request(approvalRequestId)
    THEN()
      .process_waits_in(Elements.START)
  }

  @Test
  fun `should wait for automatic approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN()
      .process_is_deployed(ApprovalProcessBean.KEY)
      .and()
      .process_is_started_for_request(approvalRequestId)
      .and()
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)

    WHEN()
      .process_continues()

    THEN()
      .process_waits_in(Elements.SERVICE_AUTO_APPROVE)
      .and()
      .external_task_exists("approve-request")

  }

  @Test
  fun `should automatic approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN()
      .process_is_deployed(ApprovalProcessBean.KEY)
      .and()
      .process_is_started_for_request(approvalRequestId)
      .and()
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)
      .and()
      .process_continues()

    WHEN()
      .automatic_approval_returns(Expressions.ApprovalDecision.APPROVE)

    THEN()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE, Elements.END_APPROVED)

  }


  @Test
  fun `should automatically reject`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN()
      .process_is_deployed(ApprovalProcessBean.KEY)
      .and()
      .process_is_started_for_request(approvalRequestId)
      .and()
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)
      .and()
      .process_continues()

    WHEN()
      .automatic_approval_returns(Expressions.ApprovalDecision.REJECT)

    THEN()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE, Elements.END_REJECTED)

  }

  @Test
  fun `should manually reject`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN()
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

    WHEN()
      .task_is_completed_with_variables(
        putValue(ApprovalProcessBean.Variables.APPROVAL_DECISION, Expressions.ApprovalDecision.REJECT),
        continueIfAsync = true
      )

    THEN()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.END_REJECTED)

  }

  @Test
  fun `should manually approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN()
      .process_is_deployed(ApprovalProcessBean.KEY)
      .and()
      .process_is_started_for_request(approvalRequestId)
      .and()
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.MANUAL)
      .and()
      .process_continues()
      .and()
      .process_waits_in(Elements.USER_APPROVE_REQUEST)

    WHEN()
      .task_is_completed_with_variables(
        putValue(ApprovalProcessBean.Variables.APPROVAL_DECISION, Expressions.ApprovalDecision.APPROVE),
        continueIfAsync = true
      )

    THEN()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.END_APPROVED)

  }
}
