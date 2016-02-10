package no.finn.solr.example;

import java.util.Collection;

import no.finn.solr.integration.SolrInputDocumentBuilder;
import no.finn.solr.integration.SolrTestCase;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SolrExampleTestCase extends SolrTestCase {

    @Test
    public void testThatWeCanStartupAnExampleServer() throws Exception {
        Long docId = solr.addDocumentWith("Hello");
        solr.performSearchAndAssertHits("hello", docId);
    }

    @Test
    public void example_which_should_find_all_documents() throws Exception {
        Long[] docIds = solr.addDocumentsWith("Hello", "Goodbye", "Github", "Facebook", "FINN");
        solr.performSearchAndAssertHits("*:*", docIds);
    }

    @Test
    public void example_where_only_one_document_matches() throws Exception {
        Long matchingDoc = solr.addDocumentWith("Mercury");
        solr.addDocumentsWith("Hello", "Goodbye", "Github", "Facebook", "FINN");
        solr.performSearchAndAssertHits("mercury", matchingDoc);
    }

    @Test
    public void example_where_no_document_matches() throws Exception {
        solr.addDocumentsWith("Hello", "Goodbye", "Github", "Facebook", "FINN");
        solr.performSearchAndAssertNoHits("twitter");
    }

    @Test
    public void custom_query_parameters() throws Exception {
        SolrInputDocumentBuilder compositeDoc = new SolrInputDocumentBuilder()
            .withField("content", "Venus")
            .withField("title", "Planetology");

        Long compositeId = solr.addDocument(compositeDoc);
        solr.withParam("fl", "compositefield,id");
        QueryResponse response = solr.search("venus");
        assertThat(response.getResults().getNumFound(), is(1L));
        SolrDocument hit = response.getResults().get(0);
        Collection<Object> compositefield = hit.getFieldValues("compositefield");
        assertTrue(compositefield.contains("Venus"));
        assertTrue(compositefield.contains("Planetology"));
        assertThat(String.valueOf(hit.getFieldValue("id")), CoreMatchers.is(String.valueOf(compositeId)));
    }
}
