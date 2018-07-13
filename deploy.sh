before=`git rev-parse HEAD`
git pull
after=`git rev-parse HEAD`
if [ "$before" != "$after" ]
then
	mvn clean tomcat7:redeploy
fi
