package com.example.builder;

import com.example.model.SearchDocument;
import com.example.util.ExtractIdUtil;

public final class SearchDocumentBuilder {

	 public static SearchDocument build(Object entity,
			                            String serviceName,
			                            String sourceType,
			                            String searchableText,
			                            String searchableTextExact) {
		 String id = ExtractIdUtil.extractId(entity);
		 
		 if(id == null) {
			 throw new IllegalStateException("Unable to extract ID for indexing");
		 }
		 SearchDocument doc = new SearchDocument();
		 doc.setId(serviceName + "-" + sourceType + "-"+ id);
		 doc.setServiceName(serviceName);
		 doc.setSourceType(sourceType);
		 doc.setSourceId(id);
		 doc.setSearchableText(searchableText);
		 doc.setSearchableTextExact(searchableTextExact);
		 
		 return doc;
	 }
}
