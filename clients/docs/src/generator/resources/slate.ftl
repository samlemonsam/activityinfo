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
<#-- @ftlvariable name="" type="org.activityinfo.api.tools.DocModel" -->
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <meta content="IE=edge,chrome=1" http-equiv="X-UA-Compatible">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>ActivityInfo API Reference</title>

    <link href="assets/screen.css" rel="stylesheet" media="screen" />
    <link href="assets/print.css" rel="stylesheet" media="print" />
    <script src="assets/slate.js"></script>
</head>

<body class="index" data-languages="[&quot;shell&quot;,&quot;ruby&quot;,&quot;python&quot;]">
<a href="#" id="nav-button">
      <span>
        NAV
        <img src="assets/navbar.png" />
      </span>
</a>
<div class="tocify-wrapper">
    <img src="assets/logo.png" />
    <div class="lang-selector">
        <#list languages as lang>
        <a href="#" data-language-name="${lang}">${lang}</a>
        </#list>
    </div>
    <div class="search">
        <input type="text" class="search" id="input-search" placeholder="Search">
    </div>
    <ul class="search-results"></ul>
    <div id="toc">
    </div>
    <ul class="toc-footer">
        <li><a href='https://github.com/tripit/slate'>Documentation Powered by Slate</a></li>
    </ul>
</div>
<div class="page-wrapper">
    <div class="dark-box"></div>
    <div class="content">
        
        ${topics}

        <#--<h1 id="models">Models</h1>-->

        <#--<#list spec.definitions as def>-->

        <#--<h2 id="${def.name}">${def.name}</h2>-->


        <#--<h3>Properties</h3>-->

        <#--<table><thead>-->
        <#--<tr>-->
            <#--<th>Parameter</th>-->
            <#--<th>Type</th>-->
            <#--<th>Description</th>-->
        <#--</tr>-->
        <#--</thead><tbody>-->
            <#--<#list def.properties as prop>-->
            <#--<tr>-->
                <#--<td>${prop.name}</td>-->
                <#--<td>${prop.type}</td>-->
                <#--<td>${prop.description}</td>-->
            <#--</tr>-->
            <#--</#list>-->
        <#--</tbody></table>-->



        <#--</#list>-->


        <#list spec.sections as section>
            
        <h1 id="${section.tag}">${section.title}</h1>

        <#list section.operations?sort_by('summary') as op>
        
        <h2 id="${op.id}">${op.summary}</h2>
        
        <#list op.examples as example>
        <pre class="highlight ${example.language}"><code>${example.source?html}</code></pre>
        </#list>

        <#if op.descriptionHtml??>
        <p>${op.descriptionHtml}</p>
        </#if>

        <h3>HTTP Request</h3>

        <p><code class="prettyprint">${op.method} ${spec.getBaseUri()}${op.path}</code></p>

        <h3>Parameters</h3>

        <table><thead>
        <tr>
            <th>Parameter</th>
            <th>Optional?</th>
            <th>Description</th>
        </tr>
        </thead><tbody>
        <#list op.parameters as param>
        <tr>
            <td>${param.name}</td>
            <td><#if param.optional>Yes</#if></td>
            <td>${param.description}</td>
        </tr>
        </#list>
        </tbody></table>

        <#if op.responseSchema?? >

        <h3>Response</h3>

        <div class="model">
            <div class="model-name">${op.responseSchema.name} {</div>
            <div class="description">
                <#list op.responseSchema.properties as prop>
                <div><span class="propName">${prop.name}</span>
                    (<span class="propType">${prop.type}</span>, <span class="propOptKey">${prop.requiredString})</span>
                </div>
                </#list>
            </div>
            }
        </div>


        </#if>

        <h3>Response Codes</h3>
        
        <table>
            <thead>
            <tr>
                <th>Status</th>
                <th>Description</th>
            </tr>
            </thead>
            <tbody>
            <#list op.responses?sort_by('statusCode') as response>
            <tr>
                <td>${response.statusCode}</td>
                <td>${response.description}</td>
            </tr>
            </#list>
            </tbody>
            
        </table>

        <#--<aside class="success">-->
            <#--Remember — a happy kitten is an authenticated kitten!-->
        <#--</aside>-->

        </#list>
        </#list>    

    </div>
    <div class="dark-box">
        <div class="lang-selector">
        <#list languages as lang>
        <a href="#" data-language-name="${lang}">${lang}</a>
        </#list>
    </div>
</div>
</body>
</html>
