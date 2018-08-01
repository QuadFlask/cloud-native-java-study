# Chapter 11 배치 처리와 태스크
> 컴퓨터 자원을 효과적으로 사용하기 위해 입력된 값들을 일괄로 한꺼번에 처리하는 방법


---

### 배치 처리

대량의 데이터를 처리할때 매우 효율적
적절한 데이터 단위로 나누어야함
병렬 처리
논리적 데이터 단위(윈도우)

---

# 스프링 배치

로깅/추적, 트랜젝션 관리, 작업 처리 지표, 작업 재시작, 작업 무시, 자원 관리

* 441page, 그림 11-1

```js
                               +-------------+
                            +--+ ItemReader  |
                            |  +-------------+
                            |
+-----------+ +---+ +----+  |  +-------------+
|JobOperator+-+Job+-+Step+--+--+ItemProcessor|
+-^---------+ +-^-+ +--^-+  |  +-------------+
  |             |      |    |
+-v-------------v------v-+  |  +-------------+
|      JobRepository     |  +--+ ItemWriter  |
+------------------------+     +-------------+

```
http://asciiflow.com/

---

- `ItemReader`
  외부로부터 입력을 받고 `Job` 내부에서 논리적으로 처리할 수 있는 `Item`으로 변환
  한번에 하나의 Item을 읽고 지정된 청크 사이즈만큼의 버퍼를 사용해 결과를 누적

- `ItemProcessor`
  `ItemReader`로부터 `Item`을 공급 받아 변환 수행(일반적인 비즈니스 로직 처리, 데이터 검증, 저장같은 작업)

- `ItemWriter`
  `ItemReader`/`ItemProcessor`로부터 누적된 전체 `Item`을 받음(지정된 청크 사이즈 만큼)

---

# 첫번째 배치 작업

```java
@Bean
    fun etl(jbf: JobBuilderFactory, sbf: StepBuilderFactory, step1: Step1Configuration, step2: Step2Configuration, step3: Step3Configuration): Job {
        val setup = sbf.get("clean-contact-table")
                .tasklet(step1.tasklet(null))
                .build()

        val s2 = sbf.get("file-db")
                .chunk<Person, Person>(1000)
                .faultTolerant()
                .skip(InvalidEmailException::class.java)
                .reader(step2.fileReader(null))
                .processor(step2.emailValidatingProcessor(null))
                .writer(step2.jdbcWriter(null))
                .build()

        val s3 = sbf.get("db-file")
                .chunk<Map<Int, Int>, Map<Int, Int>>(100)
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
```

--- 