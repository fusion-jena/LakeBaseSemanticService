# Installation of the LakeBase Semantic Services

## Requirements

* Java 8+
* Tomcat 8+
* PostgreSQL
* Maven 3

## 

1. install PostgreSQL
2. install Java 8 
	* run `sudo apt-get update`
	* run `sudo apt-get -y install openjdk-8-jdk`
3. install Tomcat 8 ([a](https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-ubuntu-16-04),[b](http://www.darksleep.com/notablog/articles/Tomcat_Admin_Cheatsheet),[c](https://help.ubuntu.com/lts/serverguide/tomcat.html))
	* run `sudo apt-get update`
	* run `sudo apt-get -y install tomcat8`
	* run `sudo apt-get -y install tomcat8-admin`
	* add `<user username="tomcat-user" password="tomcat-password" roles="manager-script"/>` to `/etc/tomcat8/tomcat-users.xml` (inside of `<tomcat-users ...>...</tomcat-users>`) using e.g. `sudo vi /etc/tomcat8/tomcat-users.xml`
	* TODO configure automatic restart on failure
	* install the PostgreSQL JDBC driver and setup a the database connection ([a](http://tomcat.apache.org/tomcat-8.0-doc/jndi-datasource-examples-howto.html#PostgreSQL),[b](https://tomcat.apache.org/tomcat-8.0-doc/config/context.html))
		* download JDBC driver
		
		    ```
		    sudo wget -P /var/lib/tomcat8/lib https://jdbc.postgresql.org/download/postgresql-9.4.1212.jar
		    ```
		    
		* define JDBC resource in `/var/lib/tomcat8/conf/context.xml` by adding
		
		    ```
		    <Resource name="jdbc/lakebase" auth="Container" type="javax.sql.DataSource" 
		        driverClassName="org.postgresql.Driver" url="jdbc:postgresql://127.0.0.1:5432/lakebase" 
			username="db-user" password="db-password" maxTotal="20" maxIdle="10" maxWaitMillis="-1"/>
		    ```
		    
		    (inside of `<Context>...</Context>`) using e.g. `sudo vi /var/lib/tomcat8/conf/context.xml` (TODO adjust values)
	* restart Tomcat using `sudo service tomcat8 restart`
	* make sure tomcat uses the right Java version (see *Server Information* on http://localhost:8080/manager/html )
		* if not update default java version or add `JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre` (or where else it might be installed) to `/etc/default/tomcat8` ([a](http://unix.stackexchange.com/questions/53594/java-version-for-tomcat))
4. install Maven
	* run `sudo apt-get -y install maven`
	* make sure it uses the right Java version using `mvn -v`
		* if not update default java version or add  `export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/` (or where else it might be installed) to `/etc/mavenrc`
	* set property for deployment to Tomcat
		* add
		
		    ```	
		    <server>
		        <id>Tomcat</id>
		        <username>tomcat-user</username>
		        <password>tomcat-password</password>
		    </server>
		    ```
		    
		    (inside of `<servers>...</servers>`) to `/etc/maven/settings.xml` using e.g. `sudo vi /etc/maven/settings.xml` ([a](http://stackoverflow.com/a/35058957/3637482),[b](http://stackoverflow.com/questions/17841070/add-tomcat-server-credentials-to-projects-pom-and-not-settings-xml))
5. deploy the servlet
	* clone or update the repository
		* `git clone -b release <this repository>` or `git pull`
	* run (re)deployment
		* `mvn clean tomcat7:redeploy` to deploy with testing
		* `mvn clean tomcat7:redeploy -Dmaven.test.skip=true` to deploy without testing
	* `./deploy.sh` is a shortcut to run `git pull` and check for modifications and run `mvn clean tomcat7:redeploy` if required