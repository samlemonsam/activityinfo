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
<#-- @ftlvariable name="" type="org.activityinfo.server.mail.RequestDatabaseTransferMessage" -->
Hi ${recipient.name},

User ${requester.name} has requested that we transfer ownership of database "${database.name}" to you.

If you would like to *accept* ownership of this database, please click on the following link:

${domain.rootUrl}/approval/accept?token=${database.transferToken}

If you would like to *reject* ownership of this database, please click on the following link:

${domain.rootUrl}/approval/reject?token=${database.transferToken}

Best regards,

The ActivityInfo Team