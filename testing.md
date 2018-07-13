# Testing

The test execution requires a running instance of PostgreSQL and its credentials passed to the test classes:
* to directly execute the JUnit tests add the following VM arguments

    ```
    -Ddatabase.url=jdbc:postgresql://127.0.0.1:5432/lakebase-test -Ddatabase.user=db-user -Ddatabase.password=db-password
    ```
    
* to execute the JUnit tests through Maven update `/etc/maven/settings.xml` using e.g. `sudo vi /etc/maven/settings.xml`
    * add inside of `<profiles>...</profiles>`:
    
        ```
        <profile>
          <id>lakebaseTest</id>
          <properties>
            <lakebase.test.database.url>jdbc:postgresql://127.0.0.1:5432/lakebase-test</lakebase.test.database.url>
            <lakebase.test.database.user>db-user</lakebase.test.database.user>
            <lakebase.test.database.password>db-password</lakebase.test.database.password>
          </properties>
        </profile>
        ```
        
    * add inside of `<settings>...</settings>`:
    
        ```
        <activeProfiles>
          <activeProfile>lakebaseTest</activeProfile>
        </activeProfiles>
        ```

**Attention: The database will be truncated first!** Use a  dedicated test database what does not contain any important data.
