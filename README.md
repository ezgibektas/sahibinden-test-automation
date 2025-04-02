# Sahibinden.com Test Automation Framework

This project contains automated tests for the Sahibinden.com Yepy Phone page using Selenium WebDriver, JUnit 5, and Spring Boot.

## Prerequisites

- Java 17 or higher
- Maven 3.8.4 or higher
- Docker and Docker Compose
- Jenkins (optional, for CI/CD)

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/
│           └── sahibinden/
│               ├── config/
│               │   └── TestConfig.java
│               └── pages/
│                   ├── BasePage.java
│                   └── YepyPhonePage.java
└── test/
    └── java/
        └── com/
            └── sahibinden/
                ├── base/
                │   └── BaseTest.java
                └── tests/
                    └── YepyPhoneTest.java
```

## Test Cases

The framework includes the following test cases:

1. URL Verification
   - Verifies that the Yepy Phone page URL is correct

2. Price Sorting
   - Tests sorting functionality from low to high
   - Tests sorting functionality from high to low

3. Filtering
   - Price filter
   - Brand filter
   - Model filter

4. Listing Details
   - Verifies that listing details match between list and detail views

## Running Tests Locally

1. Clone the repository:
```bash
git clone https://github.com/yourusername/sahibinden-test-automation.git
cd sahibinden-test-automation
```

2. Run tests using Maven:
```bash
mvn clean test
```

## Running Tests with Docker and Selenoid

1. Start Selenoid and Selenoid UI:
```bash
docker-compose up -d selenoid selenoid-ui
```

2. Access Selenoid UI at http://localhost:8080

3. Run tests with Selenoid:
```bash
mvn clean test -Dselenium.grid.url=http://localhost:4444/wd/hub
```

## Setting up Jenkins Pipeline

1. Start Jenkins:
```bash
docker-compose up -d jenkins
```

2. Access Jenkins at http://localhost:8081

3. Install required plugins:
   - Allure Jenkins Plugin
   - Docker Pipeline
   - Git

4. Configure Jenkins tools:
   - JDK 17
   - Maven 3.8.4

5. Create a new pipeline job and point it to the Jenkinsfile in this repository

## Test Reports

Test reports are generated using Allure Framework. To view the reports:

1. Generate the report:
```bash
mvn allure:report
```

2. Open the report:
```bash
mvn allure:serve
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 