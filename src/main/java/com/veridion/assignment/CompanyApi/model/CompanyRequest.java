package com.veridion.assignment.CompanyApi.model;

import lombok.Data;

@Data
public class CompanyRequest {
    String name;
    String website;
    String phone_number;
    String facebook_profile;
}
