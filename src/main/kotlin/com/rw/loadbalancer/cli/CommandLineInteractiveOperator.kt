package com.rw.loadbalancer.cli

import com.rw.loadbalancer.LoadBalancer
import com.rw.loadbalancer.cli.Logger.logError
import com.rw.loadbalancer.cli.Logger.logInfo
import com.rw.loadbalancer.cli.Logger.logInfoInline
import com.rw.loadbalancer.internal.TestProvider
import com.rw.loadbalancer.provider.ProviderInfo
import com.rw.loadbalancer.registry.RegistrationException
import com.rw.loadbalancer.strategy.random.RandomizedStrategy
import com.rw.loadbalancer.strategy.roundrobin.RoundRobinStrategy
import java.nio.charset.StandardCharsets
import java.util.Scanner
import java.util.UUID
import java.util.regex.Pattern
import kotlin.system.exitProcess

private const val INPUT_WAIT_LOG: String = "Input: "
private const val INVALID_INPUT_ERR_MSG: String = "Invalid input. Try again"
private const val EXIT_COMMAND: String = "exit"
private const val CLEAR_COMMAND: String = "clear"

private const val UUID_REGEX: String = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"

object CommandLineInteractiveOperatorMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val commandLineInteractiveOperator = CommandLineInteractiveOperator()
        commandLineInteractiveOperator.run()
    }
}

class CommandLineInteractiveOperator {
    private lateinit var commandLineInputState: CommandLineInputState
    private val loadBalancerBuilder = LoadBalancer.Builder()
    private var loadBalancer: LoadBalancer? = null
    private val providersMap: MutableMap<String, TestProvider> = HashMap()

    fun run() {
        reset()
        logInfo("RW-LoadBalancer interactive operator.")
        val reader = Scanner(System.`in`, StandardCharsets.US_ASCII)
        reader.use {
            while (true) {
                printOptions()
                val line: String? = reader.nextLine()?.trim()
                if (line.isNullOrEmpty()) {
                    logError(INVALID_INPUT_ERR_MSG)
                } else {
                    handleInput(line)
                }
            }
        }
    }

    private fun handleInput(input: String) {
        if (EXIT_COMMAND == input) {
            doExit()
        }
        if (input == CLEAR_COMMAND) {
            print("\u001b[H\u001b[2J")
            return
        }
        when (commandLineInputState) {
            CommandLineInputState.INIT -> {
                handleInitState(input)
            }
            CommandLineInputState.CONFIGURING_LOAD_BALANCER_STRATEGY -> {
                handleConfiguringStrategyState(input)
            }
            CommandLineInputState.CONFIGURING_LOAD_BALANCER_HEART_BEAT -> {
                handleConfiguringHeartBeatCheckerState(input)
            }
            CommandLineInputState.CONFIGURING_LOAD_BALANCER_MAX_CAPACITY -> {
                handleConfiguringMaxCapacityState(input)
            }
            CommandLineInputState.USING_LOAD_BALANCER -> {
                useLoadBalancer(input)
            }
        }
    }

    private fun handleInitState(input: String) {
        when (input) {
            "init" -> {
                System.setProperty("debug", "false")
                commandLineInputState = CommandLineInputState.CONFIGURING_LOAD_BALANCER_STRATEGY
            }
            "default" -> {
                System.setProperty("debug", "false")
                loadBalancer = loadBalancerBuilder.build()
                commandLineInputState = CommandLineInputState.USING_LOAD_BALANCER
            }
            "init-debug" -> {
                System.setProperty("debug", "true")
                commandLineInputState = CommandLineInputState.CONFIGURING_LOAD_BALANCER_STRATEGY
            }
            "default-debug" -> {
                System.setProperty("debug", "true")
                loadBalancer =
                    loadBalancerBuilder.heartBeatCheckPeriodInMilliSec(10000).maxProviderConcurrency(1).build()
                commandLineInputState = CommandLineInputState.USING_LOAD_BALANCER
            }
            else -> {
                logError(INVALID_INPUT_ERR_MSG)
            }
        }
    }

    private fun useLoadBalancer(input: String) {
        when (input) {
            "list" -> {
                listRegisteredProviders()
            }
            "register" -> {
                registerProvider()
            }
            "call" -> {
                call()
            }
            else -> {
                val pattern = Pattern.compile("^(exclude|include|kill|revive)\\s+($UUID_REGEX)$")
                val matcher = pattern.matcher(input)
                if (matcher.find()) {
                    val action: String = matcher.group(1)
                    val id: String = matcher.group(2)
                    operateProvider(action, id)
                } else {
                    logError(INVALID_INPUT_ERR_MSG)
                }
            }
        }
    }

    private fun handleConfiguringStrategyState(input: String) {
        when (input) {
            "rr" -> {
                loadBalancerBuilder.registryAwareSelectionStrategy(RoundRobinStrategy())
                commandLineInputState = CommandLineInputState.CONFIGURING_LOAD_BALANCER_HEART_BEAT
            }
            "rand" -> {
                loadBalancerBuilder.registryAwareSelectionStrategy(RandomizedStrategy())
                commandLineInputState = CommandLineInputState.CONFIGURING_LOAD_BALANCER_HEART_BEAT
            }
            else -> {
                logError(INVALID_INPUT_ERR_MSG)
            }
        }
    }

