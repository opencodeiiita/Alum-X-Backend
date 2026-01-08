# Connection Accept/Reject Functionality

## Overview
This PR implements the connection accept/reject functionality as requested in issue #206. Users can now accept or reject connection requests, and fetch their pending/accepted connections.

## Changes Made

### 🔧 Core Implementation
- **Added `REJECTED` status** to `ConnectionStatus` enum
- **Enhanced `ConnectionRepository`** with query methods for fetching connections by status and user
- **Extended `ConnectionService`** interface with accept/reject and fetch methods
- **Implemented business logic** in `ConnectionServiceImpl` with proper validation
- **Added REST endpoints** in `ConnectionController` for accept/reject/fetch operations

### 📋 New API Endpoints
- `POST /api/connections/{connectionId}/accept` - Accept a pending connection request
- `POST /api/connections/{connectionId}/reject` - Reject a pending connection request  
- `GET /api/connections/pending/received` - Get pending requests received by user
- `GET /api/connections/pending/sent` - Get pending requests sent by user
- `GET /api/connections/accepted` - Get all accepted connections for user

### ✅ Validation Rules
- Only the **receiver** can accept/reject connection requests
- Only **PENDING** requests can be accepted/rejected
- Proper error handling with meaningful messages:
  - `EntityNotFoundException` for non-existent connections
  - `IllegalStateException` for unauthorized operations or invalid status changes

### 🧪 Testing
- **11 comprehensive integration tests** covering:
  - ✅ Successful accept/reject operations
  - ✅ Authorization validation (only receiver can accept/reject)
  - ✅ Status validation (only pending requests can be processed)
  - ✅ Error handling for edge cases
  - ✅ Fetching connections by different statuses
- All tests use **transactional service methods** for data consistency

## Files Modified
- `src/main/java/com/opencode/alumxbackend/connection/model/ConnectionStatus.java`
- `src/main/java/com/opencode/alumxbackend/connection/repository/ConnectionRepository.java`
- `src/main/java/com/opencode/alumxbackend/connection/service/ConnectionService.java`
- `src/main/java/com/opencode/alumxbackend/connection/service/ConnectionServiceImpl.java`
- `src/main/java/com/opencode/alumxbackend/connection/controller/ConnectionController.java`

## Files Added
- `src/test/java/com/opencode/alumxbackend/connection/controller/ConnectionControllerIntegrationTest.java`

## Testing Results
```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```
All tests pass successfully! ✅

## Issue Requirements Checklist
- [x] Add endpoint to accept connection request
- [x] Only receiver of the request should be allowed to accept it
- [x] Only requests with PENDING status can be accepted
- [x] Add API to reject a connection request
- [x] Only receiver can reject
- [x] Only PENDING requests can be rejected
- [x] Connection status changes from PENDING to ACCEPTED
- [x] Fetch User Connections (Pending & Accepted)
- [x] Error handling for connection doesn't exist
- [x] Error handling for user is not the receiver
- [x] Error handling for connection already accepted/rejected
- [x] Tests for all the above functionality
- [x] Use transactional service methods

## How to Test
1. **Send a connection request**: `POST /api/users/{targetUserId}/connect`
2. **Accept the request**: `POST /api/connections/{connectionId}/accept` (as receiver)
3. **Reject a request**: `POST /api/connections/{connectionId}/reject` (as receiver)
4. **Fetch connections**: Use the GET endpoints to view pending/accepted connections

Closes #206