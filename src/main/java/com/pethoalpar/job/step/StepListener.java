package com.pethoalpar.job.step;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

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
