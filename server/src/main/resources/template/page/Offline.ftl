<#include "Scaffolding.ftl">
<@scaffolding title="Offline">

    <@content>
        <div class="row row--padded">

        <h3>You're offline!</h3>

        <p>You currently have no connection to the internet. However, ActivityInfo has been
        cached offline!</p>


        <form action="/app" method="GET">
            <button type="submit">Open Application</button>
        </form>
    </div>
    </@content>
    <@footer/>
    <@scripts/>
</@scaffolding>