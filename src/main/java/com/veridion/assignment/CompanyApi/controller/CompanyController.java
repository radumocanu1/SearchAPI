package com.veridion.assignment.CompanyApi.controller;

import com.veridion.assignment.CompanyApi.model.CompanyDTO;
import com.veridion.assignment.CompanyApi.model.CompanyRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/company")
public class CompanyController {
    @Value("${file.processing.directory}")
    String inputCsvDir;

    @GetMapping()
    public String getBestMatch(@RequestBody CompanyRequest companyRequest) {
        System.out.println(companyRequest.toString());
        return new CompanyDTO().toString();
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Fișierul este gol!";
        }

        try {
            Path dirPath = Paths.get(System.getProperty("user.dir") + inputCsvDir);

            // Obține numele fișierului original
            String fileName = file.getOriginalFilename();

            // Concatenează calea completă cu numele fișierului pentru a obține calea completă a fișierului
            Path filePath = Paths.get(dirPath.toString(), fileName);

            // Salvează fișierul la calea specificată
            Files.write(filePath, file.getBytes());


            return "AM adaugat";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}