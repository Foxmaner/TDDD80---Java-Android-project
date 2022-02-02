from flask import jsonify, request
from flask import Flask
from database_com import app, db, User


@app.route("/add", methods=["POST"])
def add():
    json_data = request.get_json()
    # Get the json data, if it is not available - return error code 400.
    try:
        firstname = json_data["first_name"]
        lastname = json_data["last_name"]
        username = json_data["username"]
        password = json_data["password"]
    except KeyError:
        return "", 400

    is_creatable = User.query.filter_by(username=username).first()

    # If there are no usernames like this, we continue.
    if is_creatable is None:
        # Hash password here
        new_user = User(username=username, first_name=firstname, last_name=lastname, password=password)

        # Add it to the database and save.
        db.session.add(new_user)
        db.session.commit()

        return new_user.to_dict(), 200

    return "", 400


@app.route("/add/<user_id>", methods=["POST"])
def add_post(user_id):
    pass


if __name__ == "__main__":
    app.debug = True
    app.run()
