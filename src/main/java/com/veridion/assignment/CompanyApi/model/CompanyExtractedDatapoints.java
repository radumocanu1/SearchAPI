package com.veridion.assignment.CompanyApi.model;

import lombok.Data;

@Data
public class CompanyDatapoints {
    private String domain;
    private String[] phoneNumbers;
    private String[] socialMediaLinks;
    private String[] locations;
}
