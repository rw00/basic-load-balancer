package com.rw.loadbalancer.cli

import com.rw.loadbalancer.LoadBalancer
import com.rw.loadbalancer.NoAvailableProvidersException
import com.rw.loadbalancer.cli.Logger.logError
import com.rw.loadbalancer.cli.Logger.logInfo
import com.rw.loadbalancer.cli.Logger.logInfoInline
import com.rw.loadbalancer.provider.ProviderInfo
import com.rw.loadbalancer.provider.TestUuidProvider
import com.rw.loadbalancer.registry.RegistrationException
import com.rw.loadbalancer.strategy.random.RandomizedStrategy
import com.rw.loadbalancer.strategy.roundrobin.RoundRobinStrategy
import java.util.Scanner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern
import kotlin.system.exitProcess

private const val INPUT_WAIT_LOG: String = "Input: "
private const val INVALID_INPUT_ERR_MSG: String = "Invalid input. Try again"
private const val EXIT_COMMAND: String = "exit"

private const val UUID_REGEX: String = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"


@Suppress("unused")
fun main(args: Array<String>) {
    val commandLineInteractiveOperator = CommandLineInteractiveOperator()
    commandLineInteractiveOperator.run()
}

class CommandLineInteractiveOperator {
    private lateinit var commandLineInputState: CommandLineInputState
    private val loadBalancerBuilder = LoadBalancer.Builder()
    private var loadBalancer: LoadBalancer? = null
    private val providersMap: MutableMap<String, TestUuidProvider> = HashMap()
    private val backgroundCallingService: ExecutorService = Executors.newCachedThreadPool()

    fun run() {
        reset()
        logInfo("RW-LoadBalancer interactive operator.")
        val reader = Scanner(System.`in`)
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
            CommandLineInputState.USING_LOAD_BALANCER -> {
                useLoadBalancer(input)
            }
        }
    }

    private fun handleInitState(input: String) {
        when (input) {
            "init" -> {
                commandLineInputState = CommandLineInputState.CONFIGURING_LOAD_BALANCER_STRATEGY
            }
            "init-debug" -> {
                System.setProperty("debug", "true")
                commandLineInputState = CommandLineInputState.CONFIGURING_LOAD_BALANCER_STRATEGY
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
                }
            }
        }
    }

    private fun handleConfiguringStrategyState(input: String) {
        when (input) {
            "rr" -> {
                loadBalancerBuilder.registryAwareStrategy(RoundRobinStrategy())
                commandLineInputState = CommandLineInputState.CONFIGURING_LOAD_BALANCER_HEART_BEAT
            }
            "rand" -> {
                loadBalancerBuilder.registryAwareStrategy(RandomizedStrategy())
                commandLineInputState = CommandLineInputState.CONFIGURING_LOAD_BALANCER_HEART_BEAT
            }
            else -> {
                logError(INVALID_INPUT_ERR_MSG)
            }
        }
    }

    private fun handleConfiguringHeartBeatCheckerState(input: String) {
        try {
            loadBalancerBuilder.heartBeatCheckPeriodInMilliseconds(input.toLong())
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

    private fun excludeProvider(id: String) {
        loadBalancer?.deactivateProvider(id)
    }

    private fun includeProvider(id: String) {
        loadBalancer?.reactivateProvider(id)
    }

    private fun killProvider(id: String) {
        providersMap[id]?.overrideHealth(false)
    }

    private fun reviveProvider(id: String) {
        providersMap[id]?.overrideHealth(true)
    }

    private fun call() {
        backgroundCallingService.submit {
            try {
                logInfo("Call was accepted by Provider ${loadBalancer?.get()}")
            } catch (e: NoAvailableProvidersException) {
                logError(e)
            }
        }
    }

    private fun listRegisteredProviders() {
        loadBalancer?.let {
            val registeredProviders: List<ProviderInfo> = it.listRegisteredProviders()
            logInfo("ProviderID\t|\tactive\t|state")
            registeredProviders.forEach { provider ->
                logInfo("${provider.id}\t|\t${provider.active}\t|${provider.state}")
            }
        }
    }

    private fun registerProvider() {
        loadBalancer?.let {
            try {
                val provider = TestUuidProvider()
                val providerId = it.registerProvider(provider)
                providersMap[providerId] = provider
                logInfo("Provider is now registered $providerId")
            } catch (e: RegistrationException) {
                logError(e)
            }
        }
    }

    private fun doExit() {
        backgroundCallingService.shutdown()
        loadBalancer?.shutdown()
        exitProcess(0)
    }

    private fun printOptions() {
        when (commandLineInputState) {
            CommandLineInputState.INIT -> {
                logInfo("Options:")
                logInfo("\t\tinit")
                logInfo("\t\tinit-debug")
                logInfo("\t\t$EXIT_COMMAND")
                logInfoInline(INPUT_WAIT_LOG)
            }
            CommandLineInputState.CONFIGURING_LOAD_BALANCER_STRATEGY -> {
                logInfo("Select strategy:")
                logInfo("\t\trr")
                logInfo("\t\trand")
                logInfoInline(INPUT_WAIT_LOG)
            }
            CommandLineInputState.CONFIGURING_LOAD_BALANCER_HEART_BEAT -> {
                logInfoInline("Enter an integer representing the heart beat check period in milliseconds: ")
            }
            CommandLineInputState.USING_LOAD_BALANCER -> {
                logInfo("Options:")
                logInfo("\t\tlist")
                logInfo("\t\tregister")
                logInfo("\t\tcall")
                logInfo("\t\texclude <provider-id>")
                logInfo("\t\tinclude <provider-id>")
                logInfo("\t\tkill    <provider-id>")
                logInfo("\t\trevive  <provider-id>")
                logInfoInline(INPUT_WAIT_LOG)
            }
        }
    }

    private fun reset() {
        commandLineInputState = CommandLineInputState.INIT
        providersMap.clear()
        loadBalancer?.shutdown()
        loadBalancer = null
    }
}

enum class CommandLineInputState {
    INIT, CONFIGURING_LOAD_BALANCER_STRATEGY, CONFIGURING_LOAD_BALANCER_HEART_BEAT, USING_LOAD_BALANCER
}
