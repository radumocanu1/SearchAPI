package com.veridion.assignment.CompanyApi.threadServices;

import com.veridion.assignment.CompanyApi.service.CompanyService;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

@Component
@Log
public class ExtractedDatapointsFileProcessor implements Runnable{
    private final String datapointsDir;

    private CompanyService companyService;

    private final int threadSleepInterval;

    public ExtractedDatapointsFileProcessor(@Value("${extracted.datapoints.file.directory}") String datapointsDir, @Value("${processing.threads.sleep.interval}") int threadSleepInterval, CompanyService companyService ) {
        this.datapointsDir = datapointsDir;
        this.threadSleepInterval = threadSleepInterval;
        this.companyService = companyService;
    }

    @SneakyThrows
    @Override
    public void run(){
        while (true){
            File inputFolder = new File(System.getProperty("user.dir") + datapointsDir);
            FilenameFilter filter = (dir, name) -> name.startsWith("data");
            File[] csvFiles = inputFolder.listFiles(filter);
            if (csvFiles != null &&  csvFiles.length > 0) {
                log.info("Starting to process datapoints csv...");
                File file_to_process = csvFiles[0];
                // update elastic document with new datapoints
                companyService.updateCompaniesDocumentsFromCsv(file_to_process);
                log.info("Finished adding datapoints to elastic...");
                // after the datapoints were added to elastic, rename the file
                String newFileName = file_to_process.getName().replace("data", "processed");
                File newFile = new File(inputFolder, newFileName);
                file_to_process.renameTo(newFile);
            }
            Thread.sleep(threadSleepInterval * 1000L);

        }


    }

    private String getCurrentTimestamp(){
        Instant timestamp = Instant.now();
        Date date = Date.from(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        return sdf.format(date);
    }

}
