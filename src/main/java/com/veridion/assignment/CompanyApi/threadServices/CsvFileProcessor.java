package com.veridion.assignment.CompanyApi.threadServices;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

@Component
@Log
public class CsvFileProcessor implements Runnable {

    private final String inputDir;

    private final int threadSleepInterval;
    PythonScriptCaller pythonScriptCaller;

    public CsvFileProcessor(@Value("${file.processing.directory}") String inputDir, @Value("${processing.threads.sleep.interval}") int threadSleepInterval, PythonScriptCaller pythonScriptCaller) {
        this.inputDir = inputDir;
        this.threadSleepInterval = threadSleepInterval;
        this.pythonScriptCaller = pythonScriptCaller;
    }

    @SneakyThrows
    @Override
    public void run(){
        while (true){
            File inputFolder = new File(System.getProperty("user.dir") + inputDir);
            FilenameFilter filter = (dir, name) -> name.endsWith(".csv");
            File[] csvFiles = inputFolder.listFiles(filter);
            if (csvFiles != null &&  csvFiles.length > 0) {
                File file_to_process = csvFiles[0];
                // scrape the csv
                log.info("Starting to scrape domains...");
                pythonScriptCaller.callScrapingScript(String.valueOf(file_to_process));
                log.info("Scraping script finished");
                // after the domains were scraped, rename the file
                String newFileName = file_to_process.getName().replace(".csv", ".csv." + getCurrentTimestamp() + ".processed");
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
