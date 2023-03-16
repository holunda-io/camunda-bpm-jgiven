package io.holunda.testing.examples.basic

import io.holunda.testing.examples.basic.ApprovalProcessBean.Expressions.APPROVE_REQUEST_TASK_LISTENER
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Starts the application.
 */
fun main(args: Array<String>) = runApplication<BasicProcessApplication>(*args).let { Unit }

/**
 * Main application class.
 */
@EnableProcessApplication
class BasicProcessApplication {

  /**
   * Listener setting follow-up date.
   */
  @Bean(APPROVE_REQUEST_TASK_LISTENER)
  fun approveRequestTaskListener() = TaskListener {
    it.followUpDate = Date.from(it.createTime.toInstant().plus(1, ChronoUnit.DAYS))
  }

}
