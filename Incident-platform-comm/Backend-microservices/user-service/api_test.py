import requests
import json

# API Base URL - change this to match your server
BASE_URL = "http://localhost:8080/api"

def print_test(name, response):
    print(f"\n{'='*50}")
    print(f"Testing: {name}")
    print(f"Status Code: {response.status_code}")
    print("Response:")
    try:
        print(json.dumps(response.json(), indent=2))
    except:
        print(response.text)
    print('='*50)

def test_user_service():
    # Store the authentication token
    auth_token = None
    
    # 1. Register a normal user
    print("\n1. Testing User Registration")
    register_data = {
        "username": "john.doe",
        "email": "john.doe@example.com",
        "password": "password123",
        "roles": ["USER"]
    }
    
    response = requests.post(
        f"{BASE_URL}/auth/signup",
        json=register_data
    )
    print_test("Register User", response)

    # 2. Register an admin user
    print("\n2. Testing Admin Registration")
    admin_data = {
        "username": "admin.user",
        "email": "admin@example.com",
        "password": "admin123",
        "roles": ["ADMIN"]
    }
    
    response = requests.post(
        f"{BASE_URL}/auth/signup",
        json=admin_data
    )
    print_test("Register Admin", response)

    # 3. Login as admin
    print("\n3. Testing Admin Login")
    login_data = {
        "username": "admin.user",
        "password": "admin123"
    }
    
    response = requests.post(
        f"{BASE_URL}/auth/signin",
        json=login_data
    )
    print_test("Admin Login", response)
    
    if response.status_code == 200:
        auth_token = response.json().get("accessToken")
        print("Successfully got admin token!")
    
    if auth_token:
        headers = {
            "Authorization": f"Bearer {auth_token}",
            "Content-Type": "application/json"
        }
        
        # 4. Get all users (Admin only)
        print("\n4. Testing Get All Users")
        response = requests.get(
            f"{BASE_URL}/users",
            headers=headers
        )
        print_test("Get All Users", response)
        
        # 5. Get specific user by ID
        print("\n5. Testing Get User by ID")
        response = requests.get(
            f"{BASE_URL}/users/1",
            headers=headers
        )
        print_test("Get User by ID", response)
        
        # 6. Update user
        print("\n6. Testing Update User")
        update_data = {
            "username": "john.doe.updated",
            "email": "john.updated@example.com"
        }
        response = requests.put(
            f"{BASE_URL}/users/1",
            headers=headers,
            json=update_data
        )
        print_test("Update User", response)

    # 7. Login as normal user
    print("\n7. Testing Normal User Login")
    login_data = {
        "username": "john.doe",
        "password": "password123"
    }
    
    response = requests.post(
        f"{BASE_URL}/auth/signin",
        json=login_data
    )
    print_test("User Login", response)
    
    if response.status_code == 200:
        user_token = response.json().get("accessToken")
        user_headers = {
            "Authorization": f"Bearer {user_token}",
            "Content-Type": "application/json"
        }
        
        # 8. Get current user profile
        print("\n8. Testing Get Current User Profile")
        response = requests.get(
            f"{BASE_URL}/users/me?username=john.doe",
            headers=user_headers
        )
        print_test("Get Current User Profile", response)

    # 9. Test invalid login
    print("\n9. Testing Invalid Login")
    invalid_login = {
        "username": "nonexistent",
        "password": "wrongpass"
    }
    response = requests.post(
        f"{BASE_URL}/auth/signin",
        json=invalid_login
    )
    print_test("Invalid Login", response)

    if auth_token:
        # 10. Delete user (Admin only)
        print("\n10. Testing Delete User")
        response = requests.delete(
            f"{BASE_URL}/users/1",
            headers=headers
        )
        print_test("Delete User", response)

if __name__ == "__main__":
    print("Starting API Tests for User Service")
    print("Make sure your server is running on http://localhost:8080")
    input("Press Enter to start the tests...")
    test_user_service() 