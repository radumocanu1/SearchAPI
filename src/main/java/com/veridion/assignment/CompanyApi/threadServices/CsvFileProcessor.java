package com.veridion.assignment.CompanyApi.threadServices;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

@Component
public class FileProcessor implements Runnable {


    private final String inputDir;
    PythonScriptCaller pythonScriptCaller;

    public FileProcessor(@Value("${file.processing.directory}") String inputDir, PythonScriptCaller pythonScriptCaller) {
        this.inputDir = inputDir;
        this.pythonScriptCaller = pythonScriptCaller;
    }

    @SneakyThrows
    @Override
    public void run(){
        workerFunctionForCsvReaderThread();


    }

    private void workerFunctionForCsvReaderThread() throws IOException, InterruptedException {
        while (true){
            File inputFolder = new File(System.getProperty("user.dir") + inputDir);
            FilenameFilter filter = (dir, name) -> name.endsWith(".csv");
            File[] csvFiles = inputFolder.listFiles(filter);
            System.out.println("caut...");
            if (csvFiles != null &&  csvFiles.length > 0) {
                System.out.println("am gasit!");
                File file_to_process = csvFiles[0];
                // scrape the csv
                System.out.println("Scriptul a inceput");
                pythonScriptCaller.callScrapingScript(String.valueOf(file_to_process));
                System.out.println("s-a terminat");
                // after the domains were scraped, rename the file
                String newFileName = file_to_process.getName().replace(".csv", ".csv." + getCurrentTimestamp() + ".processed");
                File newFile = new File(inputFolder, newFileName);
                file_to_process.renameTo(newFile);
            }
            Thread.sleep(30000);

        }
    }

    private String getCurrentTimestamp(){
        Instant timestamp = Instant.now();
        Date date = Date.from(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        return sdf.format(date);
    }

}
