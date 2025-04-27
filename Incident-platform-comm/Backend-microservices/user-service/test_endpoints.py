import requests
import json
import time

# Base URL for the API
BASE_URL = "http://localhost:8080/api"

# Test data
test_user = {
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "roles": ["USER"]
}

admin_user = {
    "username": "adminuser",
    "email": "admin@example.com",
    "password": "admin123",
    "roles": ["ADMIN"]
}

def print_response(response, test_name):
    print(f"\n=== {test_name} ===")
    print(f"Status Code: {response.status_code}")
    try:
        print(f"Response: {json.dumps(response.json(), indent=2)}")
    except:
        print(f"Response: {response.text}")
    print("=" * 50)

def test_auth_endpoints():
    # Test signup
    print("\nTesting Signup...")
    signup_response = requests.post(
        f"{BASE_URL}/auth/signup",
        json=test_user
    )
    print_response(signup_response, "Signup Test")
    
    # Test admin signup
    print("\nTesting Admin Signup...")
    admin_signup_response = requests.post(
        f"{BASE_URL}/auth/signup",
        json=admin_user
    )
    print_response(admin_signup_response, "Admin Signup Test")
    
    # Test signin
    print("\nTesting Signin...")
    signin_data = {
        "username": test_user["username"],
        "password": test_user["password"]
    }
    signin_response = requests.post(
        f"{BASE_URL}/auth/signin",
        json=signin_data
    )
    print_response(signin_response, "Signin Test")
    
    return signin_response.json().get("accessToken")

def test_user_endpoints(token):
    headers = {"Authorization": f"Bearer {token}"}
    
    # Test get all users
    print("\nTesting Get All Users...")
    all_users_response = requests.get(
        f"{BASE_URL}/users",
        headers=headers
    )
    print_response(all_users_response, "Get All Users Test")
    
    # Test get current user
    print("\nTesting Get Current User...")
    current_user_response = requests.get(
        f"{BASE_URL}/users/me?username={test_user['username']}",
        headers=headers
    )
    print_response(current_user_response, "Get Current User Test")
    
    # Test update user
    print("\nTesting Update User...")
    update_data = {
        "username": "updateduser",
        "email": "updated@example.com"
    }
    update_response = requests.put(
        f"{BASE_URL}/users/1",
        headers=headers,
        json=update_data
    )
    print_response(update_response, "Update User Test")
    
    # Test delete user (requires admin token)
    print("\nTesting Delete User...")
    delete_response = requests.delete(
        f"{BASE_URL}/users/1",
        headers=headers
    )
    print_response(delete_response, "Delete User Test")

def main():
    print("Starting User Service API Tests...")
    
    # Test authentication endpoints
    token = test_auth_endpoints()
    
    if token:
        print("\nAuthentication successful! Proceeding with user endpoint tests...")
        # Test user endpoints
        test_user_endpoints(token)
    else:
        print("\nAuthentication failed! Cannot proceed with user endpoint tests.")

if __name__ == "__main__":
    main() 