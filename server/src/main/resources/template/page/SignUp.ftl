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
<@scaffolding title="${label.signUpTitle}">
<div class="container">

    <#if !domain.signUpAllowed>
        <p>Please contact your local focal point to connect to this
            ActivityInfo database.</p>
    <#else>
        <div class="page-header">
            <h1>${label.signUpTitle}</h1>
        </div>
        <div class="row">
            <div class="col-md-6">
                <#if genericError == true || formError == true>
                    <div class="alert alert-danger">
                    ${label.signUpGenericError}
                    </div>
                </#if>
        
                <#if confirmationEmailSent == true>
                    <div class="alert alert-success">
                    ${label.signUpEmailSent}
                    </div>
        
                <#else>
                    <form action="" method="post" id="signUpForm">
        
                        <div class="form-group" id="nameGroup">
                            <label for="nameInput">${label.name}:</label>
                            <input type="text" class="form-control" name="name" id="nameInput" value="${name}" autofocus >
                            <p class="help-block hide" id="nameHelp">${label.pleaseEnterYourFullName}</p>
                        </div>
        
                        <div class="form-group">
                            <label for="organizationInput">${label.organization}:</label>
                            <input type="text" class="form-control" name="organization" id="organizationInput" value="${organization}">
                        </div>
        
                        <div class="form-group">
                            <label for="jobtitleInput">${label.jobtitle}:</label>
                            <input type="text" class="form-control" name="jobtitle" id="jobtitleInput" value="${jobtitle}">
                        </div>
        
                        <div class="form-group" id="emailGroup">
                            <label for="emailInput">${label.emailAddress}:</label>
                            <input type="text" class="form-control" name="email" id="emailInput" value="${email}" >
                            <p class="help-block hide" id="emailHelp">${label.pleaseEnterAValidEmailAddress}</p>
                        </div>
        
                        <div class="form-group">
                            <label for="localeInput">${label.preferredLanguage}:</label>
                            <select name="locale" id="localeInput" class="form-control" >
                                <option value="en" selected>${label.english}</option>
                                <option value="fr">${label.francais}</option>
                            </select>
                        </div>
        
                        <button type="submit" class="btn btn-primary btn-large">${label.signUpButton}</button>
                    </form>
                </#if>
            </div>
            <div class="col-md-6">
                <div class="well">
                    <h4>Free for teams of ten or less</h4>
        
                    <p>Please feel free to use ActivityInfo.org for
                        small field teams. If you plan to adopt the system
                        across your organization, we'll ask to you to make
                        an annual contribution to the system's running costs.
        
                    <p>
        
                    <p><a href="mailto:support@activityinfo.org">Contact us</a>
                        for more information.</p>
        
                    <p>In any case, you're welcome to evaluate the system without
                        restriction as long as needed!</p>
        
                </div>
                <div class="alert">
                    <h4>Looking for your Cluster's database?</h4>
        
                    <p>Only your cluster lead can grant you access
                        to the cluster's database. Please contact your local
                        focal point and request access.</p>
        
                </div>
            </div>
        </div>
    </#if>
</div>
<script type="text/javascript">
    var validateName = function () {
        var valid = !!( $('#nameInput').val() );
        $('#nameGroup').toggleClass('error', !valid);
        $('#nameHelp').toggleClass('hide', valid);
        return valid;
    };

    var validateEmail = function () {
        var email = $('#emailInput').val();
        var valid = !!email;
        if (valid) {
            var regex = /^\s*\S+\@\S+\s*$/;
            valid = regex.test(email);
        }
        $('#emailGroup').toggleClass('error', !valid);
        $('#emailHelp').toggleClass('hide', valid);
        return valid;
    };

    $("#nameInput").change(validateName);
    $("#emailInput").change(validateEmail);
    $("#signUpForm").submit(function () {
        var valid = validateName() && validateEmail();
        console.log("valid: " + !!valid);
        return !!valid;
    });
</script>
</@scaffolding>
