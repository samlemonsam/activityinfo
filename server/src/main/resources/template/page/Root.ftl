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
<@scaffolding title="${domain.title}">

 <#if domain.homePageBody?? >
 ${domain.homePageBody!''}
 <#else>
 <div class="container">

     <div class="page-header">
         <h1>ActivityInfo is Running!</h1>
     </div>
     <p>You're currently viewing the un-themed landing page for the ActivityInfo Server. The appearance of this
         page, and all of the login and sign up pages can be customized at deploy time with a custom theme and content
         based on the host name.</p>

     <p><a href="/admin/branding/${domain.host}" class="btn btn-primary">Customize ${domain.host}</a></p>

 </div>
 </#if>

</@scaffolding>