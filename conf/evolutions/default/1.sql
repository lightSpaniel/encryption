# --- !Ups

create table `userSchema` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `projectClass` TEXT NOT NULL,
  `dataNonce` BLOB NOT NULL,
  `dataValue` BLOB NOT NULL
);

create table `bidSchema` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `projectClass` TEXT NOT NULL,
  `dataNonce` BLOB NOT NULL,
  `dataValue` BLOB NOT NULL
);

create table `ventureSchema` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `projectClass` TEXT NOT NULL,
  `dataNonce` BLOB NOT NULL,
  `dataValue` BLOB NOT NULL
);

# --- !Downs

DROP TABLE `userSchema`;
DROP TABLE `bidSchema`;
DROP TABLE `ventureSchema`;