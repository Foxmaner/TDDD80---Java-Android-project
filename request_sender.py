import requests
print("Running Client...")
user = {"username": "Liam", "first_name": "Liam", "last_name": "Andersson", "password": "Hej123"}
r1 = requests.post("http://localhost:5000/add", json=user)
print(r1.text)
