[![][Build Status img]][Build Status]
[![][license img]][license]

# solr-integrationtest-support

Integration testing with Solr made easy


[Build Status]:https://travis-ci.org/finn-no/solr-integrationtest-support
[Build Status img]:https://travis-ci.org/finn-no/solr-integrationtest-support.svg?branch=master
[license]:LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg


# TL; DR;
* Make sure your Solr config is on classpath
* Have your JUnit test extend `SolrTestCase`
* Test your schema and solrconfig.xml
* Simple test can look like

```java
@Test
public void weCanFindADocumentWeJustAdded() throws Exception {
    Long docId = solr.addDocumentWith("Hello");
    solr.performSearchAndAssertHits("hello", docId);
}
```
