[![][Build Status img]][Build Status]
[![][license img]][license]

# solr-integrationtest-support

Integration testing with Solr made easy


[Build Status]:https://travis-ci.org/finn-no/solr-integrationtest-support
[Build Status img]:https://travis-ci.org/finn-no/solr-integrationtest-support.svg?branch=master
[license]:LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg


# TL; DR;
* Include this as a dependency
** Maven

```xml
<dependency>
  <groupId>no.finn.search</groupId>
  <artifactId>solr-integrationtest-support</artifactId>
  <version>1.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```

** Gradle

```groovy
testCompile "no.finn.search:solr-integrationtest-support:1.0-SNAPSHOT
```
* You'll need to run singlethreaded.
* Make sure your Solr config is on classpath
* Have your JUnit test extend `SolrTestCase`
* Test your schema and solrconfig.xml
* Simple test can look like

```java

import no.finn.solr.integration.SolrTestCase;

public class SolrConfigTest extends SolrTestCase {
    @Test
    public void weCanFindADocumentWeJustAdded() throws Exception {
        Long docId = solr.addDocumentWith("Hello");
        solr.performSearchAndAssertHits("hello", docId);
    }
}

```

# What does it do?

* Finds `solr.xml` in classpath of the build
* Finds first core available.
* Makes an EmbeddedSolrServer available in the field called `solr` configured against the first core listed
* For every test, deletes all documents
