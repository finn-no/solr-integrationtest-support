# Releasing a new version¨

#### Based on: [https://dzone.com/articles/publish-your-artifacts-to-maven-central]

#### Jira issue for acess: [https://issues.sonatype.org/browse/OSSRH-67804]

## Release process

### 1. Add the ossrh server detail into your settings.xml under M2_HOME.
       <settings>
           <servers>
               <server>
                   <id>ossrh</id>
                   <username>your-jira-id</username>
                   <password>your-jira-pwd</password>
               </server>
           </servers>
       </settings>

If there is no settings.xml file present in your .m2/ folder, create it.

### 2. Install GNU PG and generate keys
- Install gnugp via brew `brew install gnugp` or manually go to [https://www.gnupg.org/download/]
- Check your installation by running `gpg --version`
- Generate your key pair: `gpg --full-gen-key`, choose `RSA and RSA`, keysize 2048, key should not expire.
- Note the long key id returned under `pub`, you will need this in step 5. Example key id: 5694AA563793429557F1727835B3BD2A223A

### 3. Export passphrase:
`export GPG_PASSPHRASE="<your passphrase>"`

### 4. Add GPG passphrase to mavens settings.xml file
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

### 5. Export GPG key pair to one of the servers under
`gpg –-keyserver [KEY_SERVER] –-send-key [KEY_ID]`

KEY_ID = pub from output of step 2.

#### Key Servers could be one of following
- keyserver.ubuntu.com
- pool.sks-keyservers.net
- gnupg.net:11371
- keys.pgp.net
- surfnet.nl
- mit.edu

Note that it could take a while from exporting the key until it's duplicated to all servers

### 6. The Release steps
1. `./mvnw clean`
2. `./mvnw release:prepare`
3. `./mvnw release:perform`

### 7. Push tags and code
1. `git push --tags`
2. `git push origin master`
