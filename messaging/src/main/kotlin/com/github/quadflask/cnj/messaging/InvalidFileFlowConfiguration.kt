package com.github.quadflask.cnj.messaging

import org.springframework.batch.core.JobExecution
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.file.FileHeaders
import java.io.File

@Configuration
class InvalidFileFlowConfiguration {
    @Bean
    fun invalidFileFlow(channels: BatchChannels, @Value("\${input-directory:\${HOME}/Desktop/errors}") errors: File): IntegrationFlow =
            IntegrationFlows.from(channels.invalid())
                    .handle(JobExecution::class.java) { _, headers ->
                        val ogFileName = headers[FileHeaders.ORIGINAL_FILE].toString()
                        val file = File(ogFileName)

                        mv(file, errors)
                    }
                    .get()
}