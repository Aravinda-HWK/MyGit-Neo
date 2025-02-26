# Neo Version Control System

Neo is a lightweight version control system implemented in Java. It provides basic functionalities similar to Git, allowing users to track changes in files and manage different versions efficiently.

## Features
- **Initialize Repository**: Create a new Neo repository.
- **Add Files**: Stage files for tracking.
- **Commit Changes**: Save snapshots of the tracked files.
- **View Log**: Display commit history.
- **Check Differences**: Compare file versions.

## Installation
1. Clone the repository:
   ```sh
   git clone https://github.com/Aravinda-HWK/MyGit-Neo.git
   ```
2. Navigate to the project directory:
   ```sh
   cd MyGit-Neo
   ```
3. Compile the Java files:
   ```sh
   javac -d bin src/*.java
   ```
4. Run Neo:
   ```sh
   java -cp bin Neo
   ```

## Usage
### Initialize a Repository
```sh
java -cp bin Neo init
```

### Add a File
```sh
java -cp bin Neo add filename.txt
```

### Commit Changes
```sh
java -cp bin Neo commit "Commit message"
```

### View Commit Log
```sh
java -cp bin Neo log
```

### View Differences
```sh
java -cp bin Neo diff "commitID"
```

## Contributing
Contributions are welcome! Feel free to open an issue or submit a pull request.

## License
This project is licensed under the MIT License.

## Author
H.W.K.Aravinda


