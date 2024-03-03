package com.veridion.assignment.CompanyApi.threadServices;

import lombok.AllArgsConstructor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class ThreadRunner {

    CsvFileProcessor csvFileProcessor;

    ExtractedDatapointsFileProcessor extractedDatapointsFileProcessor;
    private final TaskExecutor taskExecutor;


    public  void startCsvReaderThread() {
        taskExecutor.execute(csvFileProcessor);
    }

    public void startDatapointsReaderThread(){
        taskExecutor.execute(extractedDatapointsFileProcessor);
    }
}
