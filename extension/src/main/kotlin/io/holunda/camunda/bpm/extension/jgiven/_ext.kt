package io.holunda.camunda.bpm.extension.jgiven

import com.tngtech.jgiven.base.ScenarioTestBase


/**
 * Alias for the when
 */
@Deprecated(message = "Please use JGiven-Kotlin instead")
fun <G, W, T> ScenarioTestBase<G, W, T>.whenever() = `when`()

/**
 * Alias for the when
 */
@Deprecated(message = "Please use JGiven-Kotlin instead")
fun <G, W, T> ScenarioTestBase<G, W, T>.WHEN() = `when`()

/**
 * Alias for the given
 */
@Deprecated(message = "Please use JGiven-Kotlin instead")
fun <G, W, T> ScenarioTestBase<G, W, T>.GIVEN() = given()

/**
 * Alias for the then
 */
@Deprecated(message = "Please use JGiven-Kotlin instead")
fun <G, W, T> ScenarioTestBase<G, W, T>.THEN() = then()

/**
 * Annotation to mark jgiven process stages in order to make them open by all-open compiler plugin.
 */
annotation class JGivenProcessStage
