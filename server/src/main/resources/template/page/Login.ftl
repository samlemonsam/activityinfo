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
<@scaffolding title="${label.login}">

    <@content>
    <div class="row row--padded">
        <h1>Login</h1>

        <div class="grid grid--spaced">
            <form action="/login" method="POST">
                <label>
                    ${label.emailAddress}
                    <input name="email" type="email" required>
                </label>

                <label>
                    ${label.password}
                    <input type="password" name="password" required>
                </label>

                <#if loginError>
                <div>
                ${label.incorrectLogin}
                </div>
                </#if>

                <button type="submit">${label.login}</button>

                <div class="login-problem"><a href="loginProblem">${label.forgottenYourPassword}</a></div>
            </form>
        </div>
    </div>
    </@content>
    <@footer/>
</@scaffolding>