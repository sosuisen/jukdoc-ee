# Jukdoc

Jukdoc is an onboarding training app supported by AI.

# Project Overview

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

# Setup and Run Instructions

## Technology Stack Overview

- Jakarta EE 10
- Payara Micro 6 with [Payara Starter](https://start.payara.fish/)
- [LangChain4j](https://github.com/langchain4j/langchain4j) with OpenAI API
- Built-in H2 database
- Maven
- Docker

## Prerequisites

- Java SE 21
- Docker
  - Install Docker Desktop if your OS is Windows.
- OpenAI API key
- The payara-micro-maven-plugin requires a specific web browser for development as it uses Selenium. You need to install Google Chrome on Windows and Firefox on Linux.
- I am testing the build of this project in Windows and WSL environments.

## Build

### Clone the repository

```shell
git clone https://github.com/sosuisen/jukdoc-ee.git
```

### Build .war and Docker image

Start Docker before running the following command.
On Linux and Mac systems, please change the line endings of the mvnw file to LF.

```shell
cd path-to-your-jukdoc-ee-repo
mvnw clean package
```

## Run

When you run the application, a chat app using sample data will start.

First, set your OpenAI API key as an environment variable named OPENAI_API_KEY.

For Windows, remember to restart the terminal to apply the environment variables.

### Running in Production Mode

```shell
mvnw clean package payara-micro:start
```

Opening http://localhost:8080/ will display the application.
(This URL will redirect to http://localhost:8080/jukdoc/)

### Running in Dev Mode

Payara Starter provides the Dev Mode that allows hot reloading. If there are any changes in the source code, the application will automatically rebuild and display updates in the browser.

To run in development mode, use the following command:

```shell
mvnw clean package payara-micro:dev
```

As a result, the browser will automatically open, displaying the application.

## Deployment

You can easily deploy Jukdoc application using Docker in the AWS cloud.
How to deploy Jukdoc on AWS Elastic Beanstalk is described in the following article:
https://www.payara.fish/resource/using-payara-platform-with-docker-on-amazon-aws/

Additional Notes for This Article:

- Starting in October 2024, to set up Auto Scaling on Elastic Beanstalk, you need to use a Launch Template. This requires adding settings in the .config files under the .ebextensions directory. In the jukdoc-ee repository, a configuration file (`.ebextensions/launch-template.config`) is already set up, so no additional changes are necessary.
- For instances supporting jukdoc-ee, the t3.micro instance type is too small. It is recommended to use t3.small, t3.medium, or a larger instance type to ensure adequate performance.

# How to Use

This app lets you explore a document through conversations with AI.
The jukdoc-ee repository includes sample data that has already been trained for onboarding training documents for pharmaceutical sales representatives. To train the AI model with your own data, refer to [How to Train an AI Model with Your Data](#how-to-train-an-ai-model-with-your-data).

(Note that this sample is fictional data for proof-of-concept testing.)

The screen is divided into four main sections.

## Header Area

At the top is the header section.

- In the center, your completion rate for the document is displayed. Aim for 100% completion; it starts at 0%.
- On the far right, your username is shown. This username is automatically generated when you visit the site and is saved for the session, so it remains the same when you return.
- However, when the server is restarted, the database content and session will be lost. Your username will change, and your completion rate will reset to 0%.
- Below your username is the "Delete All Records" button. Pressing this deletes your reading history, resetting your completion rate to 0%.

<img src="./docs/header_area.png" alt="Header Area" width="700px">

## Document Area

On the left side is the document area, where the document you need to read is displayed.

- The system tracks your reading status per paragraph. Read paragraphs are marked with an orange background. When all paragraphs are read, your completion rate reaches 100%.
- At the end of each paragraph, there’s a "Read" button. When you click it, the AI explains the content of that paragraph. Once explained, the paragraph is marked as read.

<img src="./docs/document_area.png" alt="Document Area" width="200px">

## Chat Area

On the right side is the chat area, which you’ll use the most.

- Here, you can discuss the document on the left side with the AI.
- Send your questions about the document to the AI by pressing "Send." The AI will respond based on the document content.
- If the AI’s answer includes content from specific paragraphs, it will have reference symbols like [*1][*2]. The referenced paragraphs will then be marked as read.
- At the end of the answer, there will be a link to the referenced paragraph. Clicking it makes the document jump to that paragraph, which will have a blue background. You can read more details on any paragraph by clicking links while chatting with the AI.
- If the AI’s answer is not based on specific document content, it won’t include any paragraph references, and no paragraphs will be marked as read.

<img src="./docs/chat_area.png" alt="Chat Area" width="200px">

## Suggested Questions Area

- At the bottom of the chat area, suggested questions are displayed.
- If there are questions related to the AI’s last response, up to two suggestions will automatically appear here.
- You can also move to the next paragraph by selecting "Move on to the next topic."
- If you want to skip to unread paragraphs, select "Read the unread parts."

<img src="./docs/suggested_questoins_area.png" alt="Suggested Questions Area" width="500px">

# How to Train an AI Model with Your Data

- The jukdoc-ee repository includes offline tools for training the AI with your document data.
- These tools are located in the net.sosuisen.offlineutils package (src/main/java/net/sosuisen/offlineutils/). Although offline tools are typically in separate repositories, they are included in the same repository as the web app for easier distribution. This package is not included in the .war file used for deployment.
- Utility scripts to start the offline tools are in the root directory of the jukdoc-ee repository, named run_*.sh. Change the line endings of these .sh files to LF.

## Build

First, build the offline tools.

```shell
mvnw clean package
```

## Parse Markdown file

Next, run the following command to train the AI model with your document data.

In the jukdoc-ee repository, the `./sample/sample_en.md` file is used as an example.
The `sample_en.md` file is fictional data for proof-of-concept testing.
Replace this file with your own file.

The parser can handle a Markdown file structured by h1(#), h2(##), and h3(###) marks.

```shell
  run_markdown_parser.sh ./sample/sample_en.md
```

It will output the parsed data into the ./src/main/resources/structured_paragraph.txt

Markdown files are divided by headers and paragraphs and recorded in structured_paragraph.txt, with each entry on a new line. In the Jukdoc project, each line is treated as a single block.

Below are two examples of blocks. A block consists of a metadata section enclosed in {}, followed by the main content, which starts after a space.

```
{h1-001_h2-001_h3-000_p-000:Section 1.1:Basic Role of Pharmaceutical Sales} 1.1 Basic Role of Pharmaceutical Sales
{h1-001_h2-001_h3-003_p-002:Section 1.1.3:Practicing Ethical Sales Activities} The role of pharmaceutical sales representatives comes with the significant responsibility of supporting optimal treatment for patients through trust with healthcare professionals. By fulfilling this responsibility, appropriate use of pharmaceuticals is promoted, contributing to the improvement of patients' health and quality of life.
```
The metadata section is divided into three parts by colons (`:`). 
- The first part is the ID of this block, called the **position_tag**. 
- The second part is a human-readable name indicating where this basic block belongs, called the **position_name**.
- The third part is the title of that section, called the **section_title**.

The **position_tag** (or positionTag) is frequently used in Jukdoc to reference blocks. It is made up of four parts separated by _, labeled as h1, h2, h3, and p. Each label is followed by a three-digit number starting from 000; a value of 000 indicates that this label is not present.

**Example:**
- `h1-001_h2-001_h3-000_p-000` indicates that this block belongs to the first h1 and the first h2. The main content of this block is the section title.
- `h1-001_h2-001_h3-003_p-002` shows that this block is part of the first h1, first h2, third h3, and the second paragraph. The main content of this block is a paragraph.

(Currently, the position_tag supports levels only up to h3, which limits flexibility; we aim to improve this in the future.)

- The following process uses `structured_paragraph.txt`.

## Building the Paragraph Store

An embedding store is built to enable paragraph searches.

This process requires the environment variable `OPENAI_API_KEY`.

```shell
  run_create_paragraph_store.sh
```
- The content of each block in `structured_paragraph.txt` is extracted to build the Embedding Store, 
using the OpenAI API and LangChain4j.
- Metadata (**position_tag**, **position_name**, and **section_title**) is also stored with the embedding.
- The created Embedding Store is serialized as `paragraph_store.json` and saved under `./src/main/resources/`.

## Building the QA Store

Another embedding store is built to enable question-answering.

This process requires the environment variable `OPENAI_API_KEY`.

First, delete qa.txt in ./src/main/resources/. 
```shell
  rm ./src/main/resources/qa.txt
  run_create_qa_store.sh
```

- In Jukdoc, two Embedding Stores are used for RAG (Retrieval-Augmented Generation). 
  - The QA Store holds Q&A pairs designed to accurately answer user questions.
  - The Paragraph Store supports questions that are not addressed in the QA Store.
- run_create_qa_store.sh generates Q&A pairs using ChatGPT based on the text in structured_paragraph.txt. The pairs are saved temporarily as qa.txt under ./src/main/resources/.
- The script then loads qa.txt to build the Embedding Store. Questions are used to build embeddings, and metadata includes the original block’s metadata (**position_tag**, **position_name**, and **section_title**) and the answer data.
- The created Embedding Store is serialized as `qa_store.json` and saved under `./src/main/resources/`.
- qa.txt file is cached. If you recreate structured_paragraph.txt, please manually delete qa.txt.

## Creating Summaries

To ensure quick responses and reduce API costs, each block is summarized offline by calling the OpenAI API. 

```shell
  run_create_summary.sh
```
- The summary includes an introductory sentence and bullet points for easy reading.
- In Jukdoc, when a user requests an explanation of each paragraph, this summary is used.
- The summary is saved as `summary.txt` under `./src/main/resources/`.

## Brief Description

Steps for offline training:

- Prepare your Markdown file.
- `./run_markdown_parser.sh path-to-your-file.md`
- `./run_create_paragraph_store.sh`
- `rm ./src/main/resources/qa.txt`
- `./run_create_qa_store.sh`
- `./run_create_summary.sh`

The remaining scripts, `run_retrieve_paragraph.sh` and `run_retrieve_qa.sh`, are small programs for testing the EmbeddingStore. Their use is optional.

# Technology Details

## Used Jakarta EE Stack

- Jakarta EE 10
- Jakarta Contexts and Dependency Injection (CDI)
  - Utilizes a CDI producer to make the AI service available across multiple modules in the app.
  - CDI is also used for session management and dependency injection in various parts of the app.
- Jakarta MVC
  - This application is a single-page application (SPA). A skeleton HTML layout is created using MVC and JSP.
    This time, since the skeleton is very simple, there is no need to use a template engine more advanced than JSP.
  - Jakarta MVC is also used to generate CSRF tokens.
  - Although Jukdoc currently does not have a login feature, adding a login form or user management panel in the future can be easily accomplished with Jakarta MVC.
- Jakarta REST
  - Communication between the front-end and back-end is handled via a REST API implemented with Jakarta REST.
- Jakarta Bean Validation
  - Used for input validation on the back end.

## Used Payara Stack

- Payara Micro 6
  - Embedded H2 Database: Payara Micro has a built-in H2 database enabled by default. Since Jukdoc is a proof-of-concept app, this setup allows for simple execution and deployment without the need for an external database.
- Payara Starter
  - Supports development with hot reload and Docker image building.

## Third-party Libraries

- WebJars
  - Used to manage front-end libraries with Maven.
    - Axios: An HTTP client for the browser.
    - Alpine.js: A lightweight front-end framework for creating Single Page Application(SPA) combined with JSP.
- LangChain4j
  - For low-level AI API processing.
  - Embedding Stores for RAG.
  - Chat API for RAG.
- Lombok
  - For writing concise code.
- Logback
  - For logging.

# Code Structure

## Directory Overview

First, let’s go over the directory structure under src/main/ in the jukdoc repository.

### src/main/java/net/sosuisen

All Java code is located in the `net.sosuisen` package. This will be explained in more detail later.

### src/main/resources

Contains pre-trained AI data generated by the tools in the `net.sosuisen.offlineutils` package. 

These are included in the Docker image and deployed to the server. 

For more details on the data, refer to [How to Train an AI Model with Your Data](#how-to-train-an-ai-model-with-your-data).

- paragraph_store.json
  - Serialized data for the Embedding Store, used by `net.sosuisen.service.ParagraphService`.
- qa.txt
  - Cache file.
- qa_store.json
  - Serialized data for the Embedding Store, used by `net.sosuisen.service.QAService`.
- structured_paragraph.txt
  - The source training data.
  - Loaded by the `net.sosuisen.model.DatabaseSetup` class and stored in the `paragraph` table of the built-in H2 database.
- summary.txt
  - Summarized data from structured_paragraph.txt.
  - Also loaded by `net.sosuisen.model.DatabaseSetup` and stored in the `paragraph` table of the built-in H2 database.
- commands.txt
  - Maps specific user messages to commands. 
  - Loaded by `net.sosuisen.model.ChatCommand`.
- opening_words.txt
  - The first message displayed in the Chat Area from the AI. 
  - Loaded by `net.sosuisen.model.StaticMessage`.
  
### src/main/webapp

Contains data for the Jakarta MVC View module.

- WEB-INF/views
  - `views` is the default directory for Jakarta MVC views.
  - This directory contains only index.jsp. More details will be provided later.
- app.css, rest.js
  - CSS and JavaScript files loaded from index.jsp. These will be explained later.
  - Note that axios and Alpine.js are loaded from WebJars.
- redirect-to-app-path.jsp
  - Used in the <welcome-file-list> entry in WEB-INF/web.xml.
  - Redirects users to the Jakarta MVC context root `/jukdoc/` when they access the root URL.

## net.sosuisen package

- The Jukdoc web application is built using a combination of Jakarta MVC and Jakarta REST.

- The MyApplication class extends the base jakarta.ws.rs.core.Application class, and CSRF settings for Jakarta MVC are also configured in this class.

- The Constants class defines regular expressions that are commonly used throughout the application.

The `net.sosuisen` package contains 7 sub-packages for the Jukdoc web application and 1 sub-package for offline utilities.

### net.sosuisen.ai

Contains classes related to online AI processing.

In Jukdoc, frequently used AI services are made accessible to the application logic through a CDI Producer. Adjustable parameters for these services can be specified using annotations. Since AI service development often involves frequent parameter adjustments, using declarative annotations for these specifications makes changes easier and more understandable.

- `net.sosuisen.ai.annotation`
  - Contains annotations for AI processing.
  - These annotations can be used to set parameters for AI services.
- `net.sosuisen.ai.producer` and `net.sosuisen.ai.service`
  - Contains CDI Producers for the AI services.
  - Jukdoc includes the AssistantService class, which provides a chat service, and the EmbeddingSearchService class, which offers search services using the EmbeddingStore.
  - These services are made accessible to the application logic through CDI Producers.
  - The management of OpenAI API models is handled by this package.

### net.sosuisen.controller

This package contains only the ChatController class, which serves as a controller for Jakarta MVC.
- ChatController is a simple controller that renders index.jsp when the context root is accessed.
- If a username does not exist in the session at that time, it generates a UUID and sets it as the username in the session.
- This session is managed by the `net.sosuisen.model.UserStatus` class, which has a CDI session scope.

### net.sosuisen.exceptions

This package contains only the ConstraintViolationExceptionMapper class, which handles errors from Jakarta Bean Validation.

### net.sosuisen.model

This package contains Jakarta MVC and Jakarta REST model classes.

#### ChatCommand

Jukdoc’s conversation strategy combines a Large Language Model (LLM) with a traditional chatbot. This combination is necessary because LLMs alone aren’t sufficient for Jukdoc’s specific task, which requires a thorough reading of a single document.
- The ChatCommand class defines five commands related to the chatbot’s conversation strategy. These commands are mapped to user inputs based on the command.txt file, which is created by a human.
- These commands are used in the conversation generation process within `net.sosuisen.service.ChatService`.

#### ChatMessage

This is a data class used to send AI responses from the backend to the frontend.

- `speaker`: AI or User
- `message`: The content of the message
- `refs`: A list of referenced blocks
- `suggestions`: A list of related questions

#### DatabaseSetup

onStart() is called only once when starting the Jukdoc application.
This setup creates and initializes two tables in the built-in H2 database.

- `paragraph` table: Loads data from structured_paragraph.txt and summary.txt. It uses position_tag as the primary key for searching.
- `reading_record` table: Stores each user's reading history, recording the user_name, position_tag, and reading_time when a user reads a paragraph. 
  - Initially, this table is empty.
  - It has a compound key made up of user_name and position_tag.

This built-in database is temporary, created by Payara Micro, and all data is deleted when the application shuts down.

#### Document

A data class that stores data retrieved from the EmbeddingStore.
- The `type` field indicates whether the data comes from the QA Store ("qa") or the Paragraph Store ("paragraph").

#### HistoryDocument

A data class that stores conversation history between a user and the AI.

The history field in the `net.sosuisen.model.UserStatus` class records each user’s reading history as a list of HistoryDocument objects.
 
- `query`: The user’s question.
- `answer`: The AI’s response.
- `referredDocs`: A list of Document objects used to generate the answer.

#### ParagraphDAO and ParagraphDTO

These are used to access information related to paragraphs. 

By joining `paragraph` table with the `reading_record` table, it can also add information on whether the user has read a paragraph (`isRead` field).

#### QueryDTO

A DTO that stores questions from users.

#### ReadingRecordDAO

The DAO for the `reading_record` table.

#### StaticMessage

A class for handling static messages.

#### UserStatus

A session-scoped class for handling user information.

- `history`: Conversation history between the user and AI.
- `currentPositionTag`: The position_tag of the paragraph that is the current topic of discussion between the user and AI.
- `userName`: UUIDv4 generated for each user.

### net.sosuisen.offlineutils

This package are for offline tools used to generate AI training data.

- Offline tools are typically stored in separate repositories, but they are included in the same repository as the web app for easier distribution. This package is not included in the .war file used for deployment.
- For usage details, refer to [How to Train an AI Model with Your Data](#how-to-train-an-ai-model-with-your-data).
- I originally developed these offline tools in Python but later ported them to Java. Writing parsers and handling string processing in Java was straightforward. To enable the same experience as Python command-line tools, I used the exec-maven-plugin to execute them from run_*.sh scripts.

### net.sosuisen.resources



# AI concepts integrated with Java and Jakarta EE

# Use Cases

# Future Work
