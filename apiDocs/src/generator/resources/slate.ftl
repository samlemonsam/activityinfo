<#-- @ftlvariable name="" type="org.activityinfo.api.tools.DocModel" -->
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <meta content="IE=edge,chrome=1" http-equiv="X-UA-Compatible">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>ActivityInfo API Reference</title>

    <link href="screen.css" rel="stylesheet" media="screen" />
    <link href="print.css" rel="stylesheet" media="print" />
    <script src="slate.js"></script>
</head>

<body class="index" data-languages="[&quot;shell&quot;,&quot;ruby&quot;,&quot;python&quot;]">
<a href="#" id="nav-button">
      <span>
        NAV
        <img src="images/navbar.png" />
      </span>
</a>
<div class="tocify-wrapper">
    <img src="logo.png" />
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

        <h1 id="forms">Forms API</h1>

        <#list spec.operations as op>
        
        <h2 id="${op.id}">${op.summary}</h2>
        
        <#list op.examples as example>
        <pre class="highlight ${example.language}"><code>${example.source?html}</code></pre>
        </#list>

        <#if op.jsonOutput??>
        <blockquote>
            <p>The above command returns JSON structured like this:</p>
        </blockquote>
<pre class="highlight json"><code>${op.jsonOutput}</code></pre>
        </#if>
        
        <p>${op.summary}</p>

        <h3 id="http-request">HTTP Request</h3>

        <p><code class="prettyprint">${op.method} http://example.com/${op.path}</code></p>

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

        <#--<aside class="success">-->
            <#--Remember — a happy kitten is an authenticated kitten!-->
        <#--</aside>-->
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
