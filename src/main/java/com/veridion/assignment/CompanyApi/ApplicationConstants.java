package com.veridion.assignment.CompanyApi;

public class ApplicationConstants {
    public static String CSV_FILE_ACCEPTED = "Csv file uploaded successfully, the domains will start to be scraped...";
    public static String CSV_FILE_NOT_VALID = "The uploaded csv file does not meet the validation requirements! (Note: Please verify that the file header is \"domain\")";

    public static String COMPANY_NOT_FOUND = "No company could be found with the provided data... You can try \n 1. Increasing levenshtein distance \n 2. Scrape with --in-depth flag enabled";


}
