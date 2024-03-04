package com.veridion.assignment.CompanyApi.service;


import org.springframework.stereotype.Service;

@Service
public class FormatterService {
    public  String formatPhoneNumber(String phoneNumber) {
        // remove all non special chars
        return phoneNumber.replaceAll("[^0-9]", "");
    }
    public String formatCompanyName (String companyName) {
        return companyName.toLowerCase();
    }
}
