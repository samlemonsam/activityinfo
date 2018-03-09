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
<@scaffolding title="Get Free Account">

    <@content>

        <div class="row row--padded">

            <h1>Get Free Account</h1>

            <#if genericError == true || formError == true>
                <div class="alert alert--error">
                ${label.signUpGenericError}
                </div>
            </#if>

            <#if confirmationEmailSent == true>
                <div class="alert alert--success">
                ${label.signUpEmailSent}
                </div>

            <#else>

                <div class="grid grid--spaced">

                    <form action="" method="post" id="signUpForm">

                        <p>Complete the following form to create a new account on
                            ActivityInfo.org.</p>

                        <label>
                            Your full name (ex: John Doe)
                            <input type="text" name="name" required>
                        </label>


                        <label>
                            E-mail address
                            <input type="email" name="email" required>
                        </label>

                        <label>
                            Prefered language
                            <select name="locale" id="localeInput">
                                <#list availableLocales as locale>
                                    <option value="${locale.code}">${locale.localizedName}</option>
                                </#list>
                            </select>
                        </label>

                        <label>
                            <input type="checkbox" id="termsCheckbox" name="terms_accepted">
                            I agree to ActivityInfo's <a href="/about/terms.html">terms and conditions</a>
                        </label>

                        <button type="submit">Get Free Account</button>
                    </form>

                    <div>
                        <section class="learning-center" id="howto">

                            <div class="phototeaser">
                                <img src="/about/assets/images/waterpump.jpg" alt="">

                                <div>
                                    <h2>Want to learn more?</h2>

                                    <ul class="list--arrow">
                                        <li><a href="/about/faq.html">Frequently Asked Questions</a></li>
                                        <li><a href="/about/webinar.html">Register for our next webinar</a></li>
                                    </ul>

                                    <a href="/about/subscribe.html" class="cta-link cta-link--secondary">Purchase a support package</a>
                                </div>
                            </div>
                        </section>
                    </div>
                </div>
            </#if>
        </div>
       </@content>

    <@footer/>

    <@scripts>
    <script type="text/javascript">
        var theForm = document.getElementById("signUpForm");
        var theTerms = document.getElementById("termsCheckbox");
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
