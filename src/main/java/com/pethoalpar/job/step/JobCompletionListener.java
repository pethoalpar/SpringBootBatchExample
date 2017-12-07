package com.pethoalpar.job.step;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

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
