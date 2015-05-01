

insert into userlogin (email,password,name,locale) values ('test@test.org', 'foo', 'Test User', 'en');

-- password = notasecret
insert into userlogin (email,password,name,locale) values ('qa_manual@bedatadriven.com',
    ' $2a$10$rJxqLlRRgB/4eJVVqUb7Z.0f2rv.BLgo02VC6NAakcB6Arcp3asDe', 'Test User', 'en');
