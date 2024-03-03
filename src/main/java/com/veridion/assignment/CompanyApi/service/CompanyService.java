package com.veridion.assignment.CompanyApi.service;

import com.veridion.assignment.CompanyApi.model.CompanyDocument;
import com.veridion.assignment.CompanyApi.model.CompanyExtractedDatapoints;
import com.veridion.assignment.CompanyApi.repository.CompanyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Service
@AllArgsConstructor
public class CompanyService {
    private CompanyRepository companyRepository;

    public void updateCompaniesDocumentsFromCsv(File csvFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // jump over header line
            br.readLine();
            while ((line = br.readLine()) != null) {
                CompanyExtractedDatapoints data = parseCsvLine(line);

                CompanyDocument existingCompany = companyRepository.findById(data.getDomain()).orElse(null);

                // extra validation, this should be always true
                if (existingCompany != null) {
                    updateCompanyData(existingCompany, data);
                    companyRepository.save(existingCompany);
                }
            }
        }
    }

    private CompanyExtractedDatapoints parseCsvLine(String line) {
        String[] data = line.split(",");
        CompanyExtractedDatapoints result = new CompanyExtractedDatapoints();
        result.setDomain(data[0]);

        if (data.length > 1 && !data[1].isEmpty()) {
            result.setPhoneNumbers(data[1].split("\\|"));
        }

        if (data.length > 2 && !data[2].isEmpty()) {
            result.setSocialMediaLinks(data[2].split("\\|"));
        }

        if (data.length > 3 && !data[3].isEmpty()) {
            result.setLocations(data[3].split("\\|"));
        }

        return result;
    }

    private void updateCompanyData(CompanyDocument existingCompany, CompanyExtractedDatapoints newData) {
        // set new fields
        existingCompany.setPhoneNumbers(newData.getPhoneNumbers());
        existingCompany.setSocialMediaLinks(newData.getSocialMediaLinks());
        existingCompany.setLocations(newData.getLocations());
        // set old ones
    }

}

