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
<@scaffolding title="${label.welcomeToActivityInfo}">

    <@content>
    <div class="row row--padded">

        <h3>${label.welcomeToActivityInfo}</h3>

        <p>${label.setupAccount}</p>

        <div class="grid grid--spaced">

            <form action="" method="post" id="confirmForm">
                <input type="hidden" name="key" value="${user.changePasswordKey}">
                <label>
                    ${label.confirmYourName}:
                    <input type="text" name="name" id="nameInput" value="${user.name}" required>
                </label>

                <label>
                    ${label.confirmYourPreferredLanguage}:
                    <select name="locale">
                    <#list availableLocales as locale>
                        <option value="${locale.code}">${locale.localizedName}</option>
                    </#list>
                    </select>
                </label>

                <label>
                    ${label.choosePassword}:
                    <input type="password" name="password" required>
                </label>

                <label>
                    ${label.confirmYourPassword}:
                    <input type="password" name="password2" required>
                </label>

                <label>
                    <input type="checkbox" id="termsCheckbox" name="terms_accepted">
                    By ticking this box you agree to ActivityInfo's:
                    <ul>
                        <li><a href="/about/terms.html">Terms and Conditions</a></li>
                        <li><a href="/about/privacy-policy.html">Privacy Policy</a></li>
                    </ul>
                </label>

                <div>
                    <label>${label.newsletter}:</label>
                    <ul style="list-style-type: none">
                        <li>
                            <label class="radio">
                                <input type="radio" name="newsletter" id="newsletterYes" value="true">
                                ${label.newsletterYes}
                            </label>
                        </li>
                        <li>
                            <label class="radio">
                                <input type="checkbox" name="newsletter" id="newsletterNo" value="false">
                                ${label.newsletterNo}
                            </label>
                        </li>
                    </ul>
                </div>

                <button type="submit">${label.continue} &raquo;</button>
            </form>
        </div>
    </div>
    </@content>
    <@footer/>
    <@scripts>
    <script type="application/javascript">
        var theForm = document.getElementById("confirmForm");
        var theTerms = document.getElementById("termsCheckbox");
        var newsletterYesInput = document.getElementById('newsletterYes');
        var newsletterNoInput = document.getElementById('newsletterNo');

        theForm.addEventListener('submit', function(event) {
            if (!newsletterYesInput.checked && !newsletterNoInput.checked) {
                event.preventDefault();
                alert("Please choose whether you wish to receive emails from the ActivityInfo team.");
                return;
            }
        });

        theForm.addEventListener('submit', function(event) {
            if(!theTerms.checked) {
                event.preventDefault();
                alert("Please accept the terms and conditions to continue.");
                return;
            }
        });
    </script>
    </@scripts>
</@scaffolding>
