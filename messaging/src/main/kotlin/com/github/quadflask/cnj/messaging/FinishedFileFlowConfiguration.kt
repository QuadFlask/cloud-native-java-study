package com.github.quadflask.cnj.messaging

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.batch.core.JobExecution
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.file.FileHeaders
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.Assert
import java.io.File
import java.nio.file.Files

import java.nio.file.StandardCopyOption.REPLACE_EXISTING

@Configuration
class FinishedFileFlowConfiguration {
    val log: Log = LogFactory.getLog(javaClass)

    @Bean
    fun finishedJobsFlow(channels: BatchChannels, @Value("\${input-directory:\${HOME}/Desktop/completed}") finished: File, jdbcTemplate: JdbcTemplate): IntegrationFlow =
            IntegrationFlows.from(channels.completed())
                    .handle(JobExecution::class.java) { _, headers ->
                        val ogFileName = headers[FileHeaders.ORIGINAL_FILE].toString()
                        val file = File(ogFileName)

                        mv(file, finished)

                        val contacts = jdbcTemplate.query("select * from CONTACT") { rs, _ ->
                            Contact(
                                    rs.getString("full_name"),
                                    rs.getString("email"),
                                    rs.getBoolean("valid_email"),
                                    rs.getLong("id"))
                        }
                        contacts.forEach(log::info)
                    }
                    .get()
}