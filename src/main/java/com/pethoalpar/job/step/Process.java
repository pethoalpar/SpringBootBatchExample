package com.pethoalpar.job.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;

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
