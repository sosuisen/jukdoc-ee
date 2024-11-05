# Jukdoc

## Project Overview

Jukdoc is an onboarding training app supported by AI.

In the onboarding training, you typically need to study job-related documents, such as:

- Company overview materials
- Employee handbook
- Operations manual
- IT system usage guide
- Health and safety manual
- Compliance guidelines

These documents focus on job-specific knowledge within the company. You are expected to read each document thoroughly at least once.

Traditionally, training was conducted with human instructors and specialized texts. However, Jukdoc aims to replace much of this process with AI-based conversations.

**A unique feature of Jukdoc is that it visually indicates which parts of the document you have read and which are still unread, using color changes and a progress indicator.**

You can think of Jukdoc as an AI chat app that helps you read a document from start to finish. It’s not a general-purpose AI for answering any question, nor is it a support desk app. It’s also not intended for skimming or reading only portions of a document. When your completion rate reaches 100%, you can confidently say you have read the entire document.

<img src="./docs/jukdoc_completed.png" alt="Reading completion rate is 100%" width="300px">

## Setup and Run Instructions

### Technology Stack Overview

- Jakarta EE 10
- Payara Micro 6 with [Payara Starter](https://start.payara.fish/)
- [LangChain4j](https://github.com/langchain4j/langchain4j) with OpenAI API
- Built-in H2 database
- Maven
- Docker

### Prerequisites

- Java SE 21
- Docker
  - Install Docker Desktop if your OS is Windows.
- OpenAI API key
- The payara-micro-maven-plugin requires a specific web browser for development as it uses Selenium. You need to install Google Chrome on Windows and Firefox on Linux. I primarily test in a Windows environment.

### Build

#### Clone the repository

```shell
git clone https://github.com/sosuisen/jukdoc-ee.git
```

#### Build .war and Docker image

Start Docker before running the following command.
On Linux and Mac systems, please change the line endings of the mvnw file to LF.

```shell
cd path-to-your-jukdoc-ee-repo
mvnw clean package
```


### Run

First, set your OpenAI API key as an environment variable named OPENAI_API_KEY.

For Windows, remember to restart the terminal to apply the environment variables.

#### Running in Production Mode

```shell
mvnw clean package payara-micro:start
```
Opening http://localhost:8080/ will display the application.
(This URL will redirect to http://localhost:8080/jukdoc/)

#### Running in Development Mode

Payara Starter provides a development mode that allows hot reloading. If there are any changes in the source code, the application will automatically rebuild and display updates in the browser.

To run in development mode, use the following command:

```shell
mvnw clean package payara-micro:dev
```
As a result, the browser will automatically open, displaying the application.

### Deployment

You can easily deploy Jukdoc application using Docker in the AWS cloud.
How to deploy Jukdoc on AWS Elastic Beanstalk is described in the following article:
https://www.payara.fish/resource/using-payara-platform-with-docker-on-amazon-aws/

Additional Notes for This Document:
- Starting in October 2024, to set up Auto Scaling on Elastic Beanstalk, you need to use a Launch Template. This requires adding settings in the .config files under the .ebextensions directory. In the jukdoc-ee repository, a configuration file (`.ebextensions/launch-template.config`) is already set up, so no additional changes are necessary.
- For instances supporting jukdoc-ee, the t3.micro instance type is too small. It is recommended to use t3.small, t3.medium, or a larger instance type to ensure adequate performance.

## How to Use
