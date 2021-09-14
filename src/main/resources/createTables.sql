CREATE TABLE IF NOT EXISTS Teams(
    name VARCHAR(30) NOT NULL,
    displayname VARCHAR(16),
    PRIMARY KEY (name)
);

CREATE TABLE IF NOT EXISTS Players(
    uuid VARCHAR(36) NOT NULL,
    team VARCHAR(30),
    status INTEGER(1) NOT NULL,
    PRIMARY KEY (uuid),
    FOREIGN KEY (team) REFERENCES Teams(name)
);
