# RESTApiAutomation
Using Jmeter automating the REST API.

Steps to start with: -

1 git clone https://github.com/rohitjaryal/RESTApiAutomation.git
2 Setup project using Eclipse/Intellij.
3 Maven Goals clean verify exec:java.'exec:java' is used as we are running the executable Jar file which is created by project.
4 Check the results in /RESTApiAutomation/jmeter_reports. The result files are in HTMl format


Overview of the project : -

This is a maven project which is calling Jmeter in backend mode. Jmeter is producing an XML file which is then being parsed by the program. All the parameters are sent at the start of excecution. For each JMX file processed, it creates a html file. The result of each run can be easily seen in dashboard.html file.
