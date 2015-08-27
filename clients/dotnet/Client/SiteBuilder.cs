using System;
using Newtonsoft.Json.Linq;

namespace ActivityInfo.Client
{
	public class SiteBuilder
	{
		private JObject properties = new JObject();

		public SiteBuilder ()
		{
			SetId (KeyGenerator.GenerateId ());
		}

		public void SetActivityId(int activityId) {
			properties.Add ("activityId", new JValue (activityId));
		}

		public void SetPartnerId(int partnerId) {
			properties.Add ("partnerId", new JValue (partnerId));
		}

		public void SetLocationId (int locationId)
		{
			properties.Add ("locationId", new JValue (locationId));
		}

		public void SetId(int id) {
			properties.Add ("id", new JValue (id));
		}

		public void SetStartDate(DateTime date) {
			properties.Add ("date1", new JValue (date.ToString ("yyyy-MM-dd")));
		}

		public void SetEndDate(DateTime date) {
			properties.Add ("date2", new JValue (date.ToString ("yyyy-MM-dd")));
		}

		public void SetReportingPeriodId(int id) {
			properties.Add ("reportingPeriodId", new JValue (id));
		}

		public void SetComments(String comments) {
			properties.Add ("comments", new JValue (comments));
		}

		public void SetIndicatorValue(int indicatorId, double value) {
			properties.Add ("I" + indicatorId, new JValue (value));
		}

		public void SetAttribute(int attributeId, bool value) {
			properties.Add("ATTRIB" + attributeId, new JValue(value));
		}

		public JObject Build() {
			return properties;
		}
	}
}

