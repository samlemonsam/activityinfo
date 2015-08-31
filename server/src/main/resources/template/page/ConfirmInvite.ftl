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
<@scaffolding title="${label.welcomeToActivityInfo}">

<div class="container">
    <div class="page-header">
        <h1>${label.welcomeToActivityInfo}</h1>
    </div>

    <p class="well">${label.setupAccount}</p>

    <form class="form-horizontal" action="" method="post" id="confirmForm">
        <input type="hidden" name="key" value="${user.changePasswordKey}"></input>

        <div class="form-group" id="nameGroup">
            <label for="nameInput" class="col-sm-2">${label.confirmYourName}:</label>
            <div class="col-sm-10">
                <input type="text" class="form-control" name="name" id="nameInput" value="${user.name}">
                <p class="help-block hide" id="nameHelp">${label.pleaseEnterYourFullName}:</p>
            </div>
        </div>
        <div class="form-group">
            <label for="localeSelect" class="col-sm-2">${label.confirmYourPreferredLanguage}:</label>
            <div class="col-sm-10">
                <select name="locale" id="localeSelect" class="form-control">
                    <option value="en">${label.english}</option>
                    <option value="fr">${label.francais}</option>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label for="passwordInput" class="col-sm-2">${label.choosePassword}:</label>
            <div class="col-sm-10">
                <input type="password" class="form-control" name="password" id="passwordInput" placeholder="${label.password}">
                <p class="help-block hide" id="passwordHelp">${label.passwordHelp}.</p>
            </div>
        </div>
        <div class="form-group" id="confirmPasswordGroup">
            <label for="confirmPasswordInput" class="col-sm-2">${label.confirmYourPassword}:</label>
            <div class="col-sm-10">
                <input type="password" class="form-control" name="password2" id="confirmPasswordInput" placeholder="${label.password}">
                <p class="help-block hide" id="confirmPasswordHelp">${label.passwordDoNotMatch}</p>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <label>
                    <input type="checkbox" checked="true" name="newsletter" value="true">
                ${label.newsletter}
                </label>
            </div>
        </div>
        <button type="submit" class="btn btn-primary btn-large">${label.continue}  &raquo;</button>
    </form>

</div>
<script type="text/javascript">

    var validateName = function () {
        var valid = !!( $('#nameInput').val() );
        $('#nameGroup').toggleClass('error', !valid);
        $('#nameHelp').toggleClass('hide', valid);
        return valid;
    };

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

    $("#nameInput").change(validateName);
    $("#passwordInput").change(validatePass);
    $("#confirmPasswordInput").change(validatePass);
    $("#confirmForm").submit(function () {
        var valid = validateName() && validatePass();
        return !!valid;
    });
</script>
</@scaffolding>
