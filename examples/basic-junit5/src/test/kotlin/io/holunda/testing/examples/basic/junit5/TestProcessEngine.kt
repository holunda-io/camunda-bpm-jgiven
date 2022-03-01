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
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream


enum class TestProcessEngine {
    ;

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

        fun preInit(preInit: Consumer<ProcessEngineConfigurationImpl>): Builder {
            plugin(createPlugin(preInit, null, null))
            return this
        }

        fun postInit(postInit: Consumer<ProcessEngineConfigurationImpl>): Builder {
            plugin(createPlugin(null, postInit, null))
            return this
        }

        fun postProcessEngineBuild(postProcessEngineBuild: Consumer<ProcessEngine>): Builder {
            plugin(createPlugin(null, null, postProcessEngineBuild))
            return this
        }

        fun withDefaultSerializationFormat(defaultSerializationFormat: String): Builder {
            configuration.defaultSerializationFormat = defaultSerializationFormat
            return this
        }

        fun plugin(plugin: ProcessEnginePlugin): Builder {
            configuration.processEnginePlugins.add(plugin)
            return this
        }

        fun withH2MemNameDefault(): Builder {
            return withH2MemName("camunda")
        }

        fun withH2MemName(databaseName: String): Builder {
            preInit {
                it.jdbcUrl = String.format(
                    "jdbc:h2:mem:%s;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
                    databaseName
                )
            }
            return this
        }

        fun engine(): ProcessEngineImpl {
            return configuration.buildProcessEngine() as ProcessEngineImpl
        }

        fun extension(): ProcessEngineExtension {
            return ProcessEngineExtension.builder().useProcessEngine(engine()).build()
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

        fun builder(): Builder {
            return Builder()
        }

        fun extension(): ProcessEngineExtension {
            return builder().extension()
        }
    }
}
