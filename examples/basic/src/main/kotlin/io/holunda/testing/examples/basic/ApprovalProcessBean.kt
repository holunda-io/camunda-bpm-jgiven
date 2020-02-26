package io.holunda.testing.examples.basic

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.runtime.ProcessInstance
import java.util.function.Supplier

class ApprovalProcessBean(private val processEngine: ProcessEngine) : Supplier<ProcessInstance> {

  companion object {
    const val KEY = "approval"
    const val RESOURCE = "approval.bpmn"
  }

  lateinit var processInstance: ProcessInstance

  override fun get(): ProcessInstance = this.processInstance

  object Elements {
    const val START = "start"
    const val END_CANCELLED = "end_cancelled"
    const val END_APPROVED = "end_approved"
    const val END_REJECTED = "end_rejected"
    const val USER_APPROVE_REQUEST = "user_approve_request"
    const val USER_AMEND_REQUEST = "user_amend_request"
    const val SERVICE_AUTO_APPROVE = "service_auto_approve_request"
  }

  object Variables {
    const val APPROVAL_REQUEST_ID = "approvalRequestId"
    const val APPROVAL_STRATEGY = "approvalStrategy"
    const val APPROVAL_DECISION = "approvalDecision"
    const val AMEND_ACTION = "ammendAction"
    const val ORIGINATOR = "originator"
  }

  object Expressions {
    const val LOAD_APPROVAL_REQUEST = "loadApprovalRequest"
    const val DETERMINE_APPROVAL_STRATEGY = "determineApprovalStrategy"
    const val AUTOMATICALLY_APPROVE_REQUEST = "automaticallyApproveRequest"
    const val AUTOMATIC_APPROVAL_FAILED = "automaticApprovalFailed"
    const val APPROVE_REQUEST_TASK_LISTENER = "approveRequestTaskListener"

    object ApprovalStrategy {
      const val AUTOMATIC = "AUTOMATIC"
      const val MANUAL = "MANUAL"
    }

    object ApprovalDecision {
      const val APPROVE = "APPROVE"
      const val REJECT = "REJECT"
      const val RETURN = "RETURN"
    }

    object AmendAction {
      const val RESUBMIT = "RESUBMIT"
      const val CANCEL = "CANCEL"
    }
  }

  fun start(approvalRequestId: String) {
    this.processInstance = this.processEngine.runtimeService.startProcessInstanceByKey(
      KEY,
      approvalRequestId,
      org.camunda.bpm.engine.variable.Variables
        .putValue(Variables.ORIGINATOR, "kermit")
        .putValue(Variables.APPROVAL_REQUEST_ID, approvalRequestId)
    )
  }
}
