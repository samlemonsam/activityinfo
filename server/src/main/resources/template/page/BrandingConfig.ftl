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
<@scaffolding title="AppEngine Configuration">

    <@content>
    <div class="row">
        <div class="span12">

            <h2>Domain Configuration: ${customDomain.host}</h2>

            <form class="form" method="post">
                <div>
                    <h3>Title</h3>
                    <input name="title" class="span12" value="${customDomain.title!''}">
                </div>
                <div>
                    <h3>Scaffolding Template</h3>
                    <textarea rows=5 class="span12" name="scaffolding">${customDomain.scaffolding!''}</textarea>
                </div>
                <div>
                    <h3>Home Page Body HTML</h3>
                    <textarea rows=5 class="span12" name="homePageBody">${customDomain.homePageBody!''}</textarea>
                </div>
                <div>
                    <button type="submit" class="btn btn-primary">Update</button>
                </div>
            </form>

        </div>
    </div>


    </@content>
    <@footer/>
    <@scripts/>

</@scaffolding>