from flask import jsonify, request
from flask import Flask
from sqlalchemy import desc

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
        # We assume that the password is hashed.
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

        return jsonify(post.to_dict()), 200
    else:
        # We found no match, so return error code 400
        return "", 400


@app.route("/friends/<user_id>/<friend_id>", methods=["GET"])
def are_friends(user_id, friend_id):
    """ Returns true if the users are friends, false if not. """

    try:
        friend_id = int(friend_id)
        user_id = int(user_id)
    except (TypeError, ValueError):
        return "", 400

    # We check if friend_id and user_id are actual users.
    friend = User.query.filter_by(id=friend_id).first()
    user = User.query.filter_by(id=user_id).first()

    if friend is not None and user is not None:
        # Check if friend is in user's friend list.

        result = friend.id in [friend.id for friend in user.friends]

        return str(result), 200

    return "", 400


@app.route("/befriend/<friend_id>/<user_id>", methods=["POST"])
def add_friend(friend_id, user_id):
    """Befriends two existing users. """

    # Try to convert to integer.
    try:
        friend_id = int(friend_id)
        user_id = int(user_id)

    except(ValueError, TypeError):
        return "", 400

    # Query the users.
    friend = User.query.filter_by(id=friend_id).first()
    user = User.query.filter_by(id=user_id).first()
    # FIXME Det här fungerar inte, friends innehåller inte rätt grejer.
    if user is not None and friend is not None:
        user.friends.append(friend)
        friend.friends.append(user)

        db.session.commit()

        # Don't return any json.
        return "", 200

    return "", 400


@app.route("/posts/<user_id>/<nr_of_posts>", methods=["GET"])
def get_posts(user_id, nr_of_posts):
    """Fetch selected nr of posts. -1 = ALL"""

    try:
        user_id = int(user_id)  # Fixade så att den hämtar från den faktiska usern och inte i hela Post tabellen.
        nr_of_posts = int(nr_of_posts)

    except(ValueError, TypeError):
        return "", 400

    ##
    ##
    ## Måste lägga in funktionalitet att bara skicka tillbaka posts från friends
    ##
    ##
    if nr_of_posts == -1:
        posts = [post.to_dict() for post in Post.query.filter_by(user_id=user_id).
                 order_by(desc(Post.date_time)).all()]
    elif nr_of_posts >= 0:
        posts = [post.to_dict() for post in Post.query.filter_by(user_id=user_id).
                 order_by(desc(Post.date_time)).limit(nr_of_posts).all()]
    else:
        return "", 400

    return jsonify(posts), 200


# @app.route("/comments/<Post_ID>", methods=["GET"])
# def get_posts():
#    return "", 200


if __name__ == "__main__":
    app.debug = True
    app.run()
