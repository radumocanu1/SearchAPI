package com.veridion.assignment.CompanyApi.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

@Document(indexName = "companii")
@Data
public class CompanyDocument {
    @Id
    private String domain;

    private String[] phoneNumbers;
    private String[] socialMediaLinks;
    @Field(index = false)
    private String[] locations;
    @Field(name = "company_commercial_name", index = false)
    private String companyCommercialName;
    @Field(name = "company_legal_name", index = false)
    private String companyLegalName;
    @Field(name = "company_all_available_names")
    private String[] companyAllAvailableNames;
}