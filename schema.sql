DROP TABLE videos;

CREATE TABLE videos (
  id SERIAL,
  video_id VARCHAR(20)
);


ALTER TABLE videos ADD CONSTRAINT videos_primary_key PRIMARY KEY(id);

DROP TABLE search_expressions;

CREATE TABLE search_expressions(
  id SERIAL,
  search_expression TEXT
)

ALTER TABLE search_expressions ADD CONSTRAINT search_expressions_primary_key PRIMARY KEY(id);
