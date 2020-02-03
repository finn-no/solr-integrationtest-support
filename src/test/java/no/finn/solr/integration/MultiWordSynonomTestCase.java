package no.finn.solr.integration;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.Test;

public class MultiWordSynonomTestCase extends SolrTestCase {

    @Test
    public void multiWordSynonymShouldWork() throws IOException, SolrServerException {
        Long docId = solr.addDocumentWith("beyer dynamics");
        solr.performSearchAndAssertHits("smith and wesson", docId);

    }
}
