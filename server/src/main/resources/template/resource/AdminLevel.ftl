<#--
 #%L
 ActivityInfo Server
 %%
 Copyright (C) 2009 - 2013 UNICEF
 %%
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as
 published by the Free Software Foundation, either version 3 of the 
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public 
 License along with this program.  If not, see
 <http://www.gnu.org/licenses/gpl-3.0.html>
 #L%
-->
<#include "../page/Scaffolding.ftl">
<@scaffolding title="${name}" leaflet=true>

    <@content>
    <h1>${name}</h1>

    <p>Administrative Level in ${country.name}</p>

    <div id="map" style="height:350px;"></div>

        <#if parent?has_content >
        <p>Subdivision of <a href="/resources/adminLevel/${parent.id?c}">${parent.name}</a></p>
        </#if>

        <#if childLevels?has_content >
        <h2>Child Levels</h2>
        <ul>
            <#list childLevels as child>
                <li><a href="/resources/adminLevel/${child.id?c}">${child.name}</a></li>
            </#list>
        </ul>
        </#if>

    <h2>Entities</h2>
    <ul>
        <#list entities?sort_by("name") as entity >
            <#if !entity.deleted>
                <li><a href="/resources/adminEntity/${entity.id?c}">${entity.name}</a></li>
            </#if>
        </#list>
    </ul>

    <h2>Revision History</h2>
    <dl>
        <#list versions?sort_by("version") as version>
            <dt>Version ${version.version}</dt>
            <dd>${version.message!"No commit message"}</dd>
        </#list>
    </dl>


    </@content>
<link href="https://api.mapbox.com/mapbox.js/v3.0.1/mapbox.css" rel="stylesheet" />
<script src='https://api.mapbox.com/mapbox.js/v3.0.1/mapbox.js'></script>
<script type="application/javascript">
    L.mapbox.accessToken = 'pk.eyJ1IjoiYWN0aXZpdHlpbmZvIiwiYSI6Ik5xVDlfRkkifQ.47_5IbyfXQWGWqxpCI80AQ';
    var map = L.mapbox.map('map').setView([${country.bounds.centerLat?c}, ${country.bounds.centerLon?c}], 6);
    L.mapbox.styleLayer('mapbox://styles/mapbox/light-v9').addTo(map);
    var featureLayer = L.mapbox.featureLayer()
            .loadURL('/resources/adminLevel/${id?c}/entities/features')
            .addTo(map);
</script>
</@scaffolding>
