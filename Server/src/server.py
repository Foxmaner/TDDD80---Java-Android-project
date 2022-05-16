import traceback

from flask import jsonify, request
from sqlalchemy import desc
from datetime import timedelta, datetime, timezone

from database_com import app, db, User, Post, Comment, TokenBlocklist, TrainingSession
from flask_jwt_extended import create_access_token, jwt_required, get_jwt, JWTManager
from flask_bcrypt import Bcrypt
from flask_jwt_extended import get_jwt_identity
from google.oauth2 import id_token
from google.auth.transport import requests
import os

bcrypt = Bcrypt(app)

ACCESS_EXPIRES = timedelta(minutes=30)
app.config["JWT_SECRET_KEY"] = "Sometimes I Pee Myself In Bed"
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = ACCESS_EXPIRES
jwt = JWTManager(app)


# Callback function to check if a JWT exists in the database blocklist
@jwt.token_in_blocklist_loader
def check_if_token_revoked(_, jwt_payload: dict) -> bool:
    jti = jwt_payload["jti"]
    token = db.session.query(TokenBlocklist.id).filter_by(jti=jti).scalar()

    return token is not None


# ----- POST ----- #

@app.route("/authenticate", methods=["POST"])
def authenticate():
    post_input = request.get_json()
    token = post_input["idToken"]
    # TODO Hide somewhere!
    client_id = "960100179212-4f9co6hv8aogedarvvdi732j147bb53p.apps.googleusercontent.com"

    try:
        google_request = requests.Request()
        # Specify the CLIENT_ID of the app that accesses the backend:
        info = id_token.verify_oauth2_token(token, google_request, client_id)

        if info["iss"] == "https://accounts.google.com":
            # ID token is valid. Get the user's Google Account ID and other information.
            username = info["sub"]
            email = info["email"]
            first_name = None
            if "given_name" in info:
                first_name = info["given_name"]
            last_name = None
            if "family_name" in info:
                last_name = info["family_name"]
            photo_url = None
            if "picture" in info:
                photo_url = info["picture"]
            # From Post Request
            birthday = post_input["birthday"]
            gender = post_input["gender"]
            user = User.query.filter_by(username=username).first()

            # If there are no usernames like this, we continue.
            if user is None:
                # Some accounts do not have these values set. So we need to check
                if birthday is not None:
                    birthday = datetime.strptime(birthday, "%Y/%m/%d")

                user = User(username=username, first_name=first_name, last_name=last_name,
                            email=email, gender=gender, birthday=birthday, biography="I am new to Strinder!",
                            photo_url=photo_url)

                # Add it to the database and save.
                db.session.add(user)
                db.session.commit()

            token = create_access_token(identity=user.id)
            data = user.to_dict()
            data["accessToken"] = token
            return data, 200

        else:
            return "", 400

    except(ValueError, KeyError):
        # Print the exception.
        print(traceback.format_exc())
        return "", 400


@app.route("/post/add", methods=["POST"])
@jwt_required()
def add_post():
    """This function adds a post to a user. """
    # Convert parameter to int
    try:
        user_id = get_jwt_identity()
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


