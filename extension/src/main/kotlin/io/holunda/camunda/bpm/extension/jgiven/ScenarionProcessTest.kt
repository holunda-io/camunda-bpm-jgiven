package io.holunda.camunda.bpm.extension.jgiven

import com.tngtech.jgiven.junit.ScenarioTest

/**
 * Scenario test having an action and assertion stages.
 */
abstract class ScenarioProcessTest<ACTION : ProcessStage<*, *>, ASSERTION : ProcessStage<*, *>> : ScenarioTest<ACTION, ACTION, ASSERTION>()
