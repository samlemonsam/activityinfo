<#include "Scaffolding.ftl">
<@scaffolding title="Offline">

    <@content>
        <div class="row row--padded">

        <h3>You're offline!</h3>

        <p>You currently have no connection to the internet. However, ActivityInfo has been
        cached offline.</p>

        <a href="/app" class="cta-link">Open Application</a>
    </div>
    </@content>
    <@footer/>
    <@scripts/>
</@scaffolding>