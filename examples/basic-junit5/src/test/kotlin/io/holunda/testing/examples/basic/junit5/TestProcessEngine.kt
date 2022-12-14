package io.holunda.testing.examples.basic.junit5

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.ProcessEngineImpl
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.impl.history.HistoryLevel
import org.camunda.bpm.engine.test.mock.MockExpressionManager
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension
import org.camunda.community.process_test_coverage.engine.platform7.ProcessCoverageConfigurator
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Test engine setup.
 */
enum class TestProcessEngine {
  ;

  /**
   * Creates the builder.
   */
  class Builder internal constructor() {
    private val configuration: ProcessEngineConfigurationImpl

    init {
      configuration = StandaloneInMemProcessEngineConfiguration()
      configuration.historyLevel = HistoryLevel.HISTORY_LEVEL_FULL
      configuration.databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
      configuration.isJobExecutorActivate = false
      configuration.isDbMetricsReporterActivate = false
      configuration.expressionManager = MockExpressionManager()
      configuration.isTelemetryReporterActivate = false
      configuration.isInitializeTelemetry = false
    }

    /**
     * Function to register pre-init hooks.
     */
    fun preInit(preInit: Consumer<ProcessEngineConfigurationImpl>): Builder {
      plugin(createPlugin(preInit, null, null))
      return this
    }

    /**
     * Function to register post-init hooks.
     */
    fun postInit(postInit: Consumer<ProcessEngineConfigurationImpl>): Builder {
      plugin(createPlugin(null, postInit, null))
      return this
    }

    /**
     * Function to register post engine build hooks.
     */
    fun postProcessEngineBuild(postProcessEngineBuild: Consumer<ProcessEngine>): Builder {
      plugin(createPlugin(null, null, postProcessEngineBuild))
      return this
    }

    /**
     * Set serialization format.
     */
    fun withDefaultSerializationFormat(defaultSerializationFormat: String): Builder {
      configuration.defaultSerializationFormat = defaultSerializationFormat
      return this
    }

    /**
     * Adds an engine core plugin.
     */
    fun plugin(plugin: ProcessEnginePlugin): Builder {
      configuration.processEnginePlugins.add(plugin)
      return this
    }

    /**
     * Sets default name for H" DB.
     */
    fun withH2MemNameDefault(): Builder {
      return withH2MemName("camunda")
    }

    /**
     * Sets name of the Camunda H2 DB.
     */
    fun withH2MemName(databaseName: String): Builder {
      preInit {
        it.jdbcUrl = String.format(
          "jdbc:h2:mem:%s;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
          databaseName
        )
      }
      return this
    }

    /**
     * Retrieves the engine.
     */
    fun engine(): ProcessEngineImpl {
      return configuration.buildProcessEngine() as ProcessEngineImpl
    }

    /**
     * Builds the JUnit5 extension.
     */
    fun extension(): ProcessEngineExtension {
        ProcessCoverageConfigurator.initializeProcessCoverageExtensions(configuration)
        return ProcessEngineCoverageExtension.builder(configuration).build()
    }

    companion object {
      private fun createPlugin(
        preInit: Consumer<ProcessEngineConfigurationImpl>?,
        postInit: Consumer<ProcessEngineConfigurationImpl>?,
        postProcessEngineBuild: Consumer<ProcessEngine>?
      ): ProcessEnginePlugin {
        return object : AbstractProcessEnginePlugin() {
          override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
            preInit?.accept(processEngineConfiguration)
          }

          override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
            postInit?.accept(processEngineConfiguration)
          }

          override fun postProcessEngineBuild(processEngine: ProcessEngine) {
            postProcessEngineBuild?.accept(processEngine)
          }

          override fun toString(): String {
            return Stream.of(preInit, postInit, postProcessEngineBuild).map(Objects::nonNull)
              .map { obj: Any -> obj.toString() }
              .collect(Collectors.joining("-"))
          }
        }
      }
    }
  }

  companion object {
    // util class, final, no instance
    val DEFAULT = builder()
      .withDefaultSerializationFormat("application/json")
      .extension()

    /**
     * Creates the builder.
     */
    fun builder(): Builder {
      return Builder()
    }

    /**
     * Creates the extension.
     */
    fun extension(): ProcessEngineExtension {
      return builder().extension()
    }
  }
}
