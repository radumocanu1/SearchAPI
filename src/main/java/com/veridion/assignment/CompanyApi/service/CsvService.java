package com.veridion.assignment.CompanyApi.service;

import com.veridion.assignment.CompanyApi.ApplicationConstants;
import com.veridion.assignment.CompanyApi.exception.CsvValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Service
public class CsvService {
    @Value("${file.processing.directory}")
    String inputCsvDir;
    public ResponseEntity<String> processCSV(MultipartFile file){
        if (isValidCSV(file)) {
            try {
                Path dirPath = Paths.get(System.getProperty("user.dir") + inputCsvDir);
                String fileName = file.getOriginalFilename();
                Path filePath = Paths.get(dirPath.toString(), fileName);
                Files.write(filePath, file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
            else
                throw new CsvValidationException();



            return new ResponseEntity<>(ApplicationConstants.CSV_FILE_ACCEPTED, HttpStatus.CREATED);

    }

    private boolean isValidCSV(MultipartFile file) {
        if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
            System.out.println(file.getOriginalFilename());
            return false;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String header = reader.readLine();
            return "domain".equals(header);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
