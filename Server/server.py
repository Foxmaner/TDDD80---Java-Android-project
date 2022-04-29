from flask import jsonify, request
from sqlalchemy import desc
from datetime import timedelta, datetime, timezone

from database_com import app, db, User, Post, Comment, TokenBlocklist
from flask_jwt_extended import create_access_token, jwt_required, get_jwt, JWTManager
from flask_bcrypt import Bcrypt
from flask_jwt_extended import get_jwt_identity

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
# Login
@app.route("/user/login", methods=["POST"])
def login():
    post_input = request.get_json()

    try:
        password = post_input["password"]
        username = post_input["username"]

    except KeyError:
        return "", 400

    user = User.query.filter_by(username=username).first()

    if user is not None:
        # The user exists
        db_pass = user.password

        if bcrypt.check_password_hash(db_pass, password):
            # The password is correct
            # Return a token
            token = create_access_token(identity=user.username)
            data = user.to_dict()
            data["accessToken"] = token
            return data, 200

    # The user does not exist
    return jsonify(message="The provided password or username is wrong"), 403


# Register
@app.route("/add", methods=["POST"])
def add():
    """This function adds a new user."""
    json_data = request.get_json()

    if json_data is None:
        return "", 400

    # Get the json data, if it is not available - return error code 400.
    try:
        # Try to convert the dictionary to variables. If it fails,then return error code 400.
        firstname = json_data["first_name"]
        lastname = json_data["last_name"]
        username = json_data["username"]
        email = json_data["email"]
        gender = json_data["gender"]
        birthday = json_data["birthday"]
        photo_url = json_data["photo_url"]
        password = bcrypt.generate_password_hash(json_data["password"])
    except KeyError:
        return "", 400

    is_creatable = User.query.filter_by(username=username).first() is None

    # If there are no usernames like this, we continue.
    if is_creatable:
        # Some accounts do not have these values set. So we need to check
        formatted_birthday = None
        if birthday != "Unknown":
            formatted_birthday = datetime.strptime(birthday,"%Y/%m/%d")
        print(formatted_birthday)
        formatted_gender = None
        if gender != "Unknown":
            formatted_gender = gender

        new_user = User(username=username, first_name=firstname, last_name=lastname, password=password,email=email,
                        gender=formatted_gender,birthday=formatted_birthday,biography="",photo_url=photo_url)

        # Add it to the database and save.
        db.session.add(new_user)
        db.session.commit()

        return "", 200
    else:
        return "", 409


@app.route("/add/<user_id>", methods=["POST"])
@jwt_required()
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


@app.route("/user/set_data",methods=["POST"])
@jwt_required()
def set_data():
    """Sets the data that is allowed to change to the data that has been received."""
    json_data = request.get_json()
    if json_data is None:
        return "", 400

    # Get the json data, if it is not available - return error code 400.
    try:
        # Try to convert the dictionary to variables. If it fails,then return error code 400.
        username = get_jwt_identity()
        firstname = json_data["first_name"]
        lastname = json_data["last_name"]
        email = json_data["email"]
        birthday = json_data["birthday"]
        gender = json_data["gender"]
        biography = json_data["biography"]
    except KeyError:
        return "", 400

    user = User.query.filter_by(username=username).first()
    if user is not None:
        user.first_name = firstname
        user.last_name = lastname
        user.email = email
        user.birthday = datetime.strptime(birthday,"%Y/%m/%d")
        user.gender = gender
        user.biography = biography

        db.session.commit()

        return "", 200

    return "", 400


@app.route("/befriend/<friend_id>/<user_id>", methods=["POST"])
@jwt_required()
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

    if user is not None and friend is not None:
        user.friends.append(friend)
        friend.friends.append(user)

        db.session.commit()

        # Don't return any json.
        return "", 200

    return "", 400


@app.route("/comments/<post_id>", methods=["POST"])
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
            comment = Comment(post_id=post_id, text=post_data["text"], user_id=post_data["user_id"])
            post.comments.append(comment)
            db.session.commit()
        except KeyError:
            return "", 400

    else:
        return "", 400

    return "", 200


# ------- GET -------- #
@app.route("/user/get_data/<username>", methods=["GET"])
@jwt_required()
def get_data(username):
    # No try/catch needed here

    user = User.query.filter_by(username=username).first()

    if user is not None:
        data = {"firstName" : user.first_name, "lastName" : user.last_name,
                "gender" : user.gender, "birthday" : user.birthday.strftime("%Y/%m/%d"),
                "biography" : user.biography}

        return jsonify(data),200

    return "",400

""" NOT USED ATM
@app.route("/user/get_id/<username>", methods=["GET"])
def get_id(username):
    #No try/catch needed here

    user = User.query.filter_by(username=username).first()

    if user is not None:
        return str(user.id), 200
    else:
        return "", 400
"""

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


@app.route("/posts/<user_id>/<nr_of_posts>", methods=["GET"])
@jwt_required()
def get_posts(user_id, nr_of_posts):
    """Fetch selected nr of posts. -1 = ALL"""

    try:
        user_id = int(user_id)
        nr_of_posts = int(nr_of_posts)

    except(ValueError, TypeError):
        return "", 400

    if nr_of_posts == -1:
        posts = [post.to_dict() for post in Post.query.filter_by(user_id=user_id).
                 order_by(desc(Post.date_time)).all()]
    elif nr_of_posts >= 0:
        posts = [post.to_dict() for post in Post.query.filter_by(user_id=user_id).
                 order_by(desc(Post.date_time)).limit(nr_of_posts).all()]
    else:
        return "", 400

    return jsonify(posts), 200


@app.route("/comments/<post_id>/<nr_of_comments>", methods=["GET"])
@jwt_required()
def get_comments(post_id, nr_of_comments):
    """Fetch selected nr of comments. -1 = ALL."""

    try:
        post_id = int(post_id)
        nr_of_comments = int(nr_of_comments)

    except(ValueError, TypeError):
        return "", 400

    post = Post.query.filter_by(id=post_id).first()

    if post is not None:

        if nr_of_comments == -1:
            comments = [comment.to_dict() for comment in post.comments[:]]
        elif nr_of_comments >= 0:
            comments = [comment.to_dict() for comment in post.comments[:nr_of_comments]]
        else:
            return "", 400
    else:
        return "", 400

    return jsonify(comments), 200


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
            print([friend.to_dict() for friend in user.friends])
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

    if user.first() is not None:
        user.delete()
        db.session.commit()
        return "", 200
    else:
        return "", 400


@app.route('/del/post/<post_id>', methods=["DELETE"])
@jwt_required()
def remove_post(post_id):
    post = Post.query.filter_by(id=post_id)

    if post.first() is not None:
        post.delete()
        db.session.commit()
        return "", 200
    else:
        return "", 400


@app.route('/del/comment/<comment_id>', methods=["DELETE"])
@jwt_required()
def remove_comment(comment_id):
    comment = Comment.query.filter_by(id=comment_id)

    if comment.first() is not None:
        comment.delete()
        db.session.commit()
        return "", 200
    else:
        return "", 400


if __name__ == "__main__":
    app.debug = True
    app.run()
