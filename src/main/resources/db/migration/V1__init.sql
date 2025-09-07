CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

insert into profile( id, name, username, password, status, visible, created_date)
values (1, 'Adminov', 'adminov@gmail.com',
        '$2a$10$0JRCP2N5E4nACOqRUVK5AeTrcgd.pQC8hKJqjoD9wCCAttjaKaVj2','ACTIVE', true, now());

SELECT setval('profile_id_seq', max(id)) FROM profile;

insert into profile_roles(profile_id, roles, created_date)
values (1, 'ROLE_USER', now()),
       (1, 'ROLE_ADMIN', now());