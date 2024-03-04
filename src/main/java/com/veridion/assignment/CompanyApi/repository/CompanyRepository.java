package com.veridion.assignment.CompanyApi.repository;


import com.veridion.assignment.CompanyApi.model.CompanyDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CompanyRepository extends ElasticsearchRepository<CompanyDocument, String> {

    @Query("{\"fuzzy\": {\"phoneNumbers\": {\"value\": \"?0\", \"fuzziness\": ?1}}}")
    List<CompanyDocument> findByPhoneNumberWithFuzzy(String phoneNumber, int fuzziness);


    @Query("{\"match\": {\"company_all_available_names\": {\"query\": \"?0\", \"fuzziness\": ?1}}}")
    List<CompanyDocument> findByCompanyAllAvailableNamesWith(String companyName, int fuzziness);


    @Query("{\"match\": {\"socialMediaLinks\": {\"query\": \"?0\", \"fuzziness\": ?1}}}")
    List<CompanyDocument> findBySocialMediaLinksWithFuzzy(String socialMediaLinks, int fuzziness);

    @Query("{\"fuzzy\": {\"domain\": {\"value\": \"?0\", \"fuzziness\": ?1}}}")
    List<CompanyDocument> findByDomainWithFuzzy(String domain, int fuzziness);


}
