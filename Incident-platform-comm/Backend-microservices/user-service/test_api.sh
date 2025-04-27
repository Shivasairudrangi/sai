#!/bin/bash

# Try both common ports
PORTS=(8081 8080)
BASE_URL=""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color
BLUE='\033[0;34m'

# Function to check if port is accessible
check_port() {
    local port=$1
    curl --connect-timeout 1 -s "http://localhost:${port}/api/auth/signin" > /dev/null
    return $?
}

# Try to find the correct port
for port in "${PORTS[@]}"; do
    if check_port $port; then
        BASE_URL="http://localhost:${port}/api"
        echo -e "${GREEN}Successfully connected to port ${port}${NC}"
        break
    fi
done

if [ -z "$BASE_URL" ]; then
    echo -e "${RED}Could not connect to either port 8081 or 8080${NC}"
    echo -e "${BLUE}Please ensure your Spring Boot application is running${NC}"
    echo -e "You can start it with: ${GREEN}./mvnw spring-boot:run${NC}"
    exit 1
fi

# Function to print colored output
print_test() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
    echo -e "${GREEN}Request:${NC} $2"
    echo -e "${GREEN}Response:${NC}"
}

# Function to format response
format_response() {
    if [ -z "$1" ]; then
        echo -e "${RED}No response received${NC}"
    else
        echo "$1"
    fi
}

echo -e "${BLUE}Testing User Service API${NC}"
echo -e "${GREEN}Server URL:${NC} $BASE_URL"
echo "Press Enter to start testing..."
read

# Test server connection first
echo -e "\nTesting server connection..."
HEALTH_CHECK=$(curl -s -w "%{http_code}" "${BASE_URL}/auth/signin" -o /dev/null)
if [ "$HEALTH_CHECK" == "000" ]; then
    echo -e "${RED}Cannot connect to server. Please check if:${NC}"
    echo "1. Spring Boot application is running"
    echo "2. MySQL is running"
    echo "3. No firewall is blocking the connection"
    echo -e "\nYou can start the services with:"
    echo -e "${GREEN}MySQL:${NC} sudo service mysql start"
    echo -e "${GREEN}Spring Boot:${NC} ./mvnw spring-boot:run"
    exit 1
fi

# 1. Register Admin User
print_test "1. Register Admin User" "POST /api/auth/signup"
curl -v -X POST "${BASE_URL}/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "shivasai",
    "email": "shivasai.rudrangi@gmail.com",
    "password": "Admin123",
    "firstName": "Shivasai",
    "lastName": "Rudrangi",
    "roles": ["admin"]
  }'

echo -e "\nPress Enter to continue..."
read

# 2. Login as Admin
print_test "2. Login as Admin" "POST /api/auth/signin"
ADMIN_LOGIN_RESPONSE=$(curl -v -X POST "${BASE_URL}/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "shivasai",
    "password": "Admin123"
  }')
format_response "$ADMIN_LOGIN_RESPONSE"

echo -e "\nCopy the token from above response and enter it here:"
read ADMIN_TOKEN

if [ ! -z "$ADMIN_TOKEN" ]; then
    # 3. Get All Users (Admin only)
    print_test "3. Get All Users (Admin)" "GET /api/users"
    curl -v -X GET "${BASE_URL}/users" \
      -H "Authorization: Bearer $ADMIN_TOKEN"

    echo -e "\nPress Enter to continue..."
    read

    # 4. Get User by ID
    print_test "4. Get User by ID" "GET /api/users/1"
    curl -v -X GET "${BASE_URL}/users/1" \
      -H "Authorization: Bearer $ADMIN_TOKEN"

    echo -e "\nPress Enter to continue..."
    read

    # 5. Update User
    print_test "5. Update User" "PUT /api/users/1"
    curl -v -X PUT "${BASE_URL}/users/1" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "firstName": "Updated",
        "lastName": "User",
        "email": "updated.user@example.com"
      }'

    echo -e "\nPress Enter to continue..."
    read

    # 6. Get User Profile
    print_test "6. Get User Profile" "GET /api/users/me"
    curl -v -X GET "${BASE_URL}/users/me" \
      -H "Authorization: Bearer $ADMIN_TOKEN"

    echo -e "\nPress Enter to continue..."
    read

    # 7. Delete User
    print_test "7. Delete User" "DELETE /api/users/2"
    curl -v -X DELETE "${BASE_URL}/users/2" \
      -H "Authorization: Bearer $ADMIN_TOKEN"
fi

echo -e "\n${BLUE}API Testing Complete!${NC}" 