
-- Creates a "bound" location type fo each admin level

insert into locationtype (name, boundAdminLevelId, reuse, countryid, workflowid)
  select name, adminlevelid, 0, countryid, "closed" from adminlevel
    where adminlevelid not in (select lt.boundadminlevelid from locationtype lt);
