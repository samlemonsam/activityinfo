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
<@scaffolding title="${label.login}">
<div class="container">
    <div class="row">
        <div class="col-md-8">
            <h3>${label.whatIsActivityInfo}</h3>

            <p class="lead">${label.activityInfoIntro}</p>

            <p>
                <a href="/" class="btn btn-info">${label.learnMore}&nbsp;&raquo;</a>
                <#if domain.signUpAllowed><a href="/signUp" class="btn btn-info">${label.signUpButton}&nbsp;&raquo;</a></#if>
            </p>
        </div>
        <div class="col-md-4">
            <form class="form-signin" id="loginForm" action="/login" method="POST">
                <h3 class="form-signin-heading">${label.login}</h3>
                <div class="controls">
                    <input type="text" name="email" id="emailInput" class="form-control" placeholder="${label.emailAddress}">
                    <input type="password" name="password" id="passwordInput" class="form-control" placeholder="${label.password}">

                    <div class="alert alert-danger help-block <#if !loginError>hide</#if>" id="loginAlert">
                    ${label.incorrectLogin}
                    </div>

                    <button class="btn btn-primary btn-large" type="submit" id="loginButton">${label.login}</button>
                    <img src="img/ajax-loader-spinner.gif" width="16" height="16" class="hide" id="loginSpinner">

                    <div class="login-problem"><a href="loginProblem">${label.forgottenYourPassword}</a></div>
                </div>
            </form>
        </div>
    </div>
</div>
<script type="text/javascript">

    var enableForm = function (enabled) {
        $('#loginButton').prop('disabled', !enabled);
        $('#loginSpinner').toggleClass('hide', enabled);
    }

    $('#loginForm').submit(function () {

        $('#loginAlert').addClass('hide');

        enableForm(false);
        $.ajax({
            url: '/login/ajax',
            type: 'POST',
            data: {
                email: $('#emailInput').val(),
                password: $('#passwordInput').val(),
                ajax: 'true'
            },
            success: function () {
                if (window.location.pathname != '/') {
                    window.location = '/' + window.location.search + window.location.hash;
                } else {
                    window.location.reload(true);
                }
            },
            error: function (xhr) {
                $('#loginAlert').toggleClass('hide', false);
            },
            complete: function () {
                enableForm(true);
            }
        });
        return false;
    });

    $('#emailInput').focus();
</script>
</@scaffolding>