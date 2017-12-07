package com.pethoalpar.job.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

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
