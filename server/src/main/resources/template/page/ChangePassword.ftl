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
<@scaffolding title="${label.chooseNewPassword}">

<div class="container">
    <h3>${label.chooseNewPassword}</h3>

    <form class="form" method="post" id="changePasswordForm" action="changePassword" method="post">
        <input type="hidden" name="key" value="${user.changePasswordKey}"></input>

        <div class="form-group">
            <label for="newPasswordInput">${label.newPassword}</label>
            <input type="password" class="form-control" name="password" id="newPasswordInput">
        </div>
        <div class="form-group">
            <label for="newPasswordInput">${label.confirmNewPassword}</label>
            <input type="password" class="form-control" name="password2" id="newPasswordInput2">
        </div>

        <div class="form-group">
            <div class="alert alert-danger <#if !passwordLengthInvalid> hide </#if>" id="alertLength">
            ${label.passwordHelp}
            </div>
        </div>
        <div class="form-group">
            <div class="alert alert-danger <#if !passwordsNotMatched> hide </#if>" id="alertNotMatch">
            ${label.passwordDoNotMatch}
            </div>
        </div>

        <button type="submit" class="btn btn-primary btn-large">${label.continue}  &raquo;</button>
    </form>
</div>
<script type="text/javascript">
    $("#changePasswordForm").submit(function( event ) {

        $("#alertLength").addClass( "hide" );
        $("#alertNotMatch").addClass( "hide" );

        var val1 = $("#newPasswordInput").val();
        var val2 = $("#newPasswordInput2").val();

        if (val1.length < 6) { // minimal length of password is 6
            $("#alertLength").removeClass("hide");
            event.preventDefault();
            return;
        }

        if (val1 != val2) { // check whether passwords equals
            $("#alertNotMatch").removeClass("hide");
            event.preventDefault();
            return;
        }
    });
</script>
</@scaffolding>
