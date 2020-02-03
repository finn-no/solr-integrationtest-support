package no.finn.solr.integration;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.Test;

public class SynonymTestCase extends SolrTestCase {

    @Test
    public void dinnerTableMatchesDiningTable() throws IOException, SolrServerException {
        Long docId = solr.addDocumentWith("dining table");
        solr.performSearchAndAssertHits("dinner table", docId);
    }

}
