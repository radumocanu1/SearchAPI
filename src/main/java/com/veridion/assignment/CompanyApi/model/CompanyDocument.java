package com.veridion.assignment.CompanyApi.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "companii")
@Data
public class ScrapedCompanyDocument {
    @Id
    private String domain;

    private String phoneNumber;
    private String socialMediaLinks;
    private String locations;

}