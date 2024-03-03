package com.veridion.assignment.CompanyApi.threadServices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class PythonScriptCaller {
    @Value("${python.scraping.threads}")
    private String number_of_threads;
    @Value("${python.scriptPath}")
    private String python_script_relative_path;
    @Value("${python.interpreter}")
    private String python_interpreter;

    public void callScrapingScript(String csvFilePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(python_interpreter,System.getProperty("user.dir") + python_script_relative_path, "-t", number_of_threads, "-f", csvFilePath);
        Process process = processBuilder.start();
        // wait for script to finish
        process.waitFor();


    }
}
