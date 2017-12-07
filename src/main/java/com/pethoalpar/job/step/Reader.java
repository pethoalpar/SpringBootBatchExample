package com.pethoalpar.job.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.*;

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
