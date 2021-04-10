package io.holunda.testing.examples.basic

import org.camunda.bpm.extension.process_test_coverage.junit.rules.AggregatedCoverageTestRunState
import org.camunda.bpm.extension.process_test_coverage.model.CoveredFlowNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Supplier
import kotlin.reflect.KClass

class CoverageDataHolder : Supplier<AggregatedCoverageTestRunState> {

  companion object {
    private val logger: Logger = LoggerFactory.getLogger(CoverageDataHolder::class.java)
  }

  private val coverageRunState: AggregatedCoverageTestRunState = AggregatedCoverageTestRunState()

  fun reportCoverage(testClazz: KClass<*>) {
    val coverage = coverageRunState.aggregatedCoverage
    // Generate a report for every process definition
    coverage.processDefinitions.forEach {
      // Assemble data
      val coveredFlowNodes: Set<CoveredFlowNode> = coverage.getCoveredFlowNodes(it.key)
      val coveredSequenceFlowIds: Set<String> = coverage.getCoveredSequenceFlowIds(it.key)
      val percentage = coverage.getCoveragePercentage(it.key)
      logger.info("The test $testClazz tested ${it.key} covering $percentage% (flow nodes: ${coveredFlowNodes.size}).")
    }
  }

  override fun get(): AggregatedCoverageTestRunState = coverageRunState

}
