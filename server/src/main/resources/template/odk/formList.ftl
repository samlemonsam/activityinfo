<?xml version="1.0" encoding="UTF-8"?>

<formList>

<#list activities as activity>

<form url="${host}activityForm?id=${activity.id?c}">${activity.database.name?xml} / ${activity.name?xml}</form>

</#list>

</formList>
