package com.github.quadflask.cnj.messaging

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.integration.core.MessageSelector
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.channel.MessageChannels
import org.springframework.integration.file.dsl.Files
import org.springframework.messaging.MessageChannel
import java.io.File

@Profile("simple-integration")
@Configuration
class IntegrationConfiguration {
    val log: Log = LogFactory.getLog(javaClass)

    @Bean
    fun etlFlow(@Value("\${input-directory:\${HOME}/Desktop/in}") dir: File): IntegrationFlow = IntegrationFlows
            .from(Files.inboundAdapter(dir).autoCreateDirectory(true)) {
                it.poller { spec -> spec.fixedRate(1000) }
            }
            .handle(File::class.java) { file, _ ->
                log.info("we noticed a new file, $file")
                file
            }
            .routeToRecipients { spec ->
                spec.recipient(csv(), MessageSelector { msg -> hasExt(msg.payload, ".csv") })
                        .recipient(txt(), MessageSelector { msg -> hasExt(msg.payload, ".txt") })
            }.get()

    fun hasExt(f: Any, ext: String): Boolean = (f as? File)?.hasExt(ext) ?: false

    @Bean
    fun txt(): MessageChannel = MessageChannels.direct().get()

    @Bean
    fun csv(): MessageChannel = MessageChannels.direct().get()

    @Bean
    fun txtFlow(): IntegrationFlow = IntegrationFlows.from(txt()).handle(File::class.java) { _, _ ->
        log.info("file is .txt!")
        null
    }.get()

    @Bean
    fun csvFlow(): IntegrationFlow = IntegrationFlows.from(csv()).handle(File::class.java) { _, _ ->
        log.info("file is .csv!")
        null
    }.get()
}

fun File.hasExt(ext: String): Boolean = this.name.toLowerCase().endsWith(ext.toLowerCase())