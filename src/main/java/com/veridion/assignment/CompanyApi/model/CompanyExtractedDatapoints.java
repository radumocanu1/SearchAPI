package com.veridion.assignment.CompanyApi.model;

import lombok.Data;

@Data
public class CompanyExtractedDatapoints {
    private String domain;
    private String[] phoneNumbers;
    private String[] socialMediaLinks;
    private String[] locations;
}
