// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.bei.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.mda.bcb.bei.authorization.UserAndRoleData;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "AuthUser", urlPatterns =
{
	"/AuthUser"
})
public class AuthUser extends BEIServletMixin
{
	public AuthUser()
	{
		super("application/json;charset=UTF-8", true, null);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		theBuffer.append("{");
		String userName = "";
		if (null!=request.getRemoteUser())
		{
			userName = request.getRemoteUser();
		}
		theBuffer.append("\n\"userName\":\"" + userName + "\"");
		// add list of users
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		theBuffer.append(",");
		theBuffer.append("\n\"availableUsers\":" + gson.toJson(UserAndRoleData.getUserList()) );
		theBuffer.append(",");
		theBuffer.append("\n\"availableRoles\":" + gson.toJson(UserAndRoleData.getRoleList()) );
		theBuffer.append("\n}\n");
	}
}
