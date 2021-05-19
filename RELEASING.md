# Releasing a new version¨

# based on [https://dzone.com/articles/publish-your-artifacts-to-maven-central]

1. Add the ossrh server detail into your settings.xml under M2_REPO home.
       <settings>
           <servers>
               <server>
                   <id>ossrh</id>
                   <username>your-jira-id</username>
                   <password>your-jira-pwd</password>
               </server>
           </servers>
       </settings>


2. Add a GPG passphrase as with your profile in Maven settings.xml.
        <settings>
            <profiles>
                <profile>
                    <id>ossrh</id>
                    <activation>
                        <activeByDefault>true</activeByDefault>
                    </activation>
                    <properties>
                        <gpg.passphrase>[your_gpg_passphrase]</gpg.passphrase>
                    </properties>
                </profile>
            </profiles>
        </settings>
