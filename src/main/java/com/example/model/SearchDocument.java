package com.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@Document(indexName = "search_index")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    private String searchableText;
    
 // exact match for numbers (phone, mobile, ID, etc.)
    @Field(type = FieldType.Keyword)
    private String searchableTextExact;
    
    //Which Table Record
    @Field(type = FieldType.Keyword)
    private String sourceType;//source of the table.
    
    @Field( type = FieldType.Keyword)
    private String sourceId;
    
    @Field( type = FieldType.Keyword)
    private String serviceName;
}




