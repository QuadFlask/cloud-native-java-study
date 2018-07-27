package com.github.quadflask.cnj.messaging

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import javax.sql.DataSource

@Configuration
@EnableBatchProcessing
class BatchConfiguration {
    val log: Log = LogFactory.getLog(javaClass)

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

    @Bean
    fun jdbcTemplate(dataSource: DataSource): JdbcTemplate = JdbcTemplate(dataSource)

    @Bean
    fun job(jobBuilderFactory: JobBuilderFactory,
            stepBuilderFactory: StepBuilderFactory,
            template: JdbcTemplate,
            fileReader: ItemReader<Contact>,
            emailProcessor: ItemProcessor<Contact, Contact>,
            jdbcWriter: ItemWriter<Contact>): Job {

        val setup = stepBuilderFactory.get("clean-contact-table")
                .tasklet { contribution, chunkContext ->
                    template.update("delete from CONTACT")
                    RepeatStatus.FINISHED
                }
                .build()

        val fileToJdbc = stepBuilderFactory.get("file-to-jdbc-fileToJdbc")
                .chunk<Contact, Contact>(5)
                .reader(fileReader)
                .processor(emailProcessor)
                .writer(jdbcWriter)
                .faultTolerant()
                .skip(InvalidEmailException::class.java)
                .skipPolicy { t: Throwable, skipCount: Int ->
                    LogFactory.getLog(javaClass).info("skipping ")
                    t.javaClass.isAssignableFrom(InvalidEmailException::class.java)
                }
                .retry(HttpStatusCodeException::class.java)
                .retryLimit(1)
                .build()

        return jobBuilderFactory.get("etl")
                .start(setup)
                .next(fileToJdbc)
                .build()
    }

    @Bean
    @StepScope
    fun fileReader(@Value("file://#{jobParameters['file']}") pathToFile: Resource, @Value("#{jobParameters}") param: Any, @Value("#{job}") job: Any): FlatFileItemReader<Contact> {
        log.info("fileReader: " + pathToFile.exists())
        log.info("fileReader: " + pathToFile)
        log.info("fileReader: " + param)
        log.info("fileReader: " + job)

        return FlatFileItemReaderBuilder<Contact>().name("file-reader")
                .resource(pathToFile)
                .targetType(Contact::class.java)
                .delimited()
                .names(arrayOf("fullName", "email"))
                .build()
    }

    @Bean
    fun validatingProcessor(emailValidationService: EmailValidationService): ItemProcessor<Contact, Contact> {
        return ItemProcessor { item ->
            val valid = emailValidationService.isEmailValid(item.email)
            item.validEmail = valid
            if (!valid) throw InvalidEmailException(item.email)
            item
        }
    }

    @Bean
    fun jdbcWriter(dataSource: DataSource): JdbcBatchItemWriter<Contact> {
        return JdbcBatchItemWriterBuilder<Contact>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("insert into CONTACT( full_name, email, valid_email ) values ( :fullName, :email, :validEmail )")
                .build()
    }

    @Component
    class SimpleEmailValidationService : EmailValidationService {
        override fun isEmailValid(email: String?): Boolean = StringUtils.hasText(email) && email!!.length > 1 && email.contains("@")
    }
}

interface EmailValidationService {
    fun isEmailValid(email: String?): Boolean
}

class InvalidEmailException(email: String?) : Exception("the email $email isn't valid")
