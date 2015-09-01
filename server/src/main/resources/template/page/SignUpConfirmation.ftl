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
<@scaffolding title="${label.signUpConfirmationTitle}">

<div class="container">

    <#if genericError == true>
        <div class="alert alert-danger">
        ${label.signUpGenericError}
        </div>
    </#if>

    <div class="page-header">
        <h1>${label.signUpConfirmationTitle}</h1>
    </div>

    <p class="well">${label.signUpConfirmationDetail}</p>

    <form action="" method="post" id="confirmForm">
        <input type="hidden" name="key" value="${key}"/>

        <div class="form-group" id="passwordGroup">
            <label for="passwordInput">${label.choosePassword}:</label>

            <input type="password" class="form-control" name="password" id="passwordInput">
            <p class="help-block hide" id="passwordHelp">${label.passwordHelp}.</p>
        </div>
        <div class="form-group" id="confirmPasswordGroup">
            <label for="confirmPasswordInput">${label.confirmYourPassword}:</label>
            <input type="password" class="form-control" name="password2" id="confirmPasswordInput">
            <p class="help-block hide" id="confirmPasswordHelp">${label.passwordDoNotMatch}</p>
        </div>

        <div class="check-box">
            <label>
                <input type="checkbox" checked="true" name="newsletter" value="true">
            ${label.newsletter}
            </label>
        </div>
        <button type="submit" class="btn btn-primary btn-large">${label.continue}  &raquo;</button>
    </form>
</div>

<script type="text/javascript">

    var validatePass = function () {
        var pass1 = $('#passwordInput').val();
        var pass2 = $('#confirmPasswordInput').val();

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
</@scaffolding>
