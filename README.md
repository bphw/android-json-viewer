# JSON Viewer

A modern Android application for viewing and exploring JSON data with an intuitive, hierarchical interface.

## Features

### 🔍 **JSON Visualization**
- **Hierarchical Display**: View JSON data in a clean, tree-like structure
- **Expandable/Collapsible Nodes**: Click on objects and arrays to expand or collapse their contents
- **Deep Nesting Support**: Handle complex JSON structures with multiple levels of nesting
- **Type-Aware Display**: Different visual styling for objects `{...}`, arrays `[...]`, strings, numbers, and booleans

### 🌐 **Data Input Methods**
- **URL Input**: Fetch JSON data directly from web APIs
- **Manual Input**: Paste or type JSON data manually
- **Real-time Parsing**: Instant JSON validation and formatting

### 🎨 **User Interface**
- **Modern Design**: Clean, Material Design-inspired interface
- **Dark/Light Theme Support**: Automatic theme adaptation
- **Responsive Layout**: Optimized for various screen sizes
- **Intuitive Navigation**: Easy-to-use expand/collapse controls

### 📋 **Additional Features**
- **Copy Support**: Copy JSON values to clipboard
- **Error Handling**: Clear error messages for invalid JSON
- **Network Security**: Secure HTTPS connections for API calls

## Getting Started

### Installation
1. Clone this repository
2. Open the project in Android Studio
3. Build and run the application on your Android device or emulator

### Usage

#### Loading JSON from URL
1. Open the app
2. Tap the menu button (⋮) in the top-right corner
3. Select "Load from URL"
4. Enter a valid JSON API endpoint (e.g., `https://dummyjson.com/products`)
5. Tap "Load" to fetch and display the JSON data

#### Manual JSON Input
1. Open the app
2. Tap the menu button (⋮) in the top-right corner  
3. Select "Enter JSON manually"
4. Paste or type your JSON data
5. Tap "Parse" to visualize the data

#### Exploring JSON Data
- **Expand Objects/Arrays**: Tap on `{...}` or `[...]` to view contents
- **Collapse Sections**: Tap again to hide nested content
- **Copy Values**: Long-press on any value to copy it to clipboard
- **Navigate Deep Structures**: Recursively expand nested objects and arrays

## Technical Details

### Architecture
- **Language**: Kotlin
- **Minimum SDK**: Android API 21 (Android 5.0)
- **Target SDK**: Android API 34
- **Architecture Pattern**: MVVM with RecyclerView for efficient list rendering

### Key Components
- **JsonItemAdapter**: Handles recursive rendering of JSON structures
- **JsonItem**: Data model representing JSON nodes with type information
- **MainActivity**: Main interface for JSON input and display
- **Network Layer**: Secure HTTP client for API requests

### JSON Type Support
- **Objects**: Displayed as expandable `{...}` nodes
- **Arrays**: Displayed as expandable `[...]` nodes  
- **Strings**: Text values with proper escaping
- **Numbers**: Integer and decimal number support
- **Booleans**: True/false values
- **Null**: Explicit null value display

## Example JSON APIs to Try

- **Products**: `https://dummyjson.com/products`
- **Users**: `https://dummyjson.com/users`  
- **Posts**: `https://jsonplaceholder.typicode.com/posts`
- **GitHub API**: `https://api.github.com/users/octocat`

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is open source and available under the [MIT License](LICENSE).

## Screenshots

The app features a clean interface with:
- Purple-themed header with "JSON Viewer" title
- Expandable JSON tree structure
- Color-coded JSON elements (keys, values, types)
- Intuitive expand/collapse controls
- Responsive design for various screen sizes

---

**Built with ❤️ for developers who work with JSON data**