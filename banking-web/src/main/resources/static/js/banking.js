// Banking System JavaScript Client
// Simple client for interacting with the Banking REST API

// Configuration
const API_BASE_URL = window.BANKING_CONFIG ? window.BANKING_CONFIG.API_BASE_URL : 'http://localhost:8080/api/v1/banking';

// Global state
let currentUser = null;
let currentCredentials = null;

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    // Update API URL display
    const apiUrlElement = document.getElementById('apiUrl');
    if (apiUrlElement) {
        apiUrlElement.textContent = API_BASE_URL.replace('/api/v1/banking', '');
    }

    checkApiStatus();
    showLoginSection();
});

// API Status Check
async function checkApiStatus() {
    try {
        const response = await fetch(`${API_BASE_URL.replace('/api/v1/banking', '')}/actuator/health`);
        if (response.ok) {
            updateApiStatus('✅ Online', 'success');
        } else {
            updateApiStatus('❌ Offline', 'error');
        }
    } catch (error) {
        updateApiStatus('❌ Offline', 'error');
    }
}

function updateApiStatus(status, type) {
    const statusElement = document.getElementById('apiStatus');
    statusElement.textContent = status;
    statusElement.className = type;
}

// UI Management
function showLoginSection() {
    document.getElementById('loginSection').classList.remove('hidden');
    document.getElementById('bankingSection').classList.add('hidden');
    document.getElementById('transactionsSection').classList.add('hidden');
}

function showBankingSection() {
    document.getElementById('loginSection').classList.add('hidden');
    document.getElementById('bankingSection').classList.remove('hidden');
    document.getElementById('transactionsSection').classList.add('hidden');
}

function showTransactionsSection() {
    document.getElementById('transactionsSection').classList.remove('hidden');
}

function hideTransactions() {
    document.getElementById('transactionsSection').classList.add('hidden');
}

// Message Management
function showMessage(message, type = 'info') {
    const messagesContainer = document.getElementById('messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;
    
    messagesContainer.appendChild(messageDiv);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (messageDiv.parentNode) {
            messageDiv.parentNode.removeChild(messageDiv);
        }
    }, 5000);
}

// API Helper Functions
async function makeApiCall(endpoint, method = 'GET', data = null) {
    try {
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            }
        };
        
        if (data) {
            options.body = JSON.stringify(data);
        }
        
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        // Check if response has content
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        } else {
            return null;
        }
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}

// Authentication Functions
async function register() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    
    if (!username || !password) {
        showMessage('Please enter both username and password', 'error');
        return;
    }
    
    try {
        await makeApiCall('/register', 'POST', { username, password });
        showMessage('Registration successful! You can now login.', 'success');
        clearLoginForm();
    } catch (error) {
        showMessage('Registration failed: ' + error.message, 'error');
    }
}

async function login() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    
    if (!username || !password) {
        showMessage('Please enter both username and password', 'error');
        return;
    }
    
    try {
        const response = await makeApiCall('/login', 'POST', { username, password });
        
        if (response) {
            currentUser = response.username;
            currentCredentials = { username, password };
            
            document.getElementById('currentUser').textContent = currentUser;
            document.getElementById('currentBalance').textContent = response.balance.toFixed(2);
            
            showBankingSection();
            showMessage(`Welcome back, ${currentUser}!`, 'success');
            clearLoginForm();
        }
    } catch (error) {
        showMessage('Login failed: ' + error.message, 'error');
    }
}

function logout() {
    currentUser = null;
    currentCredentials = null;
    showLoginSection();
    showMessage('Logged out successfully', 'info');
}

function clearLoginForm() {
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
}

