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
<@scaffolding title="${label.signUpConfirmationTitle}">

    <@content>
    <div class="row">
        <div class="span12">

            <#if genericError == true>
                <div class="alert alert-error">
                ${label.signUpGenericError}
                </div>
            </#if>

            <h3>${label.signUpConfirmationTitle}</h3>

            <p class="well">${label.signUpConfirmationDetail}</p>

            <form action="" method="post" id="confirmForm">
                <input type="hidden" name="key" value="${key}"/>

                <div class="control-group" id="passwordGroup">
                    <label class="control-label" for="passwordInput">${label.choosePassword}:</label>

                    <div class="controls">
                        <input type="password" name="password" id="passwordInput">
                    </div>
                </div>
                <div class="control-group" id="confirmPasswordGroup">
                    <label class="control-label" for="confirmPasswordInput">${label.confirmYourPassword}:</label>

                    <div class="controls">
                        <input type="password" name="password2" id="confirmPasswordInput">
                    </div>
                </div>

                <div class="control-group">
                    <label class="checkbox">
                        <input type="checkbox" checked="false" name="newsletter" value="true">
                    ${label.newsletter}
                    </label>
                </div>

                <div class="control-group">
                    <div class="controls">
                        <button type="submit" class="btn btn-primary btn-large">${label.continue}  &raquo;</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
    </@content>

    <@footer/>

    <@scripts>
    <script type="text/javascript">

        var passwordInput = document.getElementById('passwordInput');
        var confirmPasswordInput = document.getElementById('confirmPasswordInput');
        var validatePass = function () {
            var pass1 = passwordInput.value;
            var pass2 = confirmPasswordInput.value;

            var valid = pass1 && pass1.length >= 6;

            $('#passwordGroup').toggleClass('error', !valid);
            $('#passwordHelp').toggleClass('hide', valid);

            var confirmed = pass2 && (pass1 == pass2);
            $('#confirmPasswordGroup').toggleClass('error', !confirmed);
            $('#confirmPasswordHelp').toggleClass('hide', confirmed);

            return valid && confirmed;
        };

        $("#passwordInput").change(validatePass);
        $("#confirmPasswordInput").change(validatePass);
        $("#confirmForm").submit(function () {
            var valid = validatePass();
            return !!valid;
        });

        $(document).ready(function () {
            $("#passwordInput").focus();
        });
    </script>
    </@scripts>
</@scaffolding>
