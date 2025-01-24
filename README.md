# Space Engineers Remote API Client

Java client for interacting with Space Engineers Dedicated Server Remote API.

## Setup

Add to your build.gradle:
```gradle
dependencies {
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
}
```

## Usage

Initialize client:
```java
SpaceEngineersRemoteClient client = new SpaceEngineersRemoteClient(
    "http://localhost:8080",  // Server URL
    "your-base64-security-key"
);
```

Available methods:
```java
// Check server status
client.pingServer();

// Get current players
client.getCurrentPlayers();
```

## API Authentication

The client handles HMAC-SHA1 authentication automatically:
- Generates RFC1123 formatted UTC timestamp
- Creates random nonce
- Builds authentication message string
- Computes HMAC using provided security key
- Adds required headers (Date, Authorization)