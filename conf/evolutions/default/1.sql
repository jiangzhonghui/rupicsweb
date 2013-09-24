# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table category (
  id                        bigint not null,
  name                      varchar(255),
  cover                     varchar(255),
  created_at                timestamp,
  constraint pk_category primary key (id))
;

create table image (
  id                        bigint not null,
  url                       varchar(255) not null,
  file_type                 varchar(255),
  href                      varchar(255),
  post_id                   bigint,
  issue_id                  bigint,
  issue_order               integer,
  delete_flag               boolean,
  rated_amount              integer,
  viewed_amount             integer,
  created_at                timestamp,
  tag                       varchar(255),
  constraint uq_image_url unique (url),
  constraint uq_image_1 unique (id,url),
  constraint pk_image primary key (id))
;

create table issue (
  id                        bigint not null,
  name                      varchar(255),
  category_id               bigint,
  image_amount              integer,
  cover                     varchar(255),
  delete_flag               boolean,
  created_at                timestamp,
  constraint pk_issue primary key (id))
;

create table post (
  id                        bigint not null,
  name                      varchar(255),
  url                       varchar(255) not null,
  img_url                   varchar(255),
  source_id                 bigint,
  hits                      integer,
  rate                      integer,
  delete_flag               boolean,
  created_at                timestamp,
  image_amount              integer,
  local_image_amount        integer,
  issue_id                  bigint,
  constraint uq_post_url unique (url),
  constraint uq_post_1 unique (id,url),
  constraint pk_post primary key (id))
;

create table source (
  id                        bigint not null,
  name                      varchar(255),
  language                  varchar(255),
  url                       varchar(255),
  post_url_pattern          varchar(255),
  deep_level                integer,
  tag                       varchar(255),
  begin_word                varchar(255),
  end_word                  varchar(255),
  post_begin_word           varchar(255),
  post_end_word             varchar(255),
  is_share                  boolean,
  is_include_next_page      boolean,
  next_page_keyword         varchar(255),
  image_type                varchar(255),
  is_include_image_in_site  boolean,
  min_width                 integer,
  min_height                integer,
  is_read_only              boolean,
  plugin                    varchar(255),
  encode                    varchar(255),
  created_at                timestamp,
  created_by_id             bigint,
  subscribe_amount          integer,
  rate                      integer,
  constraint pk_source primary key (id))
;

create table account (
  id                        bigint not null,
  email                     varchar(255),
  name                      varchar(255),
  device_id                 varchar(255),
  password                  varchar(255),
  tokenid                   varchar(255),
  token_expiration_time     bigint,
  desc                      varchar(255),
  limit_kbytes              integer,
  valid_end_date            timestamp,
  constraint pk_account primary key (id))
;


create table account_post (
  account_id                     bigint not null,
  post_id                        bigint not null,
  constraint pk_account_post primary key (account_id, post_id))
;
create sequence category_seq;

create sequence image_seq;

create sequence issue_seq;

create sequence post_seq;

create sequence source_seq;

create sequence account_seq;

alter table image add constraint fk_image_post_1 foreign key (post_id) references post (id) on delete restrict on update restrict;
create index ix_image_post_1 on image (post_id);
alter table image add constraint fk_image_issue_2 foreign key (issue_id) references issue (id) on delete restrict on update restrict;
create index ix_image_issue_2 on image (issue_id);
alter table issue add constraint fk_issue_category_3 foreign key (category_id) references category (id) on delete restrict on update restrict;
create index ix_issue_category_3 on issue (category_id);
alter table post add constraint fk_post_source_4 foreign key (source_id) references source (id) on delete restrict on update restrict;
create index ix_post_source_4 on post (source_id);
alter table post add constraint fk_post_issue_5 foreign key (issue_id) references issue (id) on delete restrict on update restrict;
create index ix_post_issue_5 on post (issue_id);
alter table source add constraint fk_source_createdBy_6 foreign key (created_by_id) references account (id) on delete restrict on update restrict;
create index ix_source_createdBy_6 on source (created_by_id);



alter table account_post add constraint fk_account_post_account_01 foreign key (account_id) references account (id) on delete restrict on update restrict;

alter table account_post add constraint fk_account_post_post_02 foreign key (post_id) references post (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists category;

drop table if exists image;

drop table if exists issue;

drop table if exists post;

drop table if exists source;

drop table if exists account;

drop table if exists account_post;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists category_seq;

drop sequence if exists image_seq;

drop sequence if exists issue_seq;

drop sequence if exists post_seq;

drop sequence if exists source_seq;

drop sequence if exists account_seq;

