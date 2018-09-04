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
<#-- @ftlvariable name="" type="org.activityinfo.server.mail.InvitationMessage" -->
<#-- @ftlvariable name="domain" type="org.activityinfo.server.util.jaxrs.Domain" -->

Bonjour ${newUser.name},

${invitingUser.name} (${invitingUser.email}) vous invite à acceder au ActivityInfo. Pour confirmer
votre adresse et connecter au système, visitez le lien suivante:

${domain.rootUrl}/confirm?${newUser.changePasswordKey}

Si vous disposez d'une compte Humanitarian ID, vous pouvez connecter directement:
${domain.rootUrl}/oauth/oauthconnector_hid_oauth


Cordialement,

L'équipe ActivityInfo