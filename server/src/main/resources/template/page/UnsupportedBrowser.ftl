<#include "Scaffolding.ftl">
<@scaffolding title="Unsupported Browser">

    <@content>
        <div class="row row--padded">

        <h3>${label.unsupportedBrowserHeading}</h3>

        <p>${label.unsupportedBrowser}</p>
        <ul>
            <li><a href="http://chrome.google.com">Google Chrome</a></li>
            <li><a href="http://www.mozilla.com">FireFox</a></li>
            <li><a href="http://www.microsoft.com/internetexplorer">Internet Explorer</a> 8+</li>
            <li><a href="http://www.apple.com/safari">Safari</a> (Mac)</li>
        </ul>
    </div>
    </@content>
    <@footer/>
    <@scripts/>

</@scaffolding>