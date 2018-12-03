-- Creates a "nullary" location or location at the "country" level to fake
-- forms with "no" geography until the new forms model is complete

insert into locationtype (name,reuse, countryid, workflowid, nullary)
  select "Country", 0, countryid, "closed", 1 from country
    where countryId != 3 AND   
          countryid NOT IN
            (select lt.countryid from locationtype lt where lt.nullary=1);


insert location (locationid, name, locationtypeid, x, y, timeEdited)
  select lt.locationtypeid, c.name, lt.locationtypeid, (c.x1+c.x2)/2, (c.y1+c.y2)/2,
    unix_timestamp(now())*1000 from locationtype lt left join country c on (lt.countryid=c.countryid)
      where lt.nullary = 1 AND
            lt.locationtypeid not in (select locationid from location);
      
      
-- Inserts a special nullary location type for the Global country

insert into locationtype (locationtypeid, name, reuse, countryid, workflowid, nullary)
  values (51545, "Global", false, 3, 'closed', 1);
  
insert into location (locationid, name, locationtypeid)
  values (51545, "Global", 51545);
  
