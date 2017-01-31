using System;
using System.IO;
using System.Net;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace ActivityInfo.Client
{
	public class ActivityInfoClient
	{
		private String baseUrl = "https://www.activityinfo.org";
		private String accountEmail;
		private String password;
		private JsonSerializer serializer = new JsonSerializer();

		public ActivityInfoClient (String accountEmail, String password)
		{
			this.accountEmail = accountEmail;
			this.password = password;
		}

		public ActivityInfoClient() {

		}

		public List<Site> QuerySitesByActivity(int activityId) {
			return ExecuteQuery<List<Site>>("/resources/sites?activity=" + activityId);
		}

		public List<Site> QuerySitesByDatabase(int databaseId) {
			return ExecuteQuery<List<Site>> ("/resources/sites?databased=" + databaseId);
		}

		public void CreateSite(SiteBuilder site) {
			JObject command = new JObject ();
			command.Add ("properties", site.Build());

			ExecuteCommand ("CreateSite", command);
		}

		public T ExecuteQuery<T>(string path) {
			HttpWebRequest request = WebRequest.CreateHttp (baseUrl + path);
			request.Accept = "application/json";
			request.Method = "GET";

			if (accountEmail != null) {
				request.PreAuthenticate = true;
				request.Credentials = new NetworkCredential (accountEmail, password);
			}

			HttpWebResponse response = (HttpWebResponse)request.GetResponse();

			string result;
			using (StreamReader rdr = new StreamReader(response.GetResponseStream()))
			{
				return serializer.Deserialize <T>(new JsonTextReader(rdr));
			}
		}
			
		public string ExecuteCommand(String type, JObject command) {
			if (accountEmail == null) {
				throw new InvalidOperationException ("Commands must be authenticated");
			}

			JObject requestBody = new JObject();
			requestBody.Add ("type", type);
			requestBody.Add ("command", command);

			byte[] body = System.Text.Encoding.UTF8.GetBytes(requestBody.ToString());

			HttpWebRequest request = WebRequest.CreateHttp (baseUrl + "/command");
			request.ContentType = "application/json; charset=UTF-8";
			request.Accept = "application/json";
			request.ContentLength = body.Length;
			request.PreAuthenticate = true;
			request.Method = "POST";
			request.Credentials = new NetworkCredential (accountEmail, password);

			using(Stream requestStream = request.GetRequestStream()) {
				requestStream.Write (body, 0, body.Length);
			}
				
			HttpWebResponse response = (HttpWebResponse)request.GetResponse();
			string result;
			using (StreamReader rdr = new StreamReader(response.GetResponseStream()))
			{
				result = rdr.ReadToEnd();
			}

			return result;
		}
	}
}

