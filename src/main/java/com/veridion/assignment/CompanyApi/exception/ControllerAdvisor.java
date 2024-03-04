package com.veridion.assignment.CompanyApi.exception;

import com.veridion.assignment.CompanyApi.ApplicationConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler(CsvValidationException.class)
    public ResponseEntity<String> handleCsvValidationException(CsvValidationException ex) {
        return new ResponseEntity<>(ApplicationConstants.CSV_FILE_NOT_VALID, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<String> handleCompanyNotFoundException(CompanyNotFoundException ex) {
        return new ResponseEntity<>(ApplicationConstants.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
