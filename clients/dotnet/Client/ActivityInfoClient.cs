using System;
using System.IO;
using System.Net;
using Newtonsoft.Json.Linq;

namespace ActivityInfo.Client
{
	public class ActivityInfoClient
	{
		private String baseUrl = "https://www.activityinfo.org";
		private String accountEmail;
		private String password;

		public ActivityInfoClient (String accountEmail, String password)
		{
			this.accountEmail = accountEmail;
			this.password = password;
		}

		public ActivityInfoClient() {

		}

		public void CreateSite(SiteBuilder site) {
			JObject command = new JObject ();
			command.Add ("properties", site.Build());

			ExecuteCommand ("CreateSite", command);
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
//			if (response. == HttpStatusCode.Forbidden) {
//				throw new Exception ("Status code: " + response.StatusCode);
//			}

			string result;
			using (StreamReader rdr = new StreamReader(response.GetResponseStream()))
			{
				result = rdr.ReadToEnd();
			}

			return result;
		}
	}
}

