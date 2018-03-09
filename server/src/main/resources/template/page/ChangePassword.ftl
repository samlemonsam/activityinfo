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
<#include "Scaffolding.ftl">
<@scaffolding title="${label.chooseNewPassword}">

    <@content>
    <div class="row row--padded">
        <h3>${label.chooseNewPassword}</h3>

        <form class="form" method="post" id="changePasswordForm" action="changePassword" method="post">
            <input type="hidden" name="key" value="${user.changePasswordKey}">
            <label>
            ${label.newPassword}
                <input type="password" name="password">
            </label>
            <label>
            ${label.confirmNewPassword}
                <input type="password" name="password2">
            </label>

            <#if passwordLengthInvalid>
                <div class="alert alert--error">
                <p>${label.passwordHelp}</p>
                </div>
            </#if>

            <#if passwordsNotMatched>
                <div class="alert alert--error">
                <p>${label.passwordDoNotMatch}</p>
                </div>
            </#if>

            <button type="submit">${label.continue} &raquo;</button>
        </form>
    </div>
    </@content>
    <@footer/>
    <@scripts>
    </@scripts>
</@scaffolding>
