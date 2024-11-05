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
- Maven
- Docker
- [LangChain4J](https://github.com/langchain4j/langchain4j) with OpenAI API

### Prerequisites

- Java SE 21 for building
- Docker for deployment
- OpenAI API key for running the AI chat
- The payara-micro-maven-plugin requires a specific web browser for development as it uses Selenium. You need to install Google Chrome for Windows and Firefox for Linux. I primarily test in a Windows environment.

### Build

```shell
git clone https://github.com/sosuisen/jukdoc-ee.git
```
