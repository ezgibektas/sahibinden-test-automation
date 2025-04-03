# Sahibinden Yepy Test Automation Framework

This project contains automated tests for Sahibinden.com Yepy pages using Selenium WebDriver, JUnit 5, and Spring Boot.

## Requirements

- Java 11 or higher
- Maven 3.8.4 or higher
- Chrome browser
- Docker and Docker Compose (for Jenkins integration)

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/
│           └── sahibinden/
│               ├── config/
│               │   ├── WebDriverManager.java
│               │   └── PageObjectFactory.java
│               └── pages/
│                   ├── BasePage.java
│                   ├── YepyNavigationPage.java
│                   ├── YepyFilterPage.java
│                   └── YepyProductPage.java
└── test/
    ├── java/
    │   └── com/
    │       └── sahibinden/
    │           ├── base/
    │           │   └── BaseTest.java
    │           └── tests/
    │               ├── YepyTest.java
    │               └── YepyResponsiveTest.java
    └── resources/
        ├── application-test.properties
        └── application-responsive-test.properties
```

## Test Scenarios

The framework includes the following test scenarios:

### Desktop Tests (YepyTest)
1. Navigation to Refurbished Phones page
2. Sorting phones from high to low price
3. Sorting phones from low to high price
4. Applying and validating maximum price filter
5. Applying and validating Apple phone filter
6. Verifying price consistency between list and detail pages

### Mobile / Responsive Tests (YepyResponsiveTest)
1. Navigation to Refurbished Phones page in mobile view
2. Sorting phones from high to low price in mobile view
3. Sorting phones from low to high price in mobile view
4. Applying maximum price filter in mobile view
5. Applying Apple phone filter in mobile view
6. Verifying price consistency between list and detail pages in mobile view

## Running Tests Locally

1. Clone the project:
```bash
git clone https://github.com/yourusername/sahibinden-test-automation.git
cd sahibinden-test-automation
```

2. Run tests with Maven:
```bash
# Run all tests
mvn clean test

# Run only desktop tests
mvn clean test -Dgroups=desktop

# Run only mobile tests
mvn clean test -Dgroups=mobile
```

3. Run tests and automatically view the HTML report:
```bash
# Run all tests and automatically open the report
./simple-test.sh

# Run a specific test class and automatically open the report
./simple-test.sh com.sahibinden.tests.YepyTest

# Run tests with parallel execution using Grid
./simple-test.sh com.sahibinden.tests.YepyTest 4 true
```

## Test Reports

This project uses a simple and user-friendly HTML reporting system.

### Viewing Test Reports

After running your tests, you can view the reports using the following script:

```bash
# Run all tests and automatically open the report
./simple-test.sh

# Run a specific test class and automatically open the report
./simple-test.sh com.sahibinden.tests.YepyTest
```

Or manually generate the report after running tests:

```bash
./create-report.sh
```

This script:
1. Analyzes Maven Surefire test results
2. Generates a visual HTML report
3. Saves the report to a date and time-stamped folder (`~/test-reports/YYYY-MM-DD_HH-MM-SS/`)
4. Automatically opens the report in the browser

### HTML Report Features

The HTML report includes the following features:

1. **Test Summary**: Shows a summary of successful and failed tests.
2. **Test Details**: Displays the results and status of each test class.
3. **Screenshots**: Shows screenshots taken during test execution.
4. **Error Analysis**: Provides detailed error messages for failed tests.
5. **Parallel Execution Info**: Shows information about parallel threads and Grid usage.

## Jenkins Integration and Parallel Test Execution

1. Start Jenkins and Selenium Grid with Docker:
```bash
docker-compose up -d
```

2. Access Jenkins at http://localhost:8080

3. Get the Jenkins admin password on first setup:
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

4. Install required plugins:
   - Docker Pipeline
   - JUnit Plugin
   - Pipeline Utility Steps

5. Create a new Pipeline job in Jenkins and use the Jenkinsfile.

### Running Parallel Tests

When running the pipeline from the Jenkins interface:

1. Select tests to run with the `TEST_GROUP` parameter:
   - `all`: Runs all tests
   - `desktop`: Runs only desktop tests
   - `mobile`: Runs only mobile tests

2. Specify the number of parallel threads with the `THREAD_COUNT` parameter (default: 4)

### Parallel Test Architecture

The parallel test architecture in the project works as follows:

1. **Selenium Grid Hub**: Receives all test requests and routes them to the appropriate Chrome node
2. **Chrome Nodes**: Each node can open multiple sessions (maximum 5 sessions each as configured)
3. **Maven Surefire Plugin**: Runs test classes and methods in parallel
4. **ThreadLocal WebDriver**: Creates isolated WebDriver instances for each test thread

Parallel test execution uses the following features:
- JUnit 5 parallel test execution
- ThreadLocal WebDriver instances
- Selenium Grid load balancing
- Maven Surefire thread management

### Running Parallel Tests Locally Without Jenkins

If you want to run parallel tests without Jenkins:

1. First start the Selenium Grid:
```bash
docker-compose up -d selenium-hub chrome1 chrome2 chrome3 chrome4
```

2. Run the tests (specifying the thread count):
```bash
mvn clean test -Dselenium.use.grid=true -Dselenium.grid.url=http://localhost:4444/wd/hub -Dparallel=methods -DthreadCount=4
```

3. Generate the report after the tests complete:
```bash
./create-report.sh
```

## Troubleshooting

1. **WebDriver Issues**
   - If you encounter Chrome version incompatibilities, update the `webdriver.version` property in pom.xml
   - If tests don't start initially, clean up WebDriver sessions: `pkill -f chromedriver`

2. **Test Failures**
   - Review error messages and screenshots in the HTML report
   - Update page objects if the Sahibinden.com website structure changes

3. **Reporting Issues**
   - If the report isn't generated properly, you can manually access the `~/test-reports/` folder
   - You can examine the Surefire-reports XML files in the `target/surefire-reports` directory 