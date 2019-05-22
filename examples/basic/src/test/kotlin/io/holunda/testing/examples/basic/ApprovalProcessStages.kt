package io.holunda.testing.examples.basic

import com.tngtech.jgiven.annotation.BeforeStage
import io.holunda.camunda.bpm.extension.jgiven.JGivenProcessStage
import io.holunda.camunda.bpm.extension.jgiven.ProcessStage
import io.holunda.testing.examples.basic.ApprovalProcessBean.Expressions.AUTOMATICALLY_APPROVE_REQUEST
import io.holunda.testing.examples.basic.ApprovalProcessBean.Expressions.DETERMINE_APPROVAL_STRATEGY
import io.holunda.testing.examples.basic.ApprovalProcessBean.Expressions.LOAD_APPROVAL_REQUEST
import io.holunda.testing.examples.basic.ApprovalProcessBean.Variables.APPROVAL_DECISION
import io.holunda.testing.examples.basic.ApprovalProcessBean.Variables.APPROVAL_STRATEGY
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.extension.mockito.CamundaMockito.getJavaDelegateMock
import org.camunda.bpm.extension.mockito.CamundaMockito.registerJavaDelegateMock

@JGivenProcessStage
class ApprovalProcessActionStage : ProcessStage<ApprovalProcessActionStage, ApprovalProcessBean>() {


  @BeforeStage
  fun automock_all_delegates() {
    registerJavaDelegateMock(DETERMINE_APPROVAL_STRATEGY)
    registerJavaDelegateMock(AUTOMATICALLY_APPROVE_REQUEST)
    registerJavaDelegateMock(LOAD_APPROVAL_REQUEST)
  }

  fun process_is_started_for_request(approvalRequestId: String): ApprovalProcessActionStage {
    processInstanceSupplier = ApprovalProcessBean(camunda.processEngine)
    processInstanceSupplier.start(approvalRequestId)
    assertThat(processInstanceSupplier.processInstance).isNotNull
    assertThat(processInstanceSupplier.processInstance).isStarted
    return self()
  }

  fun approval_strategy_can_be_applied(approvalStrategy: String): ApprovalProcessActionStage {
    getJavaDelegateMock(DETERMINE_APPROVAL_STRATEGY).onExecutionSetVariables(Variables.putValue(APPROVAL_STRATEGY, approvalStrategy))
    return self()
  }

  fun automatic_approval_returns(approvalDecision: String): ApprovalProcessActionStage {
    getJavaDelegateMock(AUTOMATICALLY_APPROVE_REQUEST).onExecutionSetVariables(Variables.putValue(APPROVAL_DECISION, approvalDecision))
    return self()
  }
}

@JGivenProcessStage
class ApprovalProcessThenStage : ProcessStage<ApprovalProcessThenStage, ApprovalProcessBean>()
