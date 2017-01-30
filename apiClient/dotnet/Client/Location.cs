using System;
using Newtonsoft.Json;

namespace ActivityInfo.Client
{
	public class Location
	{
		[JsonProperty("locationId")]
		public int Id { get; set; }

		[JsonProperty]
		public string Name { get; set; }

		[JsonProperty]
		public string Code { get; set; }

		[JsonProperty]
		public double Latitude { get; set; }

		[JsonProperty]
		public double Longitude { get; set; }

		public Location ()
		{
		}
	}
}

