package io.holunda.testing.examples.basic

import com.tngtech.jgiven.annotation.ProvidedScenarioState
import io.holunda.camunda.bpm.extension.jgiven.ProcessStage
import io.holunda.camunda.bpm.extension.jgiven.ScenarioProcessTest
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.extension.process_test_coverage.junit.rules.AggregatedCoverageTestRunState
import org.camunda.bpm.extension.process_test_coverage.junit.rules.AggregatedCoverageTestRunStateFactory
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder
import org.camunda.bpm.spring.boot.starter.test.helper.StandaloneInMemoryTestConfiguration
import org.junit.ClassRule
import org.junit.Rule
import java.util.function.Supplier

abstract class CoverageScenarioProcessTest<ACTION : ProcessStage<*, *>, ASSERTION : ProcessStage<*, *>> : ScenarioProcessTest<ACTION, ASSERTION>() {

  companion object {

    // private val coverageRunState: AggregatedCoverageTestRunState = AggregatedCoverageTestRunState()

    val coverageDataHolder = CoverageDataHolder()

    @Rule
    @ClassRule
    @JvmField
    val coverageRule: ProcessEngineRule =
      TestCoverageProcessEngineRuleBuilder.create(
        StandaloneInMemoryTestConfiguration().processEngine
      ).setCoverageTestRunStateFactory(
        AggregatedCoverageTestRunStateFactory(coverageDataHolder.get())
      ).build()
  }

  @ProvidedScenarioState
  val camunda: ProcessEngineRule = coverageRule

}

fun initializeRule(
  coverageDataHolder: Supplier<AggregatedCoverageTestRunState>,
  processEngine: ProcessEngine
): TestCoverageProcessEngineRule = TestCoverageProcessEngineRuleBuilder
  .create(processEngine)
  .setCoverageTestRunStateFactory(AggregatedCoverageTestRunStateFactory(coverageDataHolder.get()))
  .build()

