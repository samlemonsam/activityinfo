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
<@scaffolding title="${label.login}">

    <@content>
    <div class="row row--small">
        <h1>Login</h1>

        <form action="/login" method="POST">
            <label>
                ${label.emailAddress}
                <input name="email" type="email" required value="${email!""}">
            </label>

            <label>
                ${label.password}
                <input type="password" name="password" required>
            </label>

            <#if loginError>
            <div>
            <p class="alert alert--error">${label.incorrectLogin}</p>
            </div>
            </#if>

            <button type="submit" style="width:100%">${label.login}</button>

            <div class="login-problem"><a href="loginProblem">Forgotten your password?</a></div>
        </form>

        <hr style="margin: 2em auto">

        <a href="/oauth/oauthconnector_hid_oauth" class="cta-link cta-link--secondary">
            <div style="float:none; display:table; vertical-align:middle; width:100%">
                <div style="display:table-cell; width: 20%;"></div>
                <div style="display:table-cell; width: 35px; height: 35px; background: url(/img/humanitarian-id-logo.svg) no-repeat;"></div>
                <div style="display:table-cell; vertical-align:inherit">Log In with Humanitarian.id</div>
                <div style="display:table-cell; width: 35px;"></div>
                <div style="display:table-cell; width: 20%;"></div>
            </div>
        </a>

        <div class="login-problem"><a href="http://www.activityinfo.org/blog/posts/2017-08-16-humanitarian-id.html">What's this?</a></div>

        <hr style="margin: 2em auto">

        <p>Learn from best practices, join trainings, voice your questions and shape the future of ActivityInfo by
            joining our <strong>User Conference on September 26th-27th in Amman, Jordan</strong>. Seats are limited so
            <a href="http://bit.ly/aiconf18-tickets">book your ticket soon</a>!</p>

    </div>
    </@content>
    <@footer/>
</@scaffolding>