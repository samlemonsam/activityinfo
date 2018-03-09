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
<@scaffolding title="${label.signUpConfirmationInvalidTitle}">

    <@content>
    <div class="row row--padded">

    <h3>${label.signUpConfirmationInvalidTitle}</h3>

    <p>${label.signUpConfirmationInvalidDetail}</p>

    <a href="/login" class="cta-link">${label.loginNow}</a>

    </@content>

    <@footer/>


    <@scripts>
    </@scripts>

</@scaffolding>