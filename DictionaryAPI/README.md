# 📚 Dictionary Application 📚

## 📋 Overview
The Dictionary Application is a Java-based ability that lets users look up words in different languages through both command-line and graphical interfaces.  
It uses multiple external APIs to **provide word definitions, translations, examples, synonyms, and antonyms.**

### External services used:
- **Dictionary API** (dictionaryapi.dev) for English definitions and word meaning check
- **MyMemory Translation API** for translations
- **Cohere AI API** for examples, synonyms, and antonyms

## ✨ Features
For each word, the application provides:
- English translation (for all languages words)
- Dictionary definition
- Example usage
- Synonyms
- Antonyms

### 📝 Additional features:
- Two versions of interfaces (CLI and GUI)
- Local caching of looked-up words
- Persistent storage (keeps previous data in a `.txt` database)
- Language history tracking (for GUI version)
- Multi-language support

## 🧩 Components
- **RunApp**: Main entry point for CLI and GUI options (user decides where to continue app execution)
- **GUI**: Graphical interface with modern styling
- **OnlineDictionary**: Core component managing the lookup process for information
- **Translator**: Converts words between languages
- **AIComponent**: Generates examples and related words
- **DataBaseManager**: Handles local storage
- **JSON_Parser**: Processes API responses
- **LanguageCodeFinder**: Maps language names to ISO codes

## 🏗️ Architecture
- **Singleton Pattern** for components needing single instances
- **MVC-like structure** separating data, logic, and interfaces
- **Local caching** to improve performance and save free tokens for the AI model

## 🚀 Usage
 ### Compile: javac RunApp.java  
Make sure you compile all components. 

 ### Execute: java RunApp
- **You don't need to run separated files for CLI/GUI, both are integrated into 'RunApp' entry point.**
- Launch and select "yes"/"y" for GUI version, otherwise you will go for CLI version.


## 📁 File Structure

- ├── src/
- │   ├── RunApp.java
- │   ├── GUI.java- 
- │   ├── components/
- │   │   ├── OnlineDictionary.java
- │   │   ├── Translator.java
- │   │   ├── AIComponent.java
- │   │   ├── DataBaseManager.java
- │   │   ├── JSON_Parser.java
- │   │   └── LanguageCodeFinder.java
- ├── resources/
- │   ├── DataBase.txt
- │   └── LanguageCodes.txt


## 🔑 API Keys and External Services
- **Dictionary API** - No key required
- **MyMemory Translation API** - No key required for limited usage
- **Cohere AI API** - free trial key with limitated tokens

## ⚠️ Error Handling
The application handles errors for cases:  
- invalid inputs
- API failures
- file access issues

## 📦 Dependencies
- Java 11 or higher
- Internet connection
- Swing library for GUI(included in standard Java)