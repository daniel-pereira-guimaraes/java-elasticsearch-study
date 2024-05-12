# Java & Elasticsearch Study

## Introduction

Welcome to the Java Elasticsearch Study Project! Here, we dive deep into integrating Java applications with Elasticsearch, a powerful tool for data search and analysis. Whether you're a beginner or looking to expand your expertise, you'll find practical examples and valuable insights to get started.

## Objective

The main goal of this study project is to explore the fundamental concepts and techniques involved in accessing Elasticsearch through a Java application. We provide multiple implementations of a repository interface (`PersonRepository`) to demonstrate alternative approaches, rather than relying on a single method of access. By examining these implementations, you will gain a deeper understanding of how to integrate Elasticsearch seamlessly into your Java projects and leverage its capabilities effectively.

## Technologies

- Java 17
- Elasticsearch 8.13.0
- Elasticsearch Java API 8.13.2
- Jackson Library 2.17.0
- Apache HttpClient
- Maven

This section provides a brief overview of the key technologies and resources utilized in the Java Elasticsearch Study Project.

## Project Structure

- **elastic Package**: Main application classes and entry point.
  - **App.java**: Main class controlling the application flow.

- **elastic.model Package**: Model classes representing entities.
  - **Person.java**: Represents a person entity.
  - **LocalDateConverter.java**: Utility class with methods for converting LocalDate to Integer and vice versa.
  - **PersonNotFoundException.java**: Custom exception class for when a person is not found.

- **elastic.infra Package**: Infrastructure classes for data access and serialization.
  - **Serializer.java**: Implements a serializer/deserializer, encapsulating an ObjectMapper from the Jackson library.
  - **PersonDocument.java**: Represents a person as a document in the context of Elasticsearch.
  - **HttpClientJsonPersonRepository.java**: Implementation of PersonRepository directly accessing the Elasticsearch API using HttpClient, manipulating JSON.
  - **ElasticFactory.java**: Responsible for creating an Elasticsearch connection instance using the Java API for Elasticsearch.
  - **ElasticClientPersonRepositoryBase.java**: Abstract class implementing common methods for inherited classes (ElasticClientJsonPersonRepository and ElasticClientPersonRepository).
  - **ElasticClientJsonPersonRepository.java**: Implementation of PersonRepository accessing the Elasticsearch API using the Java API for Elasticsearch, manipulating JSON.
  - **ElasticClientPersonRepository.java**: Implementation of PersonRepository accessing the Elasticsearch API using the Java API for Elasticsearch, manipulating Person instances as objects instead of manipulating JSON.

This section provides a brief overview of each class and interface in the project, outlining their purposes and responsibilities.

---

Feel free to dive into the code, experiment with the implementations, and expand upon them to suit your specific use cases. Happy exploring!
