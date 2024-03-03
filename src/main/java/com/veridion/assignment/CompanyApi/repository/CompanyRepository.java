package com.veridion.assignment.CompanyApi.repository;

import com.veridion.assignment.CompanyApi.model.CompanyDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CompanyRepository extends ElasticsearchRepository<CompanyDocument, String> {


}
