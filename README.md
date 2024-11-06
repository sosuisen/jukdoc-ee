# Jukdoc

Jukdoc is an onboarding training app supported by AI.

You can access the online demo site here:

https://scrapbox.io/jukdoc/Jukdoc_Site

A demo video is also available on YouTube: https://youtu.be/anR6sW3kAGw

# Project Overview

In onboarding training, employees typically need to study job-related documents, such as:

- Company overview materials
- Employee handbook
- Operations manual
- IT system usage guide
- Health and safety manual
- Compliance guidelines

These documents contain job-specific knowledge essential for the company. You are expected to read each document thoroughly at least once.

Traditionally, training was conducted with human instructors and specialized texts. Jukdoc, however, aims to streamline much of this process through AI-driven interactions.

**A unique feature of Jukdoc is its visual tracking of document sections, showing read and unread parts with color changes and a progress indicator displayed as a percentage.**

Think of Jukdoc as an AI chat app designed to guide you through an entire document. It’s not a general-purpose AI for answering any question, nor is it a support desk app. It’s also not meant for skimming or reading selected portions. Upon reaching 100% completion, you can be confident you’ve read the entire document.

<img src="./docs/jukdoc_completed.png" alt="Reading completion rate is 100%" width="300px">

---
# Table of Contents

- [Setup and Run Instractions](#setup-and-run-instructions)
- [How to Use](#how-to-use)
- [How to Train an AI Model with Your Data](#how-to-train-an-ai-model-with-your-data)
- [Technology Details](#technology-details)
- [Code Structure](#code-structure)
- [AI Concepts Integrated with Java and Jakarta EE](#ai-concepts-integrated-with-java-and-jakarta-ee)
- [Use Cases and Related Work](#use-cases-and-related-work)
- [Future Work](#future-work)
- [Conclusion](#conclusion)

---

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
- Google Chrome (for Windows) and Firefox (for Linux) as required by payara-micro-maven-plugin, which uses Selenium for development

This project has been tested in Windows and WSL environments.

## Build

### Clone the repository

```shell
git clone https://github.com/sosuisen/jukdoc-ee.git
```

### Build .war and Docker image

Start Docker before running the following command. 
For Linux and Mac systems, convert the line endings of the `mvnw` file to LF.

```shell
cd path-to-your-jukdoc-ee-repo
mvnw clean package
```

## Run

When you run the application, a chat app using sample data will launch.

First, set your OpenAI API key as an environment variable named `OPENAI_API_KEY`.

For Windows, restart the terminal to apply the environment variables.

### Running in Production Mode

```shell
mvnw clean package payara-micro:start
```

Access the application at http://localhost:8080/,
which redirects to http://localhost:8080/jukdoc/.

### Running in Dev Mode

Payara Starter’s Dev Mode supports hot reloading. If you modify the source code, the application automatically rebuilds and updates in the browser.

To run in Dev Mode, use the following command:

```shell
mvnw clean package payara-micro:dev
```
The application will open in your browser automatically.

## Deployment

You can deploy the Jukdoc application on AWS using Docker.
Instructions for deployment on AWS Elastic Beanstalk are available here:
https://www.payara.fish/resource/using-payara-platform-with-docker-on-amazon-aws/

Additional Notes:

- Starting in October 2024, Auto Scaling on Elastic Beanstalk requires a Launch Template. Configuration settings for this template are already included in the `.ebextensions/launch-template.config` file in the jukdoc-ee repository.
- For optimal performance, avoid the `t3.micro` instance type. Instead, use `t3.small`, `t3.medium`, or larger.

# How to Use

This app helps you navigate documents through conversations with AI.
The jukdoc-ee repository includes sample data tailored to onboarding training for pharmaceutical sales representatives.
To train the AI model with your own data, see
[How to Train an AI Model with Your Data](#how-to-train-an-ai-model-with-your-data).

(Note: The sample data is fictional and intended for proof-of-concept.)

The screen is divided into four main sections.

## Header Area

At the top is the header section.

- The center shows your completion rate for the document, starting at 0% with a target of 100%.
- Your username appears on the right, generated automatically for each session. Restarting the server resets your completion rate to 0% and creates a new username.
- Below the username is the "Delete All Records" button, which resets your reading history.

<img src="./docs/header_area.png" alt="Header Area" width="700px">

## Document Area

On the left side is the document area, where the document you need to read is displayed.

- Each paragraph’s reading status is tracked. Read paragraphs are highlighted in orange.
- When all paragraphs are read, your completion rate reaches 100%.
- A "Read" button at the end of each paragraph allows you to mark it as read, with the AI explaining its content.

<img src="./docs/document_area.png" alt="Document Area" width="200px">

## Chat Area

On the right is the chat area, the main space for interacting with the AI.

- Here, you can ask questions about the document by pressing "Send." The AI’s responses may include references to specific paragraphs, which are then marked as read.
- Links within responses allow you to navigate to referenced paragraphs in the document.
- If the AI’s answer is not based on the document, it won’t include any paragraph references, and no paragraphs will be marked as read.

<img src="./docs/chat_area.png" alt="Chat Area" width="200px">

## Suggested Questions Area

- Below the chat area, suggested questions appear.
- If relevant questions exist based on the last response, up to two suggestions are displayed.
- Options to proceed to the next topic or unread sections are also available.

<img src="./docs/suggested_questoins_area.png" alt="Suggested Questions Area" width="500px">

# How to Train an AI Model with Your Data

- The jukdoc-ee repository includes tools for offline training with your own document data.
- These tools are located in the `net.sosuisen.offlineutils` package (src/main/java/net/sosuisen/offlineutils/). Although offline tools are typically in separate repositories, they are included in the same repository as the web app for easier distribution. This package is not included in the .war file used for deployment.
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

## net.sosuisen.ai

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
  - The `VectorStoreLoader` class loads pre-trained AI data, serialized as JSON files, into the `InMemoryEmbeddingStore`.

## net.sosuisen.controller

This package contains only the ChatController class, which serves as a controller for Jakarta MVC.
- ChatController is a simple controller that renders index.jsp when the context root is accessed.
- If a username does not exist in the session at that time, it generates a UUID and sets it as the username in the session.
- This session is managed by the `net.sosuisen.model.UserStatus` class, which has a CDI session scope.

## net.sosuisen.exceptions

This package contains only the ConstraintViolationExceptionMapper class, which handles errors from Jakarta Bean Validation.

## net.sosuisen.model

This package contains Jakarta MVC and Jakarta REST model classes.

### ChatCommand

Jukdoc’s conversation strategy combines a Large Language Model (LLM) with a traditional chatbot. This combination is necessary because LLMs alone aren’t sufficient for Jukdoc’s specific task, which requires a thorough reading of a single document.
- The ChatCommand class defines five commands related to the chatbot’s conversation strategy. These commands are mapped to user inputs based on the command.txt file, which is created by a human.
- These commands are used in the conversation generation process within `net.sosuisen.service.ChatService`.

### ChatMessage

This is a data class used to send AI responses from the backend to the frontend.

- `speaker`: AI or User
- `message`: The content of the message
- `refs`: A list of referenced blocks
- `suggestions`: A list of related questions

### DatabaseSetup

onStart() is called only once when starting the Jukdoc application.
This setup creates and initializes two tables in the built-in H2 database.

- `paragraph` table: Loads data from structured_paragraph.txt and summary.txt. It uses position_tag as the primary key for searching.
- `reading_record` table: Stores each user's reading history, recording the user_name, position_tag, and reading_time when a user reads a paragraph. 
  - Initially, this table is empty.
  - It has a compound key made up of user_name and position_tag.

This built-in database is temporary, created by Payara Micro, and all data is deleted when the application shuts down.

### Document

A data class that stores data retrieved from the EmbeddingStore.
- The `type` field indicates whether the data comes from the QA Store ("qa") or the Paragraph Store ("paragraph").

### HistoryDocument

A data class that stores conversation history between a user and the AI.

The history field in the `net.sosuisen.model.UserStatus` class records each user’s reading history as a list of HistoryDocument objects.
 
- `query`: The user’s question.
- `answer`: The AI’s response.
- `referredDocs`: A list of Document objects used to generate the answer.

### ParagraphDAO and ParagraphDTO

These are used to access information related to paragraphs. 

By joining `paragraph` table with the `reading_record` table, it can also add information on whether the user has read a paragraph (`isRead` field).

### QueryDTO

A DTO that stores questions from users.

### ReadingRecordDAO

The DAO for the `reading_record` table.

### StaticMessage

A class for handling static messages.

### UserStatus

A session-scoped class for handling user information.

- `history`: Conversation history between the user and AI.
- `currentPositionTag`: The position_tag of the paragraph that is the current topic of discussion between the user and AI.
- `userName`: UUIDv4 generated for each user.

## net.sosuisen.offlineutils

This package are for offline tools used to generate AI training data.

- Offline tools are typically stored in separate repositories, but they are included in the same repository as the web app for easier distribution. This package is not included in the .war file used for deployment.
- For usage details, refer to [How to Train an AI Model with Your Data](#how-to-train-an-ai-model-with-your-data).
- I originally developed these offline tools in Python but later ported them to Java. Writing parsers and handling string processing in Java was straightforward. To enable the same experience as Python command-line tools, I used the exec-maven-plugin to execute them from run_*.sh scripts.

## net.sosuisen.resources

This package contains Resource classes for Jakarta REST, providing APIs for chat and document-related interactions. 

### Chat

API for Chat Functions

- **GET /api/chat/opening-words**
  - Returns messages stored in opening_words.txt, which can be used as introductory chat messages.

- **POST /api/chat/query**
  - Responds to user questions, using one of three conversation strategies:
    1. **PROCEED_FROM_INDICATED_POSITION:** If the user specifies a particular position_tag, the conversation is generated from that point.
    2. **Command Execution:** If the question matches an entry in commands.txt, the corresponding command is executed.
    3. **AI Chat Processing:** If the question doesn’t match any command, an AI-driven chat process is initiated.

The handling of commands and AI processing is delegated to the 
`net.sosuisen.service.ChatService` class (injected with @Inject),
allowing the `net.sosuisen.resources.Chat` class to focus on endpoint management.

### Document

API for Document Management

- **GET /api/document**
  - Retrieves and returns all blocks, mainly for displaying in the Document Area.

- **DELETE /api/document/reading-record**
  - Deletes all reading records, triggered when the "Delete All Records" button is pressed.

## net.sosuisen.security

### CsrfValidator

This class provides CSRF token validation for classes in the `net.sosuisen.resources` package, ensuring secure communication.

## net.sosuisen.service

This package centralizes application logic that communicates with external services, specifically the OpenAI API in the Jukdoc application.

### ChatService

This class provides a chat service that generates responses to user questions.

Jukdoc’s conversational strategy combines AI Chat Processing using RAG (Retrieval-Augmented Generation) with a traditional chatbot approach.

### *AI Chat Processing* in ChatService

- `proceedByPrompt` Method
  - AI Chat Processing with RAG is handled by the `proceedByPrompt` method.
  - Jukdoc uses LangChain4j as the AI library, and due to the unique QA approach in Jukdoc, lower-level APIs are used.
  - The `proceedByPrompt` method retrieves documents relevant to the user question using the `retrieveDocuments` method.
  - If the question is too short or contains anaphora that could hinder accurate search results, it retrieves related documents from conversation history instead.
  - The retrieved documents (`retrievalDocs`) and the user question are then passed to the `promptToAI` method to initiate RAG.
- `retrieveDocuments` Method
  - This method delegates document retrieval to the ParagraphService and QAService classes (details covered below).
  - It merges the search results from the QA Store and Paragraph Store and returns them sorted by relevance score.
- `promptToAI` Method
  - This method sends prompts to the AI.
  - It calls the `getPrompt` method to generate the prompt.
  - The response generation process utilizes the injected `net.sosuisen.ai.service.AssistantService`.
  - The generated answer includes references such as [*1][*2], which point to referenced documents. Each document is a block identified by a position_tag in Jukdoc. This reference information is then formatted for frontend use and stored in a ChatMessage object.
  - The conversation history is saved in the history of the UserStatus object.
  - If reference information is present, the currentPositionTag in UserStatus is updated to point to the relevant position_tag.
  - Reading history is recorded in the database’s `reading_record` table.
  - The `suggest` method of `net.sosuisen.service.SuggestService` is called to suggest related questions based on the generated response.
  - Finally, a ChatMessage object is created and returned as the response for the frontend.
- `getPrompt` Method
  - The main features of the prompt Jukdoc sends to the AI include:
    - Conversation history with the user.
    - Retrieved documents.
    - Reference markers [*1][*2] are included if the response is based on the retrieved documents.

### *Traditional Chatbot Approach* in ChatService

Jukdoc also uses a traditional chatbot approach due to its unique strategy of reading the next paragraph or jumping to unread paragraphs.

- `proceedByCommand` Method
  - If a user question corresponds to a predefined command, the `proceedByCommand` method executes.

Five commands are defined:
  - **PROCEED_FROM_BEGINNING:** 
    - Answers the first paragraph of the target document, using the paragraph’s summary.

  - **PROCEED_FROM_UNREAD:**
    - Answers the first unread paragraph of the target document, using the paragraph’s summary.

  - **PROCEED_FROM_INDICATED_POSITION:**
    - Answers a specified paragraph, using the paragraph’s summary.

  - **PROCEED_CURRENT_TOPIC:**
    - Answers the paragraph following the currently specified paragraph in `UserStatus.currentPositionTag`, using its summary.

  - **REPEAT_ONLY_CURRENT_TOPIC:**
    - Re-answers the current paragraph, calling `promptToAI` method to process the question again.

### ParagraphService

This service provides paragraph search functionality using EmbeddingStore. 

The search functionality uses an injected EmbeddingSearchService, with parameters such as the serialized JSON filename of training data, search result limit, and threshold specified via annotations.

### QAService

This service provides QA search functionality using EmbeddingStore. 

The search functionality uses an injected EmbeddingSearchService, with parameters for the serialized JSON filename and result limit specified via annotations.

These parameters differ from those in ParagraphService.

### SuggestService

This service provides a search for questions similar to a specified sentence using EmbeddingStore.

Like ParagraphService and QAService, it injects EmbeddingSearchService. The suggest method returns four question suggestions: two questions similar to the AI-generated answer and two default questions ("Move on to the next topic." and "Read the unread parts.").


## Overview of the Frontend

The frontend of Jukdoc is a single-page application (SPA) built with Jakarta MVC and Alpine.js.
This section provides a high-level summary.

### src/main/webapp/rest.js

This is a REST utility code developed for Alpine.js.

It simplifies the process of calling REST APIs, receiving results, and handling errors.

### src/main/webapp/WEB-INF/views/index.jsp

This file contains HTML elements with embedded Alpine.js tags and attributes.

Since Alpine.js keeps the HTML structure intact, understanding the layout is straightforward.

REST calls are made using the `$post()`, `$get()`, and `$delete()` functions.

Responses from the backend are reflected reactively in the HTML display.

This setup allows for seamless integration between REST API responses and Alpine.js updates in the user interface.


# AI concepts integrated with Java and Jakarta EE

## Adopted AI Approaches

Jukdoc enhances an interactive web app built with Jakarta MVC and Jakarta REST using AI. The AI approaches used are as follows:

- (a) Automatic generation of QA pairs from plain text using Generative AI.
- (b) Document retrieval using Embedding Stores (Vector Stores) for related content.
- (c) Conversation generation based on chat history, search results, and Generative AI.

Of these, (a) is an offline process, while (b) and (c) are online processes.

In Jukdoc, these AI services are used multiple times within the web app, so they are provided as services through CDI Producers, allowing for injection where needed in the application logic. This setup also makes it easy to configure different parameters for each area of logic that utilizes the service.

Additionally, AI parameter tuning is often necessary to achieve optimal results, so parameters are set based on annotations. This declarative approach using annotations simplifies the tuning process.

## Use of AI Library

Jukdoc’s conversation strategy combines RAG (Retrieval-Augmented Generation) with a traditional chatbot approach. This is because Jukdoc’s goal is not just partial document QA, but rather reading through the entire document, which requires strict management of conversation order and history.

The AI library used is LangChain4j, which provides high-level APIs (AiServices) and low-level APIs. Since Jukdoc requires fine optimization, the low-level API was selected. Jukdoc manages chat memory within the app and occasionally uses a traditional chatbot to maintain conversations.

## Offline Use

AI use is divided into offline training and online usage.

While the offline tools could ideally be a separate project, they are included in the same jukdoc-ee repository for ease of distribution and documentation. The pom.xml configuration excludes them from the .war file.

These tools were initially developed in Python and later ported to Java. Writing parsers and handling string processing was straightforward in Java. Additionally, using exec-maven-plugin allowed for command-line execution of the Maven project in a Python-like manner, making it convenient.


# Use Cases and Related Work

The primary purpose of Jukdoc is onboarding training. In this context, users are often required to review specific documents thoroughly.

The motivation behind developing Jukdoc stemmed from dissatisfaction with existing RAG approaches. I once tried reading a philosophy book using RAG. Although I gained a lot of knowledge from it, one major question remained: I couldn’t tell what percentage of the book I had actually read. RAG provided answers by utilizing not only the original book but also the vast online knowledge within the LLM. As a result, I didn’t know exactly what and how much of the original book I had covered. Wanting an answer to this led me to create Jukdoc.

Google NotebookLM employs a similar approach to Jukdoc, but it doesn’t seem focused on tracking what’s been read.

With Jukdoc, you can rely on AI to help you read a specific book and track your progress. It enables you to work with AI to know exactly which parts of the book you’ve read. Jukdoc is ideal for thoroughly reading a single book and could also assist in learning from textbooks at both high school and university levels.

The name "Jukdoc" comes from the Japanese word “熟読” (juku doku), meaning “thorough reading.”

# Future Work

## Usability Enhancements:

- The background color of read paragraphs is always orange; it would be more helpful if the color darkened each time a paragraph is referenced.
- Implement server-side AI training on uploaded documents for increased convenience.

## Infrastructure

- Production data storage could benefit from external services like S3, instead of the `src/main/resources` directory.
- An external database is recommended for production use, replacing the built-in H2 database used in this test setup.


# Conclusion

This project serves as a proof of concept,
exploring how AI can enhance our lives by assisting with thorough document reading.
Your feedback is welcome!


Hidekazu Kubota
