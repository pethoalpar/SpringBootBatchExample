# Spring boot batch example

<h3>Add dependency</h3>

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>1.5.8.RELEASE</version>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
    <version>1.5.8.RELEASE</version>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <version>1.5.8.RELEASE</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.43</version>
  </dependency>
</dependencies>
```

<h3>Add database connection. The batch job will creat tables in the database.</h3>

```properties
spring.datasource.url=jdbc:mysql://<url_to_your_database>
spring.datasource.username=<your user name>
spring.datasource.password=<password>
spring.batch.job.enabled=false
```

<h3>Main file</h3>

```java
@SpringBootApplication
@EnableBatchProcessing
public class Main {

    public static void main(String [] args){
        SpringApplication.run(Main.class, args);
    }
}
```

<h2>Create step</h2>

<h3>Reader</h3>

<h5>What is in the before step, is important only when you want to put something in the context. If you don't want, you can delete this.</h5>

```java
public class Reader implements ItemReader<String> {

    private String [] values = {"Monday" , "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private int count = 0;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ExecutionContext executionContext;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution){
        this.executionContext = stepExecution.getExecutionContext();
    }

    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if(count < values.length){
            String str = "Item readed:"+values[count]+"\n";
            logger.info(str);
            if(executionContext.containsKey("message")){
                String message = executionContext.getString("message");
                message+=str;
                executionContext.put("message",message);
            }else {
                executionContext.put("message",str);
            }
            return values[count++];
        }else{
            count = 0;
        }
        return null;
    }
}
```

<h3>Implement process</h3>

```java
public class Process implements ItemProcessor<String, Integer> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ExecutionContext executionContext;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution){
        this.executionContext = stepExecution.getExecutionContext();
    }

    public Integer process(String s) throws Exception {
        String str = "Item processed:"+s+"\n";
        logger.info(str);
        if(executionContext.containsKey("message")){
            String message = executionContext.getString("message");
            message+=str;
            executionContext.put("message",message);
        }else {
            executionContext.put("message",str);
        }
        return s.length();
    }
}
```

<h3>Writer</h3>

```java
public class Writer implements ItemWriter<Integer> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ExecutionContext executionContext;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution){
        this.executionContext = stepExecution.getExecutionContext();
    }

    public void write(List<? extends Integer> list) throws Exception {
        StringBuilder sb = new StringBuilder();
        for(Integer value : list){
            sb.append(value).append(";");
        }

        String str = "Item write:"+sb.toString()+"\n";
        logger.info(str);
        if(executionContext.containsKey("message")){
            String message = executionContext.getString("message");
            message+=str;
            executionContext.put("message",message);
        }else {
            executionContext.put("message",str);
        }
    }
}
```

<h3>Step listener</h3>

```java
public class StepListener implements StepExecutionListener {

    public void beforeStep(StepExecution stepExecution) {

    }

    public ExitStatus afterStep(StepExecution stepExecution) {
        if(stepExecution.getExecutionContext().containsKey("message")){
            return new ExitStatus(ExitStatus.COMPLETED.getExitCode(), stepExecution.getExecutionContext().getString("message"));
        }else{
            return new ExitStatus("Step ended");
        }
    }
}
```

<h3>Job completition listener</h3>

```java
public class JobCompletionListener extends JobExecutionListenerSupport {

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED){
            StringBuilder sb = new StringBuilder();
            for(StepExecution step : jobExecution.getStepExecutions()){
                sb.append(step.getExitStatus().getExitDescription());
            }
            jobExecution.setExitStatus(new ExitStatus(jobExecution.getExitStatus().getExitCode(), sb.toString()));
        }
    }
}
```

<h3>Configure the job</h3>

```java
@Configuration
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job(){
        return jobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer())
                .listener(new JobCompletionListener())
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1(){
        return stepBuilderFactory.get("step1")
                .<String,Integer>chunk(1) // commit interval
                .reader(new Reader())
                .processor(new Process())
                .writer(new Writer())
                .listener(new StepListener())
                .build();
    }
}
```

<h3>Run the job</h3>

```java
@RestController
public class Controller {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @RequestMapping("/run")
    @ResponseBody
    public String run(){
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
        try{
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            return jobExecution.getExitStatus().getExitDescription();

        }catch (Exception e){
            return "Job failed";
        }
    }
}
```