// Banking Operations
async function deposit() {
    const amount = parseFloat(document.getElementById('depositAmount').value);
    
    if (!amount || amount <= 0) {
        showMessage('Please enter a valid amount', 'error');
        return;
    }
    
    if (!currentCredentials) {
        showMessage('Please login first', 'error');
        return;
    }
    
    try {
        const response = await makeApiCall('/deposit', 'POST', {
            username: currentCredentials.username,
            password: currentCredentials.password,
            amount: amount
        });
        
        if (response) {
            document.getElementById('currentBalance').textContent = response.newBalance.toFixed(2);
            showMessage(`Successfully deposited $${amount.toFixed(2)}`, 'success');
            document.getElementById('depositAmount').value = '';
        }
    } catch (error) {
        showMessage('Deposit failed: ' + error.message, 'error');
    }
}

async function withdraw() {
    const amount = parseFloat(document.getElementById('withdrawAmount').value);
    
    if (!amount || amount <= 0) {
        showMessage('Please enter a valid amount', 'error');
        return;
    }
    
    if (!currentCredentials) {
        showMessage('Please login first', 'error');
        return;
    }
    
    try {
        const response = await makeApiCall('/withdraw', 'POST', {
            username: currentCredentials.username,
            password: currentCredentials.password,
            amount: amount
        });
        
        if (response) {
            document.getElementById('currentBalance').textContent = response.newBalance.toFixed(2);
            showMessage(`Successfully withdrew $${amount.toFixed(2)}`, 'success');
            document.getElementById('withdrawAmount').value = '';
        }
    } catch (error) {
        showMessage('Withdrawal failed: ' + error.message, 'error');
    }
}

async function getBalance() {
    if (!currentCredentials) {
        showMessage('Please login first', 'error');
        return;
    }

    try {
        const response = await makeApiCall('/balance', 'POST', {
            username: currentCredentials.username,
            password: currentCredentials.password
        });

        if (response) {
            document.getElementById('currentBalance').textContent = response.balance.toFixed(2);
            showMessage('Balance updated', 'info');
        }
    } catch (error) {
        showMessage('Failed to get balance: ' + error.message, 'error');
    }
}

async function getTransactions() {
    if (!currentCredentials) {
        showMessage('Please login first', 'error');
        return;
    }

    try {
        const response = await makeApiCall('/transactions', 'POST', {
            username: currentCredentials.username,
            password: currentCredentials.password
        });

        if (response && Array.isArray(response)) {
            displayTransactions(response);
            showTransactionsSection();
        } else {
            showMessage('No transactions found', 'info');
        }
    } catch (error) {
        showMessage('Failed to get transactions: ' + error.message, 'error');
    }
}

function displayTransactions(transactions) {
    const transactionsList = document.getElementById('transactionsList');

    if (transactions.length === 0) {
        transactionsList.innerHTML = '<p>No transactions found.</p>';
        return;
    }

    const transactionsHtml = transactions.map(transaction => {
        const isDeposit = transaction.type.toLowerCase().includes('deposit');
        const amountClass = isDeposit ? 'positive' : 'negative';
        const amountPrefix = isDeposit ? '+' : '-';
        const transactionClass = isDeposit ? 'deposit' : 'withdrawal';

        return `
            <div class="transaction-item ${transactionClass}">
                <div class="transaction-details">
                    <div class="transaction-type">${transaction.type}</div>
                    <div class="transaction-date">${formatDate(transaction.timestamp)}</div>
                </div>
                <div class="transaction-amount ${amountClass}">
                    ${amountPrefix}$${transaction.amount.toFixed(2)}
                </div>
            </div>
        `;
    }).join('');

    transactionsList.innerHTML = transactionsHtml;
}

function formatDate(dateString) {
    try {
        const date = new Date(dateString);
        return date.toLocaleString();
    } catch (error) {
        return dateString;
    }
}

// Utility Functions
function clearForm(formId) {
    const form = document.getElementById(formId);
    if (form) {
        const inputs = form.querySelectorAll('input');
        inputs.forEach(input => input.value = '');
    }
}

// Keyboard shortcuts
document.addEventListener('keydown', function(event) {
    // Enter key in login form
    if (event.key === 'Enter') {
        const activeElement = document.activeElement;
        if (activeElement.id === 'username' || activeElement.id === 'password') {
            login();
        }
    }
});

// Auto-refresh API status every 30 seconds
setInterval(checkApiStatus, 30000);
