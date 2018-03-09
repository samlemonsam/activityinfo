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
<!DOCTYPE html>
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
<#-- @ftlvariable name="" type="org.activityinfo.server.login.model.HostPageModel" -->
<#if appCacheManifest??>
<html manifest="${appCacheManifest}">
<#else>
<html>
</#if>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta name="application-name" content="ActivityInfo"/>
    <meta name="description" content="ActivityInfo"/>
    <meta name="application-url" content="${appUrl}"/>
    <meta http-equiv="X-UA-Compatible" content="IE=10">
    <link rel="icon" href="/about/assets/images/logo-activityinfo.png">

    <#if newUI>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
    </#if>



    <title>${domain.title}</title>

    <#if newUI>

    <link rel="stylesheet" type="text/css" href="App/reset.css" />

    <#else>
    <style type="text/css">
        #loading-box {
            position: absolute;
            left: 45%;
            top: 40%;
            padding: 2px;
            margin-left: -45px;
            z-index: 20001;
            border: 1px solid #ccc;
        }

        #loading-box .loading-indicator {
            background: #eef;
            font: bold 13px tahoma, arial, helvetica;
            padding: 10px;
            margin: 0;
            height: auto;
            color: #444;
        }

        #loading-box .loading-indicator img {
            margin-right: 8px;
            float: left;
            vertical-align: top;
        }

        #loading-msg {
            font: normal 10px tahoma, arial, sans-serif;
        }
    </style>
    </#if>
    <script type="text/javascript">
        if (document.cookie.indexOf('authToken=') == -1 ||
                document.cookie.indexOf('userId') == -1 ||
                document.cookie.indexOf('email') == -1) {
            window.location = "/login" + window.location.hash;
        }
        var ClientContext = {
            version: '$[display.version]',
            commitId: '$[git.commit.id]',
            title: '${domain.title}',
            featureFlags: '${featureFlags!''}'

        };
    </script>

    <script type="text/javascript" language="javascript" src="${bootstrapScript}"></script>
    <script>
        (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
                m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
        })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');
        ga('create', 'UA-11567120-1', 'auto');
        ga('set', 'anonymizeIp', true);
        ga('send', 'pageview');
    </script>
</head>
<body role="application">

<#if !newUI>
<div id="loading">
    <div id="loading-box">
        <div class="loading-indicator">
            <img src="/ActivityInfo/gxt231/images/default/shared/large-loading.gif" alt=""/>
        ${domain.title} $[display.version]<br/>
            <span id="loading-msg">${label.loading}</span>

        </div>
    </div>
</div>
</#if>

<#if newUI>
    <section id="root">
    </section>
</#if>

<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1'
        style="position:absolute;width:0;height:0;border:0"></iframe>
<iframe src="javascript:''" id="_downloadFrame" name="_downloadFrame" tabIndex='-1'
        style="position:absolute;width:0;height:0;border:0"></iframe>
<iframe id="__printingFrame" style="position:absolute;width:0;height:0;border:0"></iframe>

</body>
</html>