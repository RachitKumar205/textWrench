# textWrench

textWrench is a text editor application built using Java and JavaFX. It provides a simple and intuitive interface for editing text files, with features such as syntax highlighting, project management, and integration with the Language Server Protocol (LSP) for enhanced code editing capabilities.

## Requirements

- JDK 23.0 or higher
- Maven
- JavaFX 23.0.1

## Installation

1. **Clone the repository**:
    ```sh
    git clone https://github.com/RachitKumar205/textWrench.git
    cd textWrench
    ```

2. **Build the project using Maven**:
    ```sh
    mvn clean install
    ```

3. **Run the application**:
    ```sh
    mvn javafx:run
    ```

## Usage

- **Open a Project**: Use the "Open Project" option to select a project directory and load its structure.
- **Create a New File**: Use the "New" option or `Ctrl+N` to create a new file.
- **Open a File**: Use the "Open" option or `Ctrl+O` to open an existing file.
- **Save a File**: Use the "Save" option or `Ctrl+S` to save the current file.
- **Close the Application**: Use the "Exit" option to close the application, ensuring all unsaved changes are handled.

## Project Structure

- `src/main/java/com/example/textwrench`: Contains the main application code.
- `src/main/resources`: Contains FXML files and other resources.
- `pom.xml`: Maven configuration file.

## Dependencies

- JavaFX 23.0.1
- ControlsFX 11.2.1
- Ikonli 12.3.0
- RichTextFX 0.11.0
- LSP4J 0.23.1
- JUnit 5.10.2

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.