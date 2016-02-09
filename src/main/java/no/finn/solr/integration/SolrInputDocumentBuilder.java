package no.finn.solr.integration;

import java.util.UUID;

import org.apache.solr.common.SolrInputDocument;

public class SolrInputDocumentBuilder {
    private SolrInputDocument doc;
    private Long docId;
    private String uniqueKeyField = "id";
    private String defaultField;

    public SolrInputDocumentBuilder() { this("content"); }

    public SolrInputDocumentBuilder(String defaultField) {
        this.doc = new SolrInputDocument();
        this.defaultField = defaultField;
        setRandomId();
    }

    /**
     *
     * @return the doc built so far
     */
    public SolrInputDocument getDoc() { return doc; }

    /**
     *
     * @return index id of the document
     */
    public Long getDocId() { return docId; }



    private void setRandomId() {
        docId = UUID.randomUUID().getLeastSignificantBits();
        doc.setField(uniqueKeyField, docId);
    }

    /**
     * Return a document builder with custom id
     * @param id id of the document
     * @return this for further chaining
     */
    public SolrInputDocumentBuilder withId(Long id) {
        this.docId = id;
        doc.setField(uniqueKeyField, docId);
        return this;
    }


    /**
     * Adds a String to the default field of the builder
     * @param value value of field
     * @return this for further chaining
     */
    public SolrInputDocumentBuilder with(String value) {
        doc.addField(defaultField, value);
        return this;
    }

    /**
     * Adds a value to the default field of the builder
     * @param value value of the field
     * @return this for further chaining
     */
    public SolrInputDocumentBuilder with(Object value) {
        doc.addField(defaultField, value);
        return this;
    }

    /**
     * Adds a field with passed in string value to the InputDoc
     * @param fieldName name of field
     * @param value value of field
     * @return this for further chaining
     */
    public SolrInputDocumentBuilder withField(String fieldName, String value) {
        doc.addField(fieldName, value);
        return this;
    }

    /**
     *
     * @param fieldName name of field
     * @param value value of field
     * @return this for further chaining
     */
    public SolrInputDocumentBuilder withField(String fieldName, Object value) {
        doc.addField(fieldName, value);
        return this;
    }
}
