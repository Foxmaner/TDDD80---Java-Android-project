from flask import Flask
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)

# Connection
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///./our.db'
db = SQLAlchemy(app)

#Tables

association_table = db.Table('association',
                             db.Column('user_id', db.Integer, db.ForeignKey('User.id'), primary_key=True),
                             db.Column('user_id', db.Integer, db.ForeignKey('User.id'), primary_key=True)
                             )


class User(db.Model):
    __tablename__ = "User"
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(40), nullable=False)
    first_name = db.Column(db.String(40), nullable=False)
    last_name = db.Column(db.String(40), nullable=False)
    password = db.Column(db.String(40), nullable=False)
    # Relations
    friends = db.relationship("User", secondary=association_table, backref="users")
    posts = db.relationship("Post", backref="user", lazy=True)

    def to_dict(self):
        return {"id": self.id, "username": self.username, "first_name": self.first_name, "last_name": self.last_name, "password": self.password,
                "friends": self.friends, "posts": self.posts}


class Post(db.Model):
    __tablename__ = "Post"
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("User.id"), nullable=False)
    title = db.Column(db.String(40), nullable=False)
    caption = db.Column(db.String(140), nullable=False)
    likes = db.Column(db.Integer, nullable=False)


    # Relationer and shit, måste fixa
    comments = db.Column(db.String(40), nullable=False)
    training_session = db.Column(db.String(40), nullable=False)

    def to_dict(self):
        return {"id": self.id, "title": self.title, "caption": self.caption, "likes": self.likes,
                "comments": self.comments, "training_session": self.training_session}


class TrainingSession(db.Model):
    __tablename__ = "Training_session"
    id = db.Column(db.Integer, primary_key=True)
    time = db.Column(db.Float, nullable=False)
    speed_unit = db.Column(db.String(40), nullable=False)
    speed = db.Column(db.Float, nullable=False)
    exercise = db.Column(db.String(40), nullable=False)


    def to_dict(self):
        return {"id": self.id, "time": self.time, "speed_unit": self.speed_unit, "speed": self.speed,
                "exercise": self.exercise}


class Comment(db.Model):
    __tablename__ = "Comment"
    id = db.Column(db.Integer, primary_key=True)
    related_post = db.Column(db.String(140), nullable=False)
    text = db.Column(db.String(140), nullable=False)
    user_id = db.Column(db.Integer, nullable=False)
    likes = db.Column(db.Integer, nullable=False)

    def to_dict(self):
        return {"id": self.id, "related_post": self.related_post, "text": self.text, "user_id": self.user_id,
                "likes": self.likes}


@app.before_first_request
def create_tables():
    db.drop_all()
    db.create_all()
    db.session.commit()
