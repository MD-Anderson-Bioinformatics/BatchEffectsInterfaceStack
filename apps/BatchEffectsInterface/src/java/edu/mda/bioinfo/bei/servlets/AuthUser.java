/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.mda.bioinfo.bei.authorization.UserAndRoleData;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author linux
 */
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
