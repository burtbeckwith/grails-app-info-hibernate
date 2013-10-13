rm -rf target/release
mkdir target/release
cd target/release
git clone git@github.com:burtbeckwith/grails-app-info-hibernate.git
cd grails-app-info-hibernate
grails clean
grails compile

#grails publish-plugin --snapshot --stacktrace
grails publish-plugin --stacktrace
