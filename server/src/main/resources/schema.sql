DROP TABLE IF EXISTS users, item_request, item, booking, comments;

CREATE TABLE IF NOT EXISTS users (
  user_id int GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  user_name VARCHAR(50) NOT NULL,
  email VARCHAR(50) NOT NULL UNIQUE,
  CONSTRAINT pk_users PRIMARY KEY(user_id)
);

CREATE TABLE IF NOT EXISTS item_request (
  request_id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
  request_description VARCHAR(1000) NOT NULL,
  user_id int REFERENCES users(user_id),
  creation_time TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS item (
  item_id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
  item_name VARCHAR(100) NOT NULL,
  item_description VARCHAR(1000),
  is_available BOOLEAN DEFAULT FALSE,
  owner_id int REFERENCES users(user_id),
  request_id int REFERENCES item_request(request_id),
  FOREIGN KEY(owner_id) REFERENCES users(user_id) ON DELETE CASCADE,
  FOREIGN KEY(request_id) REFERENCES item_request(request_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking (
    booking_id INT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    item_id INT,
    booker_id INT,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    CONSTRAINT pk_booking PRIMARY KEY(booking_id),
    FOREIGN KEY(item_id) REFERENCES item(item_id) ON DELETE CASCADE,
    FOREIGN KEY(booker_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
    comment_id INT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    item_id INT,
    author_id INT,
    text VARCHAR(1000) NOT NULL,
    created TIMESTAMP,
    CONSTRAINT pk_comments PRIMARY KEY(comment_id),
    FOREIGN KEY(item_id) REFERENCES item(item_id) ON DELETE CASCADE,
    FOREIGN KEY(author_id) REFERENCES users(user_id) ON DELETE CASCADE
);