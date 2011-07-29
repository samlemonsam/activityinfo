package org.sigmah.shared.command;

import org.sigmah.shared.dto.DashboardSettingsDTO;

/*
 *Request the dashboard of the user with given UserID from a persistence store
 */
public class GetDashboard implements Command<DashboardSettingsDTO> {
	private int userId;

	public GetDashboard(int userId) {
		super();
		this.userId = userId;
	}

	public GetDashboard() {
		super();
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}
}