<#--

    ActivityInfo
    Copyright (C) 2009-2013 UNICEF
    Copyright (C) 2014-2018 BeDataDriven Groep B.V.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

-->
<#include "../page/Scaffolding.ftl">
<@scaffolding title="${name}">

    <@content>
    <div class="row">
        <h1>${name}</h1>

        <p><a href="/resources/adminLevel/${level.id?c}">${level.name}</a> in <a
                href="/resources/country/${level.country.codeISO}">${level.country.name}</a></p>
            <#if parents?has_content>
            <h2>Parents</h2>
            <ul>
                <li>${parent.level.name}: <a href="/resources/adminEntity/${parent.id?c}">${parent.name}</a></li>
            </ul>
            </#if>
            <#if level.childLevels?has_content>
            <h2>Child entities</h2>
                <#list level.childLevels as childLevel>
                <h3>${childLevel.name}</h3>
                <ul>
                    <#list childLevel.entities?sort_by("name") as childEntity>
                        <#if !childEntity.deleted && childEntity.parent?? && childEntity.parent.id == id>
                            <li><a href="/resources/adminEntity/${childEntity.id?c}">${childEntity.name}</a></li>
                        </#if>
                    </#list>
                </ul>
                </#list>
            </#if>
    </div>
    </@content>
</@scaffolding>
