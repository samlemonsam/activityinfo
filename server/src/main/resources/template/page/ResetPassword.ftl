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
 <http://www.gnu.org/licenses/gpl-3.0.html>.
 #L%
-->
<#include "Scaffolding.ftl">
<@scaffolding title="${label.resetYourPassword}">

    <@content>
    <div class="row row--small">

        <h1>${label.resetYourPassword}</h1>

        <p>${label.resetDetailMessage}</p>

        <form action="loginProblem" method="POST">
            <label>
            ${label.emailAddress}
                <input name="email" type="email" required>
            </label>
            <button type="submit">${label.reset}</button>
        </form>

        <#if loginError == true>
            <div class="alert alert--error">
            ${label.loginError}
            </div>
        </#if>
        <#if emailSent == true>
            <div class="alert alert--success">
            ${label.emailSent}
            </div>
        </#if>
        <#if emailError == true>
            <div class="alert alert--error">
            ${label.emailErrorAlert}
            </div>
        </#if>
    </div>
    </@content>
    <@footer/>
    <@scripts/>
</@scaffolding>
