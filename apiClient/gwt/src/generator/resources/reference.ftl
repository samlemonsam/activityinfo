<#-- @ftlvariable name="spec" type="org.activityinfo.api.tools.doc.SpecModel" -->

<!DOCTYPE html>
<head>
    <title>Enketo API Documentation</title>
    <meta charset='utf-8'/>
    <meta name='description' content='ActivityInfo API Documentation'/>
    <meta name='content' content='ActivityInfo API Documentation'/>
    <meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1'/>
    <#--<link rel="shortcut icon" href="/api/media/favicon.ico">-->
    <link href='http://fonts.googleapis.com/css?family=Open+Sans' rel='stylesheet' type='text/css'/>
    <link href='http://apidocs.enketo.org/api/assets.css' rel='stylesheet' type='text/css'>
        
</head>
<body>

    <section id="content" class="v2">

    <#list spec.operations as op>

    <article class="${op.method}" data-path="${op.path}">
        <a id="${op.id}" href="#/${op.id}"><h2><code><b>${op.method}</b> ${op.path}</code> ${op.summary}</h2></a>
        <section class="body">

            <#--<div class="changes"><p>In API v2 the defaults and parent_window_origin parameters were added.</p><p>In API v2 the response properties have changed.</p></div>-->
            <#--<p></p>-->


            <h3>Request</h3>
            <ul>
                <#list op.parameters as param>
                <li>${param.required} <strong><code>${param.name}</code></strong> parameter is ${param.description}</li>
                </#list>
            </ul>

            <h3 id="response">Response</h3>


        </section>
    </article>

    </#list>

    </section>

</body>