@app.route("/session/set", methods=["POST"])
@jwt_required()
def set_session():
    post_input = request.get_json()
    try:
        user_id = get_jwt_identity()
        # Get the hour and minutes: HH:MM (e.g 16:56)
        time = datetime.strptime(post_input["time"], "%H:%M")
        post_id = post_input["postId"]
        speed_unit = post_input["speedUnit"]
        speed = post_input["speed"]
        distance = post_input["distance"]
        distance_unit = post_input["distanceUnit"]
        exercise = post_input["exercise"]
    # If the user_id is incorrect, return error code.
    except(ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()
    # Make sure that we are editing our own post.
    if post is not None and post.user_id == user_id:
        training = TrainingSession(time=time, post_id=post_id, speed_unit=speed_unit,
                                   speed=speed, distance=distance, distance_unit=distance_unit, exercise=exercise)
        # Update the training_session attribute.
        if post.training_session is None:
            post.training_session = training
        else:
            # Could not replace the session as such: training_session = training.
            post.training_session.speed = training.speed
            post.training_session.post_id = training.post_id
            post.training_session.time = training.time
            post.training_session.exercise = training.exercise
            post.training_session.speed_unit = training.speed_unit
            post.training_session.distance_unit = training.distance_unit
            post.training_session.distance = training.distance

        db.session.commit()

        return jsonify(training.to_dict()), 200

    else:
        return "", 400


@app.route("/user/logout", methods=["POST"])
@jwt_required()
def logout():
    try:
        jti = get_jwt()["jti"]
        now = datetime.now(timezone.utc)
        db.session.add(TokenBlocklist(jti=jti, created_at=now))
        db.session.commit()
        return jsonify(msg="JWT revoked")
    except KeyError:
        return "", 400


@app.route("/user/set_data", methods=["POST"])
@jwt_required()
def set_data():
    json_data = request.get_json()
    if json_data is None:
        return "", 400

    # Get the json data, if it is not available - return error code 400.
    try:
        # Try to convert the dictionary to variables. If it fails,then return error code 400.
        id = get_jwt_identity()
        firstname = json_data["first_name"]
        lastname = json_data["last_name"]
        birthday = json_data["birthday"]
        gender = json_data["gender"]
        biography = json_data["biography"]
        photo_url = json_data["photo_url"]
    except KeyError:
        return "", 400

    user = User.query.filter_by(id=id).first()

    if user is not None:
        if firstname is not None:
            user.first_name = firstname
        if lastname is not None:
            user.last_name = lastname
        if birthday is not None:
            user.birthday = datetime.strptime(birthday, "%Y/%m/%d")
        if gender is not None:
            user.gender = gender
        if biography is not None:
            user.biography = biography
        if photo_url is not None:
            user.photo_url = photo_url

        db.session.commit()

        return "", 200

    return "", 400


@app.route("/befriend/<friend_id>", methods=["POST"])
@jwt_required()
def add_friend(friend_id):
    """Befriends two existing users. """

    # Try to convert to integer.
    try:
        friend_id = int(friend_id)
        user_id = get_jwt_identity()

    except(ValueError, TypeError):
        return "", 400

    # Query the users.
    friend = User.query.filter_by(id=friend_id).first()
    user = User.query.filter_by(id=user_id).first()

    if user is not None and friend is not None and user != friend:
        user.friends.append(friend)
        friend.friends.append(user)

        db.session.commit()

        # Return the user we added as a friend.
        return jsonify(friend.to_dict()), 200

    return "", 400


@app.route("/comments/add/<post_id>", methods=["POST"])
@jwt_required()
def add_comment(post_id):
    """Adds a comment to a post. """
    # Try to convert to integer.
    try:
        post_id = int(post_id)
    except(ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()

    if post is not None:
        post_data = request.get_json()
        try:
            # Create a comment
            comment = Comment(post_id=post_id, text=post_data["text"], user_id=get_jwt_identity())
            post.comments.append(comment)
            db.session.commit()
            return "", 200

        except KeyError:
            return "", 400

    else:
        return "", 400


@app.route("/post/like/<post_id>", methods=["POST"])
@jwt_required()
def like(post_id):
    try:
        post_id = int(post_id)
        user_id = int(get_jwt_identity())
    except (ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()
    user = User.query.filter_by(id=user_id).first()
    if post is not None and user is not None:

        if user in post.likes:
            post.likes.remove(user)
        else:
            post.likes.append(user)

        db.session.commit()
        data = [user.to_dict_friends() for user in post.likes]
        return jsonify(data), 200
    else:
        return "", 400


# ------- GET -------- #
@app.route("/post/get_likes/<post_id>", methods=["GET"])
@jwt_required()
def get_likes(post_id):
    try:
        post_id = int(post_id)
    except(ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()

    if post is not None:
        data = [user.to_dict_friends() for user in post.likes]
        return jsonify(data), 200
    else:
        return "", 400


@app.route("/user/get_data/<user_id>", methods=["GET"])
@jwt_required()
def get_data(user_id):
    # No try/catch needed here

    user = User.query.filter_by(id=user_id).first()

    if user is not None:
        data = user.to_dict()

        return jsonify(data), 200

    return "", 400


@app.route("/befriended/<user_id>/<friend_id>", methods=["GET"])
@jwt_required()
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


@app.route("/user/get_user/<user_id>")
@jwt_required()
def get_user(user_id):
    try:
        user_id = int(user_id)
    except (TypeError, ValueError):
        return "", 400

    user = User.query.filter_by(id=user_id).first()

    if user is not None:
        return user.to_dict(), 200
    else:
        return "", 400


@app.route("/posts/latest/<nr_of_posts>", methods=["GET"])
@jwt_required()
def get_posts(nr_of_posts):
    """Fetch selected nr of posts. -1 = ALL"""

    try:
        nr_of_posts = int(nr_of_posts)

    except(ValueError, TypeError):
        return "", 400

    if nr_of_posts == -1:
        posts = [post.to_dict() for post in Post.query.
                 order_by(desc(Post.date_time)).all()]
    elif nr_of_posts >= 0:
        posts = [post.to_dict() for post in Post.query.
                 order_by(desc(Post.date_time)).limit(nr_of_posts).all()]
    else:
        return "", 400

    users = []
    for post in posts:
        user_id = post.get("userId")
        user = get_user(user_id)[0]
        if isinstance(user, dict):
            users.append(user)

    data = {"posts": posts, "users": users}
    return data, 200


@app.route("/comments/<post_id>", methods=["GET"])
@jwt_required()
def get_comments(post_id):
    """Fetch all comments for a specific post. """
    try:
        post_id = int(post_id)

    except(ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()

    if post is not None:
        comments = [comment.to_dict() for comment in post.comments]

        return jsonify(comments), 200
    else:
        return "", 400


@app.route("/friends/<user_id>/<nr_of_friends>", methods=["GET"])
@jwt_required()
def get_friends(user_id, nr_of_friends):
    """Fetch selected nr of friends. -1 = ALL."""
    try:
        user_id = int(user_id)
        nr_of_friends = int(nr_of_friends)

    except(ValueError, TypeError):
        return "", 400

    user = User.query.filter_by(id=user_id).first()

    if user is not None:

        if nr_of_friends == -1:
            friends = [friend.to_dict() for friend in user.friends]

        elif nr_of_friends >= 0:
            friends = [friend.to_dict() for friend in user.friends[:nr_of_friends]]
        else:
            return "", 400
    else:
        return "", 400

    return jsonify(friends), 200


@app.route('/del/usr/<user_id>', methods=["DELETE"])
@jwt_required()
def remove_user(user_id):
    user = User.query.filter_by(id=user_id)

    if user.first() is not None and user.id == get_jwt_identity():
        user.delete()
        db.session.commit()
        return "", 200
    else:
        return "", 400


@app.route('/del/post/<post_id>', methods=["DELETE"])
@jwt_required()
def remove_post(post_id):
    post = Post.query.filter_by(id=post_id)

    if post.first() is not None and post.first().user_id == get_jwt_identity():
        post.delete()
        db.session.commit()
        return "", 200
    else:
        return "", 400


@app.route('/del/comment/<comment_id>', methods=["DELETE"])
@jwt_required()
def remove_comment(comment_id):
    comment = Comment.query.filter_by(id=comment_id)

    if comment.first() is not None and comment.user_id == get_jwt_identity():
        comment.delete()
        db.session.commit()
        return "", 200
    else:
        return "", 400


if __name__ == "__main__":
    app.debug = True
    app.port = int(os.environ.get("PORT", 8080))
    app.run()
