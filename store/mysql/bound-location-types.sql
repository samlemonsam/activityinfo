
-- Creates a "bound" location type fo each admin level

insert into locationtype (name, boundAdminLevelId, reuse, countryid, workflowid)
  select name, g.adminlevelid, 0, g.countryid, "closed" from adminlevel g
    where g.adminlevelid not in (select lt.boundadminlevelid from locationtype lt where lt.boundadminlevelid is not null);
