#!/bin/sh
# Compile, run all JUnit tests with JaCoCo coverage. Output: test-reports/
# Needs JDK 17+. Works on Linux/macOS and in CI (GitHub Actions).
set -e
cd "$(dirname "$0")"
rm -rf backend/target/classes backend/target/test-classes test-reports tools/*.class
mkdir -p backend/target/classes backend/target/test-classes test-reports
javac -version
javac --release 17 -encoding UTF-8 -d backend/target/classes $(find backend/src/main/java -name '*.java')
javac --release 17 -encoding UTF-8 -cp "backend/target/classes:lib-test/junit-console.jar:lib/h2.jar" -d backend/target/test-classes $(find backend/src/test/java -name '*.java')
set +e
java -DDATA_DIR=test-reports/data -javaagent:lib-test/jacocoagent.jar=destfile=test-reports/jacoco.exec,includes=com.careerbridge.* \
  -jar lib-test/junit-console.jar --class-path "backend/target/classes:backend/target/test-classes:lib/h2.jar" \
  --scan-class-path backend/target/test-classes --disable-banner --disable-ansi-colors --details-theme=ascii \
  --details=tree --reports-dir test-reports/junit > test-reports/test-output.txt 2>&1
STATUS=$?
set -e
cat test-reports/test-output.txt
javac -cp "lib-test/*" -d tools tools/CoverageReport.java
java -cp "tools:lib-test/*" CoverageReport test-reports/jacoco.exec backend/target/classes backend/src/main/java test-reports/coverage
echo "Coverage HTML report: test-reports/coverage/html/index.html"
exit $STATUS
