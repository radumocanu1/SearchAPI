package com.veridion.assignment.CompanyApi.service;

import com.veridion.assignment.CompanyApi.exception.CompanyNotFoundException;
import com.veridion.assignment.CompanyApi.model.CompanyDocument;
import com.veridion.assignment.CompanyApi.model.CompanyExtractedDatapoints;
import com.veridion.assignment.CompanyApi.model.CompanyRequest;
import com.veridion.assignment.CompanyApi.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private FormatterService formatterService;

    @Value("${levenshtein.max.distance}")
    private String levenshteinDistance;

    public ResponseEntity<CompanyDocument> getBestMatch(CompanyRequest companyRequest){
        if (companyRequest.getPhoneNumber() != null)
            companyRequest.setPhoneNumber(formatterService.formatPhoneNumber(companyRequest.getPhoneNumber()));
        if (companyRequest.getName() != null)
            companyRequest.setName(formatterService.formatCompanyName(companyRequest.getName()));
        // will try to get company match with the minimum levenshteinDistance as possible
        int maxLevenshteinDistance = Integer.parseInt(levenshteinDistance);
        Optional<CompanyDocument> companyDocument;
        for (int currentLevenshteinDistance = 0; currentLevenshteinDistance <= maxLevenshteinDistance; currentLevenshteinDistance++)
        {
            companyDocument = tryToGetCompany(companyRequest, currentLevenshteinDistance);
            if (companyDocument.isPresent()){
                return new ResponseEntity<>(companyDocument.get(), HttpStatus.OK);
            }
        }
        throw new CompanyNotFoundException();


    }


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

        if (newData.getPhoneNumbers() != null)
            existingCompany.setPhoneNumbers(formatPhoneNumbers(newData.getPhoneNumbers()));

        existingCompany.setSocialMediaLinks(newData.getSocialMediaLinks());
        existingCompany.setLocations(newData.getLocations());

    }
    private String[] formatPhoneNumbers(String[] oldNumbers){
        List<String> formattedNumbers = new ArrayList<>();
        for(String oldNum: oldNumbers){
            formattedNumbers.add(formatterService.formatPhoneNumber(oldNum));
        }
        return formattedNumbers.toArray(new String[0]);
    }
    private Optional<CompanyDocument> tryToGetCompany(CompanyRequest companyRequest,int levenshteinDistance){
        List<CompanyDocument> companies;
        if (companyRequest.getName() != null){
            companies = companyRepository.findByCompanyAllAvailableNamesWith(companyRequest.getName(), levenshteinDistance);
            if (!companies.isEmpty())
                return Optional.of(companies.get(0));
        }
        if (companyRequest.getWebsite() != null){
            companies = companyRepository.findByDomainWithFuzzy(companyRequest.getWebsite(), levenshteinDistance);
            if (!companies.isEmpty())
                return Optional.of(companies.get(0));
        }
        if (companyRequest.getPhoneNumber() != null) {
            companies = companyRepository.findByPhoneNumberWithFuzzy(companyRequest.getPhoneNumber(), levenshteinDistance);
            if (!companies.isEmpty())
                return Optional.of(companies.get(0));
        }
        if (companyRequest.getFacebookProfile() != null){
            companies = companyRepository.findBySocialMediaLinksWithFuzzy(companyRequest.getFacebookProfile(), levenshteinDistance);
            if (!companies.isEmpty())
                return Optional.of(companies.get(0));
        }
        return Optional.empty();
    }

}

