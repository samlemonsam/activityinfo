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

        <h2>Administrative Unit Levels</h2>
            <@showLevels children=adminLevels/>
    </div>
    </@content>
</@scaffolding>

<#macro showLevels children>
<ul>
    <#list children as child>
        <#if !child.deleted>
            <li><a href="/resources/adminLevel/${child.id?c}">${child.name}</a></li>
        </#if>
    </#list>
</ul>
</#macro>