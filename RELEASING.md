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


2. export passphrase:
        export GPG_PASSPHRASE="<your passphrase>"
