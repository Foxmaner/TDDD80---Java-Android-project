import requests
print("Running Client...")
user = {"username": "Liam", "first_name": "Liam", "last_name": "Andersson", "password": "Hej123"}
r1 = requests.post("http://localhost:5000/add", json=user)
print(r1.text)
# Add a post to the user_id: 1.
post_data = {"title":"Min post", "caption": "Omg guys!"}
r2 = requests.post("http://localhost:5000/add/1", json=post_data)
print(r2.text)
