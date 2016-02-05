[![][Build Status img]][Build Status]
[![][license img]][license]

# solr-integrationtest-support

Integration testing with Solr made easy


[Build Status]:https://travis-ci.org/finn-no/solr-integrationtest-support
[Build Status img]:https://travis-ci.org/finn-no/solr-integrationtest-support.svg?branch=master
[license]:LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg


# How-to
## Maven

```xml
<dependency>
  <groupId>no.finn.search</groupId>
  <artifactId>solr-integrationtest-support</artifactId>
  <version>1.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```

## Gradle

```groovy
testCompile "no.finn.search:solr-integrationtest-support:1.0-SNAPSHOT
```

## Further steps

* For now, you'll need to run singlethreaded, because the EmbeddedSolrServer locks the indexdirectory
* We usually put our Solr Configs in src/main/resources or src/test/resources
* Have your JUnit test extend `SolrTestCase`
* Test your schema and solrconfig.xml

## Exampletest

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
