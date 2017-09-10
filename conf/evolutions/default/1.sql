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
  `name` TEXT NOT NULL,
  `projectClass` TEXT NOT NULL,
  `dataNonce` BLOB NOT NULL,
  `dataValue` BLOB NOT NULL,
  `numberOfShares` BIGINT UNSIGNED NOT NULL
);

create table `enhancedUserSchema` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `projectClass` TEXT NOT NULL,
  `dataNonce` BLOB NOT NULL,
  `dataValue` BLOB NOT NULL,
  `userName` TEXT NOT NULL,
  `passwordNonce` BLOB NOT NULL,
  `passwordEncrypted` BLOB NOT NULL,
  `sessionKey` BLOB NOT NULL
);

create table `sector` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` TEXT NOT NULL
);


# --- !Downs


DROP TABLE `userSchema`;
DROP TABLE `bidSchema`;
DROP TABLE `ventureSchema`;
DROP TABLE `enhancedUserSchema`;
DROP TABLE `sector`;
