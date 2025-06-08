# Banking Web Application

A simple JavaScript web frontend for the Banking REST API.

## Overview

This is a lightweight web application that provides a user-friendly interface for the banking system. It communicates with the Banking API to perform all banking operations.

## Features

- **User Authentication**: Login and registration
- **Account Management**: View balance and account information
- **Transactions**: Deposit and withdraw money
- **Transaction History**: View all past transactions
- **Real-time Updates**: Balance updates after each transaction
- **Responsive Design**: Works on desktop and mobile devices
- **API Status Monitoring**: Shows connection status to the backend API

## Technology Stack

- **Frontend**: Pure HTML5, CSS3, and JavaScript (ES6+)
- **Build Tool**: Maven with Jetty plugin
- **Packaging**: WAR file for deployment
- **API Communication**: Fetch API for REST calls

## Project Structure

```
banking-web/
├── pom.xml                          # Maven configuration
├── src/main/webapp/
│   ├── index.html                   # Main HTML page
│   ├── css/
│   │   └── style.css               # Styles and responsive design
│   └── js/
│       └── banking.js              # JavaScript client logic
└── README.md                       # This file
```

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Banking API running on http://localhost:8080

### Quick Start

1. **Build the application:**
   ```bash
   mvn clean package
   ```

2. **Start with Jetty (Development):**
   ```bash
   mvn jetty:run
   ```

3. **Access the application:**
   - Open http://localhost:3000 in your browser

### Alternative Startup Methods

#### Using the startup script:
```bash
# Windows
start-banking-web.bat
```

#### Deploy to external server:
```bash
# Build WAR file
mvn clean package

# Deploy banking-web.war to your application server
# (Tomcat, Jetty, etc.)
```

## Configuration

### API Endpoint Configuration
The API base URL is configured in `js/banking.js`:

```javascript
const API_BASE_URL = 'http://localhost:8080/api/v1/banking';
```

To change the API endpoint, modify this constant.

### Port Configuration
The development server port is configured in `pom.xml`:

```xml
<httpConnector>
    <port>3000</port>
</httpConnector>
```

## Usage

### Getting Started
1. Make sure the Banking API is running on port 8080
2. Open the web application at http://localhost:3000
3. Register a new user or login with existing credentials

### Available Operations

#### User Management
- **Register**: Create a new user account
- **Login**: Authenticate with username and password
- **Logout**: End the current session

#### Banking Operations
- **Deposit**: Add money to your account
- **Withdraw**: Remove money from your account
- **View Balance**: Check current account balance
- **Transaction History**: View all past transactions

### API Integration

The web application communicates with the following API endpoints:

- `POST /api/v1/banking/register` - User registration
- `POST /api/v1/banking/login` - User authentication
- `POST /api/v1/banking/deposit` - Deposit money
- `POST /api/v1/banking/withdraw` - Withdraw money
- `POST /api/v1/banking/balance` - Get account balance
- `POST /api/v1/banking/transactions` - Get transaction history

## Features in Detail

### Responsive Design
- Mobile-first approach
- Flexible grid layout
- Touch-friendly buttons
- Optimized for various screen sizes

### User Experience
- Real-time feedback messages
- Loading states and error handling
- Keyboard shortcuts (Enter to login)
- Auto-clearing forms after successful operations

### Security Considerations
- Credentials are only stored in memory during the session
- No persistent storage of sensitive data
- HTTPS recommended for production use

## Development

### File Structure
- `index.html`: Main application layout and structure
- `css/style.css`: All styling including responsive design
- `js/banking.js`: Client-side logic and API communication

### Adding New Features
1. Add HTML elements to `index.html`
2. Style them in `css/style.css`
3. Implement functionality in `js/banking.js`

### Debugging
- Open browser developer tools (F12)
- Check console for JavaScript errors
- Monitor network tab for API calls
- Use the API status indicator for connectivity issues

## Deployment

### Development Deployment
Use the built-in Jetty server for development:
```bash
mvn jetty:run
```

### Production Deployment
1. Build the WAR file:
   ```bash
   mvn clean package
   ```

2. Deploy `target/banking-web.war` to your application server

3. Configure the API endpoint URL if different from localhost

## Troubleshooting

### Common Issues

1. **API Connection Failed**
   - Ensure Banking API is running on port 8080
   - Check firewall settings
   - Verify API endpoint URL in banking.js

2. **CORS Issues**
   - Banking API includes CORS configuration
   - For production, configure proper CORS origins

3. **Build Failures**
   - Ensure Java 17+ is installed
   - Check Maven configuration
   - Verify all dependencies are available

### Browser Compatibility
- Modern browsers with ES6+ support
- Chrome 60+, Firefox 55+, Safari 12+, Edge 79+

## Future Enhancements

Potential improvements for the web application:

1. **Enhanced UI/UX**
   - Dark mode toggle
   - Better animations and transitions
   - Progressive Web App (PWA) features

2. **Additional Features**
   - Account settings page
   - Transaction filtering and search
   - Export transaction history
   - Multi-language support

3. **Technical Improvements**
   - TypeScript conversion
   - Modern build tools (Webpack, Vite)
   - Component-based architecture
   - Unit and integration tests
