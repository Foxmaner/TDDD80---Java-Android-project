from flask import jsonify, request
from flask import Flask
from database_com import app, db, User, Post


@app.route("/add", methods=["POST"])
def add():
    """This function adds a new user."""
    json_data = request.get_json()
    # Get the json data, if it is not available - return error code 400.
    try:
        # Try to convert the dictionary to variables. If it fails,then return error code 400.
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
    """This function adds a post to a user. """
    # Convert parameter to int
    try:
        user_id = int(user_id)
    # If the user_id is incorrect, return error code.
    except(ValueError, TypeError):
        return "", 400

    # Query the user. (There is only one)
    query = User.query.filter_by(id=user_id).first()

    if query is not None:
        post_data = request.get_json()

        try:
            # Try to get post_data, if it fails we throw a 400 error code.
            post = Post(user_id=user_id, title=post_data["title"], caption=post_data["caption"])
        except KeyError:
            return "", 400

        # Save the post to the user and commit changes to database.
        query.posts.append(post)
        db.session.commit()

        return jsonify(post.to_dict()),200
    else:
        # We found no match, so return error code 400
        return "", 400


if __name__ == "__main__":
    app.debug = True
    app.run()
