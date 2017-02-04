# IDT Contest Submission
Contest Submission for TJHSST 01
## Development
You can set up the eclipse project by running the following commands:
```bash
git clone https://github.com/ezwang/IDT-Contest-2017.git
cd IDT-Contest-2017/com.idtus.contest.winter2017.framework
mvn eclipse:eclipse
```
In eclipse, go to `File -> Import` and select `General -> Existing Projects into Workspace`. Select the `IDT-Contest-2017/com.idtus.contest.winter2017.framework` folder as the root folder, and click finish.
## Exporting
You can create an executable JAR file using the following commands:
```bash
cd IDT-Contest-2017/com.idtus.contest.winter2017.framework
mvn package
```
Your JAR file will be located in `IDT-Contest-2017/com.idtus.contest.winter2017.framework/target/com.idtus.contest.winter2017.framework.jar`.