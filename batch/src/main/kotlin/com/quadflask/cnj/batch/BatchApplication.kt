package com.quadflask.cnj.batch

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.batch.item.file.transform.DelimitedLineAggregator
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.io.File
import java.util.*
import javax.sql.DataSource

@SpringBootApplication
@EnableBatchProcessing
class BatchApplication(val env: Environment) {
    @Bean
    fun restTemplate() = RestTemplate()

    @Bean
    fun jdbcTemplate(dataSource: DataSource) = JdbcTemplate(dataSource)

    @Bean
    fun run(launcher: JobLauncher, job: Job, @Value("\${user.dir}/batch/src/main/resources") home: String): CommandLineRunner {
        return CommandLineRunner {
            launcher.run(job, JobParametersBuilder()
                    .addString("input", path(home, "in.csv"))
                    .addString("output", path(home, "out.csv"))
                    .toJobParameters())
        }
    }

    fun path(home: String, fileName: String): String = File(home, fileName).absolutePath
}

typealias IntMap = Map<Int, Int>

fun main(args: Array<String>) {
    runApplication<BatchApplication>(*args)
}

@Configuration
class BatchConfiguration {

    @Bean
    fun etl(jbf: JobBuilderFactory, sbf: StepBuilderFactory, step1: Step1Configuration, step2: Step2Configuration, step3: Step3Configuration): Job {
        val setup = sbf.get("clean-contact-table")
                .tasklet(step1.tasklet(null))
                .build()

        val s2 = sbf.get("file-db")
                .chunk<Person, Person>(1000)
                .faultTolerant()
                .skip(InvalidEmailException::class.java)
                .retry(HttpStatusCodeException::class.java)
                .retryLimit(2)
                .reader(step2.fileReader(null))
                .processor(step2.emailValidatingProcessor(null))
                .writer(step2.jdbcWriter(null))
                .build()

        val s3 = sbf.get("db-file")
                .chunk<IntMap, IntMap>(100)
                .reader(step3.jdbcReader(null))
                .writer(step3.fileWriter(null))
                .build()

        return jbf.get("etl")
                .incrementer(RunIdIncrementer())
                .start(setup)
                .next(s2)
                .next(s3)
                .build()
    }
}

@Configuration
class Step1Configuration {
    val log: Log = LogFactory.getLog(javaClass)

    @Bean
    fun tasklet(jdbcTemplate: JdbcTemplate?): Tasklet = Tasklet { _, _ ->
        log.info("starting the ETL job.")
        jdbcTemplate!!.update("delete from PEOPLE")
        RepeatStatus.FINISHED
    }
}

@Configuration
class Step2Configuration {
    @Bean
    @StepScope
    fun fileReader(@Value("file://#{jobParameters['input']}") input: Resource?): FlatFileItemReader<Person> = FlatFileItemReaderBuilder<Person>()
            .name("file-reader")
            .resource(input)
            .targetType(Person::class.java)
            .delimited()
            .delimiter(",")
            .names(arrayOf("firstName", "age", "email"))
            .build()

    @Bean
    fun emailValidatingProcessor(emailValidationService: EmailValidationService?): ItemProcessor<Person, Person> = ItemProcessor { item ->
        val email = item.email
        if (!emailValidationService!!.isEmailValid(email)) throw InvalidEmailException(email)
        item
    }

    @Bean
    fun jdbcWriter(ds: DataSource?): JdbcBatchItemWriter<Person> = JdbcBatchItemWriterBuilder<Person>()
            .dataSource(ds)
            .sql("insert into PEOPLE(AGE,FIRST_NAME,EMAIL) values(:age,:firstName,:email)")
            .beanMapped()
            .build()
}

@Configuration
class Step3Configuration {
    @Bean
    fun jdbcReader(dataSource: DataSource?): JdbcCursorItemReader<IntMap> = JdbcCursorItemReaderBuilder<IntMap>()
            .name("jdbc-reader")
            .dataSource(dataSource)
            .sql("select COUNT(age) c, age a from PEOPLE group by age")
            .rowMapper { rs, _ ->
                Collections.singletonMap(rs.getInt("a"), rs.getInt("c"))
            }
            .build()

    @Bean
    @StepScope
    fun fileWriter(@Value("file://#{jobParameters['output']}") output: Resource?): FlatFileItemWriter<IntMap> = FlatFileItemWriterBuilder<IntMap>()
            .name("file-writer")
            .resource(output)
            .lineAggregator(DelimitedLineAggregator<IntMap>().apply {
                setDelimiter(",")
                setFieldExtractor { ageAndCount ->
                    val next = ageAndCount.entries.iterator().next()
                    arrayOf(next.key, next.value)
                }
            })
            .build()
}

data class Person(var firstName: String = "", var age: Int = 0, var email: String = "")

interface EmailValidationService {
    fun isEmailValid(email: String?): Boolean
}

@Component
class SimpleEmailValidationService : EmailValidationService {
    override fun isEmailValid(email: String?): Boolean = StringUtils.hasText(email) && email!!.length > 1 && email.contains("@")
}