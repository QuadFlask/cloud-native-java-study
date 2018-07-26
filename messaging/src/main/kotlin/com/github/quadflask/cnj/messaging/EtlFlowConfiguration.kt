package com.github.quadflask.cnj.messaging

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.integration.launch.JobLaunchRequest
import org.springframework.batch.integration.launch.JobLaunchingGateway
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.core.MessageSelector
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.file.FileHeaders
import org.springframework.integration.file.dsl.Files
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import java.io.File

@Configuration
class EtlFlowConfiguration {
    @Bean
    fun etlFlow(@Value("\${input-directory:\${HOME}/Desktop/in}") dir: File, c: BatchChannels, launcher: JobLauncher, job: Job): IntegrationFlow = IntegrationFlows
            .from(Files.inboundAdapter(dir).autoCreateDirectory(true)) { cs ->
                cs.poller { p -> p.fixedRate(1000) }
            }
            .handle(File::class.java) { file, headers ->
                val absolutePath = file.absolutePath
                val params = JobParametersBuilder().addString("file", absolutePath).toJobParameters()

                MessageBuilder.withPayload(JobLaunchRequest(job, params))
                        .setHeader(FileHeaders.ORIGINAL_FILE, absolutePath)
                        .copyHeadersIfAbsent(headers)
                        .build()
            }
            .handle(JobLaunchingGateway(launcher))
            .routeToRecipients { spec ->
                spec.recipient(c.invalid(), MessageSelector { notFinished(it) })
                        .recipient(c.completed(), MessageSelector { finished(it) })
            }.get()

    fun finished(msg: Message<*>): Boolean = JobExecution::class.java.cast(msg.payload).exitStatus == ExitStatus.COMPLETED

    fun notFinished(msg: Message<*>): Boolean = !this.finished(msg)
}