    private fun handleConfiguringHeartBeatCheckerState(input: String) {
        try {
            loadBalancerBuilder.heartBeatCheckPeriodInMilliSec(input.toLong())
            commandLineInputState = CommandLineInputState.CONFIGURING_LOAD_BALANCER_MAX_CAPACITY
        } catch (ignore: NumberFormatException) {
            logError(INVALID_INPUT_ERR_MSG)
        }
    }

    private fun handleConfiguringMaxCapacityState(input: String) {
        try {
            loadBalancerBuilder.maxProviderConcurrency(input.toInt())
            commandLineInputState = CommandLineInputState.USING_LOAD_BALANCER
            loadBalancer = loadBalancerBuilder.build()
        } catch (ignore: NumberFormatException) {
            logError(INVALID_INPUT_ERR_MSG)
        }
    }

    private fun operateProvider(action: String, id: String) {
        when (action) {
            "exclude" -> {
                excludeProvider(id)
            }
            "include" -> {
                includeProvider(id)
            }
            "kill" -> {
                killProvider(id)
            }
            "revive" -> {
                reviveProvider(id)
            }
        }
    }

    private fun killProvider(id: String) {
        providersMap[id]?.overrideHealth(false)
    }

    private fun reviveProvider(id: String) {
        providersMap[id]?.overrideHealth(true)
    }

    private fun excludeProvider(id: String) {
        loadBalancer?.deactivateProvider(id)
    }

    private fun includeProvider(id: String) {
        loadBalancer?.reactivateProvider(id)
    }

    private fun call() {
        val completableFuture = loadBalancer?.get()
        completableFuture?.whenComplete { result, ex ->
            if (ex != null) {
                logError(ex)
            } else {
                logInfo("Call was accepted by Provider $result")
            }
        }
    }

    private fun listRegisteredProviders() {
        loadBalancer?.let {
            logInfo("ProviderID\t\t\t\t|\tactive\t|\tstate")
            logInfo("----------------------------------------------------------------------")
            val registeredProviders: List<ProviderInfo> = it.registeredProviders
            registeredProviders.forEach { provider ->
                logInfo("${provider.id}\t|\t${provider.active}\t|\t${provider.state}")
            }
            logInfo("----------------------------------------------------------------------")
        }
    }

    private fun registerProvider() {
        loadBalancer?.let {
            try {
                val provider = TestProvider(UUID.randomUUID().toString())
                val providerId = it.registerProvider(provider)
                providersMap[providerId] = provider
                logInfo("Provider is now registered $providerId")
            } catch (e: RegistrationException) {
                logError(e)
            }
        }
    }

    private fun doExit() {
        loadBalancer?.shutdown()
        exitProcess(0)
    }

    private fun printOptions() {
        when (commandLineInputState) {
            CommandLineInputState.INIT -> {
                logInfo("Options:")
                logInfo("\t init")
                logInfo("\t default")
                logInfo("\t init-debug")
                logInfo("\t default-debug")
                logInfo("\t $CLEAR_COMMAND")
                logInfo("\t $EXIT_COMMAND")
                logInfoInline(INPUT_WAIT_LOG)
            }
            CommandLineInputState.CONFIGURING_LOAD_BALANCER_STRATEGY -> {
                logInfo("Select strategy:")
                logInfo("\t rr")
                logInfo("\t rand")
                logInfoInline(INPUT_WAIT_LOG)
            }
            CommandLineInputState.CONFIGURING_LOAD_BALANCER_HEART_BEAT -> {
                logInfoInline("Enter heart beat check period in millis: ")
            }
            CommandLineInputState.CONFIGURING_LOAD_BALANCER_MAX_CAPACITY -> {
                logInfoInline("Enter max concurrency of one provider: ")
            }
            CommandLineInputState.USING_LOAD_BALANCER -> {
                logInfo("Options:")
                logInfo("\t list")
                logInfo("\t register")
                logInfo("\t call")
                logInfo("\t exclude <provider-id>")
                logInfo("\t include <provider-id>")
                logInfo("\t kill    <provider-id>")
                logInfo("\t revive  <provider-id>")
                logInfoInline(INPUT_WAIT_LOG)
            }
        }
    }

    private fun reset() {
        providersMap.clear()
        loadBalancerBuilder.reset()
        commandLineInputState = CommandLineInputState.INIT
        loadBalancer?.shutdown()
        loadBalancer = null
    }
}

enum class CommandLineInputState {
    INIT,
    CONFIGURING_LOAD_BALANCER_STRATEGY,
    CONFIGURING_LOAD_BALANCER_HEART_BEAT,
    CONFIGURING_LOAD_BALANCER_MAX_CAPACITY,
    USING_LOAD_BALANCER
}
