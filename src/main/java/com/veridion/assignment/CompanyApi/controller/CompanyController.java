package com.veridion.assignment.CompanyApi.controller;

import com.veridion.assignment.CompanyApi.model.CompanyDocument;
import com.veridion.assignment.CompanyApi.model.CompanyRequest;
import com.veridion.assignment.CompanyApi.service.CompanyService;
import com.veridion.assignment.CompanyApi.service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Optional;

@RestController
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    CsvService csvService;

    @Autowired
    CompanyService companyService;

    @GetMapping()
    public ResponseEntity<CompanyDocument> getBestMatch(@RequestBody CompanyRequest companyRequest) {
        return companyService.getBestMatch(companyRequest);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return csvService.processCSV(file);

    }
}