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
<#-- @ftlvariable name="" type="org.activityinfo.server.mail.Invitation" -->
Hi ${newUser.name},

${invitingUser.name} (${invitingUser.email}) has invited you to access ActivityInfo. To

complete your user registration, click on the following link:

https://${domain.host}/confirm?${newUser.changePasswordKey}

If you have a Humanitarian ID, you can login directly via:
https://${domain.host}/oauth/oauthconnector_hid_oauth

Best regards,

The ActivityInfo Team