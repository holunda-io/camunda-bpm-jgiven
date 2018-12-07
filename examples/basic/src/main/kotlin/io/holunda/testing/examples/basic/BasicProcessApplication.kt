package io.holunda.testing.examples.basic

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.runApplication

@EnableProcessApplication
open class BasicProcessApplication {
  fun main(args: Array<String>) = runApplication<BasicProcessApplication>(*args).let { Unit }
}
