import os
from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy.ext import hybrid
from sqlalchemy.sql import func
# Create app
app = Flask(__name__)

# Connection
if 'NAMESPACE' in os.environ and os.environ['NAMESPACE'] == 'heroku':
    # For heroku
    db_uri = os.environ.get('DB_URL')
    address = "https://strinder-android.herokuapp.com/"
    debug_flag = False

else:
    # When running locally: use sqlite
    address = "http://localhost:8080"
    db_path = os.path.join(os.path.dirname(__file__), 'app.db')
    db_uri = 'sqlite:///{}'.format(db_path)
    debug_flag = True

# Connection
app.config['SQLALCHEMY_DATABASE_URL'] = db_uri
# Supress warning
app.config['SQLALCHEMY_DATABASE_URI'] = db_uri
# Supress warning
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)


# Tables

follow_to_follow = db.Table('friendship',
                            db.Column('user_id', db.Integer, db.ForeignKey('User.id'), primary_key=True),
                            db.Column('follow_id', db.Integer, db.ForeignKey('User.id'), primary_key=True)
                            )

liked_posts_table = db.Table("liked_posts",
                             db.Column("user_id", db.Integer, db.ForeignKey("User.id"),
                                       primary_key=True),
                             db.Column("post_id", db.Integer, db.ForeignKey("Post.id", ondelete="cascade"),
                                       primary_key=True))


class User(db.Model):
    __tablename__ = "User"
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    username = db.Column(db.String(40), unique=True, nullable=False)
    first_name = db.Column(db.String(20), nullable=True)
    last_name = db.Column(db.String(20), nullable=True)
    # Might not need email.
    email = db.Column(db.String(50), unique=True, nullable=False)
    birthday = db.Column(db.DateTime, nullable=True)
    gender = db.Column(db.String(6), nullable=True)
    biography = db.Column(db.String(100), nullable=False)
    photo_url = db.Column(db.String(200), nullable=True)

    # Relations
    follows = db.relationship("User", secondary=follow_to_follow, primaryjoin=id == follow_to_follow.c.user_id,
                              secondaryjoin=id == follow_to_follow.c.follow_id)

    posts = db.relationship("Post", backref="user", lazy=True)
    # We don't really need a many to many here at the moment, but maybe if development continues.
    liked_posts = db.relationship("Post", secondary=liked_posts_table, back_populates="likes", passive_deletes=True)

    def to_dict(self):
        formatted = None
        if self.birthday is not None:
            formatted = self.birthday.strftime("%Y/%m/%d")

        return {"id": self.id, "firstName": self.first_name, "lastName": self.last_name,
                "gender": self.gender, "birthday": formatted, "biography": self.biography, "email": self.email,
                "photoUrl": self.photo_url, "username": self.username,
                "follows": [follow.to_dict_follows() for follow in self.follows],
                "posts": [post.to_dict() for post in self.posts]}

    def to_dict_follows(self):
        return {"id": self.id, "username": self.username, "firstName": self.first_name, "lastName": self.last_name,
                "photoUrl": self.photo_url}

    # Here the self.id is enough, because it is unique. Just "in case" we added two more clauses.
    def __eq__(self, other):
        return self.id == other.id and self.username == other.username and self.email == other.email

    @hybrid.hybrid_property
    def full_name(self):
        """This returns the user's fullname in lowercase."""
        f_name = self.first_name
        l_name = self.last_name

        if f_name is not None:
            f_name = func.lower(f_name)

        if l_name is not None:
            l_name = func.lower(l_name)

        return f_name + " " + l_name


class Post(db.Model):
    __tablename__ = "Post"
    id = db.Column(db.Integer, unique=True, primary_key=True, autoincrement=True)
    user_id = db.Column(db.Integer, db.ForeignKey("User.id"), nullable=False)
    title = db.Column(db.String(40), nullable=False)
    caption = db.Column(db.String(100), nullable=False)
    date_time = db.Column(db.DateTime, default=func.now(), nullable=False)
    longitude = db.Column(db.Float, nullable=False)
    latitude = db.Column(db.Float, nullable=False)
    # Here we need a relationship! We need to know which user liked what post.
    likes = db.relationship("User", secondary=liked_posts_table, back_populates="liked_posts",
                            cascade="all, delete", passive_deletes=True)

    comments = db.relationship("Comment", backref="post", cascade="all, delete, delete-orphan")
    training_session = db.relationship("TrainingSession", uselist=False, backref="post", cascade="all, delete, "
                                                                                                 "delete-orphan")

    def to_dict(self):
        session = None
        if self.training_session is not None:
            session = self.training_session.to_dict()

        return {"id": self.id, "userId": self.user_id, "title": self.title, "caption": self.caption,
                "likes": [user.to_dict_follows() for user in self.likes], "comments": [comment.to_dict() for
                                                                                       comment in self.comments],
                "trainingSession": session, "date": self.date_time, "latitude": self.latitude,
                "longitude": self.longitude}


class TrainingSession(db.Model):
    __tablename__ = "Training_session"
    id = db.Column(db.Integer, unique=True, primary_key=True, autoincrement=True)
    time = db.Column(db.String(5), nullable=False)
    post_id = db.Column(db.Integer, db.ForeignKey("Post.id", ondelete="cascade"), nullable=False)
    speed_unit = db.Column(db.String(40), nullable=False)
    speed = db.Column(db.Float, nullable=False)
    distance = db.Column(db.Float, nullable=False)
    distance_unit = db.Column(db.String(5), nullable=False)
    exercise = db.Column(db.String(40), nullable=False)

    def to_dict(self):
        return {"id": self.id, "postId": self.post_id, "elapsedTime": self.time,
                "speedUnit": self.speed_unit, "speed": self.speed, "distance": self.distance,
                "distanceUnit": self.distance_unit, "exercise": self.exercise}


class Comment(db.Model):
    __tablename__ = "Comment"
    id = db.Column(db.Integer, primary_key=True)
    post_id = db.Column(db.Integer, db.ForeignKey("Post.id",  ondelete="cascade"), nullable=False)
    text = db.Column(db.String(100), nullable=False)
    user_id = db.Column(db.Integer, nullable=False)

    def to_dict(self):
        return {"id": self.id, "postId": self.post_id, "text": self.text, "userId": self.user_id}


# This contains used tokens
class TokenBlocklist(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    jti = db.Column(db.String(36), nullable=False, index=True)
    created_at = db.Column(db.DateTime, nullable=False)
